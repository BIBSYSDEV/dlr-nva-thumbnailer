package no.sikt.nva.thumbnail.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageResizer {

    public static final String PNG = "PNG";
    public static final int LEFT = 0;
    public static final int TOP = 0;
    private final int thumbWidth;
    private final int thumbHeight;
    private final BufferedImage inputImage;

    public ImageResizer(int thumbWidth, int thumbHeight, File inputFile) throws IOException {
        this.thumbWidth = thumbWidth;
        this.thumbHeight = thumbHeight;
        this.inputImage = ImageIO.read(inputFile);
    }

    public void writeThumbnailToFile(File output) throws IOException {
        var originalWidth = inputImage.getWidth();
        var originalHeight = inputImage.getHeight();
        if (imageIsAlreadyScaled(originalWidth, originalHeight)) {
            writeInputToOutput(inputImage, output);
        } else {
            processImage(
                inputImage,
                originalWidth,
                originalHeight, output);
        }
    }

    private void processImage(BufferedImage bufferedImage, int originalWidth, int originalHeight, File output)
        throws IOException {
        paint(Scale.fromOriginalSize(originalWidth, originalHeight, thumbWidth, thumbHeight), bufferedImage, output);
    }

    private void paint(Scale scale, BufferedImage bufferedImage, File output) throws IOException {
        var outputImage = new BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics2D = outputImage.createGraphics();

        // Fill background with white color
        graphics2D.setBackground(Color.WHITE);
        graphics2D.setPaint(Color.WHITE);
        graphics2D.fillRect(LEFT, TOP, thumbWidth, thumbHeight);

        // Enable smooth, high-quality resampling
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        graphics2D.drawImage(bufferedImage,
                             scale.getOffsetX(),
                             scale.getOffsetY(),
                             scale.getScaledWidth(),
                             scale.getScaledHeight(),
                             null);
        graphics2D.dispose();
        writeToOutput(outputImage, output);
    }

    private void writeInputToOutput(BufferedImage bufferedImage, File output) throws IOException {
        writeToOutput(bufferedImage, output);
    }

    private void writeToOutput(BufferedImage bufferedImage, File output) throws IOException {
        ImageIO.write(bufferedImage, PNG, output);
    }

    private boolean imageIsAlreadyScaled(int originalWidth, int originalHeight) {
        return originalWidth == thumbWidth && originalHeight == thumbHeight;
    }

    private static final  class Scale {

        private static final double SAME_SCALE = 1.0;
        private final int scaledWidth;
        private final int scaledHeight;
        private final int offsetX;
        private final int offsetY;

        private Scale(int scaledWidth, int scaledHeight, int offsetX, int offsetY) {

            this.scaledWidth = scaledWidth;
            this.scaledHeight = scaledHeight;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }

        public static Scale fromOriginalSize(int originalWidth, int originalHeight, int thumbWidth, int thumbHeight) {
            var resizeRatio = Math.min(((double) thumbWidth) / originalWidth, ((double) thumbHeight) / originalHeight);

            if (resizeRatio > SAME_SCALE) {
                resizeRatio = SAME_SCALE;
            }

            var scaledWidth = (int) Math.round(originalWidth * resizeRatio);
            var scaledHeight = (int) Math.round(originalHeight * resizeRatio);

            // Center:
            var offsetX = (thumbWidth - scaledWidth) / 2;
            var offsetY = (thumbHeight - scaledHeight) / 2;

            return new Scale(scaledWidth, scaledHeight, offsetX, offsetY);
        }

        public int getScaledWidth() {
            return scaledWidth;
        }

        public int getScaledHeight() {
            return scaledHeight;
        }

        public int getOffsetX() {
            return offsetX;
        }

        public int getOffsetY() {
            return offsetY;
        }
    }
}
