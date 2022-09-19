package no.sikt.nva.thumbnail.thumbnailer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
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
    private PDDocument document;

    public PdfThumbnailer(ThumbnailerInitializer thumbnailerInitializer) {
        super();
        this.temporaryFileForPartiallyGeneratedResults = new File(
            thumbnailerInitializer.getPartiallyProcessedFilename());
    }

    @Override
    public void generateThumbnail(File input, File output) throws IOException {

        loadPDF(input);
        encodeInput();
        resizeToThumbnailSpecs(output);
        temporaryFileForPartiallyGeneratedResults.deleteOnExit();
    }

    @Override
    public List<String> getAcceptedMimeTypes() {
        return List.of("application/pdf");
    }

    @Override
    public void close() throws IOException {
        if (Objects.nonNull(document)) {
            document.close();
        }
    }

    private void loadPDF(File input) throws IOException {
        this.document = PDDocument.load(input);
    }

    private void resizeToThumbnailSpecs(File output) throws IOException {
        var imageResizer = new ImageResizer(thumbWidth,
                                            thumbHeight,
                                            temporaryFileForPartiallyGeneratedResults);
        imageResizer.writeThumbnailToFile(output);
    }

    private void encodeInput() throws IOException {
        var bufferedImage = writeImageFirstPage();
        ImageIO.write(bufferedImage,
                      PNG_FILE_FORMAT,
                      temporaryFileForPartiallyGeneratedResults);
    }

    private BufferedImage writeImageFirstPage() throws IOException {

        var pdfRenderer = new PDFRenderer(document);
        return pdfRenderer.renderImageWithDPI(FIRST_PAGE_INDEX,
                                              DPI,
                                              ImageType.RGB);
    }
}
