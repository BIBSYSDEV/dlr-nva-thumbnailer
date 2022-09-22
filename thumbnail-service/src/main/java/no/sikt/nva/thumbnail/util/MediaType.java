package no.sikt.nva.thumbnail.util;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public enum MediaType {
    APPLICATION_PDF("application/pdf"),
    APPLICATION_MS_WORD("application/msword"),
    APPLICATION_OPEN_XML_OFFICE_WORD("application/vnd.openxmlformats-officedocument.wordprocessingml"),
    APPLICATION_OPEN_XML_OFFICE_WORD_DOC("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    APPLICATION_MS_EXCEL("application/vnd.ms-excel"),
    APPLICATION_OPEN_XML_OFFICE_SPREADSHEET("application/vnd.openxmlformats-officedocument.spreadsheetml"),
    APPLICATION_OPEN_XML_OFFICE_SPREADSHEET_SHEET("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    APPLICATION_OPEN_XML_OFFICE_PRESENTATION(
        "application/vnd.openxmlformats-officedocument.presentationml"),
    APPLICATION_OPEN_XML_OFFICE_PRESENTATION_PRESENTATION(
        "application/vnd.openxmlformats-officedocument.presentationml.presentation"),
    APPLICATION_MS_ADVANCED_SYSTEMS_FORMAT("application/vnd.ms-asf"),
    APPLICATION_FF_MPEG("application/ffmpeg"),
    VIDEO_MS_VIDEO("video/x-msvideo"),
    VIDEO_X_FLV("video/x-flv"),
    VIDEO_WEBM("video/webm"),
    VIDEO_MPEG("video/mpeg"),
    VIDEO_X_M4V("video/x-m4v"),
    VIDEO_MP4("video/mp4"),
    VIDEO_OGG("video/ogg"),
    VIDEO_X_MATROSKA("video/x-matroska"),
    VIDEO_QUICKTIME("video/quicktime"),
    APPLICATION_SUN_XML_WRITER("application/vnd.sun.xml.writer"),
    APPLICATION_SUN_XML_WRITER_TEMPLATE("application/vnd.sun.xml.writer.template"),
    APPLICATION_SUN_XML_WRITER_GLOBAL("application/vnd.sun.xml.writer.global"),
    APPLICATION_SUN_XML_CALC("application/vnd.sun.xml.calc"),
    APPLICATION_SUN_XML_CALC_TEMPLATE("application/vnd.sun.xml.calc.template"),
    APPLICATION_STARDIVISION_CALC("application/vnd.stardivision.calc"),
    APPLICATION_SUN_XML_IMPRESS("application/vnd.sun.xml.impress"),
    APPLICATION_SUN_XML_IMPRESS_TEMPLATE("application/vnd.sun.xml.impress.template"),
    APPLICATION_STARDIVISION_IMPRESS("application/vnd.stardivision.impress sdd"),
    APPLICATION_SUN_XML_DRAW("application/vnd.sun.xml.draw"),
    APPLICATION_SUN_XML_DRAW_TEMPLATE("application/vnd.sun.xml.draw.template"),
    APPLICATION_STARDIVISION_DRAW("application/vnd.stardivision.draw"),
    APPLICATION_SUN_XML_MATH("application/vnd.sun.xml.math"),
    APPLICATION_STARDIVISION_MATH("application/vnd.stardivision.math"),
    APPLICATION_OASIS_OPENDOCUMENT_TEXT("application/vnd.oasis.opendocument.text"),
    APPLICATION_OASIS_OPENDOCUMENT_TEXT_TEMPLATE("application/vnd.oasis.opendocument.text-template"),
    APPLICATION_OASIS_OPENDOCUMENT_TEXT_WEB("application/vnd.oasis.opendocument.text-web"),
    APPLICATION_OASIS_OPENDOCUMENT_TEXT_MASTER("application/vnd.oasis.opendocument.text-master"),
    APPLICATION_OASIS_OPENDOCUMENT_GRAPHICS("application/vnd.oasis.opendocument.graphics"),
    APPLICATION_OASIS_OPENDOCUMENT_GRAPHICS_TEMPLATE("application/vnd.oasis.opendocument.graphics-template"),
    APPLICATION_OASIS_OPENDOCUMENT_PRESENTATION("application/vnd.oasis.opendocument.presentation"),
    APPLICATION_OASIS_OPENDOCUMENT_PRESENTATION_TEMPLATE("application/vnd.oasis.opendocument.presentation-template"),
    APPLICATION_OASIS_OPENDOCUMENT_SPREADSHEET("application/vnd.oasis.opendocument.spreadsheet"),
    APPLICATION_OASIS_OPENDOCUMENT_SPREADSHEET_TEMPLATE("application/vnd.oasis.opendocument.spreadsheet-template"),
    APPLICATION_OASIS_OPENDOCUMENT_CHART("application/vnd.oasis.opendocument.chart"),
    APPLICATION_OASIS_OPENDOCUMENT_FORMULA("application/vnd.oasis.opendocument.formula"),
    APPLICATION_OASIS_OPENDOCUMENT_DATABASE("application/vnd.oasis.opendocument.database"),
    APPLICATION_OASIS_OPENDOCUMENT_IMAGE("application/vnd.oasis.opendocument.image");

    private final String value;

    private static final Map<String, MediaType> MEDIA_TYPES_MAP = new ConcurrentHashMap<>(MediaType.values().length);

    static {
        Arrays.stream(MediaType.values()).forEach(mediaType -> MEDIA_TYPES_MAP.put(mediaType.getValue(), mediaType));
    }

    MediaType(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static MediaType fromValue(final String value) {
        final MediaType mediaType = MEDIA_TYPES_MAP.get(value);
        if (Objects.isNull(mediaType)) {
            throw new IllegalArgumentException(value);
        }
        return mediaType;
    }
}
