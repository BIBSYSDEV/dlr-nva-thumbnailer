package no.sikt.nva.thumbnail;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import no.sikt.nva.thumbnail.thumbnailer.FFMpegThumbnailer;
import no.sikt.nva.thumbnail.thumbnailer.MsOfficeThumbnailer;
import no.sikt.nva.thumbnail.thumbnailer.NativeImageThumbnailer;
import no.sikt.nva.thumbnail.thumbnailer.OpenOfficeThumbnailer;
import no.sikt.nva.thumbnail.thumbnailer.PdfThumbnailer;
import no.sikt.nva.thumbnail.thumbnailer.ThumbnailerInitializer;

public class ThumbnailerManager {

    public static final String NOT_SUPPORTED_MIMETYPE_S = "Not supported mimetype %s";
    private final List<Thumbnailer> thumbnailers;

    public ThumbnailerManager(ThumbnailerInitializer thumbnailerInitializer) {
        this.thumbnailers = List.of(new NativeImageThumbnailer(),
                                    new FFMpegThumbnailer(thumbnailerInitializer),
                                    new PdfThumbnailer(thumbnailerInitializer),
                                    new MsOfficeThumbnailer(),
                                    new OpenOfficeThumbnailer());
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

    public List<String> getAcceptedMimeTypes() {
        return thumbnailers
                   .stream()
                   .map(Thumbnailer::getAcceptedMimeTypes)
                   .flatMap(Collection::stream)
                   .collect(Collectors.toList());
    }
}
