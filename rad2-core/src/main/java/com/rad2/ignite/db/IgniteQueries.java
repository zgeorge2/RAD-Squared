/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.ignite.db;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO class instantiated from SystemProperties (from the configuration files). Acts as a repository of all
 * IgniteQueries specified in conf files.
 */
public class IgniteQueries {
    // Map<MODEL CLASS, Map<QUERY NAME, QUERY TEMPLATE>>
    private final Map<String, Map<String, String>> modelClassToQueryMap;

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
        return this.modelClassToQueryMap.computeIfAbsent(queryModelClass, k -> new HashMap<>());
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        this.modelClassToQueryMap.forEach((key, value) -> {
            sb.append(String.format("%s:", key)).append("\n");
            value.forEach((key1, value1) -> {
                sb.append(String.format("\t[%s] -> [%s]", key, value));
                sb.append("\n");
            });
        });
        return sb.toString();
    }
}
