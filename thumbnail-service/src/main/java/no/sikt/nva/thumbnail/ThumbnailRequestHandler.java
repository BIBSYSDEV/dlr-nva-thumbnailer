package no.sikt.nva.thumbnail;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import nva.commons.core.JacocoGenerated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThumbnailRequestHandler implements RequestHandler<Void, Void> {

    private static final Logger logger = LoggerFactory.getLogger(ThumbnailRequestHandler.class);

    @JacocoGenerated
    public ThumbnailRequestHandler() {
    }


    @Override
    public Void handleRequest(Void input, Context context) {
        logger.debug("Request received");
        return null;
    }
}
