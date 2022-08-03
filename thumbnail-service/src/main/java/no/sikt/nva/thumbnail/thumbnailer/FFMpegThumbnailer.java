package no.sikt.nva.thumbnail.thumbnailer;

import java.io.File;
import java.io.IOException;
import java.util.List;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import no.sikt.nva.thumbnail.AbstractThumbnailer;
import no.sikt.nva.thumbnail.util.ImageResizer;
import nva.commons.core.JacocoGenerated;

public class FFMpegThumbnailer extends AbstractThumbnailer {

    public static final String TMP_VIDEOSNAP_PNG = "/tmp/videosnap.png";

    @JacocoGenerated
    @Override
    public void generateThumbnail(File input, File output) throws IOException {

        //according to https://github.com/serverlesspub/ffmpeg-aws-lambda-layer/blob/master/example/src/index.js
        // ffmpeg is accessed in the lambda layer by path "/opt/bin/ffmpeg"
        FFmpeg ffmpeg = new FFmpeg("/usr/bin/ffmpeg");
        FFprobe ffprobe = new FFprobe("/usr/bin/ffprobe");
        File partiallyProcessed = new File(TMP_VIDEOSNAP_PNG);
        FFmpegBuilder builder = new FFmpegBuilder()
                                    .setInput(input.getAbsolutePath())     // Filename, or a FFmpegProbeResult
                                    .overrideOutputFiles(true) // Override the output if it exists
                                    .addOutput(partiallyProcessed.getAbsolutePath())   // Filename for the destination
                                    .setFrames(1)
                                    .done();

        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);

        // Run a one-pass encode
        executor.createJob(builder).run();

        resizeToThumbnailSpecs(partiallyProcessed, output);
        partiallyProcessed.deleteOnExit();
    }

    //ffmpeg doesn't operate with mimetypes but with formats.
    //Here are some of the formats converted to mimetypes:
    @Override
    public List<String> getAcceptedMimeTypes() {
        return List.of("application/vnd.ms-asf",
                       "application/ffmpeg",
                       "video/x-msvideo",
                       "video/x-flv",
                       "video/webm",
                       "video/mpeg",
                       "video/x-m4v",
                       "video/mp4",
                       "video/ogg",
                       "video/x-matroska",
                       "video/mpeg",
                       "video/quicktime"
        );
    }

    @JacocoGenerated
    private void resizeToThumbnailSpecs(File partiallyProcessed, File output) throws IOException {
        ImageResizer imageResizer = new ImageResizer(thumbWidth, thumbHeight, partiallyProcessed);
        imageResizer.writeThumbnailToFile(output);
    }
}
