package no.sikt.nva.testutils.thumbnailer;

import java.io.IOException;
import net.bramp.ffmpeg.FFprobe;

public class FakeFFprobe extends FFprobe {

    public FakeFFprobe() throws IOException {
        super();
    }

    @Override
    public boolean isFFprobe() {
        return true;
    }
}
