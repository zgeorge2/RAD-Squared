package com.vmware.examples;

import com.vmware.examples.test.InsSort;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class InsSortTest extends TestCase {
    private List<String> getTestList() {
        List<String> l = new ArrayList<String>();
        for (char i = 'z'; i >= 'a'; i--) {
            l.add("" + i);
        }
        Collections.shuffle(l);
        return l;
    }

    public void testNativeSortStreams() {
        List<String> l = this.getTestList();
        new InsSort<String>().nativeSortStreams(l, new StringComparator());
        assertTrue(l.get(l.size() - 1).equals("z"));
    }

    public void testSort() {
        List<String> l = this.getTestList();
        new InsSort<String>().sort(l, new StringComparator());
        assertTrue(l.get(l.size() - 1).equals("z"));
    }

    public void testNativeSort() {
        List<String> l = this.getTestList();
        new InsSort<String>().nativeSort(l, new StringComparator());
        assertTrue(l.get(l.size() - 1).equals("z"));
    }

    public void testNativeSortStreamsHybrid() {
        List<String> l = this.getTestList();
        new InsSort<String>().nativeSortStreamsHybrid(l, new StringComparator());
        assertTrue(l.get(l.size() - 1).equals("z"));
    }

    private class StringComparator implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            return o1.compareTo(o2);
        }
    }
}