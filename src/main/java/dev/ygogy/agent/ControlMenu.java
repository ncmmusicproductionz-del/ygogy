package dev.ygogy.agent;

import javax.swing.*;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;

/**
 * Small live-tuning window (separate from the game's own GL window). Each row edits
 * "scale:<hookId>" in {@link Settings}, which the injected hooks read every call.
 * An in-game GL overlay can replace this once we know the game's render loop.
 */
final class ControlMenu {
    private ControlMenu() {}

    static void open() {
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("[agent] headless: control menu skipped");
            return;
        }
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Zomboid agent");
            f.setLayout(new GridLayout(0, 1));
            f.add(slider("camera zoom x", "scale:zombie.iso.IsoCamera.getZoom"));
            f.setAlwaysOnTop(true);
            f.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
            f.setSize(360, 120);
            f.setVisible(true);
        });
    }

    private static JPanel slider(String label, String key) {
        JPanel p = new JPanel();
        JSlider s = new JSlider(25, 400, 100);
        JLabel l = new JLabel(label + " 1.00");
        s.addChangeListener(e -> {
            double v = s.getValue() / 100.0;
            Settings.set(key, v);
            l.setText(String.format("%s %.2f", label, v));
        });
        p.add(l);
        p.add(s);
        return p;
    }
}
