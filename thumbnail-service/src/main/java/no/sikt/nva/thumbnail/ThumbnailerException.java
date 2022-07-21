package no.sikt.nva.thumbnail;

import java.io.Serial;

public class ThumbnailerException extends Exception {

    @Serial
    private static final long serialVersionUID = -7988812285439060247L;

    public ThumbnailerException() {
        super();
    }

    public ThumbnailerException(String message) {
        super(message);
    }

    public ThumbnailerException(Throwable cause) {
        super(cause);
    }

    public ThumbnailerException(String message, Throwable cause) {
        super(message, cause);
    }
}
