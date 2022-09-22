package no.sikt.nva.thumbnail.thumbnailer;

import static no.sikt.nva.thumbnail.util.ImageResizer.PNG;
import co.elastic.thumbnails4j.core.Dimensions;
import co.elastic.thumbnails4j.core.Thumbnailer;
import co.elastic.thumbnails4j.core.ThumbnailingException;
import co.elastic.thumbnails4j.doc.DOCThumbnailer;
import co.elastic.thumbnails4j.docx.DOCXThumbnailer;
import co.elastic.thumbnails4j.pptx.PPTXThumbnailer;
import co.elastic.thumbnails4j.xls.XLSThumbnailer;
import co.elastic.thumbnails4j.xlsx.XLSXThumbnailer;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.imageio.ImageIO;
import no.sikt.nva.thumbnail.AbstractThumbnailer;
import no.sikt.nva.thumbnail.ThumbnailerException;
import no.sikt.nva.thumbnail.util.MediaType;

public class MsOfficeThumbnailer extends AbstractThumbnailer {

    @Override
    public void generateThumbnail(File input, File output, String mimeType) throws IOException, ThumbnailerException {
        final MediaType mediaType = MediaType.fromValue(mimeType);

        final Thumbnailer thumbnailer;
        switch (mediaType) {
            case APPLICATION_MS_WORD:
                thumbnailer = new DOCThumbnailer();
                break;
            case APPLICATION_OPEN_XML_OFFICE_WORD:
            case APPLICATION_OPEN_XML_OFFICE_WORD_DOC:
                thumbnailer = new DOCXThumbnailer();
                break;
            case APPLICATION_MS_EXCEL:
                thumbnailer = new XLSThumbnailer();
                break;
            case APPLICATION_OPEN_XML_OFFICE_SPREADSHEET:
            case APPLICATION_OPEN_XML_OFFICE_SPREADSHEET_SHEET:
                thumbnailer = new XLSXThumbnailer();
                break;
            case APPLICATION_OPEN_XML_OFFICE_PRESENTATION:
            case APPLICATION_OPEN_XML_OFFICE_PRESENTATION_PRESENTATION:
                thumbnailer = new PPTXThumbnailer();
                break;
            default:
                throw new ThumbnailerException(String.format("Unexpected mime type: %s", mimeType));
        }

        try {
            List<Dimensions> dimensions = Collections.singletonList(
                new Dimensions(thumbWidth, thumbHeight));
            List<BufferedImage> thumbnails = thumbnailer.getThumbnails(input, dimensions);
            ImageIO.write(thumbnails.get(0), PNG, output);
        } catch (ThumbnailingException e) {
            throw new ThumbnailerException("Failed to generate thumbnail!", e);
        }
    }

    @Override
    public List<String> getAcceptedMimeTypes() {
        return List.of(
            MediaType.APPLICATION_MS_WORD.getValue(),
            MediaType.APPLICATION_OPEN_XML_OFFICE_WORD_DOC.getValue(),
            MediaType.APPLICATION_OPEN_XML_OFFICE_WORD.getValue(),
            MediaType.APPLICATION_MS_EXCEL.getValue(),
            MediaType.APPLICATION_OPEN_XML_OFFICE_SPREADSHEET.getValue(),
            MediaType.APPLICATION_OPEN_XML_OFFICE_SPREADSHEET_SHEET.getValue(),
            MediaType.APPLICATION_OPEN_XML_OFFICE_PRESENTATION.getValue(),
            MediaType.APPLICATION_OPEN_XML_OFFICE_PRESENTATION_PRESENTATION.getValue()
        );
    }
}
