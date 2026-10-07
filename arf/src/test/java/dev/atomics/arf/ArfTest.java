package dev.atomics.arf;

import static org.junit.jupiter.api.Assertions.*;

import dev.atomics.arf.camera.*;
import dev.atomics.arf.content.*;
import dev.atomics.arf.core.*;
import dev.atomics.arf.pack.*;
import dev.atomics.arf.render.*;
import dev.atomics.arf.snapshot.*;
import java.util.*;
import org.junit.jupiter.api.Test;

class ArfTest {
    // ---- bootstrap ----
    private static ArfBootstrap.Environment env(String zb, String gl, String... ext) {
        return new ArfBootstrap.Environment() {
            public Optional<String> zombieBuddyVersion() { return Optional.ofNullable(zb); }
            public Optional<String> glVersion() { return Optional.ofNullable(gl); }
            public Set<String> glExtensions() { return Set.of(ext); }
        };
    }
    private static Properties cfg() {
        Properties p = new Properties();
        p.setProperty("min.zombiebuddy", "0.1.0");
        p.setProperty("gl.required", "4.3");
        p.setProperty("gl.preferred", "4.5");
        p.setProperty("gl.extensions", "GL_X");
        return p;
    }

    @Test void bootstrapActiveWhenAllChecksPass() {
        var r = ArfBootstrap.run(env("0.2.0", "4.6.0 NVIDIA 550", "GL_X"), cfg());
        assertTrue(r.active());
        assertTrue(r.glPreferredMet());
    }
    @Test void bootstrapInertOnMissingZombieBuddy() {
        var r = ArfBootstrap.run(env(null, "4.5", "GL_X"), cfg());
        assertFalse(r.active());
        assertTrue(r.problems().get(0).contains("ZombieBuddy"));
    }
    @Test void bootstrapInertOnMacOsStyleGl41() {
        var r = ArfBootstrap.run(env("1.0", "4.1 Metal - 88", "GL_X"), cfg());
        assertFalse(r.active());
        assertTrue(r.problems().stream().anyMatch(s -> s.contains("below required")));
    }
    @Test void bootstrapReportsAllProblemsAtOnce() {
        var r = ArfBootstrap.run(env("0.0.1", "3.3", "GL_Y"), cfg());
        assertEquals(3, r.problems().size());
    }
    @Test void versionCompare() {
        assertTrue(ArfVersion.parse("4.3").atLeast(ArfVersion.parse("4.3.0")));
        assertTrue(ArfVersion.parse("4.10").atLeast(ArfVersion.parse("4.9")));
        assertFalse(ArfVersion.parse("4.2.9").atLeast(ArfVersion.parse("4.3")));
    }
    @Test void patchConflictsDetected() {
        assertEquals(List.of("zombie.core.Core#EndFrameUI"), PatchTargets.conflicts(Set.of("zombie.core.Core#EndFrameUI", "other#x")));
    }

    // ---- snapshot ----
    @Test void snapshotBufferKeepsNewestAndDropsStale() {
        var b = new SnapshotBuffer();
        b.publish(new WorldSnapshot(5, 12, 0, 0, 0, 0, 0, 0, false, List.of(), List.of()));
        b.publish(new WorldSnapshot(3, 12, 0, 0, 0, 0, 0, 0, false, List.of(), List.of()));
        assertEquals(5, b.latest().frameId());
    }
    @Test void dayFactorPeaksAtNoon() {
        var noon = new WorldSnapshot(1, 12, 0, 0, 0, 0, 0, 0, false, List.of(), List.of());
        var midnight = new WorldSnapshot(1, 0, 0, 0, 0, 0, 0, 0, false, List.of(), List.of());
        assertEquals(1f, noon.dayFactor(), 1e-5);
        assertEquals(0f, midnight.dayFactor(), 1e-5);
    }

