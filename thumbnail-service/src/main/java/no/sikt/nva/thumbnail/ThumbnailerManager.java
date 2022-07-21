package no.sikt.nva.thumbnail;

import static no.sikt.nva.thumbnail.ThumbnailerConstants.THUMBNAIL_DEFAULT_HEIGHT;
import static no.sikt.nva.thumbnail.ThumbnailerConstants.THUMBNAIL_DEFAULT_WIDTH;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import no.sikt.nva.thumbnail.thumbnailer.NativeImageThumbnailer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThumbnailerManager implements Thumbnailer {

    public static final String NOT_SUPPORTED_MIMETYPE_S = "Not supported mimetype %s";
    private static final Logger logger = LoggerFactory.getLogger(ThumbnailerManager.class);
    private final List<Thumbnailer> thumbnailers;
    private int currentImageHeight;
    private int currentImageWidth;

    public ThumbnailerManager() {
        this.currentImageHeight = THUMBNAIL_DEFAULT_HEIGHT;
        this.currentImageWidth = THUMBNAIL_DEFAULT_WIDTH;
        this.thumbnailers = List.of(new NativeImageThumbnailer());
    }

    @Override
    public void generateThumbnail(File input, File output, String mimeType) throws IOException, ThumbnailerException {
        var correctThumbnailer =
            thumbnailers.stream()
                .filter(thumbnailer -> thumbnailer.getAcceptedMimeTypes().contains(mimeType))
                .findFirst();
        if (correctThumbnailer.isPresent()) {
            var something = correctThumbnailer.get();
            something.generateThumbnail(input, output, mimeType);
        } else {
            throw new ThumbnailerException(String.format(NOT_SUPPORTED_MIMETYPE_S, mimeType));
        }
    }

    @Override
    public void generateThumbnail(URL input, File output, String mimeType) throws IOException, ThumbnailerException {

    }

    @Override
    public void generateThumbnail(File input, File output) throws IOException, ThumbnailerException {

    }

    @Override
    public void generateThumbnail(URL input, File output) throws IOException, ThumbnailerException {

    }

    @Override
    public void close() {
        thumbnailers.forEach(this::closeIndividualThumbnail);
    }

    @Override
    public void setImageSize(int width, int height) {
        currentImageWidth = width;
        currentImageHeight = height;
        thumbnailers.forEach(thumbnailer -> setImageSize(width, height));
    }

    @Override
    public int getCurrentImageWidth() {
        return currentImageWidth;
    }

    @Override
    public int getCurrentImageHeight() {
        return currentImageHeight;
    }

    @Override
    public List<String> getAcceptedMimeTypes() {
        return thumbnailers
                   .stream()
                   .map(Thumbnailer::getAcceptedMimeTypes)
                   .flatMap(Collection::stream)
                   .collect(Collectors.toList());
    }

    private void closeIndividualThumbnail(Thumbnailer thumbnailer) {
        try {
            thumbnailer.close();
        } catch (IOException e) {
            logger.warn("could not close " + thumbnailer.getClass().getName());
        }
    }
}
