package io.alapierre.ksef.fop;

import org.apache.fop.apps.FOPException;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.configuration.ConfigurationException;
import org.apache.fop.configuration.DefaultConfigurationBuilder;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

class FopRendererPool {

    private final List<FopRenderer> renderers;
    private final AtomicInteger nextRenderer = new AtomicInteger();

    FopRendererPool(InputStream fopConfig, int rendererPoolSize) throws ConfigurationException {
        int safePoolSize = Math.max(1, rendererPoolSize);
        if (safePoolSize > 1) {
            throw new ConfigurationException(
                    "rendererPoolSize greater than 1 requires a repeatable FOP configuration source");
        }

        this.renderers = Collections.singletonList(new FopRenderer(buildConfiguration(fopConfig)));
    }

    FopRendererPool(FopConfigSource fopConfigSource, int rendererPoolSize) throws IOException, ConfigurationException {
        int safePoolSize = Math.max(1, rendererPoolSize);

        List<FopRenderer> configuredRenderers = new ArrayList<>(safePoolSize);
        for (int i = 0; i < safePoolSize; i++) {
            configuredRenderers.add(new FopRenderer(buildConfiguration(fopConfigSource)));
        }
        this.renderers = Collections.unmodifiableList(configuredRenderers);
    }

    void render(OutputStream out, FopRenderer.RenderOperation operation) throws FOPException, TransformerException {
        selectRenderer().render(out, operation);
    }

    private FopRenderer selectRenderer() {
        int index = Math.floorMod(nextRenderer.getAndIncrement(), renderers.size());
        return renderers.get(index);
    }

    private static Configuration buildConfiguration(InputStream fopConfig) throws ConfigurationException {
        DefaultConfigurationBuilder cfgBuilder = new DefaultConfigurationBuilder();
        return cfgBuilder.build(fopConfig);
    }

    private static Configuration buildConfiguration(FopConfigSource fopConfigSource) throws IOException, ConfigurationException {
        try (InputStream fopConfig = fopConfigSource.open()) {
            return buildConfiguration(fopConfig);
        }
    }

    interface FopConfigSource {
        InputStream open() throws IOException;
    }
}
