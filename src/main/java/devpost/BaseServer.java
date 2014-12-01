package devpost;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * BaseServer
 * @author Kazuhiko Arase
 */
public abstract class BaseServer {

    protected final Logger logger = Logger.getLogger(getClass().getName() );

    private final String name;
    private final int port;

    private ServerSocket ss = null;
    private ExecutorService es = null;

    protected BaseServer(final String name, final int port) {
        this.name = name;
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public int getPort() {
        return port;
    }

    public void start() {

        es = Executors.newCachedThreadPool();
        es.execute(new Runnable() {
            public void run() {
                try {
                    ss = new ServerSocket(port);
                    while (true) {
                        es.execute(createSocketTask(ss.accept() ) );
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
        logger.info(name + " server start at port " + port);
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
        logger.info(name + " server shutdown");
    }

    protected abstract Runnable createSocketTask(
            Socket socket) throws Exception ;
}
