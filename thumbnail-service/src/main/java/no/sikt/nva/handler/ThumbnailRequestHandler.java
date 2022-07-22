package no.sikt.nva.handler;

import static nva.commons.core.attempt.Try.attempt;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import no.sikt.nva.thumbnail.ThumbnailerException;
import no.sikt.nva.thumbnail.ThumbnailerManager;
import no.unit.nva.s3.S3Driver;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.StringUtils;
import nva.commons.core.attempt.Failure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class ThumbnailRequestHandler implements RequestHandler<S3Event, Void> {

    public static final String COULD_NOT_CREATE_THUMBNAIL_LOG_MESSAGE = "Could not create thumbnail";
    public static final String CURRENTLY_SUPPORTING_THE_FOLLOWING_MIME_TYPES_LOG_MESSAGE = "Currently Supporting the "
                                                                                           + "following Mime Types: ";
    public static final String FILENAME_PREFIX = "filename=\"";
    public static final String FILENAME_POSTFIX = "\"";
    private static final Logger logger = LoggerFactory.getLogger(ThumbnailRequestHandler.class);

    // protects against overwriting existing files in
    // directory, plus it's the only place aws allows filewriting; To view thumbnails created locally: replace with
    // "" and comment out outputFile.deleteOnExit() / inputFile.deleteOnExit();
    private static final String INPUT_FILE_NAME_PREFIX = "/tmp/";
    private static final String OUTPUT_FILE_NAME = "thumbnail.png";
    private static final String THUMBNAIL_BUCKET_ENVIRONMENT_FIELD = "THUMBNAIL_BUCKET";
    public static final String DELIMITER = ", ";
    private final String thumbnailBucketName;
    private final S3Client s3Client;
    private File inputFile;
    private String mimeTypeFromS3Response;

    @JacocoGenerated
    public ThumbnailRequestHandler() {
        this(S3Driver.defaultS3Client().build());
    }

    public ThumbnailRequestHandler(S3Client s3Client) {
        this.thumbnailBucketName = new Environment().readEnv(THUMBNAIL_BUCKET_ENVIRONMENT_FIELD);
        this.s3Client = s3Client;
    }

    @Override
    public Void handleRequest(S3Event s3Event, Context context) {
        inputFile = readFile(s3Event);
        var outputFile = generateThumbnail();
        writeThumbnailToS3(outputFile);
        outputFile.deleteOnExit();
        inputFile.deleteOnExit();
        return null;
    }

    private void writeThumbnailToS3(File outputFile) {
        try {
            s3Client.putObject(PutObjectRequest.builder()
                                   .bucket(thumbnailBucketName)
                                   .key("keyForFeature(featureState.getFeature())")
                                   .build(), RequestBody.fromFile(outputFile));
        } catch (RuntimeException e) {
            logger.warn(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private File generateThumbnail() {
        try (var thumbnailerManager = new ThumbnailerManager()) {
            logCurrentSupportedMimeTypes(thumbnailerManager);
            var outPutFile = new File(INPUT_FILE_NAME_PREFIX + OUTPUT_FILE_NAME);
            thumbnailerManager.generateThumbnail(inputFile, outPutFile, mimeTypeFromS3Response);
            return outPutFile;
        } catch (IOException | ThumbnailerException e) {
            logger.warn(COULD_NOT_CREATE_THUMBNAIL_LOG_MESSAGE);
            throw new RuntimeException(e);
        }
    }

    private void logCurrentSupportedMimeTypes(ThumbnailerManager thumbnailerManager) {
        var supportedMimeTypes = String.join(DELIMITER, thumbnailerManager.getAcceptedMimeTypes());
        logger.info(CURRENTLY_SUPPORTING_THE_FOLLOWING_MIME_TYPES_LOG_MESSAGE + supportedMimeTypes);
    }

    private File readFile(S3Event s3Event) {
        var objectKey = getObjectKey(s3Event);
        var bucketName = getBucketName(s3Event);
        var objectRequest = createObjectRequest(objectKey, bucketName);
        return attempt(() -> s3Client.getObject(objectRequest))
                   .map(this::readResponseAndRetrieveMimeType)
                   .orElseThrow(this::logFailureAndThrowException);
    }

    private RuntimeException logFailureAndThrowException(Failure<File> failure) {
        logger.warn(failure.getException().getMessage());
        throw new RuntimeException(failure.getException());
    }

    private File readResponseAndRetrieveMimeType(ResponseInputStream<GetObjectResponse> response) {
        mimeTypeFromS3Response = response.response().contentType();
        String inputFileName = determineFileName(response);
        File inputFile = new File(inputFileName);
        try (var fileOutputStream = Files.newOutputStream(inputFile.toPath())) {
            fileOutputStream.write(response.readAllBytes());
        } catch (Exception e) {
            logger.warn(e.getMessage());
            throw new RuntimeException(e);
        }
        return inputFile;
    }

    private String determineFileName(ResponseInputStream<GetObjectResponse> responseInputStream) {
        return INPUT_FILE_NAME_PREFIX
               + responseInputStream.response().contentDisposition().replace(
            FILENAME_PREFIX, StringUtils.EMPTY_STRING).replace(FILENAME_POSTFIX, StringUtils.EMPTY_STRING);
    }

    private GetObjectRequest createObjectRequest(String objectKey, String bucketName) {
        return GetObjectRequest
                   .builder()
                   .key(objectKey)
                   .bucket(bucketName)
                   .build();
    }

    private String getBucketName(S3Event s3Event) {
        return s3Event.getRecords().get(0).getS3().getBucket().getName();
    }

    private String getObjectKey(S3Event s3Event) {
        return s3Event.getRecords().get(0).getS3().getObject().getKey();
    }
}
