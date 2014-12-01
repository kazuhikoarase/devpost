package devpost;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;

/**
 * SMTPServer
 * @author Kazuhiko Arase
 */
public class SMTPServer extends BaseServer {

    private static final Session session =
            Session.getInstance(new Properties() );

    private static final Date NULL_DATE = new Date(0L);

    private final File mboxDir;

    public SMTPServer(final int port, final File mboxDir) {
        super("devpost", port);
        this.mboxDir = mboxDir;
    }

    @Override
    protected Runnable createSocketTask(Socket socket) throws Exception {
        return new SMTPConnection(socket, session, mboxDir);
    }

    public MimeMessage getMessage(String msgId) throws Exception {
        return loadMessage(new File(mboxDir,
                Util.getFilenameByMsgId(msgId) ) );
    }

    protected List<MimeMessage> getAllMessages() {
        List<MimeMessage> messages = new ArrayList<MimeMessage>();
        for (File file : mboxDir.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.isFile() &&
                                file.getName().endsWith(".eml");
                    }
                }) ) {
            try {
                messages.add(loadMessage(file) );
            } catch(Exception e) {
                // ignore
                logger.log(Level.INFO, e.getMessage(), e);
            }
        }
        Collections.sort(messages, new Comparator<MimeMessage>() {
            @Override
            public int compare(final MimeMessage o1, final MimeMessage o2) {
                return -getSentDate(o1).compareTo(getSentDate(o2) );
            }
            protected Date getSentDate(MimeMessage msg) {
                Date date = null;
                try {
                    date = msg.getSentDate();
                } catch(Exception e) {
                }
                return date != null? date : NULL_DATE;
            }
        });
        return messages;
    }

    public void deleteMessage(String msgId) throws Exception {
        String filename = Util.getFilenameByMsgId(msgId);
        File file = new File(mboxDir, filename);
        file.delete();
    }

    protected MimeMessage loadMessage(File file) throws Exception {
        InputStream in = new BufferedInputStream(new FileInputStream(file) );
        try {
            return new MimeMessage(session, in);
        } finally {
            in.close();
        }
    }
}
