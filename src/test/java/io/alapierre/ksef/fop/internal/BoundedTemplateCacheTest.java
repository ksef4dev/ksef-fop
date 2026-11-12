/*
 * SPDX-License-Identifier: Apache-2.0
 */
package io.alapierre.ksef.fop.internal;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BoundedTemplateCacheTest {

    @Test
    void evictsLeastRecentlyUsedEntryWhenFull() {
        BoundedTemplateCache<String, String> cache = new BoundedTemplateCache<String, String>(2);
        cache.putIfAbsent("a", "A");
        cache.putIfAbsent("b", "B");

        cache.get("a");                 // "b" becomes least recently used
        cache.putIfAbsent("c", "C");

        assertThat(cache.size()).isEqualTo(2);
        assertThat(cache.get("a")).isEqualTo("A");
        assertThat(cache.get("b")).isNull();
        assertThat(cache.get("c")).isEqualTo("C");
    }

    @Test
    void putIfAbsentKeepsFirstValue() {
        BoundedTemplateCache<String, String> cache = new BoundedTemplateCache<String, String>(2);

        assertThat(cache.putIfAbsent("a", "first")).isEqualTo("first");
        assertThat(cache.putIfAbsent("a", "second")).isEqualTo("first");
        assertThat(cache.get("a")).isEqualTo("first");
    }

    @Test
    void shrinkingMaxSizeEvictsImmediately() {
        BoundedTemplateCache<String, String> cache = new BoundedTemplateCache<String, String>(5);
        for (String key : new String[]{"a", "b", "c", "d"}) {
            cache.putIfAbsent(key, key.toUpperCase());
        }

        cache.setMaxSize(2);

        assertThat(cache.size()).isEqualTo(2);
        assertThat(cache.get("c")).isEqualTo("C");
        assertThat(cache.get("d")).isEqualTo("D");
    }

    @Test
    void maxSizeIsAtLeastOne() {
        BoundedTemplateCache<String, String> cache = new BoundedTemplateCache<String, String>(0);
        cache.putIfAbsent("a", "A");
        cache.putIfAbsent("b", "B");

        assertThat(cache.size()).isEqualTo(1);
        assertThat(cache.get("b")).isEqualTo("B");
    }
}
