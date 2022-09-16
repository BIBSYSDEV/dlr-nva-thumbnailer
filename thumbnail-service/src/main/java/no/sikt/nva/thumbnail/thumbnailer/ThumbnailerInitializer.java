package no.sikt.nva.thumbnail.thumbnailer;

import static no.sikt.nva.thumbnail.ThumbnailerConstants.FFMPEG_PATH_ON_AWS_LAYER;
import static no.sikt.nva.thumbnail.ThumbnailerConstants.FFPROBE_PATH_ON_AWS_LAYER;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFprobe;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.StringUtils;

public class ThumbnailerInitializer {

    private final FFmpeg ffmpeg;
    private final FFprobe ffprobe;
    private final String partiallyProcessedFilename;

    public ThumbnailerInitializer(Builder builder) {
        this.ffprobe = builder.getFFprobe();
        this.ffmpeg = builder.getFFmpeg();
        this.partiallyProcessedFilename = builder.getPartiallyProcessedFilename();
    }

    public FFmpeg getFFmpeg() {
        return ffmpeg;
    }

    public FFprobe getFFprobe() {
        return ffprobe;
    }
    public String getPartiallyProcessedFilename() {
        return partiallyProcessedFilename;
    }

    public static class Builder {

        private FFmpeg ffmpeg;
        private FFprobe ffprobe;
        private String partiallyProcessedFilename;

        public Builder withFFmpeg(FFmpeg ffmpeg) {
            this.ffmpeg = ffmpeg;
            return this;
        }

        public Builder withFFprobe(FFprobe ffprobe) {
            this.ffprobe = ffprobe;
            return this;
        }

        public Builder withPartiallyProcessedFilename(String partiallyProcessedFilename) {
            this.partiallyProcessedFilename = partiallyProcessedFilename;
            return this;
        }

        public FFmpeg getFFmpeg() {
            return ffmpeg;
        }

        public FFprobe getFFprobe() {
            return ffprobe;
        }

        public String getPartiallyProcessedFilename() {
            return partiallyProcessedFilename;
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
                if (StringUtils.isBlank(partiallyProcessedFilename)) {
                    setDefaultPartiallyProcessedFileName();
                }
                return new ThumbnailerInitializer(this);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @JacocoGenerated
        private void setDefaultPartiallyProcessedFileName() {
            this.partiallyProcessedFilename = String.format("/tmp/%s.png", UUID.randomUUID());
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
