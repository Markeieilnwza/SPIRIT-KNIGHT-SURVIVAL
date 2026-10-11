package sprite;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageReader;
import javax.imageio.ImageIO;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class SpriteLoader {
    private static final Map<String, Sprite> CACHE = new HashMap<>();

    private SpriteLoader() {
    }

    public static void draw(Graphics2D g, String fileName, int x, int y, int size, Color fallback) {
        if (!drawIfAvailable(g, fileName, x, y, size)) {
            g.setColor(fallback);
            g.fillRect(x, y, size, size);
        }
    }

    public static boolean drawIfAvailable(Graphics2D g, String fileName, int x, int y, int size) {
        Sprite sprite = load(fileName);
        if (sprite == null) {
            return false;
        }
        Object oldHint = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(sprite.currentFrame(), x, y, size, size, null);
        if (oldHint != null) {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, oldHint);
        }
        return true;
    }

    private static Sprite load(String fileName) {
        if (CACHE.containsKey(fileName)) {
            return CACHE.get(fileName);
        }
        Sprite sprite = null;
        File file = new File("res", fileName);
        if (!file.isFile()) {
            file = new File(fileName);
        }
        if (!file.isFile() && fileName != null && fileName.contains("/")) {
            file = new File("res", new File(fileName).getName());
        }
        if (file.isFile()) {
            try {
                sprite = fileName.toLowerCase().endsWith(".gif")
                        ? loadGif(file) : loadStillImage(file);
            } catch (IOException exception) {
                System.err.println("Failed to load image " + file.getPath() + ": "
                        + exception.getMessage());
            }
        }
        CACHE.put(fileName, sprite);
        return sprite;
    }

    private static Sprite loadStillImage(File file) throws IOException {
        BufferedImage image = ImageIO.read(file);
        if (image == null) {
            System.err.println("Unable to read image: " + file.getPath());
            return null;
        }
        return new Sprite(List.of(image), new int[] {1});
    }

    private static Sprite loadGif(File file) throws IOException {
        try (ImageInputStream input = ImageIO.createImageInputStream(file)) {
            if (input == null) {
                throw new IOException("Unable to open image stream");
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw new IOException("No GIF reader available");
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(input, false, false);
                int frameCount = reader.getNumImages(true);
                Node streamRoot = reader.getStreamMetadata()
                        .getAsTree("javax_imageio_gif_stream_1.0");
                Node screen = findChild(streamRoot, "LogicalScreenDescriptor");
                int width = getAttribute(screen, "logicalScreenWidth", reader.getWidth(0));
                int height = getAttribute(screen, "logicalScreenHeight", reader.getHeight(0));
                BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                List<BufferedImage> frames = new ArrayList<>(frameCount);
                int[] delays = new int[frameCount];
                FrameInfo previous = null;
                BufferedImage previousCanvas = null;
                for (int i = 0; i < frameCount; i++) {
                    IIOMetadata metadata = reader.getImageMetadata(i);
                    Node root = metadata.getAsTree("javax_imageio_gif_image_1.0");
                    Node descriptor = findChild(root, "ImageDescriptor");
                    int left = getAttribute(descriptor, "imageLeftPosition", 0);
                    int top = getAttribute(descriptor, "imageTopPosition", 0);
                    BufferedImage frame = reader.read(i);
                    Graphics2D graphics = canvas.createGraphics();
                    if (previous != null) {
                        if ("restoreToBackgroundColor".equals(previous.disposal)) {
                            graphics.setComposite(java.awt.AlphaComposite.Clear);
                            graphics.fillRect(previous.left, previous.top,
                                    previous.width, previous.height);
                        } else if ("restoreToPrevious".equals(previous.disposal)
                                && previousCanvas != null) {
                            graphics.setComposite(java.awt.AlphaComposite.Src);
                            graphics.drawImage(previousCanvas, 0, 0, null);
                        }
                    }
                    previousCanvas = "restoreToPrevious".equals(getDisposal(root))
                            ? copyImage(canvas) : null;
                    graphics.setComposite(java.awt.AlphaComposite.SrcOver);
                    graphics.drawImage(frame, left, top, null);
                    graphics.dispose();
                    frames.add(copyImage(canvas));
                    delays[i] = getFrameDelay(metadata);
                    previous = new FrameInfo(left, top, frame.getWidth(), frame.getHeight(),
                            getDisposal(root));
                }
                return new Sprite(frames, delays);
            } finally {
                reader.dispose();
            }
        }
    }

    private static int getFrameDelay(IIOMetadata metadata) {
        Node root = metadata.getAsTree("javax_imageio_gif_image_1.0");
        Node control = findChild(root, "GraphicControlExtension");
        if (control != null) {
            return Math.max(2, getAttribute(control, "delayTime", 10)) * 10;
        }
        return 100;
    }

    private static String getDisposal(Node root) {
        Node control = findChild(root, "GraphicControlExtension");
        if (control == null || control.getAttributes().getNamedItem("disposalMethod") == null) {
            return "none";
        }
        return control.getAttributes().getNamedItem("disposalMethod").getNodeValue();
    }

    private static Node findChild(Node parent, String name) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (name.equals(children.item(i).getNodeName())) {
                return children.item(i);
            }
        }
        return null;
    }

    private static int getAttribute(Node node, String name, int fallback) {
        if (node == null || node.getAttributes().getNamedItem(name) == null) {
            return fallback;
        }
        return Integer.parseInt(node.getAttributes().getNamedItem(name).getNodeValue());
    }

    private static BufferedImage copyImage(BufferedImage image) {
        BufferedImage copy = new BufferedImage(
                image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = copy.createGraphics();
        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();
        return copy;
    }

    private static final class FrameInfo {
        private final int left;
        private final int top;
        private final int width;
        private final int height;
        private final String disposal;

        private FrameInfo(int left, int top, int width, int height, String disposal) {
            this.left = left;
            this.top = top;
            this.width = width;
            this.height = height;
            this.disposal = disposal;
        }
    }

    private static final class Sprite {
        private final List<BufferedImage> frames;
        private final int[] delays;
        private final long duration;
        private final long startTime = System.nanoTime();

        private Sprite(List<BufferedImage> frames, int[] delays) {
            this.frames = frames;
            this.delays = delays;
            long total = 0L;
            for (int delay : delays) {
                total += delay;
            }
            duration = total * 1_000_000L;
        }

        private BufferedImage currentFrame() {
            long elapsed = (System.nanoTime() - startTime) % duration;
            for (int i = 0; i < delays.length; i++) {
                long frameDuration = delays[i] * 1_000_000L;
                if (elapsed < frameDuration) {
                    return frames.get(i);
                }
                elapsed -= frameDuration;
            }
            return frames.get(0);
        }
    }
}
