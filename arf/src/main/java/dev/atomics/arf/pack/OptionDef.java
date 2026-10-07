package dev.atomics.arf.pack;

import java.util.List;
import java.util.Map;

/**
 * One entry of options.json. Options are declared as data (not parsed from shader comments) so the menu is
 * reliable. Injected into every program as {@code #define ARF_OPT_<NAME> <value>}.
 */
public record OptionDef(String name, String type, Object defaultValue, Double min, Double max, Double step, String dependsOn) {

    public static OptionDef from(Map<String, Object> m) {
        String name = (String) m.get("name");
        String type = (String) m.getOrDefault("type", "bool");
        if (name == null || !name.matches("[A-Z][A-Z0-9_]*")) throw new IllegalArgumentException("option name must be UPPER_SNAKE: " + name);
        if (!List.of("bool", "int", "float", "enum").contains(type)) throw new IllegalArgumentException("unknown option type " + type + " for " + name);
        return new OptionDef(name, type, m.get("default"), num(m.get("min")), num(m.get("max")), num(m.get("step")), (String) m.get("dependsOn"));
    }

    private static Double num(Object o) { return o instanceof Double d ? d : null; }

    /** The GLSL token for a value: bools become 1/0, ints are unsuffixed, floats always carry a decimal point. */
    public String glslValue(Object v) {
        Object val = v == null ? defaultValue : v;
        return switch (type) {
            case "bool" -> Boolean.TRUE.equals(val) ? "1" : "0";
            case "int" -> Integer.toString(clamp(((Number) val).doubleValue()).intValue());
            case "float" -> {
                String t = Double.toString(clamp(((Number) val).doubleValue()));
                yield t;
            }
            default -> String.valueOf(val instanceof Double d ? d.intValue() : val);
        };
    }

    private Double clamp(double d) {
        if (min != null) d = Math.max(min, d);
        if (max != null) d = Math.min(max, d);
        return d;
    }
}
