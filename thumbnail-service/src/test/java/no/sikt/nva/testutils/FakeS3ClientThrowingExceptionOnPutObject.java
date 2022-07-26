package no.sikt.nva.testutils;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

public class FakeS3ClientThrowingExceptionOnPutObject extends FakeS3ClientWithPutObjectSupport {
    public static final String INPUT_BUCKET_NAME = "input-bucket-name";

    private final String expectedErrorMessage;

    public FakeS3ClientThrowingExceptionOnPutObject(String expectedErrorMessage, String filename, String mimeType,
                                                    String path) {
        super(filename, path, mimeType);
        this.expectedErrorMessage = expectedErrorMessage;
    }

    @Override
    public PutObjectResponse putObject(PutObjectRequest putObjectRequest, RequestBody requestBody)
        throws AwsServiceException, SdkClientException {
        if (putObjectRequest.bucket().equalsIgnoreCase(INPUT_BUCKET_NAME)) {
            return PutObjectResponse.builder().build();
        } else {
            throw new RuntimeException(expectedErrorMessage);
        }
    }
}