package devpost;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * PlainTextReader
 * @author Kazuhiko Arase
 */
public class PlainTextReader {
    private final InputStream in;
    private boolean end;
    public PlainTextReader(final InputStream in) {
        this.in = in;
        this.end = false;
    }
    public String readLine() throws IOException {
        if (end) {
            return null;
        }
        final ByteArrayOutputStream buf = new ByteArrayOutputStream();
        try {
            int b;
            while (true) {
                b = in.read();
                if (b == -1) {
                    end = true;
                    break;
                }
                buf.write(b);
                if (b == '\n') {
                    break;
                }
            }
        } finally {
            buf.close();
        }
        final String line = new String(buf.toByteArray(), "ISO-8859-1");
        if (end && line.length() == 0) {
            return null;
        }
        return line.replaceAll("\\r?\\n$", "");
    }
    public void close() throws IOException {
        in.close();
    }
}
