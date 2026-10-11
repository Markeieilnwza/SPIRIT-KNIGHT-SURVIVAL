package core;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Set;

public final class Input implements KeyListener {
    private final Set<Integer> pressedKeys = new HashSet<>();
    private int lastKeyPressed = KeyEvent.VK_UNDEFINED;

    public boolean isDown(int keyCode) {
        return pressedKeys.contains(keyCode);
    }

    public boolean consumePressed(int keyCode) {
        if (lastKeyPressed != keyCode) {
            return false;
        }
        lastKeyPressed = KeyEvent.VK_UNDEFINED;
        return true;
    }

    @Override
    public void keyPressed(KeyEvent event) {
        pressedKeys.add(event.getKeyCode());
        lastKeyPressed = event.getKeyCode();
    }

    @Override
    public void keyReleased(KeyEvent event) {
        pressedKeys.remove(event.getKeyCode());
    }

    @Override
    public void keyTyped(KeyEvent event) {
        // Typed characters are not used.
    }
}
