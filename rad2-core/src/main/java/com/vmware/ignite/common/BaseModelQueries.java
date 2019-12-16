package com.vmware.ignite.common;

import com.vmware.common.utils.NTxHolder;
import com.vmware.ignite.common.queries.*;
import com.vmware.ignite.db.IgniteQueries;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This class provides a generic implementation wrapper around the IgniteRegistry. Both the Data Grid
 * (queries) and ComputeGrid can be used through this class. The class is specific to the DModel subclass
 * that's stored in the registry.
 */
public class BaseModelQueries<K extends DModel> {
    private static Logger logger = LoggerFactory.getLogger(BaseModelQueries.class);
    private RegistryManager rm;
    private IgniteRegistry<String, K> reg;
    private Class registryModelClass;
    private String defaultOrderByColumn;

    public BaseModelQueries(RegistryManager rm, Class registryModelClass, String defaultOrderByColumn) {
        this.rm = rm;
        this.registryModelClass = registryModelClass;
        this.defaultOrderByColumn = defaultOrderByColumn;
        this.reg = new IgniteRegistry<>(
            rm, this.getCacheConfigKey(),
            cc -> {
                // need to set indexed types else the K type table will not be
                // recognized in Sql Queries.
                cc.setIndexedTypes(String.class, this.getRegModel());
                cc.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);
            });
    }

    /**
     * simple broadcaster across the compute grid
     */
    public boolean broadcastMessage(String message) {
        boolean ret = false;
        try (NTxHolder<IgniteProvider.ITx> tx = this.reg.createTx()) {
            String msg = String.format("Broadcast Message [timestamp: %d][%s]", System.currentTimeMillis(),
                message);
            this.reg.broadcastMessage(msg);
            ret = true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return ret;
    }

    /**
     * Performs the addition to the registry object against the key provided within an Ignite transaction.
     * Note that the add happens ONLY if an existing object is not found against the provided key. This method
     * is synchronized to allow for the presence of mutable variables that might be affected by the call to
     * func.get.
     *
     * @param key  the key to use in the registry
     * @param func calling this supplier returns the new instance of the object to be stored in the registry
     * @return true if the add happens successfully. false if an object already exists against the key.
     */
    public boolean add(String key, Supplier<K> func) {
        boolean ret = false;
        try (NTxHolder<IgniteProvider.ITx> tx = this.reg.createTx()) {
            if (this.reg.getValue(key) == null) {
                this.reg.putValue(key, func.get());
                ret = true;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return ret;
    }

    public boolean update(String key, Supplier<K> func) {
        boolean ret = false;
        try (NTxHolder<IgniteProvider.ITx> tx = this.reg.createTx()) {
            this.reg.putValue(key, func.get());
            ret = true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return ret;
    }

    public boolean remove(String key) {
        try (NTxHolder<IgniteProvider.ITx> tx = this.reg.createTx()) {
            return this.reg.remove(key);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * Performs the supplied func on the registry object accessed by key within an Ignite transaction. Hence
     * changes to the object made by the function are committed. Nested transactions are not supported for
     * now. This method is synchronized to allow for the presence of mutable variables that might be affected
     * by the call to func.apply.
     *
     * @param key  the key to use in the registry
     * @param func calling this function performs the operation on the found object (against key) within an
     *             Ignite transaction.
     * @return the value of calling the passed in function.  if the function doesn't get called, then return
     * null. Such as when the key doesn't find an object.
     */
    public synchronized <R> R apply(String key, Function<K, R> func) {
        R ret = null;
        try (NTxHolder<IgniteProvider.ITx> tx = this.reg.createTx()) {
            K k = this.get(key); // get the Model object
            if (k != null) {
                ret = func.apply(k); // apply function on the object
                this.reg.putValue(key, k); // putAttr the potentially changed object back into the reg
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return ret;
    }

    /**
     * Performs the supplied func on the registry objects whose parent matches parentKey within an Ignite
     * transaction. Hence changes to the object made by the function are committed. Nested transactions are
     * not supported for now. This method is synchronized to allow for the presence of mutable variables that
     * might be affected by the call to func.apply.
     *
     * @param parentKey get the entries in the registry whose parent matches this parentKey
     * @param func      apply the function to each of the returned entries
     * @param <R>       the type of the object returned by applying func on the registry entry
     * @return a map of each registry entry's key to the return value of applying func on that regisry entry
     */
    public synchronized <R> Map<String, R> applyToChildrenOfParent(String parentKey, Function<K, R> func) {
        Map<String, R> ret = new HashMap<>();
        try (NTxHolder<IgniteProvider.ITx> tx = this.reg.createTx()) {
            this.getAllOfParent(parentKey).forEach(k -> { // get the Model objects
                R r = func.apply(k); // apply function on the object
                this.reg.putValue(k.getKey(), k); // putAttr the potentially changed object back into the reg
                ret.put(k.getKey(), r); // store the result against the object's key
            });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return ret;
    }

    /**
     * Performs the supplied func on EVERY object in this registry within an Ignite transaction. Hence changes
     * to the object made by the function are committed. Nested transactions are not supported for now. This
     * method is synchronized to allow for the presence of mutable variables that might be affected by the
     * call to func.apply.
     *
     * @param func apply the function to each of the entries in the registry
     * @param <R>  the type of the object returned by applying func on the registry entry
     * @return a map of each registry entry's key to the return value of applying func on that regisry entry
     */
    public synchronized <R> Map<String, R> applyToAll(Function<K, R> func) {
        Map<String, R> ret = new HashMap<>();
        try (NTxHolder<IgniteProvider.ITx> tx = this.reg.createTx()) {
            this.getAll().forEach(k -> { // get the Model objects
                R r = func.apply(k); // apply function on the object
                this.reg.putValue(k.getKey(), k); // putAttr the potentially changed object back into the reg
                ret.put(k.getKey(), r); // store the result against the object's key
            });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return ret;
    }

    /**
     * Performs the supplied func on the every object retrieved by the NAMED QUERY, performed within an Ignite
     * transaction. Hence changes to the object made by the function are committed. Nested transactions are
     * not supported for now. This method is synchronized to allow for the presence of mutable variables that
     * might be affected by the call to func.apply.
     *
     * @param qName the selector query used to get the list of Registry objects to which the func is to be
     *              applied
     * @param func  apply the function to each of the entries in the registry
     * @param <R>   the type of the object returned by applying func on the registry entry
     * @param args  any arguments that need to be applied on the query.
     * @return a map of each registry entry's key to the return value of applying func on that regisry entry
     */
    public synchronized <R> Map<String, R> applyToFiltered(Function<K, R> func, String qName,
                                                           Object... args) {
        Map<String, R> ret = new HashMap<>();
        try (NTxHolder<IgniteProvider.ITx> tx = this.reg.createTx()) {
            this.getResultsOfNamedQuery(qName, args).forEach(k -> { // get the Model objects matching query
                R r = func.apply(k); // apply function on the object
                this.reg.putValue(k.getKey(), k); // putAttr the potentially changed object back into the reg
                ret.put(k.getKey(), r); // store the result against the object's key
            });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return ret;
    }

    public K get(String key) {
        return this.reg.getOne(new ISingleQuery<>(this.getRegModel(), key));
    }

    public List<K> getAll() {
        return this.reg.getAll(new IAllQuery(this.getRegModel(), this.getModelName()));
    }

    public List<K> getAllOfParent(Object... args) {
        return this.reg.getAll(new IAllOfParentQuery(getRegModel(), getModelName(), getOrderByCol(), args));
    }

    public List<K> getAllWithName(Object... args) {
        return this.reg.getAll(new IAllWithNameQuery(getRegModel(), args));
    }

    public List<K> getResultsOfNamedQuery(String queryName, Object... args) {
        String query = this.getIgniteQueries().getQueryTemplate(getCacheConfigKey(), queryName);
        return this.getResultsOfSqlQuery(query, args);
    }

    public List<K> getResultsOfSqlQuery(String query, Object... args) {
        return this.reg.getAll(new INamedQuery<>(getRegModel(), query, args));
    }

    public List<K> getResultsOfQueryWithFields(Map<String, String> cols) {
        return this.reg.getAll(new IQueryWithColumns(this.getRegModel(), this.getModelName(), cols));
    }

    private final String getCacheConfigKey() {
        return getRegModel().getSimpleName().toUpperCase();
    }

    private Class getRegModel() {
        return registryModelClass;
    }

    private String getModelName() {
        String ccKey = this.getCacheConfigKey();
        // Construct the name to use in queries by concatenation the
        // the schema name within quotes + "." + the class name in all caps.
        // this effectively becomes something like DBANK.DBANK
        return String.format("\"%s\".\"%s\"", ccKey, ccKey);
    }

    private String getOrderByCol() {
        return defaultOrderByColumn;
    }

    private RegistryManager getRM() {
        return this.rm;
    }

    private SystemConfigRegistry getSCReg() {
        return (SystemConfigRegistry) this.getRM().get(SystemConfigRegistry.class);
    }

    private IgniteQueries getIgniteQueries() {
        return this.getSCReg().getIgniteQueries();
    }
}
