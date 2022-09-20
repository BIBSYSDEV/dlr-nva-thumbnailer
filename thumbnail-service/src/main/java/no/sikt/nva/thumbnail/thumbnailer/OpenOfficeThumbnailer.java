package no.sikt.nva.thumbnail.thumbnailer;

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
            "application/vnd.sun.xml.writer",
            "application/vnd.sun.xml.writer.template",
            "application/vnd.sun.xml.writer.global",
            "application/vnd.sun.xml.calc",
            "application/vnd.sun.xml.calc.template",
            "application/vnd.stardivision.calc",
            "application/vnd.sun.xml.impress",
            "application/vnd.sun.xml.impress.template ",
            "application/vnd.stardivision.impress sdd",
            "application/vnd.sun.xml.draw",
            "application/vnd.sun.xml.draw.template",
            "application/vnd.stardivision.draw",
            "application/vnd.sun.xml.math",
            "application/vnd.stardivision.math",
            "application/vnd.oasis.opendocument.text",
            "application/vnd.oasis.opendocument.text-template",
            "application/vnd.oasis.opendocument.text-web",
            "application/vnd.oasis.opendocument.text-master",
            "application/vnd.oasis.opendocument.graphics",
            "application/vnd.oasis.opendocument.graphics-template",
            "application/vnd.oasis.opendocument.presentation",
            "application/vnd.oasis.opendocument.presentation-template",
            "application/vnd.oasis.opendocument.spreadsheet",
            "application/vnd.oasis.opendocument.spreadsheet-template",
            "application/vnd.oasis.opendocument.chart",
            "application/vnd.oasis.opendocument.formula",
            "application/vnd.oasis.opendocument.database",
            "application/vnd.oasis.opendocument.image",

            "application/zip" /* Could be an OpenOffice file! */
        );
    }
}
