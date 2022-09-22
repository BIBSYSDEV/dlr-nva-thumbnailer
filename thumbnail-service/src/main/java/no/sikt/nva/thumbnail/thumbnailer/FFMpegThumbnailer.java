package no.sikt.nva.thumbnail.thumbnailer;

import static no.sikt.nva.thumbnail.util.MediaType.APPLICATION_FF_MPEG;
import static no.sikt.nva.thumbnail.util.MediaType.APPLICATION_MS_ADVANCED_SYSTEMS_FORMAT;
import static no.sikt.nva.thumbnail.util.MediaType.VIDEO_MP4;
import static no.sikt.nva.thumbnail.util.MediaType.VIDEO_MPEG;
import static no.sikt.nva.thumbnail.util.MediaType.VIDEO_MS_VIDEO;
import static no.sikt.nva.thumbnail.util.MediaType.VIDEO_OGG;
import static no.sikt.nva.thumbnail.util.MediaType.VIDEO_QUICKTIME;
import static no.sikt.nva.thumbnail.util.MediaType.VIDEO_WEBM;
import static no.sikt.nva.thumbnail.util.MediaType.VIDEO_X_FLV;
import static no.sikt.nva.thumbnail.util.MediaType.VIDEO_X_M4V;
import static no.sikt.nva.thumbnail.util.MediaType.VIDEO_X_MATROSKA;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import no.sikt.nva.thumbnail.AbstractThumbnailer;
import no.sikt.nva.thumbnail.util.ImageResizer;

public class FFMpegThumbnailer extends AbstractThumbnailer {

    public static final boolean OVERRIDE_OUTPUT = true;
    private final File temporaryFileForPartiallyGeneratedResults;
    private final FFmpeg ffmpeg;
    private final FFprobe ffprobe;

    public FFMpegThumbnailer(ThumbnailerInitializer thumbnailerInitializer) {
        super();
        this.ffmpeg = thumbnailerInitializer.getFFmpeg();
        this.ffprobe = thumbnailerInitializer.getFFprobe();
        this.temporaryFileForPartiallyGeneratedResults = new File(
            thumbnailerInitializer.getPartiallyProcessedFilename());
    }

    @Override
    public void generateThumbnail(File input, File output) throws IOException {

        encodeInput(input);
        resizeToThumbnailSpecs(output);
        temporaryFileForPartiallyGeneratedResults.deleteOnExit();
    }

    //ffmpeg doesn't operate with mimetypes but with formats.
    //Here are some of the formats converted to mimetypes:
    @Override
    public List<String> getAcceptedMimeTypes() {
        return List.of(APPLICATION_MS_ADVANCED_SYSTEMS_FORMAT.getValue(),
                       APPLICATION_FF_MPEG.getValue(),
                       VIDEO_MS_VIDEO.getValue(),
                       VIDEO_X_FLV.getValue(),
                       VIDEO_WEBM.getValue(),
                       VIDEO_MPEG.getValue(),
                       VIDEO_X_M4V.getValue(),
                       VIDEO_MP4.getValue(),
                       VIDEO_OGG.getValue(),
                       VIDEO_X_MATROSKA.getValue(),
                       VIDEO_QUICKTIME.getValue()
        );
    }

    private FFmpegBuilder setupEncodingConfiguration(File input) {
        var fileOrFFmpegProbeResult = input.getAbsolutePath();
        var destinationFilename = temporaryFileForPartiallyGeneratedResults.getAbsolutePath();
        return new FFmpegBuilder()
                   .setInput(fileOrFFmpegProbeResult)
                   .overrideOutputFiles(OVERRIDE_OUTPUT)
                   .addOutput(destinationFilename)
                   .setStartOffset(3, TimeUnit.SECONDS)
                   .setFrames(1)
                   .done();
    }

    private void encodeInput(File input) {
        var config = setupEncodingConfiguration(input);
        runOnePassEncode(config);
    }

    private void runOnePassEncode(FFmpegBuilder builder) {
        var executor = new FFmpegExecutor(ffmpeg, ffprobe);
        executor.createJob(builder).run();
    }

    private void resizeToThumbnailSpecs(File output) throws IOException {
        ImageResizer imageResizer = new ImageResizer(thumbWidth, thumbHeight,
                                                     temporaryFileForPartiallyGeneratedResults);
        imageResizer.writeThumbnailToFile(output);
    }
}
