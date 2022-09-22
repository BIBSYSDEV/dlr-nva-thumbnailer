package no.sikt.nva.thumbnail.thumbnailer;

import static no.sikt.nva.thumbnail.util.MediaType.APPLICATION_OASIS_OPENDOCUMENT_DATABASE;
import static no.sikt.nva.thumbnail.util.MediaType.APPLICATION_OASIS_OPENDOCUMENT_FORMULA;
import static no.sikt.nva.thumbnail.util.MediaType.APPLICATION_OASIS_OPENDOCUMENT_GRAPHICS;
import static no.sikt.nva.thumbnail.util.MediaType.APPLICATION_OASIS_OPENDOCUMENT_GRAPHICS_TEMPLATE;
import static no.sikt.nva.thumbnail.util.MediaType.APPLICATION_OASIS_OPENDOCUMENT_IMAGE;
import static no.sikt.nva.thumbnail.util.MediaType.APPLICATION_OASIS_OPENDOCUMENT_PRESENTATION;
import static no.sikt.nva.thumbnail.util.MediaType.APPLICATION_OASIS_OPENDOCUMENT_PRESENTATION_TEMPLATE;
import static no.sikt.nva.thumbnail.util.MediaType.APPLICATION_OASIS_OPENDOCUMENT_SPREADSHEET;
import static no.sikt.nva.thumbnail.util.MediaType.APPLICATION_OASIS_OPENDOCUMENT_SPREADSHEET_TEMPLATE;
import static no.sikt.nva.thumbnail.util.MediaType.APPLICATION_OASIS_OPENDOCUMENT_TEXT;
import static no.sikt.nva.thumbnail.util.MediaType.APPLICATION_OASIS_OPENDOCUMENT_TEXT_MASTER;
import static no.sikt.nva.thumbnail.util.MediaType.APPLICATION_OASIS_OPENDOCUMENT_TEXT_TEMPLATE;
import static no.sikt.nva.thumbnail.util.MediaType.APPLICATION_OASIS_OPENDOCUMENT_TEXT_WEB;
import static no.sikt.nva.thumbnail.util.MediaType.APPLICATION_STARDIVISION_CALC;
import static no.sikt.nva.thumbnail.util.MediaType.APPLICATION_STARDIVISION_DRAW;
import static no.sikt.nva.thumbnail.util.MediaType.APPLICATION_STARDIVISION_IMPRESS;
import static no.sikt.nva.thumbnail.util.MediaType.APPLICATION_STARDIVISION_MATH;
import static no.sikt.nva.thumbnail.util.MediaType.APPLICATION_SUN_XML_CALC;
import static no.sikt.nva.thumbnail.util.MediaType.APPLICATION_SUN_XML_CALC_TEMPLATE;
import static no.sikt.nva.thumbnail.util.MediaType.APPLICATION_SUN_XML_DRAW;
import static no.sikt.nva.thumbnail.util.MediaType.APPLICATION_SUN_XML_DRAW_TEMPLATE;
import static no.sikt.nva.thumbnail.util.MediaType.APPLICATION_SUN_XML_IMPRESS;
import static no.sikt.nva.thumbnail.util.MediaType.APPLICATION_SUN_XML_IMPRESS_TEMPLATE;
import static no.sikt.nva.thumbnail.util.MediaType.APPLICATION_SUN_XML_MATH;
import static no.sikt.nva.thumbnail.util.MediaType.APPLICATION_SUN_XML_WRITER;
import static no.sikt.nva.thumbnail.util.MediaType.APPLICATION_SUN_XML_WRITER_GLOBAL;
import static no.sikt.nva.thumbnail.util.MediaType.APPLICATION_SUN_XML_WRITER_TEMPLATE;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import no.sikt.nva.thumbnail.AbstractThumbnailer;
import no.sikt.nva.thumbnail.ThumbnailerException;
import no.sikt.nva.thumbnail.util.ImageResizer;

public class OpenOfficeThumbnailer extends AbstractThumbnailer {

    @Override
    public void generateThumbnail(File input, File output) throws IOException, ThumbnailerException {
        try (ZipFile zipFile = new ZipFile(input)) {
            final ZipEntry entry = zipFile.getEntry("Thumbnails/thumbnail.png");
            if (entry == null) {
                throw new ThumbnailerException(
                    "Zip file does not contain 'Thumbnails/thumbnail.png' . Is this really an OpenOffice-File?");
            }
            try (InputStream in = zipFile.getInputStream(entry)) {
                final ImageResizer resizer = new ImageResizer(thumbWidth, thumbHeight, in);
                resizer.writeThumbnailToFile(output);
            }
        } catch (ZipException e) {
            throw new ThumbnailerException("This is not a zipped file. Is this really an OpenOffice-File?", e);
        }
    }

    @Override
    public List<String> getAcceptedMimeTypes() {
        return List.of(
            APPLICATION_SUN_XML_WRITER.getValue(),
            APPLICATION_SUN_XML_WRITER_TEMPLATE.getValue(),
            APPLICATION_SUN_XML_WRITER_GLOBAL.getValue(),
            APPLICATION_SUN_XML_CALC.getValue(),
            APPLICATION_SUN_XML_CALC_TEMPLATE.getValue(),
            APPLICATION_STARDIVISION_CALC.getValue(),
            APPLICATION_SUN_XML_IMPRESS.getValue(),
            APPLICATION_SUN_XML_IMPRESS_TEMPLATE.getValue(),
            APPLICATION_STARDIVISION_IMPRESS.getValue(),
            APPLICATION_SUN_XML_DRAW.getValue(),
            APPLICATION_SUN_XML_DRAW_TEMPLATE.getValue(),
            APPLICATION_STARDIVISION_DRAW.getValue(),
            APPLICATION_SUN_XML_MATH.getValue(),
            APPLICATION_STARDIVISION_MATH.getValue(),
            APPLICATION_OASIS_OPENDOCUMENT_TEXT.getValue(),
            APPLICATION_OASIS_OPENDOCUMENT_TEXT_TEMPLATE.getValue(),
            APPLICATION_OASIS_OPENDOCUMENT_TEXT_WEB.getValue(),
            APPLICATION_OASIS_OPENDOCUMENT_TEXT_MASTER.getValue(),
            APPLICATION_OASIS_OPENDOCUMENT_GRAPHICS.getValue(),
            APPLICATION_OASIS_OPENDOCUMENT_GRAPHICS_TEMPLATE.getValue(),
            APPLICATION_OASIS_OPENDOCUMENT_PRESENTATION.getValue(),
            APPLICATION_OASIS_OPENDOCUMENT_PRESENTATION_TEMPLATE.getValue(),
            APPLICATION_OASIS_OPENDOCUMENT_SPREADSHEET.getValue(),
            APPLICATION_OASIS_OPENDOCUMENT_SPREADSHEET_TEMPLATE.getValue(),
            APPLICATION_OASIS_OPENDOCUMENT_SPREADSHEET_TEMPLATE.getValue(),
            APPLICATION_OASIS_OPENDOCUMENT_FORMULA.getValue(),
            APPLICATION_OASIS_OPENDOCUMENT_DATABASE.getValue(),
            APPLICATION_OASIS_OPENDOCUMENT_IMAGE.getValue()
        );
    }
}
