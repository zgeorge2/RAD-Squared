package com.rad2.common.utils;

public final class Pair<L, R> {
    L left;
    R right;

    public Pair(L l, R r) {
        left = l;
        right = r;
    }

    public L getLeft() {
        return left;
    }

    public R getRight() {
        return right;
    }
}
