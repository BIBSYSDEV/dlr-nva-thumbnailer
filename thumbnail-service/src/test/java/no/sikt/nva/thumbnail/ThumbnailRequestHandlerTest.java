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
import no.sikt.nva.handler.ThumbnailRequestHandler;
import no.sikt.nva.testutils.FakeS3ClientThrowingExceptionOnGetObject;
import no.sikt.nva.testutils.FakeS3ClientThrowingExceptionOnPutObject;
import no.sikt.nva.testutils.FakeS3ClientWithPutObjectSupport;
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
    public static final String INPUT_BUCKET_NAME = "input-bucket-name";
    private static final String UNSUPPORTED_FILES_PATH = "unsupported_files";
    private static final String ZIP_FILE = "zip_is_not_supported.zip";
    private static final String ZIP_MIME_TYPE = "application/zip";
    private static final String JPEG_FILE = "pug.jpeg";
    private static final String JPEG_MIME_TYPE = "image/jpeg";
    private static final String ALREADY_HAVE_SIZE_AS_THUMBNAIL_OUTPUT = "correct-size.png";
    private static final String THUMBNAIL_URI_TEMPLATE_STRING = "https://%s.s3.%s.amazonaws.com/%s";
    private static final String THUMBNAIL_BUCKET_NAME = "dlr-nva-thumbnails";
    private static final String QUICK_TIME_MOVIE_FILENAME = "quickTimeWindow.mov";
    private static final String QUICK_TIME_MIME_TYPE = "video/quicktime";
    private static final String MOVIE_PATH = "videos";
    private TestAppender appender;



    @BeforeEach
    public void init() {
        this.appender = LogUtils.getTestingAppenderForRootLogger();
    }

    @Test
    public void shouldBeAbleToConvertQuickTimeMovie() throws IOException {
        var s3Path = randomS3Path();
        var expectedThumbnailURL = craftExpectedURL(s3Path);
        var s3Client = new FakeS3ClientWithPutObjectSupport(QUICK_TIME_MOVIE_FILENAME, MOVIE_PATH, QUICK_TIME_MIME_TYPE);
        var s3Event = createNewFileUploadEvent(MOVIE_PATH + "/" + QUICK_TIME_MOVIE_FILENAME,
                                               s3Client, s3Path);
        var handler = new ThumbnailRequestHandler(s3Client);
        var thumbnailUrl = handler.handleRequest(s3Event, CONTEXT);
        assertThat(thumbnailUrl, is(equalTo(expectedThumbnailURL)));
    }

    @Test
    void shouldThrowExceptionWhenCannotUseS3Client() throws IOException {
        var expectedMessage = randomString();
        var s3Client = new FakeS3ClientThrowingExceptionOnGetObject(expectedMessage);
        var s3Event = createNewFileUploadEvent(IMAGES_PATH + "/" + JPEG_FILE, s3Client);
        var handler = new ThumbnailRequestHandler(s3Client);
        assertThrows(RuntimeException.class, () -> handler.handleRequest(s3Event, CONTEXT));
        assertThat(appender.getMessages(), containsString(expectedMessage));
    }

    @Test
    void shouldThrowExceptionWhenMimeTypeIsNotSupported() throws IOException {
        var s3Client = new FakeS3ClientWithPutObjectSupport(ZIP_FILE, UNSUPPORTED_FILES_PATH, ZIP_MIME_TYPE);
        var s3Event = createNewFileUploadEvent(UNSUPPORTED_FILES_PATH + "/" + ZIP_FILE,
                                               s3Client);
        var handler = new ThumbnailRequestHandler(s3Client);
        assertThrows(RuntimeException.class, () -> handler.handleRequest(s3Event, CONTEXT));
        assertThat(appender.getMessages(), containsString(COULD_NOT_CREATE_THUMBNAIL_LOG_MESSAGE));
    }

    @Test
    void shouldThrowExceptionWhenS3ClientWontPutObject() throws IOException {
        var expectedMessage = randomString();
        var s3Client = new FakeS3ClientThrowingExceptionOnPutObject(expectedMessage, JPEG_FILE, JPEG_MIME_TYPE,
                                                                    IMAGES_PATH);
        var s3Event = createNewFileUploadEvent(IMAGES_PATH + "/" + JPEG_FILE, s3Client);
        var handler = new ThumbnailRequestHandler(s3Client);
        assertThrows(RuntimeException.class, () -> handler.handleRequest(s3Event, CONTEXT));
        assertThat(appender.getMessages(), containsString(expectedMessage));
    }

    @Test
    void shouldUploadNativeImageToS3WhenNoExceptionOccurs() throws IOException {
        var s3Path = randomS3Path();
        var expectedThumbnailURL = craftExpectedURL(s3Path);
        var s3Client = new FakeS3ClientWithPutObjectSupport(JPEG_FILE, IMAGES_PATH, JPEG_MIME_TYPE);
        var s3Event = createNewFileUploadEvent(IMAGES_PATH + "/" + JPEG_FILE, s3Client, s3Path);
        var handler = new ThumbnailRequestHandler(s3Client);
        var thumbnailUrl = handler.handleRequest(s3Event, CONTEXT);
        assertThat(thumbnailUrl, is(equalTo(expectedThumbnailURL)));
    }

    @Test
    void shouldHandleIdenticalSizedThumbnails() throws IOException {
        var s3Path = randomS3Path();
        var expectedThumbnailURL = craftExpectedURL(s3Path);
        var s3Client = new FakeS3ClientWithPutObjectSupport(ALREADY_HAVE_SIZE_AS_THUMBNAIL_OUTPUT, IMAGES_PATH,
                                                            PNG_MIME_TYPE);
        var s3Event = createNewFileUploadEvent(IMAGES_PATH + "/" + ALREADY_HAVE_SIZE_AS_THUMBNAIL_OUTPUT,
                                               s3Client, s3Path);
        var handler = new ThumbnailRequestHandler(s3Client);
        var thumbnailUrl = handler.handleRequest(s3Event, CONTEXT);
        assertThat(thumbnailUrl, is(equalTo(expectedThumbnailURL)));
    }

    @Test
    void shouldHandleTinySizedThumbnails() throws IOException {
        var s3Path = randomS3Path();
        var expectedThumbnailURL = craftExpectedURL(s3Path);
        var s3Client = new FakeS3ClientWithPutObjectSupport(TINY_IMAGE, IMAGES_PATH, PNG_MIME_TYPE);
        var s3Event = createNewFileUploadEvent(IMAGES_PATH + "/" + TINY_IMAGE,
                                               s3Client, s3Path);
        var handler = new ThumbnailRequestHandler(s3Client);
        var thumbnailUrl = handler.handleRequest(s3Event, CONTEXT);
        assertThat(thumbnailUrl, is(equalTo(expectedThumbnailURL)));
    }

    private URL craftExpectedURL(UnixPath s3Path) throws MalformedURLException {
        return new URL(String.format(THUMBNAIL_URI_TEMPLATE_STRING,
                                     THUMBNAIL_BUCKET_NAME,
                                     Region.EU_WEST_1,
                                     s3Path));
    }

    private S3Event createNewFileUploadEvent(String path,
                                             FakeS3ClientWithPutObjectSupport s3Client) throws IOException {
        var s3Driver = new S3Driver(s3Client, INPUT_BUCKET_NAME);
        var insertStream = IoUtils.inputStreamFromResources(path);
        var uri = s3Driver.insertFile(randomS3Path(), insertStream);
        return createS3Event(uri);
    }

    private S3Event createNewFileUploadEvent(String path,
                                             FakeS3ClientWithPutObjectSupport s3Client,
                                             UnixPath s3Path) throws IOException {
        var s3Driver = new S3Driver(s3Client, INPUT_BUCKET_NAME);
        var insertStream = IoUtils.inputStreamFromResources(path);
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