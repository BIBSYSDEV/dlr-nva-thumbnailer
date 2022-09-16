package no.sikt.nva.testutils.thumbnailer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import javax.annotation.Nonnull;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.info.Codec;
import net.bramp.ffmpeg.info.Format;
import net.bramp.ffmpeg.progress.ProgressListener;
import nva.commons.core.ioutils.IoUtils;

public class FakeFFmpeg extends FFmpeg {

    public static final String JPEG_FILE = "images/pug.jpeg";
    private final String temporaryFilename;

    public FakeFFmpeg(String temporaryFilename) throws IOException {
        super();
        this.temporaryFilename = temporaryFilename;
    }

    @Override
    public boolean isFFmpeg() {
        return true;
    }

    @Nonnull
    @Override
    public List<Codec> codecs() {
        return List.of();
    }

    @Nonnull
    @Override
    public List<Format> formats() {
        return List.of();
    }

    @Override
    public void run(List<String> arguments) {
        writeToPartiallyProsessedFile();
    }

    @Override
    public void run(FFmpegBuilder ffmpegBuilder) {
        run(List.of());
    }

    @Override
    public void run(FFmpegBuilder ffmpegBuilder, ProgressListener listener) {
        run(List.of());
    }

    @Nonnull
    @Override
    public String version() {
        return "4.0";
    }

    private void writeToPartiallyProsessedFile() {
        File partiallyProsessed = new File(temporaryFilename);
        try (var fileOutputStream = Files.newOutputStream(partiallyProsessed.toPath());
            var mockFile = IoUtils.inputStreamFromResources(JPEG_FILE)) {
            fileOutputStream.write(mockFile.readAllBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
