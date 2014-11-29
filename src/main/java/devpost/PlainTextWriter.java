package devpost;

import java.io.IOException;
import java.io.OutputStream;

/**
 * PlainTextWriter
 * @author Kazuhiko Arase
 */
public class PlainTextWriter {
    private final OutputStream out;
    public PlainTextWriter(final OutputStream out) {
        this.out = out;
    }
    public void println(final String s) throws IOException {
        out.write(s.getBytes("ISO-8859-1") );
        out.write('\r');
        out.write('\n');
    }
    public void close() throws IOException {
        out.close();
    }
}
