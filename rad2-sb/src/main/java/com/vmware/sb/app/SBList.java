package com.vmware.sb.app;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * SBList is a convenience class around List<T>
 */
public class SBList<T> {
    private List<T> tList;

    public SBList(List<T> tList) {
        this.tList = tList;
    }

    public List<T> get() {
        return tList;
    }

    public void add(T t) {
        this.tList.add(t);
    }

    public void apply(Consumer<T> consumer) {
        this.tList.stream().forEach(ctrl -> consumer.accept(ctrl));
    }

    public Stream<T> stream() {
        return this.tList.stream();
    }
}
