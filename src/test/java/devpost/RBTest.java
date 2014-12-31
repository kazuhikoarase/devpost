package devpost;

import java.util.Locale;
import java.util.ResourceBundle;

import org.junit.Assert;
import org.junit.Test;

public class RBTest {

    @Test
    public void rb_en() {
        ResourceBundle.Control control =
                new XMLResourceBundle.Control();
        ResourceBundle rb = ResourceBundle.getBundle("devpost/message",
                Locale.ENGLISH, control);
        Assert.assertEquals("Subject", rb.getString("msg.subject") );
    }

    @Test
    public void rb_ja() {
        ResourceBundle.Control control =
                new XMLResourceBundle.Control();
        ResourceBundle rb = ResourceBundle.getBundle("devpost/message",
                Locale.JAPANESE, control);
        Assert.assertEquals("件名", rb.getString("msg.subject") );
    }
}
