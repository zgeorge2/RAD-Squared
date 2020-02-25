/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.ignite.common.queries;

import org.apache.ignite.cache.query.SqlQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IQueryWithColumns<K, V> extends IQuery<K, V> {
    public IQueryWithColumns(Class modelClass, String modelName, Map<String, String> cols) {
        super(() -> {
            StringBuilder qs = new StringBuilder();
            qs.append(String.format("select * from %s where ", modelName));
            List<String> parameters = new ArrayList<>();
            for (String key : cols.keySet()) {
                parameters.add(String.format("%s='%s'", key, cols.get(key)));
            }
            qs.append(String.join(" and ", parameters));
            String selectQuery = qs.toString();
            return new SqlQuery<>(modelClass, selectQuery);
        });
    }
}
