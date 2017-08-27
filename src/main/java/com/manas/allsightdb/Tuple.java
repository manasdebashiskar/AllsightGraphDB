package com.manas.allsightdb;

public class Tuple<K,V> {
    K left;
    V right;
    public K getLeft() {
        return left;
    }
    public V getRight() {
        return right;
    }
    public Tuple(K left , V right) {
        this.left = left;
        this.right = right;
    }
}
