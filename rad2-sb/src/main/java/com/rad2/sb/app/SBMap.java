/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.sb.app;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * SBMap<T> is a convenience class that takes an SBList<T> and maps each List item to a String that's obtained
 * from the keymapper applied to each list item.
 */
public class SBMap<T> {
    private Map<String, T> tMap;

    public SBMap(SBList<T> tList, Function<T, String> keyMapper) {
        this.tMap = tList.stream()
            .collect(Collectors.toMap(t -> keyMapper.apply(t), Function.identity()));
    }

    public T get(String typePrefix) {
        return this.tMap.get(typePrefix);
    }

    public void apply(Consumer<Map.Entry<String, T>> consumer) {
        this.tMap.entrySet().stream().forEach(entry -> consumer.accept(entry));
    }

    public void applyToValues(Consumer<T> consumer) {
        this.tMap.values().stream().forEach(v -> consumer.accept(v));
    }
}

