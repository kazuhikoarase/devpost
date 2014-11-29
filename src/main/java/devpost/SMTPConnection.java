package devpost;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Logger;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;

/**
 * SMTPConnection
 * @author Kazuhiko Arase
 */
public class SMTPConnection implements Runnable {
    
    private final Logger logger = Logger.getLogger(getClass().getName() );
    
    private final Socket socket;
    private final Session session;
    private final File mboxDir;
    
    private PlainTextReader in;
    private PlainTextWriter out;

    public SMTPConnection(
        final Socket socket,
        final Session session,
        final File mboxDir
    ) {
        this.socket = socket;
        this.session = session;
        this.mboxDir = mboxDir;
    }

    @Override
    public void run() {
        try {
            try {
                in = new PlainTextReader(socket.getInputStream() );
                try {
                    out = new PlainTextWriter(socket.getOutputStream() );
                    try {
                        doSMTP();
                    } finally {
                        out.close();
                    }
                } finally {
                    in.close();
                }
            } finally {
                socket.close();
            }
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void doSMTP() throws Exception {

        final String domain = InetAddress.getLocalHost().getHostName();

        String line;
        String[] cmd;

        println("220 " + domain + " Service ready");

        while ( (line = readLine() ) != null) {
            cmd = splitCmd(line);
            if ("DATA".equals(cmd[0]) ) {
                println("354 Start mail input; end with <CRLF>.<CRLF>");
                readMessage();
            } else if ("QUIT".equals(cmd[0]) ) {
                try {
                    println("221 " + domain + 
                            " Service closing transmission channel");
                } catch(SocketException e) {
                    // disconnected.
                }
                break;
            } else {
                // always positive
                println("250 OK");
            }
        }
    }

    protected void readMessage() throws Exception {
        String line;
        final StringBuilder buf = new StringBuilder();
        while ( (line = readLine() ) != null) {
            if (".".equals(line) ) {
                break;
            }
            buf.append(line);
            buf.append('\r');
            buf.append('\n');
        }
        println("250 OK");

        final MimeMessage msg = parse(buf.toString().getBytes("ISO-8859-1") );
        // set server time
        if (msg.getSentDate() == null) {
            //msg.setSentDate(new Date() );
        }
        msg.setHeader("X-Server-Addr",
                socket.getInetAddress().getHostAddress() );
        saveMessage(msg);
    }

    protected void saveMessage(MimeMessage msg) throws Exception {
        OutputStream out = new FileOutputStream(
            new File(mboxDir, Util.getFilenameByMsgId(msg.getMessageID() ) ) );
        try {
            msg.writeTo(out);
        } finally {
            out.close();
        }
    }

    protected MimeMessage parse(byte[] data) throws Exception {
        InputStream in = new ByteArrayInputStream(data);
        try {
            return new MimeMessage(session, in);
        } finally {
            in.close();
        }
    }

    protected void println(String line) throws IOException {
        log(line);
        out.println(line);
    }

    protected String readLine() throws IOException {
        String line = in.readLine();
        log(line);
        return line;
    }

    protected void log(String line) {
        logger.info(line);
    }

    protected static String[] splitCmd(String line) {
        int index = line.indexOf('\u0020');
        if (index != -1) {
            return new String[]{
                    line.substring(0, index).toUpperCase(),
                    line.substring(index + 1)};
        }
        return new String[]{line.toUpperCase(),""};
    }
}
