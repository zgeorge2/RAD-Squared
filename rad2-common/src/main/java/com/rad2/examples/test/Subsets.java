package com.rad2.examples.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Subsets<T> {
    public Map<String, List<T>> getSubsets(List<T> l) {
        int size = l.size();
        Map<String, List<T>> map = new HashMap<>();
        String format = "%" + size + "s";
        for (int i = 0; i < Math.pow(2, size); i++) {
            String s = String.format(format, Integer.toBinaryString(i)).replace(' ', '0');
            List<T> sub = IntStream.range(0, size).filter(j -> s.charAt(j) == '1').mapToObj(l::get).collect(Collectors.toList());
            map.put(s, sub);
        }
        return map;
    }

    public Map<String, List<T>> getSubsets(List<T> l, int k) {
        int size = l.size();
        Map<String, List<T>> map = new HashMap<>();
        String format = "%" + size + "s";
        IntStream.range(0, (int) Math.pow(2, size)).filter(i -> Integer.bitCount(i) == k).forEach(i -> {
            String s = String.format(format, Integer.toBinaryString(i)).replace(' ', '0');
            List<T> sub = IntStream.range(0, size).filter(j -> s.charAt(j) == '1').mapToObj(l::get).collect(Collectors.toList());
            map.put(s, sub);
        });
        return map;
    }
}
