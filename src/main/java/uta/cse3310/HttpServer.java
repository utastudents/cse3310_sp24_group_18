// Taken from https://github.com/BudDavis/TicTacToe/blob/main/src/main/java/uta/cse3310/HttpServer.java
package uta.cse3310;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import net.freeutils.httpserver.HTTPServer;
import net.freeutils.httpserver.HTTPServer.ContextHandler;
import net.freeutils.httpserver.HTTPServer.FileContextHandler;
import net.freeutils.httpserver.HTTPServer.Request;
import net.freeutils.httpserver.HTTPServer.Response;
import net.freeutils.httpserver.HTTPServer.VirtualHost;

// http server include is a GPL licensed package from
//            http://www.freeutils.net/source/jlhttp/

public class MyHttpServer {

    private static final String DEFAULT_HTML_DIRECTORY = "./html";
    int serverPort = 8080;
    String directoryName = DEFAULT_HTML_DIRECTORY;

    public MyHttpServer(int portNumber, String directory) {
        serverPort = portNumber;
        directoryName = directory;
    }

    public void startServer() {
        try {
            File directory = new File(directoryName);
            if (!directory.canRead())
                throw new FileNotFoundException(directory.getAbsolutePath());

            // set up server
            HTTPServer server = new HTTPServer(serverPort);
            VirtualHost host = server.getVirtualHost(null); // default host
            host.setAllowGeneratedIndex(true); // with directory index pages
            host.addContext("/", new FileContextHandler(directory));
            host.addContext("/api/time", new ContextHandler() {
                public int serve(Request req, Response resp) throws IOException {
                    long currentTime = System.currentTimeMillis();
                    resp.getHeaders().add("Content-Type", "text/plain");
                    resp.send(200, String.format("%tF %<tT", currentTime));
                    return 0;
                }
            });
            server.start();
        } catch (Exception e) {
            System.err.println("Error: " + e);
        }

    }

} HttpServer {
    
}
