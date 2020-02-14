package com.rad2.ignite.common;

import com.rad2.akka.common.RegistryStateDTO;
import com.rad2.common.collection.INAryTreeNodeData;
import com.rad2.common.utils.PrintUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class BaseModelRegistry<K extends DModel>
        implements INAryTreeNodeData, UsesRegistryManager {
    private RegistryManager rm;
    private BaseModelQueries<K> queryMap;

    protected BaseModelRegistry() {
        PrintUtils.printToActor("*** Creating  instance of %s ***", this.getClass());
    }

    public void initialize(RegistryManager rm) {
        this.rm = rm;
        this.queryMap = new BaseModelQueries<>(this.rm, this.getModelClass(), this.getDefaultOrderByColumn());
    }

    /**
     * simple broadcaster across the compute grid
     *
     * @return
     */
    public boolean broadcastMessage(String message) {
        return this.getQueryMap().broadcastMessage(message);
    }

    /**
     * Hook that can be implemented by the child class to do custom handling AFTER the state has been added to
     * the registry
     */
    public void postAdd(RegistryStateDTO dto) {
    }

    /**
     * Hook that can be implemented by the child class to do custom handling BEFORE the state has been added
     * to the registry
     */
    public void preAdd(RegistryStateDTO dto) {
    }

    /**
     * add the Supplier provided object to the registry against the given key. If an object is already
     * present, it is NOT replaced.
     *
     * @return
     */
    private boolean add(String key, Supplier<K> func) {
        return this.getQueryMap().add(key, func);
    }

    /**
     * update the Supplier provided object into the registry against the given key.
     *
     * @return
     */
    private boolean update(String key, Supplier<K> func) {
        return this.getQueryMap().update(key, func);
    }

    /**
     * Actor State that needs to be added to the registry must be sent in via the DTO. The Registry adds such
     * state and then returns the pKey for the entry
     *
     * @return
     */
    public final String add(RegistryStateDTO dto) {
        this.preAdd(dto);
        boolean addSuccess = this.add(dto.getKey(), () -> dto.toModel());
        if (addSuccess) this.postAdd(dto); // if the addition was unsuccessful, then avoid the post action
        return dto.getKey();
    }

    /**
     * Actor State that needs to be updated into the registry must be sent in via the DTO. The Registry
     * updates such state and then returns the pKey for the entry
     *
     * @return
     */
    public String update(RegistryStateDTO dto) {
        boolean isSuccess = this.update(dto.getKey(), () -> dto.toModel());
        return dto.getKey();
    }

    /**
     * Actor State that needs to be added to the registry is created by the Function and then added to the
     * registry. The method then returns the pKey for the entry
     *
     * @return
     */
    public final String add(String key, Function<K, RegistryStateDTO> func) {
        return this.add(this.apply(key, func));
    }

    /**
     * Actor State that needs to be updated to the registry is created by the Function and then added to the
     * registry. The method then returns the pKey for the entry
     */
    public final <R extends RegistryStateDTO> String update(String key, Function<K, R> func) {
        return this.update(this.apply(key, func));
    }

    /**
     * remove the registry entry
     */
    public <K extends DModel> void remove(K model) {
        if (model == null) return;
        this.queryMap.remove(model.getKey());
    }

    /**
     * For all registry entries whose parent is the parentKey, remove entries that pass the function matcher
     */
    public void removeChildrenOfParentMatching(String parentKey, Function<K, Boolean> matcher) {
        this.getQueryMap().removeChildrenOfParentMatching(parentKey, matcher);
    }

    /**
     * For the registry object matching the key, apply the Function
     *
     */
    public <R> R apply(String key, Function<K, R> func) {
        return this.getQueryMap().apply(key, func);
    }

    /**
     * For all registry entries whose parent is the parentKey, perform the given Consumer function
     */
    public <R> Map<String, R> applyToChildrenOfParent(String parentKey, Function<K, R> func) {
        return this.getQueryMap().applyToChildrenOfParent(parentKey, func);
    }

    /**
     * For all registry entries, perform the given Consumer function
     */
    public <R> Map<String, R> applyToAll(Function<K, R> func) {
        return this.getQueryMap().applyToAll(func);
    }

    /**
     * For all registry entries filtered by the named query, perform the given Consumer function
     */
    public <R> Map<String, R> applyToFiltered(Function<K, R> func, String queryName, Object... args) {
        return this.getQueryMap().applyToFiltered(func, queryName, args);
    }

    /**
     * Generate the next sequence id for an entry in this registry
     *
     * @return
     */
    public UUID generateNewId() {
        return UUID.randomUUID();
    }

    /**
     * Get the specific registry entry by the value of the key
     *
     * @return
     */
    public K get(String key) {
        return this.getQueryMap().get(key);
    }

    /**
     * Get all the registry entries
     *
     * @return
     */
    public List<K> getAll() {
        return this.getQueryMap().getAll();
    }

    /**
     * Get all the registry entries that have the given name
     */
    public List<K> getAllWithName(String name) {
        return this.getQueryMap().getAllWithName(name);
    }

    /**
     * Get the registry entries whose column values match those of args (each arg is a column name to its
     * value).
     *
     * @return
     */
    public List<K> getResultFromQueryWithFields(Map<String, String> args) {
        return this.getQueryMap().getResultsOfQueryWithFields(args);
    }

    public List<K> fetchResultList(String query, Object... args) {
        return this.queryMap.getResultsOfSqlQuery(query, args);
    }

    private BaseModelQueries<K> getQueryMap() {
        return queryMap;
    }

    /**
     * construct a key using a parentKey and the name of the registry entry.
     *
     * @return
     */
    protected String getKey(String parentKey, String name) {
        return String.format("%s/%s", parentKey, name);
    }

    /**
     * Get the type of the DTO Class used to store state in the Registry.
     *
     * @return
     */
    protected abstract Class getModelClass();

    /**
     * Get the default column in the DModel class to use for orderBy clauses.
     *
     * @return
     */
    protected String getDefaultOrderByColumn() {
        return "key";
    }

    @Override
    /**
     * THe default parent name is the name of the root node in the registry manager. Override this in child
     * registry classes if needed, using the getTreeNodeName method of the actual parent (which would be
     * another Registry instance).
     */
    public final String getParentTreeNodeName() {
        Class parentRegistryClass = this.getParentRegistryClass();
        if (parentRegistryClass == null) {
            return RegistryManager.ROOT_NODE_NAME; // use the root node in the RegistryManager as default
        }
        return this.reg(parentRegistryClass).getTreeNodeName();
    }

    public String getTreeNodeName() {
        return this.getClass().getSimpleName();
    }

    /**
     * Return the Class of the parent registry. Registries are arranged in a tree within the Registry Manager
     * in order to facilitate Actor reincarnation from the Registries (for those Actors that need to be
     * reincarnated)
     *
     * @return
     */
    public Class getParentRegistryClass() {
        return null;
    }

    @Override
    public RegistryManager getRM() {
        return this.rm;
    }
}

