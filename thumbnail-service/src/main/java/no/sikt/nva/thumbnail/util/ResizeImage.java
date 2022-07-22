package no.sikt.nva.thumbnail.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResizeImage {

    public static final double SAME_SCALE = 1.0;
    /**
     * The logger for this class.
     */
    private static final Logger mLog = LoggerFactory.getLogger(ResizeImage.class);
    private final int thumbWidth;
    private final int thumbHeight;
    private BufferedImage inputImage;
    private boolean isProcessed;
    private BufferedImage outputImage;
    private int imageWidth;
    private int imageHeight;
    private double resizeRatio;
    private int scaledWidth;
    private int scaledHeight;
    private int offsetX;
    private int offsetY;

    public ResizeImage(int thumbWidth, int thumbHeight) {
        this.thumbWidth = thumbWidth;
        this.thumbHeight = thumbHeight;
        this.isProcessed = false;
        this.resizeRatio = 1.0;
    }

    public void setInputImage(File input) throws IOException {
        BufferedImage image = ImageIO.read(input);
        setInputImage(image);
    }

    public void setInputImage(BufferedImage input) {
        this.inputImage = input;
        isProcessed = false;
        imageWidth = inputImage.getWidth(null);
        imageHeight = inputImage.getHeight(null);
    }

    public void writeOutput(File output) throws IOException {
        writeOutput(output, "PNG");
    }

    public void writeOutput(File output, String format) throws IOException {
        mLog.debug("about to write output to file {} with format {}... processing first...", output.getAbsolutePath(),
                   format);
        if (!isProcessed) {
            process();
        }
        mLog.debug("about to let ImageIO.write outputImage {} with format {} to file {} ", outputImage.toString(),
                   format, output.getAbsolutePath());

        ImageIO.write(outputImage, format, output);
    }

    private void process() {
        if (imageWidth == thumbWidth && imageHeight == thumbHeight) {
            mLog.debug("outputImage = inputImage");
            outputImage = inputImage;
        } else {
            calcDimensions();
            paint();
        }

        isProcessed = true;
    }

    private void calcDimensions() {
        resizeRatio = Math.min(((double) thumbWidth) / imageWidth, ((double) thumbHeight) / imageHeight);
        if (resizeRatio > SAME_SCALE) {
            resizeRatio = SAME_SCALE;
        }

        scaledWidth = (int) Math.round(imageWidth * resizeRatio);
        scaledHeight = (int) Math.round(imageHeight * resizeRatio);

        // Center:
        offsetX = (thumbWidth - scaledWidth) / 2;
        offsetY = (thumbHeight - scaledHeight) / 2;

        mLog.debug("recalculated dimensions");
    }

    private void paint() {
        outputImage = new BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics2D = outputImage.createGraphics();

        // Fill background with white color
        graphics2D.setBackground(Color.WHITE);
        graphics2D.setPaint(Color.WHITE);
        graphics2D.fillRect(0, 0, thumbWidth, thumbHeight);

        // Enable smooth, high-quality resampling
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        graphics2D.drawImage(inputImage,
                             offsetX,
                             offsetY,
                             scaledWidth,
                             scaledHeight,
                             null);
        graphics2D.dispose();
    }
}
