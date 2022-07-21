package no.sikt.nva.thumbnail.util;

import java.awt.Image;
import java.awt.image.ImageObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"PMD.DoNotUseThreads", "PMD.AvoidUsingVolatile"})
public class ThumbnailReadyObserver implements ImageObserver {

    private static final Logger mLog = LoggerFactory.getLogger(ThumbnailReadyObserver.class);
    private final Thread toNotify;
    public volatile boolean ready;

    public ThumbnailReadyObserver(Thread toNotify) {
        this.toNotify = toNotify;
        ready = false;
    }

    @Override
    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {

        mLog.info("Observer debug info: imageUpdate: " + infoflags);
        if ((infoflags & ImageObserver.ALLBITS) > 0) {
            ready = true;
            mLog.info("Observer says: Now ready!");
            toNotify.notifyAll();
            return true;
        }
        return false;
    }
}
