package no.sikt.nva.thumbnail;

import static no.sikt.nva.handler.ThumbnailRequestHandler.COULD_NOT_CREATE_THUMBNAIL_LOG_MESSAGE;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
import java.net.URI;
import java.time.Instant;
import java.util.List;
import no.sikt.nva.handler.ThumbnailRequestHandler;
import no.unit.nva.s3.S3Driver;
import no.unit.nva.stubs.FakeS3Client;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.core.paths.UnixPath;
import nva.commons.core.paths.UriWrapper;
import nva.commons.logutils.LogUtils;
import nva.commons.logutils.TestAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

class ThumbnailRequestHandlerTest {

    public static final long SOME_FILE_SIZE = 100L;
    public static final Context CONTEXT = mock(Context.class);
    public static final RequestParametersEntity EMPTY_REQUEST_PARAMETERS = null;
    public static final ResponseElementsEntity EMPTY_RESPONSE_ELEMENTS = null;
    public static final UserIdentityEntity EMPTY_USER_IDENTITY = null;
    private static final String CONTENT_DISPOSITION = "filename=\"%s\"";
    private static final String UNSUPPORTED_FILES_PATH = "unsupported_files";
    private static final String ZIP_FILE = "zip_is_not_supported.zip";
    private static final String ZIP_MIME_TYPE = "application/zip";
    private static final String IMAGES_PATH = "images";
    private static final String JPEG_FILE = "pug.jpeg";
    private static final String JPEG_MIME_TYPE = "image/jpeg";
    private static final String ALREADY_HAVE_SIZE_AS_THUMBNAIL_OUTPUT = "correct-size.png";
    private static final String PNG_MIME_TYPE = "image/png";
    private static final String TINY_IMAGE = "tiny.png";
    private ThumbnailRequestHandler handler;
    private S3Client s3Client;
    private S3Driver s3Driver;
    private TestAppender appender;

    @BeforeEach
    public void init() {
        this.s3Client = mock(S3Client.class);
        this.s3Driver = new S3Driver(s3Client, "ignoredValue");
        this.handler = new ThumbnailRequestHandler(s3Client);
        this.appender = LogUtils.getTestingAppenderForRootLogger();
    }

    @Test
    void shouldThrowExceptionWhenCannotUseS3Client() throws IOException {
        var s3Event = createNewFileUploadEvent(IMAGES_PATH + "/" + JPEG_FILE, JPEG_MIME_TYPE, JPEG_FILE);
        var expectedMessage = randomString();
        s3Client = new FakeS3ClientThrowingExceptionOnGetObject(expectedMessage);
        handler = new ThumbnailRequestHandler(s3Client);
        assertThrows(RuntimeException.class, () -> handler.handleRequest(s3Event, CONTEXT));
        assertThat(appender.getMessages(), containsString(expectedMessage));
    }

    @Test
    void shouldThrowExceptionWhenMimeTypeIsNotSupported() throws IOException {
        var s3Event = createNewFileUploadEvent(UNSUPPORTED_FILES_PATH + "/" + ZIP_FILE, ZIP_MIME_TYPE, ZIP_FILE);
        assertThrows(RuntimeException.class, () -> handler.handleRequest(s3Event, CONTEXT));
        assertThat(appender.getMessages(), containsString(COULD_NOT_CREATE_THUMBNAIL_LOG_MESSAGE));
    }

    @Test
    void shouldThrowExceptionWhenS3ClientWontPutObject() throws IOException {
        var s3Event = createNewFileUploadEvent(IMAGES_PATH + "/" + JPEG_FILE, JPEG_MIME_TYPE, JPEG_FILE);
        var expectedMessage = randomString();
        s3PutObjectThrowsException(expectedMessage);
        assertThrows(RuntimeException.class, () -> handler.handleRequest(s3Event, CONTEXT));
        assertThat(appender.getMessages(), containsString(expectedMessage));
    }

    @Test
    void shouldUploadNativeImageToS3WhenNoExceptionOccurs() throws IOException {
        var s3Event = createNewFileUploadEvent(IMAGES_PATH + "/" + JPEG_FILE, JPEG_MIME_TYPE, JPEG_FILE);
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(
            PutObjectResponse.builder().build());
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        assertDoesNotThrow(() -> handler.handleRequest(s3Event, CONTEXT));
    }

    @Test
    void shouldHandleIdenticalSizedThumbnails() throws IOException {
        var s3Event = createNewFileUploadEvent(IMAGES_PATH + "/" + ALREADY_HAVE_SIZE_AS_THUMBNAIL_OUTPUT, PNG_MIME_TYPE,
                                               ALREADY_HAVE_SIZE_AS_THUMBNAIL_OUTPUT);
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(
            PutObjectResponse.builder().build());
        assertDoesNotThrow(() -> handler.handleRequest(s3Event, CONTEXT));
        verify(s3Client, times(2)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }


    @Test
    void shouldHandleTinySizedThumbnails() throws IOException {
        var s3Event = createNewFileUploadEvent(IMAGES_PATH + "/" + TINY_IMAGE, PNG_MIME_TYPE,
                                               TINY_IMAGE);
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(
            PutObjectResponse.builder().build());
        assertDoesNotThrow(() -> handler.handleRequest(s3Event, CONTEXT));
        verify(s3Client, times(2)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }



    private void s3PutObjectThrowsException(String expectedMessage) {
        when(s3Client.putObject(
            any(PutObjectRequest.class),
            any(RequestBody.class)))
            .thenThrow(new RuntimeException(expectedMessage));
    }

    private S3Event createNewFileUploadEvent(String path,
                                             String mimeType,
                                             String fileName) throws IOException {
        var contentDisposition = String.format(CONTENT_DISPOSITION, fileName);
        var insertStream = IoUtils.inputStreamFromResources(path);
        var getObjectStream = IoUtils.inputStreamFromResources(path);
        var uri = s3Driver.insertFile(randomS3Path(), insertStream);
        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(
            new ResponseInputStream<>(
                GetObjectResponse.builder().contentDisposition(contentDisposition).contentType(mimeType).build(),
                AbortableInputStream.create(getObjectStream))
        );
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

    private static class FakeS3ClientThrowingExceptionOnGetObject extends FakeS3Client {

        private final String expectedErrorMessage;

        public FakeS3ClientThrowingExceptionOnGetObject(String expectedErrorMessage) {
            super();
            this.expectedErrorMessage = expectedErrorMessage;
        }

        @Override
        public <ReturnT> ReturnT getObject(GetObjectRequest getObjectRequest,
                                           ResponseTransformer<GetObjectResponse, ReturnT> responseTransformer) {
            throw new RuntimeException(expectedErrorMessage);
        }
    }
}