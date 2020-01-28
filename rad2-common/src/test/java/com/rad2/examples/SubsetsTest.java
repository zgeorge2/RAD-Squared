package com.rad2.examples;

import com.rad2.examples.test.Subsets;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SubsetsTest {
    private List<String> getTestList() {
        List<String> l = new ArrayList<>();
        for (char i = 'a'; i <= 'e'; i++) {
            l.add("" + i);
        }
        return l;
    }

    @Test
    public void testGetSubsets() {
        List<String> l = this.getTestList();
        Map<String, List<String>> map = new Subsets<String>().getSubsets(l);
        Assert.assertTrue(map.containsKey("01011") && map.get("01011").containsAll(Arrays.asList("b", "d", "e")));
    }

    @Test
    public void testGetKSubsets() {
        List<String> l = this.getTestList();
        int k = 3;
        Map<String, List<String>> map = new Subsets<String>().getSubsets(l, k);
        Assert.assertTrue(map.containsKey("01101") && map.get("01101").containsAll(Arrays.asList("b", "c", "e")));
    }
}