/*
 * SPDX-License-Identifier: Apache-2.0
 */
package io.alapierre.ksef.fop.internal;

import io.alapierre.ksef.fop.http.RemoteResourceFetcher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.transform.Templates;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Compiled template cache: thread safety across resource roots and bounded size.
 */
class XmlFactoriesTemplateCacheTest {

    private static final String MAIN = "<xsl:stylesheet version=\"3.0\" "
            + "xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
            + "<xsl:include href=\"inc.xsl\"/></xsl:stylesheet>";

    private static final String INC = "<xsl:stylesheet version=\"3.0\" "
            + "xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
            + "<xsl:template match=\"/\"><r/></xsl:template></xsl:stylesheet>";

    /** Serves MAIN/INC for any tenant root; the short pause widens the window for interleaving. */
    private static final RemoteResourceFetcher FETCHER = url -> {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        String body = url.getPath().endsWith("/inc.xsl") ? INC : MAIN;
        return body.getBytes(StandardCharsets.UTF_8);
    };

    @BeforeEach
    void setUp() {
        XmlFactories.clearCompiledTemplateCache();
        XmlFactories.setCompiledTemplateCacheMaxSize(1000);
    }

    @AfterEach
    void tearDown() {
        XmlFactories.clearCompiledTemplateCache();
        XmlFactories.setCompiledTemplateCacheMaxSize(XmlFactories.DEFAULT_TEMPLATE_CACHE_MAX_SIZE);
    }

    /**
     * Regression: the include resolver used to be stored in the shared Saxon configuration, so
     * concurrent compilations for different roots resolved includes with another request's
     * resolver and failed with "Unexpected URI scheme".
     */
    @Test
    void concurrentCompilationsForDifferentRootsAllSucceed() throws Exception {
        int tenants = 40;

        List<Templates> compiled = compileConcurrently(8, 400, tenants);

        assertThat(compiled).hasSize(400).doesNotContainNull();
        assertThat(XmlFactories.compiledTemplateCacheSize()).isEqualTo(tenants);
    }

    @Test
    void resolversWithEqualRootsShareCompiledTemplate() throws Exception {
        Templates first = XmlFactories.getTemplate(resolver(1), mainUrl(1));
        Templates second = XmlFactories.getTemplate(resolver(1), mainUrl(1));

        assertThat(second).isSameAs(first);
        assertThat(XmlFactories.compiledTemplateCacheSize()).isEqualTo(1);
    }

    @Test
    void cacheEvictsLeastRecentlyUsedEntryWhenFull() throws Exception {
        XmlFactories.setCompiledTemplateCacheMaxSize(3);
        Templates tenant0 = XmlFactories.getTemplate(resolver(0), mainUrl(0));
        Templates tenant1 = XmlFactories.getTemplate(resolver(1), mainUrl(1));
        XmlFactories.getTemplate(resolver(2), mainUrl(2));

        // touch tenant 0 so tenant 1 becomes the least recently used entry
        assertThat(XmlFactories.getTemplate(resolver(0), mainUrl(0))).isSameAs(tenant0);
        XmlFactories.getTemplate(resolver(3), mainUrl(3));

        assertThat(XmlFactories.compiledTemplateCacheSize()).isEqualTo(3);
        assertThat(XmlFactories.getTemplate(resolver(0), mainUrl(0))).isSameAs(tenant0);
        assertThat(XmlFactories.getTemplate(resolver(1), mainUrl(1))).isNotSameAs(tenant1);
    }

    @Test
    void shrinkingMaxSizeEvictsImmediately() throws Exception {
        for (int tenant = 0; tenant < 5; tenant++) {
            XmlFactories.getTemplate(resolver(tenant), mainUrl(tenant));
        }
        assertThat(XmlFactories.compiledTemplateCacheSize()).isEqualTo(5);

        XmlFactories.setCompiledTemplateCacheMaxSize(2);

        assertThat(XmlFactories.compiledTemplateCacheSize()).isEqualTo(2);
    }

    /**
     * Compiles templates for {@code tenants} roots round-robin; all threads start together to
     * maximise interleaving. Fails if any compilation throws.
     */
    private static List<Templates> compileConcurrently(int threads, int compilations, int tenants) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<Templates>> futures = new ArrayList<>();
            for (int i = 0; i < compilations; i++) {
                int tenant = i % tenants;
                futures.add(executor.submit(() -> {
                    start.await();
                    return XmlFactories.getTemplate(resolver(tenant), mainUrl(tenant));
                }));
            }
            start.countDown();
            List<Templates> results = new ArrayList<>();
            for (Future<Templates> future : futures) {
                results.add(future.get(60, TimeUnit.SECONDS));
            }
            return results;
        } finally {
            executor.shutdownNow();
        }
    }

    private static TemplateResolver resolver(int tenant) throws Exception {
        return new TemplateResolver(Collections.singletonList(URI.create(root(tenant))), FETCHER);
    }

    private static String root(int tenant) {
        return "http://templates.test/xslt/SALES_INVOICE/" + tenant;
    }

    private static String mainUrl(int tenant) {
        return root(tenant) + "/main.xsl";
    }
}
