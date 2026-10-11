package world;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * Infinite graveyard background: seamless grass tile + hash-placed props
 * (trees, pumpkins, graves, rare mansion). Props are deterministic per cell,
 * so the world looks the same every time you scroll back.
 * Props are solid: call {@link #resolve(double, double, double)} after moving an entity.
 */
public final class GridBackground {
    private static final int TILE = 512;               // grass_tile.png is 512x512
    private static final int CELL = 320;               // one prop slot per cell
    private static final int PROP_CHANCE = 3;          // 1 in 3 cells has a prop
    private static final int MANSION_CHANCE = 90;      // 1 in 90 cells is a mansion
    private static final int SAFE_RADIUS = 220;        // keep spawn area clear
    private static final int MAX_PROP_W = 380;         // widest sprite (mansion)
    private static final int MAX_PROP_H = 220;         // tallest sprite

    private static final Color GROUND_FALLBACK = new Color(44, 46, 16);

    private static final String[] TREES = {"deco_tree.png"};
    private static final String[] PUMPKINS = {"deco_pumpkin.png"};
    private static final String[] GRAVES = {
            "deco_grave_small_0.png", "deco_grave_small_1.png",
            "deco_grave_small_2.png", "deco_grave_small_3.png"};
    private static final String[] GRAVE_CLUSTERS = {
            "deco_graves_big_0.png", "deco_graves_big_1.png"};
    private static final String MANSION = "deco_mansion.png";

    private static final double[] DEFAULT_HITBOX = {0.8, 0.30};
    private static final Map<String, double[]> HITBOX = new HashMap<>();

    static {
        HITBOX.put("deco_tree.png", new double[]{0.30, 0.20});      
        HITBOX.put("deco_pumpkin.png", new double[]{0.85, 0.40});
        HITBOX.put("deco_grave_small_0.png", new double[]{0.70, 0.30});
        HITBOX.put("deco_grave_small_1.png", new double[]{0.80, 0.30});
        HITBOX.put("deco_grave_small_2.png", new double[]{0.80, 0.30});
        HITBOX.put("deco_grave_small_3.png", new double[]{0.70, 0.30});
        HITBOX.put("deco_graves_big_0.png", new double[]{0.90, 0.35});
        HITBOX.put("deco_graves_big_1.png", new double[]{0.90, 0.35});
        HITBOX.put("deco_mansion.png", new double[]{0.95, 0.45});    
    }

    private static final Map<String, BufferedImage> CACHE = new HashMap<>();
    private static final BufferedImage MISSING = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

    private static RadialGradientPaint vignette;
    private static int vignetteW, vignetteH;

    private GridBackground() {
    }

    /** A placed prop: world position is the bottom-center of the sprite. */
    private static final class Prop {
        final String name;
        final BufferedImage img;
        final int wx, wy;

        Prop(String name, BufferedImage img, int wx, int wy) {
            this.name = name;
            this.img = img;
            this.wx = wx;
            this.wy = wy;
        }
    }

    public static void render(Graphics2D g, Camera camera, int width, int height) {
        Object oldInterp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        drawGround(g, camera, width, height);
        drawProps(g, camera, width, height);

        if (oldInterp != null) {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, oldInterp);
        }
        drawVignette(g, width, height);
    }

    // ------------------------------------------------------------- collision

    /**
     * Pushes a circle (center x,y, given radius, in world coordinates) out of any prop it overlaps.
     * Returns the corrected {x, y}. Because it pushes along the surface normal, the entity
     * slides along walls instead of sticking.
     */
    public static double[] resolve(double x, double y, double radius) {
        for (int pass = 0; pass < 2; pass++) {          // 2 passes handle touching two props at once
            int firstCx = (int) Math.floor((x - radius - MAX_PROP_W / 2.0) / CELL);
            int lastCx = (int) Math.floor((x + radius + MAX_PROP_W / 2.0) / CELL);
            int firstCy = (int) Math.floor((y - radius) / CELL);
            int lastCy = (int) Math.floor((y + radius + MAX_PROP_H) / CELL);

            for (int cy = firstCy; cy <= lastCy; cy++) {
                for (int cx = firstCx; cx <= lastCx; cx++) {
                    Prop p = propAt(cx, cy);
                    if (p == null) {
                        continue;
                    }
                    double[] f = HITBOX.getOrDefault(p.name, DEFAULT_HITBOX);
                    double left = p.wx - p.img.getWidth() * f[0] / 2.0;
                    double right = p.wx + p.img.getWidth() * f[0] / 2.0;
                    double bottom = p.wy;
                    double top = p.wy - p.img.getHeight() * f[1];

                    double nearX = Math.max(left, Math.min(x, right));
                    double nearY = Math.max(top, Math.min(y, bottom));
                    double dx = x - nearX;
                    double dy = y - nearY;
                    double d2 = dx * dx + dy * dy;
                    if (d2 >= radius * radius) {
                        continue;
                    }
                    if (d2 > 1e-9) {
                        double d = Math.sqrt(d2);
                        double push = radius - d;
                        x += dx / d * push;
                        y += dy / d * push;
                    } else {
                        // center is inside the box: leave through the nearest side
                        double toLeft = x - left;
                        double toRight = right - x;
                        double toTop = y - top;
                        double toBottom = bottom - y;
                        double m = Math.min(Math.min(toLeft, toRight), Math.min(toTop, toBottom));
                        if (m == toLeft) x = left - radius;
                        else if (m == toRight) x = right + radius;
                        else if (m == toTop) y = top - radius;
                        else y = bottom + radius;
                    }
                }
            }
        }
        return new double[]{x, y};
    }

    // ---------------------------------------------------------------- ground

    private static void drawGround(Graphics2D g, Camera camera, int width, int height) {
        int offsetX = (int) Math.floor(camera.worldToScreenX(0.0));
        int offsetY = (int) Math.floor(camera.worldToScreenY(0.0));

        int firstTx = (int) Math.floor(-offsetX / (double) TILE);
        int firstTy = (int) Math.floor(-offsetY / (double) TILE);
        int lastTx = firstTx + width / TILE + 1;
        int lastTy = firstTy + height / TILE + 1;

        BufferedImage tile = load("grass_tile.png");
        for (int ty = firstTy; ty <= lastTy; ty++) {
            for (int tx = firstTx; tx <= lastTx; tx++) {
                int sx = tx * TILE + offsetX;
                int sy = ty * TILE + offsetY;
                if (tile == MISSING) {
                    g.setColor(GROUND_FALLBACK);
                    g.fillRect(sx, sy, TILE, TILE);
                } else {
                    g.drawImage(tile, sx, sy, TILE, TILE, null);
                }
            }
        }
    }

    // ----------------------------------------------------------------- props

    private static void drawProps(Graphics2D g, Camera camera, int width, int height) {
        double originX = camera.worldToScreenX(0.0);
        double originY = camera.worldToScreenY(0.0);
        double worldLeft = -originX;
        double worldTop = -originY;

        int firstCx = (int) Math.floor((worldLeft - MAX_PROP_W) / CELL);
        int lastCx = (int) Math.floor((worldLeft + width + MAX_PROP_W) / CELL);
        int firstCy = (int) Math.floor(worldTop / CELL);
        int lastCy = (int) Math.floor((worldTop + height + MAX_PROP_H) / CELL);

        // Row by row (top to bottom) so lower props overlap higher ones.
        for (int cy = firstCy; cy <= lastCy; cy++) {
            for (int cx = firstCx; cx <= lastCx; cx++) {
                Prop p = propAt(cx, cy);
                if (p == null) {
                    continue;
                }
                int sx = (int) Math.floor(camera.worldToScreenX(p.wx)) - p.img.getWidth() / 2;
                int sy = (int) Math.floor(camera.worldToScreenY(p.wy)) - p.img.getHeight();
                if (sx > width || sy > height || sx + p.img.getWidth() < 0 || sy + p.img.getHeight() < 0) {
                    continue;
                }
                g.drawImage(p.img, sx, sy, null);
            }
        }
    }

    /** The single source of truth for what sits in a cell (used by drawing AND collision). */
    private static Prop propAt(int cx, int cy) {
        int h = hash(cx, cy);
        boolean mansion = Math.floorMod(h >>> 5, MANSION_CHANCE) == 0;
        if (!mansion && Math.floorMod(h >>> 3, PROP_CHANCE) != 0) {
            return null;
        }

        int wx = cx * CELL + Math.floorMod(h >>> 9, CELL);
        int wy = cy * CELL + Math.floorMod(h >>> 17, CELL);
        if (Math.hypot(wx, wy) < SAFE_RADIUS) {
            return null;
        }

        String name = mansion ? MANSION : pickProp(h >>> 11);
        BufferedImage img = load(name);
        if (img == MISSING) {
            return null;
        }
        return new Prop(name, img, wx, wy);
    }

    /** Weighted pick: pumpkins and small graves common, clusters and trees less so. */
    private static String pickProp(int bits) {
        int r = Math.floorMod(bits, 100);
        if (r < 28) return TREES[Math.floorMod(bits >>> 7, TREES.length)];
        if (r < 55) return PUMPKINS[Math.floorMod(bits >>> 7, PUMPKINS.length)];
        if (r < 85) return GRAVES[Math.floorMod(bits >>> 7, GRAVES.length)];
        return GRAVE_CLUSTERS[Math.floorMod(bits >>> 7, GRAVE_CLUSTERS.length)];
    }

    // --------------------------------------------------------------- helpers

    private static BufferedImage load(String name) {
        BufferedImage cached = CACHE.get(name);
        if (cached != null) {
            return cached;
        }
        BufferedImage img = MISSING;
        File file = new File("res", name);
        if (!file.isFile()) {
            System.err.println("Background image not found: " + file.getPath());
        } else {
            try {
                BufferedImage read = ImageIO.read(file);
                if (read == null) {
                    System.err.println("Unsupported background image: " + file.getPath());
                } else {
                    img = read;
                }
            } catch (IOException exception) {
                System.err.println("Failed to load background image " + file.getPath()
                        + ": " + exception.getMessage());
            }
        }
        CACHE.put(name, img);
        return img;
    }

    private static int hash(int x, int y) {
        int h = x * 73856093 ^ y * 19349663;
        h ^= h >>> 13;
        h *= 0x5bd1e995;
        h ^= h >>> 15;
        return h;
    }

    private static void drawVignette(Graphics2D g, int width, int height) {
        if (vignette == null || vignetteW != width || vignetteH != height) {
            float radius = (float) Math.hypot(width, height) / 2f;
            vignette = new RadialGradientPaint(
                    new Point2D.Float(width / 2f, height / 2f), radius,
                    new float[]{0.55f, 1.0f},
                    new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 170)});
            vignetteW = width;
            vignetteH = height;
        }
        g.setPaint(vignette);
        g.fillRect(0, 0, width, height);
    }
}