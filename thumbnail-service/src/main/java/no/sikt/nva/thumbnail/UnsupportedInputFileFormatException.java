package no.sikt.nva.thumbnail;

import java.io.IOException;
import java.io.Serial;

public class UnsupportedInputFileFormatException extends IOException {

    @Serial
    private static final long serialVersionUID = -8728813367662852880L;

    public UnsupportedInputFileFormatException() {
        super();
    }

    public UnsupportedInputFileFormatException(String arg0) {
        super(arg0);
    }

    public UnsupportedInputFileFormatException(Throwable arg0) {
        super(arg0);
    }

    public UnsupportedInputFileFormatException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }
}
