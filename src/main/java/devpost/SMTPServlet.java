package devpost;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.mail.Address;
import javax.mail.Message.RecipientType;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;

/**
 * SMTPServlet
 * @author Kazuhiko Arase
 */
@SuppressWarnings("serial")
public class SMTPServlet extends HttpServlet {

    private SMTPServer server;

    private int port;
    private File mboxDir;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        port = Integer.parseInt(config.getInitParameter("port") );
        mboxDir = new File(config.getInitParameter("mboxDir") );
        server = new SMTPServer(port, mboxDir);
        server.start();
    }

    @Override
    public void destroy() {
        server.shutdown();
    }

    @Override
    protected void service(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws ServletException, IOException {

        final ResourceBundle.Control control =
                new XMLResourceBundle.Control();
        request.setAttribute("devpostMessage", 
                new LocalizationContext(
                ResourceBundle.getBundle("devpost/message",
                request.getLocale(), control) ) );
        
        try {
            if ("/get".equals(request.getPathInfo() ) ) {
                download(request, response);
            } else if ("/view".equals(request.getPathInfo() ) ) {
                view(request, response);
            } else if ("/delete".equals(request.getPathInfo() ) ) {
                delete(request, response);
            } else {
                list(request, response);
            }
        } catch(RuntimeException e) {
            throw e;
        } catch(Exception e) {
            throw new ServletException(e);
        }
    }

    @SuppressWarnings("unchecked")
    protected void list(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws ServletException, IOException {

        List<MimeMessage> messages = server.getAllMessages();

        List<Map<String, Object>> mbox = new ArrayList<Map<String,Object>>();
        for(MimeMessage msg : messages) {
            try {
                Map<String, Object> item = new HashMap<String, Object>();
                item.put("msgId", msg.getMessageID() );
                final Date sentDate = msg.getSentDate();
                if (sentDate != null) {
                    item.put("sentDate", 
                            new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").
                            format(sentDate) );
                } else {
                    item.put("sentDate", ""); 
                }
                String[] serverAddr = msg.getHeader(Constants.X_SERVER_ADDR);
                if (serverAddr != null && serverAddr.length > 0) {
                    item.put("serverAddr", serverAddr[0]);
                }
                item.put("subject", msg.getSubject() );
                item.put("from", msg.getFrom() );
                item.put("rcptTo", getAllRecipients(msg) );
                mbox.add(item);
            } catch(Exception e) {
                // ignore
            }
        }

        Map<String, List<Map<String, Object>>> groupMbox = new LinkedHashMap<String, List<Map<String,Object>>>();
        for (Map<String, Object> item : mbox) {
            for (String rcptTo : (Set<String>)item.get("rcptTo") ) {
                if (!groupMbox.containsKey(rcptTo) ) {
                    groupMbox.put(rcptTo, new ArrayList<Map<String, Object>>());
                }
                groupMbox.get(rcptTo).add(item);
            }
        }
        request.setAttribute("port", port);
        request.setAttribute("mboxDir", mboxDir);
        request.setAttribute("groupMbox", groupMbox);
        getServletContext().
            getRequestDispatcher("/WEB-INF/pages/list.jspx").
            forward(request, response);
    }

    protected Set<String> getAllRecipients(MimeMessage msg) throws Exception {
        Set<String> rcpts = new HashSet<String>();
        String[] xRcpts = msg.getHeader(Constants.X_RECIPIENTS);
        if (xRcpts != null && xRcpts.length > 0) {
            for (String rcpt : xRcpts[0].split(",") ) {
                rcpts.add(rcpt);
            }
            return rcpts;
        }
        addRecipients(rcpts, msg.getRecipients(RecipientType.TO) );
        addRecipients(rcpts, msg.getRecipients(RecipientType.CC) );
        addRecipients(rcpts, msg.getRecipients(RecipientType.BCC) );
        return rcpts;
    }

    private void addRecipients(Set<String> rcpts, Address[] addrs) {
        if (addrs != null) {
            for (Address addr : addrs) {
                rcpts.add(addr.toString().toLowerCase() );
            }
        }
    }

    protected void view(
            
        HttpServletRequest request,
        HttpServletResponse response
    ) throws Exception {
        final String msgId = request.getParameter("msgId");
        request.setAttribute("message", server.getMessage(msgId) );
        getServletContext().
            getRequestDispatcher("/WEB-INF/pages/view.jspx").
            forward(request, response);
    }

    protected void delete(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws Exception {
      final String[] msgIdList = request.getParameterValues("msgId[]");
      if (msgIdList != null) {
          for (final String msgId : msgIdList) {
              server.deleteMessage(msgId);
          }
      }
      response.setContentType("application/json");
      Writer out = response.getWriter();
      out.write("{}");
      out.close();
    }

    protected void download(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws Exception {

        final String msgId = request.getParameter("msgId");
        final MimeMessage msg = server.getMessage(msgId);

        String downloadName = msg.getSubject() + ".eml";
        final Date sentDate = msg.getSentDate();
        if (sentDate != null) {
            downloadName = new SimpleDateFormat("yyyy-MM-dd_HH-mm_ss").
                    format(sentDate) + "_" + downloadName;
        }
        downloadName = downloadName.
                replaceAll("[^A-Za-z0-9_\\-@\\.\\u0100-\\uffff]+", "_");

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; " +
                Util.encodeFilename(request, downloadName) );

        final OutputStream out = new BufferedOutputStream(
                response.getOutputStream() );
        try {
            msg.writeTo(out);
        } finally {
            out.close();
        }
    }
}