    // ---- camera ----
    @Test void inputRemapFollowsYaw() {
        float[] fwd = InputMapper.remap(1, 0, 0);
        assertEquals(1f, fwd[0], 1e-5);
        assertEquals(0f, fwd[1], 1e-5);
        float[] turned = InputMapper.remap(1, 0, (float) Math.PI / 2);   // facing south
        assertEquals(0f, turned[0], 1e-5);
        assertEquals(1f, turned[1], 1e-5);
        float[] diag = InputMapper.remap(1, 1, 0);
        assertEquals(1f, (float) Math.hypot(diag[0], diag[1]), 1e-5);   // diagonal not faster
    }
    @Test void everyModeSwitchClearsHeldKeys() {
        var rig = new CameraRig();
        rig.input.press(87);
        rig.setMode(CameraMode.FIRST_PERSON);
        assertFalse(rig.input.isHeld(87));
        rig.input.press(87);
        rig.setMode(CameraMode.THIRD_PERSON);
        assertFalse(rig.input.isHeld(87));
    }
    @Test void firstPersonEyeSitsInFrontOfHead() {
        var rig = new CameraRig();
        rig.blendSeconds = 0;
        rig.setMode(CameraMode.FIRST_PERSON);
        Pose p = rig.update(1f, new Vec3(10, 10, 1.7f), CollisionProbe.NONE);
        assertEquals(10.08f, p.position().x(), 1e-4);
    }
    @Test void pitchIsClamped() {
        var rig = new CameraRig();
        rig.setMode(CameraMode.FIRST_PERSON);
        rig.look(0, -1_000_000);
        rig.update(1f, Vec3.ZERO, CollisionProbe.NONE);
        assertEquals(CameraRig.MAX_PITCH, rig.pose().pitch(), 1e-5);
    }
    @Test void lookIsIgnoredInIsometric() {
        var rig = new CameraRig();
        rig.look(100, 100);
        assertEquals(0f, rig.yaw());
    }
    @Test void thirdPersonSpringArmStopsAtWall() {
        var rig = new CameraRig();
        rig.blendSeconds = 0;
        rig.setMode(CameraMode.THIRD_PERSON);
        Vec3 head = new Vec3(0, 0, 1.7f);
        Pose free = rig.update(1f, head, CollisionProbe.NONE);
        Pose hit = rig.update(1f, head, (a, b, r) -> 0.25f);
        double freeDist = free.position().sub(head).length();
        double hitDist = hit.position().sub(head).length();
        assertTrue(hitDist < freeDist);
        assertTrue(freeDist > 2.5);
    }
    @Test void modeBlendInterpolatesOverQuarterSecond() {
        var rig = new CameraRig();
        rig.setMode(CameraMode.FIRST_PERSON);
        rig.update(1f, Vec3.ZERO, CollisionProbe.NONE);
        rig.setMode(CameraMode.THIRD_PERSON);
        Pose start = rig.update(0f, Vec3.ZERO, CollisionProbe.NONE);
        Pose mid = rig.update(0.125f, Vec3.ZERO, CollisionProbe.NONE);
        Pose end = rig.update(0.2f, Vec3.ZERO, CollisionProbe.NONE);
        double d0 = start.position().length(), d1 = mid.position().length(), d2 = end.position().length();
        assertTrue(d0 < d1 && d1 < d2);
    }
    @Test void fovClampedToSliderRange() {
        var rig = new CameraRig();
        rig.setFov(200);
        assertEquals(110f, rig.fovDeg);
        rig.setFov(10);
        assertEquals(60f, rig.fovDeg);
    }
    @Test void aimRayHitsGroundPlane() {
        Pose looking = new Pose(new Vec3(0.5f, 0.5f, 2), 0, (float) -Math.PI / 4);
        Vec3 hit = AimRay.groundHit(looking, 0).orElseThrow();
        assertEquals(2.5f, hit.x(), 1e-4);
        assertArrayEquals(new int[] {2, 0}, AimRay.toTile(hit));
        assertTrue(AimRay.groundHit(new Pose(new Vec3(0, 0, 2), 0, 0.5f), 0).isEmpty());   // looking up
    }
    @Test void cutawayOnlyInThirdPerson() {
        assertTrue(CameraRig.fadeLevel(CameraMode.THIRD_PERSON, 1, 0, true));
        assertFalse(CameraRig.fadeLevel(CameraMode.FIRST_PERSON, 1, 0, true));
        assertFalse(CameraRig.fadeLevel(CameraMode.THIRD_PERSON, 0, 0, true));
    }
    @Test void matrixMultiplyIdentity() {
        float[] id = {1,0,0,0, 0,1,0,0, 0,0,1,0, 0,0,0,1};
        float[] p = Mat4.perspective(75, 1.6f, 0.1f, 100f);
        assertArrayEquals(p, Mat4.multiply(id, p), 1e-6f);
    }

    // ---- packs ----
    private static final Map<String, String> MIN_PACK = Map.of(
        "pack.json", "{\"name\":\"Mini\",\"version\":\"1\",\"author\":\"me\",\"minApi\":1}",
        "shaders/composite1.fsh", "void main(){}\n");

    private static PackManager manager() throws PackException { return Builtin.newManager(); }

