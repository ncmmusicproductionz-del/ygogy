package dev.atomics.arf.content;

import java.util.Optional;

/** Public extension point: turns a tile object into geometry, or declines so the next provider is tried. */
@FunctionalInterface
public interface MeshProvider {
    Optional<Mesh> provide(TileObject obj);
}
