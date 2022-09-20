package no.sikt.nva.thumbnail.thumbnailer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;
import no.sikt.nva.thumbnail.AbstractThumbnailer;
import no.sikt.nva.thumbnail.util.ImageResizer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

public class PdfThumbnailer extends AbstractThumbnailer {

    private static final int FIRST_PAGE_INDEX = 0;
    //Out of heap memory? decrease DPI might solve the problem.
    private static final int DPI = 100;
    private static final String PNG_FILE_FORMAT = "PNG";
    private final File temporaryFileForPartiallyGeneratedResults;

    public PdfThumbnailer(ThumbnailerInitializer thumbnailerInitializer) {
        super();
        this.temporaryFileForPartiallyGeneratedResults = new File(
            thumbnailerInitializer.getPartiallyProcessedFilename());
    }

    @Override
    public void generateThumbnail(File input, File output) throws IOException {
        encodeInput(input);
        resizeToThumbnailSpecs(output);
        temporaryFileForPartiallyGeneratedResults.deleteOnExit();
    }

    @Override
    public List<String> getAcceptedMimeTypes() {
        return List.of("application/pdf");
    }

    private static BufferedImage writeImageFirstPage(File input) throws IOException {
        try (var document = PDDocument.load(input)) {
            var pdfRenderer = new PDFRenderer(document);
            return pdfRenderer.renderImageWithDPI(FIRST_PAGE_INDEX,
                                                  DPI,
                                                  ImageType.RGB);
        }
    }

    private void resizeToThumbnailSpecs(File output) throws IOException {
        var imageResizer = new ImageResizer(thumbWidth,
                                            thumbHeight,
                                            temporaryFileForPartiallyGeneratedResults);
        imageResizer.writeThumbnailToFile(output);
    }

    private void encodeInput(File input) throws IOException {
        var bufferedImage = writeImageFirstPage(input);
        ImageIO.write(bufferedImage,
                      PNG_FILE_FORMAT,
                      temporaryFileForPartiallyGeneratedResults);
    }
}
