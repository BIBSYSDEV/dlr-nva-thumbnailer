package no.sikt.nva.testutils;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

public class FakeS3ClientThrowingExceptionOnGetObject extends FakeS3ClientWithPutObjectSupport {

    public static final String PNG_MIME_TYPE = "image/png";
    public static final String TINY_IMAGE = "tiny.png";
    public static final String IMAGES_PATH = "images";

    private final String expectedErrorMessage;

    public FakeS3ClientThrowingExceptionOnGetObject(String expectedErrorMessage) {
        super(TINY_IMAGE, IMAGES_PATH, PNG_MIME_TYPE);
        this.expectedErrorMessage = expectedErrorMessage;
    }

    @Override
    public ResponseInputStream getObject(GetObjectRequest getObjectRequest) {
        throw new RuntimeException(expectedErrorMessage);
    }
}