    @Test void optionsBecomeDefines() throws Exception {
        var pack = ShaderPack.load(Builtin.defaultPackSource());
        var d = pack.defines("LOW", Map.of("EXPOSURE", 2.0));
        assertEquals("0", d.get("SHADOWS"));
        assertEquals("2.0", d.get("EXPOSURE"));
        assertEquals("0.0", d.get("BLOOM_STRENGTH"));
    }
    @Test void optionValuesAreClamped() throws Exception {
        var pack = ShaderPack.load(Builtin.defaultPackSource());
        assertEquals("4.0", pack.defines("HIGH", Map.of("EXPOSURE", 99.0)).get("EXPOSURE"));
    }
    @Test void badOptionNameRejected() {
        var src = PackSource.memory(Map.of(
            "pack.json", "{\"name\":\"x\"}", "options.json", "{\"options\":[{\"name\":\"lower\",\"type\":\"bool\"}]}"));
        assertThrows(PackException.class, () -> ShaderPack.load(src));
    }
    @Test void packNeedingNewerApiRejected() {
        var src = PackSource.memory(Map.of("pack.json", "{\"name\":\"x\",\"minApi\":2}"));
        assertThrows(PackException.class, () -> ShaderPack.load(src));
    }
    @Test void preprocessorInjectsDefinesAndIncludes() throws Exception {
        var pre = new ShaderPreprocessor(PackSource.memory(Map.of("a.fsh", "#version 430 core\n#include \"arf/pbr.glsl\"\nvoid main(){}\n")), Builtin.stdlib());
        var r = pre.process("a.fsh", new LinkedHashMap<>(Map.of("FOO", "1")));
        assertTrue(r.source().startsWith("#version 430 core\n#define ARF_OPT_FOO 1\n"));
        assertTrue(r.source().contains("arfOctEncode"));
        assertEquals(List.of("a.fsh", "arf/pbr.glsl"), r.files());
    }
    @Test void preprocessorDefaultsVersionLine() throws Exception {
        var pre = new ShaderPreprocessor(PackSource.memory(Map.of("a.fsh", "void main(){}\n")), Builtin.stdlib());
        assertTrue(pre.process("a.fsh", Map.of()).source().startsWith("#version 430 core\n"));
    }
    @Test void circularIncludeIsReported() {
        var pre = new ShaderPreprocessor(PackSource.memory(Map.of(
            "a.fsh", "#include \"b.glsl\"\n", "include/b.glsl", "#include \"include/b.glsl\"\n")), Builtin.stdlib());
        var e = assertThrows(PackException.class, () -> pre.process("a.fsh", Map.of()));
        assertTrue(e.getMessage().contains("circular"));
    }
    @Test void missingIncludeNamesFile() {
        var pre = new ShaderPreprocessor(PackSource.memory(Map.of("a.fsh", "#include \"nope.glsl\"\n")), Builtin.stdlib());
        assertTrue(assertThrows(PackException.class, () -> pre.process("a.fsh", Map.of())).getMessage().contains("nope.glsl"));
    }
    @Test void directorySourceCannotEscapePackFolder(@org.junit.jupiter.api.io.TempDir java.nio.file.Path dir) throws Exception {
        var pack = java.nio.file.Files.createDirectory(dir.resolve("pack"));
        java.nio.file.Files.writeString(dir.resolve("secret.txt"), "x");
        java.nio.file.Files.writeString(pack.resolve("ok.txt"), "y");
        var src = PackSource.directory(pack);
        assertEquals(Optional.of("y"), src.read("ok.txt"));
        assertTrue(src.read("../secret.txt").isEmpty());
    }
    @Test void defaultPackLoadsAndCommits() throws Exception {
        var mgr = manager();
        var dev = new NullRenderDevice();
        assertTrue(mgr.reload(dev, ShaderPack.load(Builtin.defaultPackSource()), "HIGH", Map.of()), mgr.lastError().orElse(""));
        assertTrue(mgr.active().get().programs().containsKey("deferred_lighting"));
        assertTrue(mgr.active().get().programs().get("final").get("frag").contains("#define ARF_OPT_EXPOSURE 1.0"));
    }
    @Test void omittedStagesFallBackToDefaultPack() throws Exception {
        var mgr = manager();
        var user = ShaderPack.load(PackSource.memory(MIN_PACK));
        var prep = mgr.prepare(user, "HIGH", Map.of());
        assertTrue(prep.programs().containsKey("composite1"));                 // the pack's own stage
        assertTrue(prep.programs().containsKey("deferred_lighting"));          // default pack's stage
        assertTrue(prep.programs().get("composite1").get("vert").contains("gl_VertexID"));   // fullscreen fallback
    }
    @Test void failedCompileKeepsPreviousPack() throws Exception {
        var mgr = manager();
        var good = new NullRenderDevice();
        var defPack = ShaderPack.load(Builtin.defaultPackSource());
        assertTrue(mgr.reload(good, defPack, "HIGH", Map.of()));
        var before = mgr.active().get();

        RenderDevice failing = new NullRenderDevice() {
            @Override public int createPipeline(String n, Map<String, String> s) throws CompileException {
                if (n.equals("final")) throw new CompileException("final.fsh:3: syntax error");
                return super.createPipeline(n, s);
            }
        };
        assertFalse(mgr.reload(failing, defPack, "HIGH", Map.of()));
        assertSame(before, mgr.active().get());
        assertEquals("final.fsh:3: syntax error", mgr.lastError().get());
    }
    @Test void jsonParsesNestedAndReportsLine() {
        var m = Json.object("{\"a\":[1,2,{\"b\":null}],\"c\":\"x\\ny\",\"d\":true}");
        assertEquals(List.of(1.0, 2.0, new LinkedHashMap<>(Collections.singletonMap("b", null))), m.get("a"));
        var e = assertThrows(IllegalArgumentException.class, () -> Json.parse("{\n\"a\":\n}"));
        assertTrue(e.getMessage().contains("line 3"));
    }
    @Test void uniformApiIsStable() {
        assertEquals(1, ArfUniforms.API);
        assertTrue(ArfUniforms.GROUPS.get("Player state").contains("arf_Panic"));
        assertTrue(ArfUniforms.GROUPS.get("Camera").contains("arf_PrevViewProj"));
    }
    @Test void framePassOrderMatchesDesign() {
        assertEquals(List.of("SHADOW", "GBUFFER", "SSAO", "LIGHTING", "TRANSLUCENT", "VOLUMETRICS", "POST", "UI"),
            Arrays.stream(FramePass.values()).map(Enum::name).toList());
        assertArrayEquals(new int[] {16, 9, 24}, FramePass.CLUSTER_GRID);
    }

