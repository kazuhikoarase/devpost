package devpost;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.junit.Test;

public class SendTest {

    protected Session getSession(final boolean auth) {

        final Properties props = new Properties();
        props.setProperty("mail.smtp.host", "localhost");
        props.setProperty("mail.smtp.port", "2525");
        if (auth) {
            props.setProperty("mail.smtp.auth", "true");
            return Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication("authid", "authpass");
                }
            });
        } else {
            return Session.getInstance(props, null);
        }
    }

    private final String mailEncoding_en = "ISO-8859-1";

    private final String mailEncoding_ja = "ISO-2022-JP";

    protected Date getSentDate(int hour) throws Exception {
        return new Date();
        /*
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2014);
        cal.set(Calendar.MONTH, 0);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return cal.getTime();
        */
    }

    @Test
    public void sendTextMail_en() throws Exception{

        final MimeMessage msg = new MimeMessage(getSession(false) );
        msg.setFrom(new InternetAddress("sender@example.com",
                "test user", mailEncoding_en) );
        msg.setReplyTo(InternetAddress.parse("reply@example.com", false) );
        msg.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse("testuser_en@example.com", false) );
        msg.setSentDate(getSentDate(1) );
        msg.setSubject(
                "longlonglonglonglonglonglonglong" + 
                "longlonglonglonglonglonglonglonglonglongsubject",
                mailEncoding_en);
        msg.setText(fixEOL("Hi,\nThis is a text mail."), mailEncoding_en);

        Transport.send(msg);
    }

    @Test
    public void sendHTMLMail_en() throws Exception{

        final MimeMessage msg = new MimeMessage(getSession(false) );
        msg.setFrom(new InternetAddress("sender@example.com",
                "test user", mailEncoding_en) );
        msg.setReplyTo(InternetAddress.parse("reply@example.com", false) );
        msg.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse("testuser_en@example.com", false) );
        msg.setSentDate(getSentDate(2) );
        msg.setSubject("test of html mail", mailEncoding_en);
        msg.setContent(
            fixEOL("<html><body><h1>Hi,</h1><p>This is a html mail.</p></body></html>"),
            "text/html;charset=" + mailEncoding_en);

        Transport.send(msg);
    }

    @Test
    public void sendTextMail_ja() throws Exception{

        final MimeMessage msg = new MimeMessage(getSession(false) );
        msg.setFrom(new InternetAddress("sender@example.com",
                "テスト ユーザー", mailEncoding_ja) );
        msg.setReplyTo(InternetAddress.parse("reply@example.com", false) );
        msg.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse("testuser_ja@example.com", false) );
        msg.setSentDate(getSentDate(3) );
        msg.setSubject(
                "長い長い長い長い長い長い長い長い長い長い長い長い長い長い長い件名",
                mailEncoding_ja);
        msg.setText(fixEOL("こんにちは、\nこれはテキストメールです。"), mailEncoding_ja);

        Transport.send(msg);
    }

    @Test
    public void sendHTMLMail_ja() throws Exception{

        final MimeMessage msg = new MimeMessage(getSession(false) );
        msg.setFrom(new InternetAddress("sender@example.com",
                "テスト ユーザー", mailEncoding_ja) );
        msg.setReplyTo(InternetAddress.parse("reply@example.com", false) );
        msg.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse("testuser_ja@example.com", false) );
        msg.setSentDate(getSentDate(4) );
        msg.setSubject("HTMLメールのテスト", mailEncoding_ja);
        msg.setContent(
            fixEOL("<html><body><h1>こんにちは、</h1><p>これはHTMLメールです。</p></body></html>"),
            "text/html;charset=" + mailEncoding_ja);

        Transport.send(msg);
    }

    protected static String fixEOL(final String s) throws Exception {
        final StringBuilder buf = new StringBuilder();
        final BufferedReader in = new BufferedReader(new StringReader(s) );
        try {
            String line;
            while ( (line = in.readLine() ) != null) {
                buf.append(line);
                buf.append('\r');
                buf.append('\n');
            }
            return buf.toString();
        } finally {
            in.close();
        }
    }
}
