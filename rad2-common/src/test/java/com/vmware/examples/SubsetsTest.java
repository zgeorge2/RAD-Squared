package com.vmware.examples;

import com.vmware.examples.test.Subsets;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SubsetsTest extends TestCase {
    private List<String> getTestList() {
        List<String> l = new ArrayList<String>();
        for (char i = 'a'; i <= 'e'; i++) {
            l.add("" + i);
        }
        return l;
    }

    public void testGetSubsets() {
        List<String> l = this.getTestList();
        Map<String, List<String>> map = new Subsets<String>().getSubsets(l);
        assertTrue(map.containsKey("01011") && map.get("01011").containsAll(Arrays.asList("b", "d", "e")));
    }

    public void testGetKSubsets() {
        List<String> l = this.getTestList();
        int k = 3;
        Map<String, List<String>> map = new Subsets<String>().getSubsets(l, k);
        assertTrue(map.containsKey("01101") && map.get("01101").containsAll(Arrays.asList("b", "c", "e")));
    }
}