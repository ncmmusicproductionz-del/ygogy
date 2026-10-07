package dev.atomics.arf.snapshot;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Hand-off between the main thread (producer) and the render thread (consumer). Snapshots are immutable, so
 * publishing is a single atomic reference swap: the renderer always draws from the last COMPLETE snapshot and
 * can never observe a half-written one.
 */
public final class SnapshotBuffer {
    private final AtomicReference<WorldSnapshot> latest = new AtomicReference<>(WorldSnapshot.empty());

    /** Main thread, after PZ's update. Out-of-order frames are dropped. */
    public void publish(WorldSnapshot s) {
        latest.accumulateAndGet(s, (cur, next) -> next.frameId() >= cur.frameId() ? next : cur);
    }

    /** Render thread, once per frame. */
    public WorldSnapshot latest() { return latest.get(); }
}
