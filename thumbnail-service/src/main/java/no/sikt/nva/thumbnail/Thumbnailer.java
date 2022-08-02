package no.sikt.nva.thumbnail;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public interface Thumbnailer extends Closeable {

    /**
     * Generate a Thumbnail of the input file.
     *
     * @param input    Input file that should be processed
     * @param output   File in which should be written
     * @param mimeType MIME-Type of input file (null if unknown)
     * @throws IOException          If file cannot be read/written
     * @throws ThumbnailerException If the thumbnailing process failed.
     */
    void generateThumbnail(File input, File output, String mimeType) throws IOException, ThumbnailerException;

    /**
     * Generate a Thumbnail of the input file.
     *
     * @param input    Input URL that should be processed
     * @param output   File in which should be written
     * @param mimeType MIME-Type of input file (null if unknown)
     * @throws IOException          If file cannot be read/written
     * @throws ThumbnailerException If the thumbnailing process failed.
     */
    void generateThumbnail(URL input, File output, String mimeType) throws IOException, ThumbnailerException;

    /**
     * Generate a Thumbnail of the input file.
     *
     * @param input  Input file that should be processed
     * @param output File in which should be written
     * @throws IOException          If file cannot be read/written
     * @throws ThumbnailerException If the thumbnailing process failed.
     */
    void generateThumbnail(File input, File output) throws IOException, ThumbnailerException;

    /**
     * Generate a Thumbnail of the input file.
     *
     * @param input  Input URL that should be processed
     * @param output File in which should be written
     * @throws IOException          If file cannot be read/written
     * @throws ThumbnailerException If the thumbnailing process failed.
     */
    void generateThumbnail(URL input, File output) throws IOException, ThumbnailerException;

    /**
     * Set a new Thumbnail size. All following thumbnails will be generated in this size.
     *
     * @param width  Width in Pixel
     * @param height Height in Pixel
     */
    void setImageSize(int width, int height);

    /**
     * Get the currently set Image Width of this Thumbnailer.
     *
     * @return image width of created thumbnails.
     */
    int getCurrentImageWidth();

    /**
     * Get the currently set Image Height of this Thumbnailer.
     *
     * @return image height of created thumbnails.
     */
    int getCurrentImageHeight();

    /**
     * Get a list of all MIME Types that this Thumbnailer is ready to process.
     *
     * @return List of MIME Types. If null, all Files may be passed to this Thumbnailer.
     */
    List<String> getAcceptedMimeTypes();
}
