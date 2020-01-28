package com.rad2.examples;

import com.rad2.examples.test.InsSort;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class InsSortTest {
    private List<String> getTestList() {
        List<String> l = new ArrayList<>();
        for (char i = 'z'; i >= 'a'; i--) {
            l.add("" + i);
        }
        Collections.shuffle(l);
        return l;
    }

    @Test
    public void testNativeSortStreams() {
        List<String> l = this.getTestList();
        new InsSort<String>().nativeSortStreams(l, new StringComparator());
        Assert.assertEquals("z", l.get(l.size() - 1));
    }

    @Test
    public void testSort() {
        List<String> l = this.getTestList();
        new InsSort<String>().sort(l, new StringComparator());
        Assert.assertEquals(l.get(l.size() - 1), "z");
    }

    @Test
    public void testNativeSort() {
        List<String> l = this.getTestList();
        new InsSort<String>().nativeSort(l, new StringComparator());
        Assert.assertEquals(l.get(l.size() - 1), "z");
    }

    @Test
    public void testNativeSortStreamsHybrid() {
        List<String> l = this.getTestList();
        new InsSort<String>().nativeSortStreamsHybrid(l, new StringComparator());
        Assert.assertEquals(l.get(l.size() - 1), "z");
    }

    private static class StringComparator implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            return o1.compareTo(o2);
        }
    }
}