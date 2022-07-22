package no.sikt.nva.thumbnail.thumbnailer;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;
import no.sikt.nva.thumbnail.AbstractThumbnailer;
import no.sikt.nva.thumbnail.ThumbnailerException;
import no.sikt.nva.thumbnail.util.ResizeImage;

public class NativeImageThumbnailer extends AbstractThumbnailer {

    @Override
    public void generateThumbnail(File input, File output) throws IOException, ThumbnailerException {
        ResizeImage resizer = new ResizeImage(thumbWidth, thumbHeight);
        resizer.setInputImage(input);
        resizer.writeOutput(output);
    }

    /**
     * Get a List of accepted File Types. Normally, these are: bmp, jpg, wbmp, jpeg, png, gif The exact list may depend
     * on the Java installation.
     *
     * @return MIME-Types
     */
    @Override
    public List<String> getAcceptedMimeTypes() {
        return Arrays.asList(ImageIO.getReaderMIMETypes());
    }
}
