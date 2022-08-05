package no.sikt.nva.thumbnail.thumbnailer;

import static no.sikt.nva.thumbnail.ThumbnailerConstants.FFMPEG_PATH_ON_AWS_LAYER;
import static no.sikt.nva.thumbnail.ThumbnailerConstants.FFPROBE_PATH_ON_AWS_LAYER;
import java.io.IOException;
import java.util.Objects;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFprobe;
import nva.commons.core.JacocoGenerated;

public class ThumbnailerInitializer {

    private final FFmpeg ffmpeg;
    private final FFprobe ffprobe;

    public ThumbnailerInitializer(Builder builder) {
        this.ffprobe = builder.getFFprobe();
        this.ffmpeg = builder.getFFmpeg();
    }

    public FFmpeg getFFmpeg() {
        return ffmpeg;
    }

    public FFprobe getFFprobe() {
        return ffprobe;
    }

    public static class Builder {

        private FFmpeg ffmpeg;
        private FFprobe ffprobe;

        public Builder withFFmpeg(FFmpeg ffmpeg) {
            this.ffmpeg = ffmpeg;
            return this;
        }

        public Builder withFFprobe(FFprobe ffprobe) {
            this.ffprobe = ffprobe;
            return this;
        }

        public FFmpeg getFFmpeg() {
            return ffmpeg;
        }

        public FFprobe getFFprobe() {
            return ffprobe;
        }

        @JacocoGenerated
        public ThumbnailerInitializer build() {
            try {
                if (Objects.isNull(ffmpeg)) {
                    setDefaultFFmpeg();
                }
                if (Objects.isNull(ffprobe)) {
                    setDefaultFFprobe();
                }
                return new ThumbnailerInitializer(this);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @JacocoGenerated
        private void setDefaultFFmpeg() throws IOException {
            this.ffmpeg = new FFmpeg(FFMPEG_PATH_ON_AWS_LAYER);
        }

        @JacocoGenerated
        private void setDefaultFFprobe() throws IOException {
            this.ffprobe = new FFprobe(FFPROBE_PATH_ON_AWS_LAYER);
        }
    }
}