    // ---- content ----
    @Test void meshChainFirstMatchWins() {
        var r = new MeshResolver();
        var wall = TileObject.of("walls_01_0", TileObject.Kind.WALL);
        assertEquals(Mesh.Source.PROCEDURAL, r.resolve(wall).source());
        r.registerModel("walls_01_0", "models/wall.glb");
        assertEquals(Mesh.Source.EXPLICIT, r.resolve(wall).source());
        assertEquals(Mesh.Source.BILLBOARD, r.resolve(TileObject.of("tree_0", TileObject.Kind.PLANT)).source());
        assertEquals(Mesh.Source.FALLBACK_BOX, r.resolve(TileObject.of("crate_0", TileObject.Kind.OTHER)).source());
    }
    @Test void customProviderBeatsBuiltinsButNotExplicit() {
        var r = new MeshResolver();
        r.addProvider(o -> o.sprite().startsWith("roof_") ? Optional.of(new Mesh(Mesh.Source.PROCEDURAL, "custom")) : Optional.empty());
        assertEquals("custom", r.resolve(TileObject.of("roof_1", TileObject.Kind.ROOF)).name());
        r.registerModel("roof_1", "m.obj");
        assertEquals(Mesh.Source.EXPLICIT, r.resolve(TileObject.of("roof_1", TileObject.Kind.ROOF)).source());
    }
    @Test void materialDefaultsKeepSpriteOnlyContentLit() {
        var reg = new MaterialRegistry();
        var m = reg.lookup("unknown_sprite");
        assertFalse(m.hasNormal());
        assertEquals(MaterialClass.DEFAULT, m.materialClass());
        assertEquals(0.8f, Material.DEFAULT_ROUGHNESS);
        assertEquals(0f, Material.DEFAULT_METAL);
    }
    @Test void materialsJsonLoads() {
        var reg = new MaterialRegistry();
        assertEquals(1, reg.loadJson("{\"walls_exterior_house_01_0\":{\"normal\":\"n.png\",\"class\":\"wood\"}}"));
        var m = reg.lookup("walls_exterior_house_01_0");
        assertEquals(MaterialClass.WOOD, m.materialClass());
        assertEquals("n.png", m.normal());
        assertEquals("walls_exterior_house_01_0", m.albedo());
    }
}
