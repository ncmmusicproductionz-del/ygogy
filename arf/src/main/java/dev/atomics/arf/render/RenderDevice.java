package dev.atomics.arf.render;

import java.util.Map;

/**
 * Thin GPU abstraction (buffers, textures, pipelines, passes). All draw work goes through this so a Vulkan
 * backend can be added later without touching passes or shader packs. v1 implements it on OpenGL 4.3 core
 * through PZ's existing LWJGL context (M2).
 */
public interface RenderDevice {
    enum Format { RGBA8, RG16F, RGBA16F, R11G11B10F, D32F }

    int createBuffer(long sizeBytes);
    int createTexture(int width, int height, Format format);
    void destroy(int handle);

    /** @param stageSources shader stage ("vert"/"frag") to fully preprocessed GLSL; throws {@link CompileException}. */
    int createPipeline(String name, Map<String, String> stageSources) throws CompileException;

    void beginPass(FramePass pass, int... colourAttachments);
    void endPass();

    final class CompileException extends Exception {
        public CompileException(String message) { super(message); }
    }
}
