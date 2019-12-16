package com.vmware.ignite.db;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO class instantiated from SystemProperties (from the configuration files). Acts as a repository of all
 * IgniteQueries specified in conf files.
 */
public class IgniteQueries {
    // Map<MODEL CLASS, Map<QUERY NAME, QUERY TEMPLATE>>
    private Map<String, Map<String, String>> modelClassToQueryMap;

    public IgniteQueries() {
        this.modelClassToQueryMap = new HashMap<>();
    }

    /**
     * Return a String representing a query template, obtained by the Registry Model class and query name
     *
     * @return null if no query template matching query model class and query name can be found.
     */
    public String getQueryTemplate(String queryModelClass, String queryName) {
        Map<String, String> queriesOfModelClass = this.modelClassToQueryMap.get(queryModelClass);
        if (queriesOfModelClass == null) {
            return null;
        }
        return queriesOfModelClass.get(queryName);
    }

    /**
     * Adds query templates against queryModelClass and queryName
     */
    public void addQuery(String queryModelClass, String queryName, String queryTemplate) {
        Map<String, String> queryMap = this.getQueryMap(queryModelClass);
        queryMap.put(queryName, queryTemplate);
    }

    private Map<String, String> getQueryMap(String queryModelClass) {
        Map<String, String> ret = this.modelClassToQueryMap.get(queryModelClass);
        if (ret == null) {
            ret = new HashMap<>();
            this.modelClassToQueryMap.put(queryModelClass, ret);
        }
        return ret;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        this.modelClassToQueryMap.entrySet()
            .forEach(mClassEntry -> {
                sb.append(String.format("%s:", mClassEntry.getKey())).append("\n");
                mClassEntry.getValue().entrySet().forEach(qEntry -> {
                    sb.append(String.format("\t[%s] -> [%s]", mClassEntry.getKey(), mClassEntry.getValue()));
                    sb.append("\n");
                });
            });
        return sb.toString();
    }
}
