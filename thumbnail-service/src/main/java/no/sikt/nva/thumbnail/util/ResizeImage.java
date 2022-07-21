package no.sikt.nva.thumbnail.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import no.sikt.nva.thumbnail.UnsupportedInputFileFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResizeImage {

    /**
     * Scale input image so that width and height is equal (or smaller) to the output size. The other dimension will be
     * smaller or equal than the output size.
     */
    public static final int RESIZE_FIT_BOTH_DIMENSIONS = 2;
    /**
     * Scale input image so that width or height is equal to the output size. The other dimension will be bigger or
     * equal than the output size.
     */
    public static final int RESIZE_FIT_ONE_DIMENSION = 3;
    /**
     * Do not resize the image. Instead, crop the image (if smaller) or center it (if bigger).
     */
    public static final int NO_RESIZE_ONLY_CROP = 4;
    /**
     * Do not try to scale the image up, only down. If bigger, center it.
     */
    public static final int DO_NOT_SCALE_UP = 16;
    /**
     * If output image is bigger than input image, allow the output to be smaller than expected (the size of the input
     * image).
     */
    public static final int ALLOW_SMALLER = 32;
    /**
     * The logger for this class.
     */
    private static final Logger mLog = LoggerFactory.getLogger(ResizeImage.class);
    public int resizeMethod = RESIZE_FIT_BOTH_DIMENSIONS;
    public int extraOptions = DO_NOT_SCALE_UP;
    private BufferedImage inputImage;
    private boolean isProcessed;
    private BufferedImage outputImage;
    private int imageWidth;
    private int imageHeight;
    private int thumbWidth;
    private int thumbHeight;
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

    public void setInputImage(InputStream input) throws IOException {
        BufferedImage image = ImageIO.read(input);
        setInputImage(image);
    }

    public void setInputImage(BufferedImage input) throws UnsupportedInputFileFormatException {
        if (input == null) {
            throw new UnsupportedInputFileFormatException("The image reader could not open the file.");
        }

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
            calcDimensions(resizeMethod);
            paint();
        }

        isProcessed = true;
    }

    private void calcDimensions(int resizeMethod) {
        switch (resizeMethod) {
            case RESIZE_FIT_BOTH_DIMENSIONS:
                resizeRatio = Math.min(((double) thumbWidth) / imageWidth, ((double) thumbHeight) / imageHeight);
                break;

            case RESIZE_FIT_ONE_DIMENSION:
                resizeRatio = Math.max(((double) thumbWidth) / imageWidth, ((double) thumbHeight) / imageHeight);
                break;

            case NO_RESIZE_ONLY_CROP:
                resizeRatio = 1.0;
                break;
            default:
                break;
        }
        if ((extraOptions & DO_NOT_SCALE_UP) > 0 && resizeRatio > 1.0) {
            resizeRatio = 1.0;
        }

        scaledWidth = (int) Math.round(imageWidth * resizeRatio);
        scaledHeight = (int) Math.round(imageHeight * resizeRatio);

        if ((extraOptions & ALLOW_SMALLER) > 0 && scaledWidth < thumbWidth && scaledHeight < thumbHeight) {
            thumbWidth = scaledWidth;
            thumbHeight = scaledHeight;
        }

        // Center if smaller.
        if (scaledWidth < thumbWidth) {
            offsetX = (thumbWidth - scaledWidth) / 2;
        } else {
            offsetX = 0;
        }

        if (scaledHeight < thumbHeight) {
            offsetY = (thumbHeight - scaledHeight) / 2;
        } else {
            offsetY = 0;
        }

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

        ThumbnailReadyObserver observer = new ThumbnailReadyObserver(Thread.currentThread());
        boolean scalingComplete = graphics2D.drawImage(inputImage, offsetX, offsetY, scaledWidth, scaledHeight,
                                                       observer);

        if (!scalingComplete && observer != null) {
            // ImageObserver must wait for ready
            if (mLog.isDebugEnabled()) {
                throw new RuntimeException("Scaling is not yet complete!");
            } else {
                mLog.warn("ResizeImage: Scaling is not yet complete!");

                while (!observer.ready) {
                    System.err.println("Waiting .4 sec...");
                    try {
                        Thread.sleep(400);
                    } catch (InterruptedException ignored) {
                        // ignored
                    }
                }
            }
        }

        graphics2D.dispose();
    }
}
