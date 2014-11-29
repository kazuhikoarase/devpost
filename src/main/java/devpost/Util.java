package devpost;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;

/**
 * Util
 * @author Kazuhiko Arase
 */
public class Util {
    private Util() {
    }
    public static String getFilenameByMsgId(String msgId)
    throws IOException {
        return URLEncoder.encode(msgId, "UTF-8") + ".eml";
    }
    public static String encodeFilename(
        final HttpServletRequest request, 
        final String filename
    ) throws IOException {
        final String ua = request.getHeader("User-Agent");
        if (ua != null && ua.contains("MSIE") ) {
            // * fixed to MS932
            // http://support.microsoft.com/default.aspx?scid=kb;ja;436616
            // ftp://ftp.rfc-editor.org/in-notes/rfc2231.txt
            return "filename=\"" +
                new String(filename.getBytes("MS932"), "ISO-8859-1") +
                "\"";
        }
        return "filename*=UTF-8''" + 
            URLEncoder.encode(filename, "UTF-8");
    }
}
