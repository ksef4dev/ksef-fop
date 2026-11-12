/*
 * SPDX-License-Identifier: Apache-2.0
 */
package io.alapierre.ksef.fop.internal;

import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * Small synchronized LRU cache used for compiled stylesheets.
 *
 * <p>Lookups and inserts are cheap and happen under the lock; callers compute values (e.g. compile
 * a stylesheet) outside of it and publish them with {@link #putIfAbsent}, so a slow computation
 * never blocks hits for other keys.</p>
 *
 * @param <K> key type
 * @param <V> value type
 */
final class BoundedTemplateCache<K, V> {

    private final LinkedHashMap<K, V> entries = new LinkedHashMap<K, V>(16, 0.75f, true);
    private int maxSize;

    BoundedTemplateCache(int maxSize) {
        this.maxSize = Math.max(1, maxSize);
    }

    /** Returns the cached value (marking it as recently used) or {@code null}. */
    synchronized V get(K key) {
        return entries.get(key);
    }

    /**
     * Stores {@code value} unless another thread cached the key first.
     *
     * @return the value now held by the cache for {@code key}
     */
    synchronized V putIfAbsent(K key, V value) {
        V existing = entries.get(key);
        if (existing != null) {
            return existing;
        }
        entries.put(key, value);
        evictOverflow();
        return value;
    }

    /** Changes the capacity (at least 1), evicting least recently used entries if needed. */
    synchronized void setMaxSize(int maxSize) {
        this.maxSize = Math.max(1, maxSize);
        evictOverflow();
    }

    synchronized void clear() {
        entries.clear();
    }

    synchronized int size() {
        return entries.size();
    }

    private void evictOverflow() {
        Iterator<K> leastRecentlyUsedFirst = entries.keySet().iterator();
        while (entries.size() > maxSize && leastRecentlyUsedFirst.hasNext()) {
            leastRecentlyUsedFirst.next();
            leastRecentlyUsedFirst.remove();
        }
    }
}
