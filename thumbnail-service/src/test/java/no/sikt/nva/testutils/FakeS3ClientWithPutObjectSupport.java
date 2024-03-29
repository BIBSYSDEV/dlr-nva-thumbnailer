package no.sikt.nva.testutils;

import java.io.InputStream;
import no.unit.nva.stubs.FakeS3Client;
import nva.commons.core.ioutils.IoUtils;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

public class FakeS3ClientWithPutObjectSupport extends FakeS3Client {

    public static final String PATH_DELIMITER = "/";
    private static final String CONTENT_DISPOSITION = "filename=\"%s\"";
    private final String mimeType;
    private final InputStream getObjectStream;
    private final boolean shouldHaveContentDisposition;
    private final String filename;

    public FakeS3ClientWithPutObjectSupport(String filename, String path, String mimeType,
                                            Boolean shouldHaveContentDisposition) {
        super();
        this.mimeType = mimeType;
        this.getObjectStream = IoUtils.inputStreamFromResources(path + PATH_DELIMITER + filename);
        this.shouldHaveContentDisposition = shouldHaveContentDisposition;
        this.filename = filename;
    }

    public FakeS3ClientWithPutObjectSupport(String filename, String path, String mimeType) {
        this(filename, path, mimeType, true);
    }

    @Override
    public ResponseInputStream getObject(GetObjectRequest getObjectRequest) {
        return shouldHaveContentDisposition
                   ? new ResponseInputStream<>(
            GetObjectResponse
                .builder()
                .contentDisposition(generateContentDisposition())
                .contentType(mimeType)
                .build(),
            AbortableInputStream
                .create(getObjectStream))
                   : new ResponseInputStream<>(
                       GetObjectResponse
                           .builder()
                           .contentType(mimeType)
                           .build(),
                       AbortableInputStream.create(
                           getObjectStream));
    }

    @Override
    public S3Utilities utilities() {
        return S3Utilities.builder().region(Region.EU_WEST_1).build();
    }

    @Override
    public PutObjectResponse putObject(PutObjectRequest putObjectRequest, RequestBody requestBody)
        throws AwsServiceException, SdkClientException {
        return PutObjectResponse.builder().build();
    }

    @Override
    public String serviceName() {
        return null;
    }

    @Override
    public void close() {

    }

    private String generateContentDisposition() {
        return String.format(CONTENT_DISPOSITION, filename);
    }
}
