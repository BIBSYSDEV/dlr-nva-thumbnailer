package no.sikt.nva.thumbnail;

import no.unit.nva.stubs.FakeContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ThumbnailRequestHandlerTest {
    private FakeContext context;
    private ThumbnailRequestHandler handler;

    @BeforeEach
    public void init() {
        this.context = new FakeContext();
        this.handler = new ThumbnailRequestHandler();
    }

    @AfterEach
    public void tearDown() {

    }

    @Test
    void shouldReturnHandleUriWhenInputIsValidUri() {
        handler.handleRequest(null, context);
    }

}