package dev.atomics.arf.content;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Resolves every tile object through a chain of providers, first match wins:
 * explicit model, procedural, billboard, fallback box. The fallback always matches, so nothing is invisible.
 * Mods add explicit models (OBJ/glTF roof kits, furniture packs) or prepend their own providers.
 */
public final class MeshResolver {
    private final Map<String, String> explicitModels = new HashMap<>();
    private final List<MeshProvider> chain = new ArrayList<>();

    public MeshResolver() {
        chain.add(obj -> Optional.ofNullable(explicitModels.get(obj.sprite())).map(m -> new Mesh(Mesh.Source.EXPLICIT, m)));
        chain.add(MeshResolver::procedural);
        chain.add(obj -> obj.billboardHint() || obj.kind() == TileObject.Kind.PLANT
            ? Optional.of(new Mesh(Mesh.Source.BILLBOARD, obj.sprite())) : Optional.empty());
        chain.add(obj -> Optional.of(new Mesh(Mesh.Source.FALLBACK_BOX, obj.sprite())));
    }

    /** A mod registers an OBJ/glTF for a sprite name. */
    public void registerModel(String sprite, String modelPath) { explicitModels.put(sprite, modelPath); }

    /** Custom providers run after explicit models but before the built-ins. */
    public void addProvider(MeshProvider p) { chain.add(1, p); }

    public Mesh resolve(TileObject obj) {
        for (MeshProvider p : chain) {
            Optional<Mesh> m = p.provide(obj);
            if (m.isPresent()) return m.get();
        }
        throw new IllegalStateException("fallback provider must always match");
    }

    private static final EnumSet<TileObject.Kind> PROCEDURAL_KINDS =
        EnumSet.of(TileObject.Kind.WALL, TileObject.Kind.FLOOR, TileObject.Kind.STAIRS, TileObject.Kind.FENCE, TileObject.Kind.ROOF);

    /** Geometry generated from tile properties (wall direction, height level, roof slope). */
    private static Optional<Mesh> procedural(TileObject o) {
        if (!PROCEDURAL_KINDS.contains(o.kind())) return Optional.empty();
        String name = switch (o.kind()) {
            case WALL -> "wall:dir" + o.wallDirection() + ":lvl" + o.heightLevel();
            case ROOF -> "roof:slope" + o.roofSlope();
            default -> o.kind().name().toLowerCase(java.util.Locale.ROOT) + ":lvl" + o.heightLevel();
        };
        return Optional.of(new Mesh(Mesh.Source.PROCEDURAL, name));
    }
}
