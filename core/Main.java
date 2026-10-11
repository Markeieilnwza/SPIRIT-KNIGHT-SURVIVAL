package core;

import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame window = new JFrame("spirit knight survival");
            GamePanel panel = new GamePanel();
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setContentPane(panel);
            window.setPreferredSize(new Dimension(1980, 1080));
            window.pack();
            window.setLocationRelativeTo(null);
            window.setResizable(false);
            window.setVisible(true);
            panel.requestFocusInWindow();
            panel.start();
        });
    }
}
