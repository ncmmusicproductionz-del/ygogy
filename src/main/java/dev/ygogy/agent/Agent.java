package dev.ygogy.agent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Java agent entry point.
 *
 * Launch: java -javaagent:zomboid-agent.jar=path/to/agent.properties ...
 *
 * Config (agent.properties):
 *   discover.prefix = zombie.            # dump methods of every loaded class with this prefix
 *   discover.file   = agent-classes.txt  # where the dump goes
 *   hook.1 = float:zombie.iso.IsoCamera#getZoom       # kind:class#method ; kind = float|double
 *   scale.zombie.iso.IsoCamera.getZoom = 1.5          # initial multiplier (also offset.<id>)
 *   menu   = true                                     # open the Swing control window
 */
public final class Agent {
    private Agent() {}

    public static void premain(String args, Instrumentation inst) {
        install(args, inst);
    }

    public static void agentmain(String args, Instrumentation inst) {
        install(args, inst);
    }

    static void install(String args, Instrumentation inst) {
        Properties cfg = load(args);
        AgentBuilder builder = new AgentBuilder.Default()
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .disableClassFormatChanges()
                .ignore(ElementMatchers.nameStartsWith("net.bytebuddy.")
                        .or(ElementMatchers.nameStartsWith("dev.ygogy.")));

        String prefix = cfg.getProperty("discover.prefix");
        if (prefix != null && !prefix.isBlank()) {
            builder = discovery(builder, prefix, cfg.getProperty("discover.file", "agent-classes.txt"));
        }

        for (String key : cfg.stringPropertyNames()) {
            if (key.startsWith("hook.")) {
                builder = hook(builder, cfg.getProperty(key).trim());
            } else if (key.startsWith("scale.") || key.startsWith("offset.")) {
                // scale.zombie.iso.IsoCamera.getZoom = 1.5   ('.' not ':' because ':' ends a properties key)
                int dot = key.indexOf('.');
                try {
                    Settings.set(key.substring(0, dot) + ":" + key.substring(dot + 1),
                            Double.parseDouble(cfg.getProperty(key).trim()));
                } catch (NumberFormatException e) {
                    System.err.println("[agent] ignoring non-numeric " + key);
                }
            }
        }

        builder.installOn(inst);

        if (Boolean.parseBoolean(cfg.getProperty("menu", "false"))) {
            ControlMenu.open();
        }
    }

    /** Read-only: logs the shape of the game's classes so we know what to hook. Changes no bytecode. */
    private static AgentBuilder discovery(AgentBuilder b, String prefix, String file) {
        Path out = Path.of(file);
        return b.type(ElementMatchers.nameStartsWith(prefix))
                .transform((builder, type, loader, module, domain) -> {
                    try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(out,
                            java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND))) {
                        w.println(type.getName());
                        for (MethodDescription.InDefinedShape m : type.getDeclaredMethods()) {
                            w.println("    " + m);
                        }
                    } catch (IOException e) {
                        System.err.println("[agent] discovery write failed: " + e);
                    }
                    return builder;
                });
    }

    private static AgentBuilder hook(AgentBuilder b, String spec) {
        // kind:class#method
        int colon = spec.indexOf(':');
        int hash = spec.indexOf('#');
        if (colon < 0 || hash < colon) {
            System.err.println("[agent] bad hook spec (want kind:class#method): " + spec);
            return b;
        }
        String kind = spec.substring(0, colon);
        String cls = spec.substring(colon + 1, hash);
        String method = spec.substring(hash + 1);

        Class<?> advice = switch (kind) {
            case "float" -> Hooks.FloatValue.class;
            case "double" -> Hooks.DoubleValue.class;
            case "call" -> Hooks.AfterCall.class;
            default -> null;
        };
        if (advice == null) {
            System.err.println("[agent] unknown hook kind: " + kind);
            return b;
        }
        ElementMatcher.Junction<MethodDescription> m = ElementMatchers.named(method);
        if (!kind.equals("call")) {
            m = m.and(ElementMatchers.returns(kind.equals("float") ? float.class : double.class));
        }
        final ElementMatcher.Junction<MethodDescription> methodMatcher = m;
        System.err.println("[agent] hooking " + kind + " " + cls + "#" + method);
        return b.type(ElementMatchers.named(cls))
                .transform((builder, type, loader, module, domain) ->
                        builder.visit(Advice.to(advice).on(methodMatcher)));
    }

    private static Properties load(String args) {
        Properties p = new Properties();
        if (args == null || args.isBlank()) return p;
        try (InputStream in = Files.newInputStream(Path.of(args))) {
            p.load(in);
        } catch (IOException e) {
            System.err.println("[agent] cannot read config " + args + ": " + e);
        }
        return p;
    }
}
