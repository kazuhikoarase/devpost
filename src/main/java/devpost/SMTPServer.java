package devpost;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;

/**
 * SMTPServer
 * @author Kazuhiko Arase
 */
public class SMTPServer {

    private static final Session session =
            Session.getInstance(new Properties() );

    private static final Date NULL_DATE = new Date(0);

    private final Logger logger = Logger.getLogger(getClass().getName() );

    private ServerSocket ss = null;
    private ExecutorService es = null;

    private final int port;
    private final File mboxDir;

    public SMTPServer(final int port, final File mboxDir) {
        this.port = port;
        this.mboxDir = mboxDir;
    }

    public void start() {

        es = Executors.newCachedThreadPool();
        es.execute(new Runnable() {
            public void run() {
                try {
                    ss = new ServerSocket(port);
                    while (true) {
                        es.execute(new SMTPConnection(ss.accept(),
                                session, mboxDir) );
                    }
                } catch(SocketException e) {
                    // closed.
                } catch(RuntimeException e) {
                    throw e;
                } catch(Exception e) {
                    throw new RuntimeException(e);
                }
            }
        } );
        logger.info("devpost server start at port " + port);
    }

    public void shutdown() {
        try {
            if (ss != null) {
                ss.close();
            }
        } catch(RuntimeException e) {
            throw e;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        try {
            es.shutdown();
            es.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch(RuntimeException e) {
            throw e;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        logger.info("devpost server shutdown");
    }

    public MimeMessage getMessage(String msgId) throws Exception {
        return loadMessage(new File(mboxDir,
                Util.getFilenameByMsgId(msgId) ) );
    }

    protected List<MimeMessage> getAllMessages() {
        List<MimeMessage> messages = new ArrayList<MimeMessage>();
        for (File file : mboxDir.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".eml");
                    }
                }) ) {
            try {
                messages.add(loadMessage(file) );
            } catch(Exception e) {
                // ignore
            }
        }
        Collections.sort(messages, new Comparator<MimeMessage>() {
            @Override
            public int compare(final MimeMessage o1, final MimeMessage o2) {
                Date d1 = getSentDate(o1);
                Date d2 = getSentDate(o2);
                d1 = (d1 != null)? d1 : NULL_DATE;
                d2 = (d2 != null)? d2 : NULL_DATE;
                return -d1.compareTo(d2);
            }
        });
        return messages;
    }

    public void deleteMessage(String msgId) throws Exception {
        String filename = Util.getFilenameByMsgId(msgId);
        File file = new File(mboxDir, filename);
        file.delete();
    }

    protected Date getSentDate(MimeMessage msg) {
        try {
            return msg.getSentDate();
        } catch(Exception e) {
            return null;
        }
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
