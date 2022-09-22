package no.sikt.nva.thumbnail;

import static no.sikt.nva.handler.ThumbnailRequestHandler.COULD_NOT_CREATE_THUMBNAIL_LOG_MESSAGE;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.RequestParametersEntity;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.ResponseElementsEntity;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3BucketEntity;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3Entity;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3EventNotificationRecord;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3ObjectEntity;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.UserIdentityEntity;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import no.sikt.nva.handler.ThumbnailRequestHandler;
import no.sikt.nva.testutils.FakeS3ClientThrowingExceptionOnGetObject;
import no.sikt.nva.testutils.FakeS3ClientThrowingExceptionOnPutObject;
import no.sikt.nva.testutils.FakeS3ClientWithPutObjectSupport;
import no.sikt.nva.testutils.thumbnailer.FakeFFmpeg;
import no.sikt.nva.testutils.thumbnailer.FakeFFprobe;
import no.sikt.nva.thumbnail.thumbnailer.ThumbnailerInitializer;
import no.sikt.nva.thumbnail.util.MediaType;
import no.unit.nva.s3.S3Driver;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.core.paths.UnixPath;
import nva.commons.core.paths.UriWrapper;
import nva.commons.logutils.LogUtils;
import nva.commons.logutils.TestAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.regions.Region;

class ThumbnailRequestHandlerTest {

    public static final long SOME_FILE_SIZE = 100L;
    public static final Context CONTEXT = mock(Context.class);
    public static final RequestParametersEntity EMPTY_REQUEST_PARAMETERS = null;
    public static final ResponseElementsEntity EMPTY_RESPONSE_ELEMENTS = null;
    public static final UserIdentityEntity EMPTY_USER_IDENTITY = null;
    public static final String IMAGES_PATH = "images";
    public static final String PNG_MIME_TYPE = "image/png";
    public static final String TINY_IMAGE = "tiny.png";
    public static final String DOCUMENTS_PATH = "documents";
    public static final String OPEN_OFFICE_GRAPHICS_FILE = "open-office-graphics.odg";
    public static final String OPEN_OFFICE_PRESENTATION_FILE = "open-office-presentation.odp";
    public static final String OPEN_OFFICE_SPREADSHEET_FILE = "open-office-spreadsheet.ods";
    public static final String OPEN_OFFICE_TEXT_FILE = "open-office-text.odt";
    public static final String OPEN_OFFICE_TEXT_NOT_ZIP_FILE = "open-office-text-not-zip.odt";
    public static final String OPEN_OFFICE_TEXT_WITHOUT_THUMBNAIL_FILE = "open-office-text-without-thumbnail.odt";
    public static final String WORD_DOC_FILE = "word-document.doc";
    public static final String WORD_DOCX_FILE = "word-document.docx";
    public static final String EXCEL_XLS_FILE = "excel-document.xls";
    public static final String EXCEL_XLSX_FILE = "excel-document.xlsx";
    public static final String POWERPOINT_PPT_FILE = "powerpoint-presentation.pptx";
    public static final String INPUT_BUCKET_NAME = "input-bucket-name";
    private static final String UNSUPPORTED_FILES_PATH = "unsupported_files";
    private static final String BINARY_FILE = "octet-stream-not-supported.bin";
    private static final String APPLICATION_OCTET_STREAM_MIME_TYPE = "application/octet-stream";
    private static final String JPEG_FILE = "pug.jpeg";
    private static final String JPEG_MIME_TYPE = "image/jpeg";
    private static final String OOG_MIME_TYPE = "application/vnd.oasis.opendocument.graphics";
    private static final String OOP_MIME_TYPE = "application/vnd.oasis.opendocument.presentation";
    private static final String OOS_MIME_TYPE = "application/vnd.oasis.opendocument.spreadsheet";
    private static final String OOT_MIME_TYPE = "application/vnd.oasis.opendocument.text";
    private static final String ALREADY_HAVE_SIZE_AS_THUMBNAIL_OUTPUT = "correct-size.png";
    private static final String THUMBNAIL_URI_TEMPLATE_STRING = "https://%s.s3.%s.amazonaws.com/%s";
    private static final String THUMBNAIL_BUCKET_NAME = "dlr-nva-thumbnails";
    private static final String QUICK_TIME_MOVIE_FILENAME = "quickTimeWindow.mov";
    private static final String QUICK_TIME_MIME_TYPE = "video/quicktime";
    private static final String MOVIE_PATH = "videos";
    private static final String PDF_FILENAME = "wireframe.pdf";
    private static final String PDF_MIMETYPE = "application/pdf";
    private TestAppender appender;

    private ThumbnailerInitializer thumbnailerInitializer;

