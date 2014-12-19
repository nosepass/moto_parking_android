package com.github.nosepass.motoparking.util;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A naive two-way map implementation. Falls apart on edge cases like having an identical key and
 * value.
 */
public class HashBiMap<K,V> implements Map<K,V> {
    private HashMap<K,V> forward;
    private HashMap<V,K> backward;

    public HashBiMap() {
        forward = new HashMap<>();
        backward = new HashMap<>();
    }

    private HashBiMap(HashMap<K,V> forward, HashMap<V,K> backward) {
        this.forward = forward;
        this.backward = backward;
    }

    @Override
    public void clear() {
        forward.clear();
        backward.clear();
    }

    @Override
    public boolean containsKey(Object key) {
        return forward.containsKey(key) || backward.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return forward.containsValue(value) || backward.containsValue(value);
    }

    @NonNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        return forward.entrySet();
    }

    @Override
    public V get(Object key) {
        return forward.get(key);
    }

    @Override
    public boolean isEmpty() {
        return forward.isEmpty();
    }

    @NonNull
    @Override
    public Set<K> keySet() {
        return forward.keySet();
    }

    @Override
    public V put(K key, V value) {
        backward.put(value, key);
        return forward.put(key, value);
    }

//    @Override
//    public V forcePut(K key, V value) {
//        return null;
//    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public V remove(Object key) {
        V value = forward.remove(key);
        backward.remove(value);
        return value;
    }

    @Override
    public int size() {
        return forward.size();
    }

    @Override
    public Set<V> values() {
        return backward.keySet();
    }

    //@Override
    public HashBiMap<V, K> inverse() {
        return new HashBiMap<V, K>(backward, forward);
    }
}
