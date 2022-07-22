package no.sikt.nva.thumbnail;

import static no.sikt.nva.thumbnail.ThumbnailerConstants.THUMBNAIL_DEFAULT_HEIGHT;
import static no.sikt.nva.thumbnail.ThumbnailerConstants.THUMBNAIL_DEFAULT_WIDTH;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import nva.commons.core.JacocoGenerated;

public abstract class AbstractThumbnailer implements Thumbnailer {

    /**
     * Height of thumbnail picture to create (in Pixel).
     */
    protected int thumbHeight;

    /*
     * Width of thumbnail picture to create (in Pixel).
     */
    protected int thumbWidth;

    /**
     * Keep memory if this thumbnailer was closed before.
     */
    protected boolean closed;

    /**
     * Initialize the thumbnail size from default constants.
     */
    public AbstractThumbnailer() {
        this.thumbHeight = THUMBNAIL_DEFAULT_HEIGHT;
        this.thumbWidth = THUMBNAIL_DEFAULT_WIDTH;
        this.closed = false;
    }

    /**
     * Generate a Thumbnail of the input file. (You can override this method if you want to handle the different
     * MIME-Types).
     *
     * @param input    Input file that should be processed.
     * @param output   File in which should be written.
     * @param mimeType MIME-Type of input file (null if unknown).
     * @throws IOException          If file cannot be read/written.
     * @throws ThumbnailerException If the thumbnailing process failed.
     */
    @Override
    public void generateThumbnail(File input, File output, String mimeType) throws IOException, ThumbnailerException {
        // Ignore MIME-Type-Hint
        generateThumbnail(input, output);
    }

    /**
     * Generate a Thumbnail of the input file. (You can override this method if you want to handle the different
     * MIME-Types).
     *
     * @param input    InputStream that should be processed.
     * @param output   OutputStream which should be written.
     * @param mimeType MIME-Type of input file (null if unknown).
     * @throws IOException          If file cannot be read/written.
     * @throws ThumbnailerException If the thumbnailing process failed.
     */
    @JacocoGenerated
    @Override
    public void generateThumbnail(URL input, File output, String mimeType) throws IOException, ThumbnailerException {
        generateThumbnail(input, output);
    }

    @JacocoGenerated
    @Override
    public void generateThumbnail(File input, File output) throws IOException, ThumbnailerException {
        throw new ThumbnailerException("This Thumbnailer doesn't support File/file!");
    }

    @JacocoGenerated
    @Override
    public void generateThumbnail(URL input, File output) throws IOException, ThumbnailerException {
        throw new ThumbnailerException("This Thumbnailer doesn't support URL!");
    }

    /**
     * This function will be called after all Thumbnails are generated. Note: This acts as a Deconstructor. Do not
     * expect this object to work after calling this method.
     *
     * @throws IOException If some errors occured during finalising.
     */
    @Override
    public void close() throws IOException {
        // Do nothing for now - other Thumbnailer may need cleanup code here.
        closed = true;
    }

    /**
     * Set a new Thumbnail size. All following thumbnails will be generated in this size.
     *
     * @param thumbWidth  Width in Pixel.
     * @param thumbHeight Height in Pixel.
     */
    @JacocoGenerated
    @Override
    public void setImageSize(int thumbWidth, int thumbHeight) {
        this.thumbHeight = thumbHeight;
        this.thumbWidth = thumbWidth;
    }

    /**
     * Get the currently set Image Width of this Thumbnailer.
     *
     * @return image width of created thumbnails.
     */
    @JacocoGenerated
    @Override
    public int getCurrentImageWidth() {
        return thumbWidth;
    }

    /**
     * Get the currently set Image Height of this Thumbnailer.
     *
     * @return image height of created thumbnails.
     */
    @JacocoGenerated
    @Override
    public int getCurrentImageHeight() {
        return thumbHeight;
    }

    /**
     * Get a list of all MIME Types that this Thumbnailer is ready to process. You should override this method in order
     * to give hints when which Thumbnailer is most appropriate. If you do not override this method, the Thumbnailer
     * will be called in any case - awaiting a ThumbnailException if this thumbnailer cannot treat such a file.
     *
     * @return List of MIME Types. If empty, all Files may be passed to this Thumbnailer.
     */
    @Override
    public abstract List<String> getAcceptedMimeTypes();
}
