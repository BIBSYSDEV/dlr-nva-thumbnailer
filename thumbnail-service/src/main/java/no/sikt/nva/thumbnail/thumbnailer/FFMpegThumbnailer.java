package no.sikt.nva.thumbnail.thumbnailer;

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

    private final FFmpeg ffmpeg;
    private final FFprobe ffprobe;

    public static final String TMP_VIDEOSNAP_PNG = "/tmp/videosnap.png";

    public FFMpegThumbnailer(FFmpeg ffmpeg, FFprobe ffprobe) {
        super();
        this.ffmpeg = ffmpeg;
        this.ffprobe = ffprobe;
    }

    @Override
    public void generateThumbnail(File input, File output) throws IOException {
        //
        File partiallyProcessed = new File(TMP_VIDEOSNAP_PNG);
        FFmpegBuilder builder = new FFmpegBuilder()
                                    .setInput(input.getAbsolutePath())     // Filename, or a FFmpegProbeResult
                                    .overrideOutputFiles(true) // Override the output if it exists
                                    .addOutput(partiallyProcessed.getAbsolutePath())   // Filename for the destination
                                    .setStartOffset(3, TimeUnit.SECONDS)
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

    private void resizeToThumbnailSpecs(File partiallyProcessed, File output) throws IOException {
        ImageResizer imageResizer = new ImageResizer(thumbWidth, thumbHeight, partiallyProcessed);
        imageResizer.writeThumbnailToFile(output);
    }
}