    @BeforeEach
    public void init() throws IOException {
        this.appender = LogUtils.getTestingAppenderForRootLogger();
        var temporaryFilename = String.format("/tmp/%s.png", UUID.randomUUID());
        this.thumbnailerInitializer = new ThumbnailerInitializer.Builder()
                                          .withPartiallyProcessedFilename(temporaryFilename)
                                          .withFFmpeg(new FakeFFmpeg(temporaryFilename))
                                          .withFFprobe(new FakeFFprobe())
                                          .build();
    }

    @Test
    public void shouldBeAbleToCreateThumbnailFromPdf() throws IOException {
        var shouldHaveContentDisposition = false;
        assertThumbnailGenerated(DOCUMENTS_PATH, PDF_FILENAME,
                                                     PDF_MIMETYPE,
                                                     shouldHaveContentDisposition);
    }

    @Test
    public void shouldBeAbleToConvertQuickTimeMovie() throws IOException {
        var shouldHaveContentDisposition = false;
        assertThumbnailGenerated(MOVIE_PATH, QUICK_TIME_MOVIE_FILENAME,
                                                     QUICK_TIME_MIME_TYPE,
                                                     shouldHaveContentDisposition);
    }

    @Test
    void shouldThrowExceptionWhenCannotUseS3Client() throws IOException {
        var expectedMessage = randomString();
        var s3Client = new FakeS3ClientThrowingExceptionOnGetObject(expectedMessage);
        var s3Event = createNewFileUploadEvent(UnixPath.of(IMAGES_PATH, JPEG_FILE), s3Client);
        var handler = new ThumbnailRequestHandler(s3Client, thumbnailerInitializer);
        assertThrows(RuntimeException.class, () -> handler.handleRequest(s3Event, CONTEXT));
        assertThat(appender.getMessages(), containsString(expectedMessage));
    }

    @Test
    void shouldThrowExceptionWhenMimeTypeIsNotSupported() throws IOException {
        var s3Client = new FakeS3ClientWithPutObjectSupport(BINARY_FILE, UNSUPPORTED_FILES_PATH,
                                                            APPLICATION_OCTET_STREAM_MIME_TYPE);
        var s3Event = createNewFileUploadEvent(UnixPath.of(UNSUPPORTED_FILES_PATH, BINARY_FILE),
                                               s3Client);
        var handler = new ThumbnailRequestHandler(s3Client, thumbnailerInitializer);
        assertThrows(RuntimeException.class, () -> handler.handleRequest(s3Event, CONTEXT));
        assertThat(appender.getMessages(), containsString(COULD_NOT_CREATE_THUMBNAIL_LOG_MESSAGE));
    }

    @Test
    void shouldThrowExceptionWhenS3ClientWontPutObject() throws IOException {
        var expectedMessage = randomString();
        var s3Client = new FakeS3ClientThrowingExceptionOnPutObject(expectedMessage, JPEG_FILE, JPEG_MIME_TYPE,
                                                                    IMAGES_PATH);
        var s3Event = createNewFileUploadEvent(UnixPath.of(IMAGES_PATH, JPEG_FILE), s3Client);
        var handler = new ThumbnailRequestHandler(s3Client, thumbnailerInitializer);
        assertThrows(RuntimeException.class, () -> handler.handleRequest(s3Event, CONTEXT));
        assertThat(appender.getMessages(), containsString(expectedMessage));
    }

    @Test
    void shouldUploadNativeImageToS3WhenNoExceptionOccurs() throws IOException {
        boolean shouldHaveContentDisposition = true;
        assertThumbnailGenerated(IMAGES_PATH, JPEG_FILE, JPEG_MIME_TYPE,
                                 shouldHaveContentDisposition);
    }

    @Test
    void shouldHandleIdenticalSizedThumbnails() throws IOException {
        boolean shouldHaveContentDisposition = true;
        assertThumbnailGenerated(IMAGES_PATH, ALREADY_HAVE_SIZE_AS_THUMBNAIL_OUTPUT, PNG_MIME_TYPE,
                                 shouldHaveContentDisposition);
    }

    @Test
    void shouldHandleTinySizedThumbnails() throws IOException {
        boolean shouldHaveContentDisposition = true;
        assertThumbnailGenerated(IMAGES_PATH, TINY_IMAGE, PNG_MIME_TYPE,
                                 shouldHaveContentDisposition);
    }

    @Test
    void shouldHandleFilesWithoutContentDisposition() throws IOException {
        boolean shouldHaveContentDisposition = false;
        assertThumbnailGenerated(IMAGES_PATH, TINY_IMAGE, PNG_MIME_TYPE,
                                 shouldHaveContentDisposition);
    }

