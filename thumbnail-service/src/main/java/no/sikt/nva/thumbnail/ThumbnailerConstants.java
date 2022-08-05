package no.sikt.nva.thumbnail;

public final class ThumbnailerConstants {

    public static final int THUMBNAIL_DEFAULT_HEIGHT = 300;

    public static final int THUMBNAIL_DEFAULT_WIDTH = 400;

    //according to https://github.com/serverlesspub/ffmpeg-aws-lambda-layer/blob/master/example/src/index.js
    // ffmpeg is accessed in the lambda layer by path "/opt/bin/ffmpeg"
    public static final String FFMPEG_PATH_ON_AWS_LAYER = "/opt/bin/ffmpeg";
    public static final String FFPROBE_PATH_ON_AWS_LAYER = "/opt/bin/ffprobe";

    private ThumbnailerConstants() {
    }
}
