/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.examples.test;

import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;


public class InsSort<T> {

    public void nativeSort(List<T> input, Comparator<T> comparator) {
        for (int i = 1; i < input.size(); i++) {
            for (int j = i; j > 0; j--) {
                if (comparator.compare(input.get(j), input.get(j - 1)) < 0)
                    swap(input, j, j - 1);
            }
        }
    }

    public void nativeSortStreamsHybrid(List<T> input, Comparator<T> comparator) {
        IntStream.range(1, input.size())
                .forEach(i -> {
                    for (int j = i; j > 0; j--) {
                        if (comparator.compare(input.get(j), input.get(j - 1)) < 0)
                            swap(input, j, j - 1);
                    }
                });
    }

    public void nativeSortStreams(List<T> input, Comparator<T> comparator) {
        IntStream.range(1, input.size())
                .forEach(i -> IntStream.range(0, i)
                        .map(j -> i - j) // reverse the range
                        .forEach(j -> { // insert sort after compare
                            if (comparator.compare(input.get(j), input.get(j - 1)) < 0)
                                swap(input, j, j - 1);
                        }));
    }

    public void sort(List<T> input, Comparator<T> comparator) {
        input.sort(comparator);
    }

    private void swap(List<T> input, int i, int j) {
        T x_i = input.get(i);
        input.set(i, input.get(j));
        input.set(j, x_i);
    }

}