    /*
        OpenOffice documents:
     */
    @Test
    void shouldThrowExceptionWhenOpenOfficeDocumentIsNotZip() throws IOException {
        var s3Client = new FakeS3ClientWithPutObjectSupport(OPEN_OFFICE_TEXT_NOT_ZIP_FILE,
                                                            UNSUPPORTED_FILES_PATH,
                                                            OOT_MIME_TYPE);
        var s3Event = createNewFileUploadEvent(
            UnixPath.of(UNSUPPORTED_FILES_PATH, OPEN_OFFICE_TEXT_NOT_ZIP_FILE),
            s3Client);
        var handler = new ThumbnailRequestHandler(s3Client, thumbnailerInitializer);
        assertThrows(RuntimeException.class, () -> handler.handleRequest(s3Event, CONTEXT));

        assertThat(appender.getMessages(), containsString(COULD_NOT_CREATE_THUMBNAIL_LOG_MESSAGE));
    }

    @Test
    void shouldThrowExceptionWhenOpenOfficeDocumentDoesNotContainThumbnail() throws IOException {
        var s3Client = new FakeS3ClientWithPutObjectSupport(OPEN_OFFICE_TEXT_WITHOUT_THUMBNAIL_FILE,
                                                            UNSUPPORTED_FILES_PATH,
                                                            OOT_MIME_TYPE);
        var s3Event = createNewFileUploadEvent(
            UnixPath.of(UNSUPPORTED_FILES_PATH, OPEN_OFFICE_TEXT_WITHOUT_THUMBNAIL_FILE),
            s3Client);
        var handler = new ThumbnailRequestHandler(s3Client, thumbnailerInitializer);
        assertThrows(RuntimeException.class, () -> handler.handleRequest(s3Event, CONTEXT));

        assertThat(appender.getMessages(), containsString(COULD_NOT_CREATE_THUMBNAIL_LOG_MESSAGE));
    }

    @Test
    void shouldUploadThumbnailToS3ForOpenOfficeGraphicsDocument() throws IOException {
        boolean shouldHaveContentDisposition = false;
        assertThumbnailGenerated(DOCUMENTS_PATH, OPEN_OFFICE_GRAPHICS_FILE, OOG_MIME_TYPE,
                                 shouldHaveContentDisposition);
    }

    @Test
    void shouldUploadThumbnailToS3ForOpenOfficePresentationDocument() throws IOException {
        boolean shouldHaveContentDisposition = false;
        assertThumbnailGenerated(DOCUMENTS_PATH, OPEN_OFFICE_PRESENTATION_FILE, OOP_MIME_TYPE,
                                 shouldHaveContentDisposition);
    }

    @Test
    void shouldUploadThumbnailToS3ForOpenOfficeSpreadsheetDocument() throws IOException {
        boolean shouldHaveContentDisposition = false;
        assertThumbnailGenerated(DOCUMENTS_PATH, OPEN_OFFICE_SPREADSHEET_FILE, OOS_MIME_TYPE,
                                 shouldHaveContentDisposition);
    }

    @Test
    void shouldUploadThumbnailToS3ForOpenOfficeTextDocument() throws IOException {
        boolean shouldHaveContentDisposition = false;
        assertThumbnailGenerated(DOCUMENTS_PATH, OPEN_OFFICE_TEXT_FILE, OOT_MIME_TYPE,
                                 shouldHaveContentDisposition);
    }

    @Test
    void shouldUploadThumbnailToS3ForWordDoc() throws IOException {
        boolean shouldHaveContentDisposition = false;
        assertThumbnailGenerated(DOCUMENTS_PATH, WORD_DOC_FILE,
                                 MediaType.APPLICATION_MS_WORD.getValue(),
                                 shouldHaveContentDisposition);
    }

    @Test
    void shouldUploadThumbnailToS3ForWordDocx() throws IOException {
        boolean shouldHaveContentDisposition = false;
        assertThumbnailGenerated(DOCUMENTS_PATH, WORD_DOCX_FILE,
                                 MediaType.APPLICATION_OPEN_XML_OFFICE_WORD_DOC.getValue(),
                                 shouldHaveContentDisposition);
    }

    @Test
    void shouldUploadThumbnailToS3ForWordDocxTruncatedMimetype() throws IOException {
        boolean shouldHaveContentDisposition = false;
        assertThumbnailGenerated(DOCUMENTS_PATH, WORD_DOCX_FILE,
                                 MediaType.APPLICATION_OPEN_XML_OFFICE_WORD.getValue(),
                                 shouldHaveContentDisposition);
    }

    @Test
    void shouldUploadThumbnailToS3ForExcelXls() throws IOException {
        boolean shouldHaveContentDisposition = false;
        assertThumbnailGenerated(DOCUMENTS_PATH, EXCEL_XLS_FILE,
                                 MediaType.APPLICATION_MS_EXCEL.getValue(),
                                 shouldHaveContentDisposition);
    }

