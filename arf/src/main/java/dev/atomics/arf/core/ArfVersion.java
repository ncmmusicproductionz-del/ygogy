package dev.atomics.arf.core;

/** Dotted numeric version ("4.3", "0.1.0", "4.5.0 NVIDIA 550") with component-wise comparison. */
public record ArfVersion(int[] parts) implements Comparable<ArfVersion> {
    /** Parses leading digits and dots; anything after the first non-version character is ignored. */
    public static ArfVersion parse(String s) {
        int end = 0;
        while (end < s.length() && (Character.isDigit(s.charAt(end)) || s.charAt(end) == '.')) end++;
        String head = s.substring(0, end);
        if (head.isEmpty()) throw new IllegalArgumentException("not a version: " + s);
        String[] bits = head.split("\\.");
        int[] p = new int[bits.length];
        for (int i = 0; i < bits.length; i++) p[i] = bits[i].isEmpty() ? 0 : Integer.parseInt(bits[i]);
        return new ArfVersion(p);
    }

    @Override public int compareTo(ArfVersion o) {
        int n = Math.max(parts.length, o.parts.length);
        for (int i = 0; i < n; i++) {
            int a = i < parts.length ? parts[i] : 0, b = i < o.parts.length ? o.parts[i] : 0;
            if (a != b) return Integer.compare(a, b);
        }
        return 0;
    }

    public boolean atLeast(ArfVersion min) { return compareTo(min) >= 0; }
}
