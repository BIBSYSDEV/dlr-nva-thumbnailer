package no.sikt.nva.thumbnail;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import no.sikt.nva.thumbnail.thumbnailer.FFMpegThumbnailer;
import no.sikt.nva.thumbnail.thumbnailer.NativeImageThumbnailer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThumbnailerManager implements Closeable {

    public static final String NOT_SUPPORTED_MIMETYPE_S = "Not supported mimetype %s";
    public static final String COULD_NOT_CLOSE_INDIVIDUAL_THUMBNAIL_WARNING = "could not close %s";
    private static final Logger logger = LoggerFactory.getLogger(ThumbnailerManager.class);
    private final List<Thumbnailer> thumbnailers;

    public ThumbnailerManager() {
        this.thumbnailers = List.of(new NativeImageThumbnailer(), new FFMpegThumbnailer());
    }

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
    public void close() {
        thumbnailers.forEach(this::closeIndividualThumbnail);
    }

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
        } catch (IOException ignored) {
            logger.warn(String.format(
                COULD_NOT_CLOSE_INDIVIDUAL_THUMBNAIL_WARNING,
                thumbnailer.getClass().getName()));
        }
    }
}
