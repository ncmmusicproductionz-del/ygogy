package dev.atomics.arf.render;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Headless device that records calls. Used by tests and when ARF is inert. */
public class NullRenderDevice implements RenderDevice {
    public final List<String> log = new ArrayList<>();
    private int next = 1;

    @Override public int createBuffer(long sizeBytes) { log.add("buffer " + sizeBytes); return next++; }
    @Override public int createTexture(int w, int h, Format f) { log.add("texture " + w + "x" + h + " " + f); return next++; }
    @Override public void destroy(int handle) { log.add("destroy " + handle); }
    @Override public int createPipeline(String name, Map<String, String> src) throws CompileException { log.add("pipeline " + name); return next++; }
    @Override public void beginPass(FramePass pass, int... colour) { log.add("begin " + pass); }
    @Override public void endPass() { log.add("end"); }
}