    @Test
    void shouldUploadThumbnailToS3ForExcelXlsx() throws IOException {
        boolean shouldHaveContentDisposition = false;
        assertThumbnailGenerated(DOCUMENTS_PATH, EXCEL_XLSX_FILE,
                                 MediaType.APPLICATION_OPEN_XML_OFFICE_SPREADSHEET_SHEET.getValue(),
                                 shouldHaveContentDisposition);
    }

    @Test
    void shouldUploadThumbnailToS3ForExcelXlsxTruncatedMimeType() throws IOException {
        boolean shouldHaveContentDisposition = false;
        assertThumbnailGenerated(DOCUMENTS_PATH, EXCEL_XLSX_FILE,
                                 MediaType.APPLICATION_OPEN_XML_OFFICE_SPREADSHEET.getValue(),
                                 shouldHaveContentDisposition);
    }

    @Test
    void shouldUploadThumbnailToS3ForPowerpointPptx() throws IOException {
        boolean shouldHaveContentDisposition = false;
        assertThumbnailGenerated(DOCUMENTS_PATH, POWERPOINT_PPT_FILE,
                                 MediaType.APPLICATION_OPEN_XML_OFFICE_PRESENTATION_PRESENTATION.getValue(),
                                 shouldHaveContentDisposition);
    }

    @Test
    void shouldUploadThumbnailToS3ForPowerpointPptxTruncatedMimeType() throws IOException {
        boolean shouldHaveContentDisposition = false;
        assertThumbnailGenerated(DOCUMENTS_PATH, POWERPOINT_PPT_FILE,
                                 MediaType.APPLICATION_OPEN_XML_OFFICE_PRESENTATION.getValue(),
                                 shouldHaveContentDisposition);
    }

    private void assertThumbnailGenerated(final String folderName,
                                          final String fileName,
                                          final String mimetype,
                                          final boolean shouldHaveContentDisposition)
        throws IOException {

        var s3Path = randomS3Path();
        var expectedThumbnailURL = craftExpectedURL(s3Path);
        var s3Client = new FakeS3ClientWithPutObjectSupport(fileName, folderName, mimetype,
                                                            shouldHaveContentDisposition);
        var s3Event = createNewFileUploadEvent(UnixPath.of(folderName, fileName), s3Client, s3Path);
        var handler = new ThumbnailRequestHandler(s3Client, thumbnailerInitializer);
        var thumbnailUrl = handler.handleRequest(s3Event, CONTEXT);

        assertThat(thumbnailUrl, is(equalTo(expectedThumbnailURL)));
    }

    private URL craftExpectedURL(UnixPath s3Path) throws MalformedURLException {
        return new URL(String.format(THUMBNAIL_URI_TEMPLATE_STRING,
                                     THUMBNAIL_BUCKET_NAME,
                                     Region.EU_WEST_1,
                                     s3Path));
    }

    private S3Event createNewFileUploadEvent(UnixPath path,
                                             FakeS3ClientWithPutObjectSupport s3Client) throws IOException {
        var s3Driver = new S3Driver(s3Client, INPUT_BUCKET_NAME);
        var insertStream = IoUtils.inputStreamFromResources(path.toString());
        var uri = s3Driver.insertFile(randomS3Path(), insertStream);
        return createS3Event(uri);
    }

    private S3Event createNewFileUploadEvent(UnixPath path,
                                             FakeS3ClientWithPutObjectSupport s3Client,
                                             UnixPath s3Path) throws IOException {
        var s3Driver = new S3Driver(s3Client, INPUT_BUCKET_NAME);
        var insertStream = IoUtils.inputStreamFromResources(path.toString());
        var uri = s3Driver.insertFile(s3Path, insertStream);
        return createS3Event(uri);
    }

    private S3Event createS3Event(URI uri) {
        return createS3Event(UriWrapper.fromUri(uri).toS3bucketPath().toString());
    }

    private S3Event createS3Event(String expectedObjectKey) {
        var eventNotification = new S3EventNotificationRecord(randomString(),
                                                              randomString(),
                                                              randomString(),
                                                              randomDate(),
                                                              randomString(),
                                                              EMPTY_REQUEST_PARAMETERS,
                                                              EMPTY_RESPONSE_ELEMENTS,
                                                              createS3Entity(expectedObjectKey),
                                                              EMPTY_USER_IDENTITY);
        return new S3Event(List.of(eventNotification));
    }

    private S3Entity createS3Entity(String expectedObjectKey) {
        var bucket = new S3BucketEntity(randomString(), EMPTY_USER_IDENTITY, randomString());
        var object = new S3ObjectEntity(expectedObjectKey, SOME_FILE_SIZE, randomString(), randomString(),
                                        randomString());
        var schemaVersion = randomString();
        return new S3Entity(randomString(), bucket, object, schemaVersion);
    }

    private String randomDate() {
        return Instant.now().toString();
    }

    private UnixPath randomS3Path() {
        return UnixPath.of(randomString());
    }
}