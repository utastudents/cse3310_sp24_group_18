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

public class HttpServer {

    int port = 8080;
    String dirname = "src/main/webapp/html";

    public HttpServer(int portNum, String dirName) {
        port = portNum;
        dirname = dirName;
    }

    public void start() {
        try {
            File dir = new File(dirname);
            if (!dir.canRead()) {
                throw new FileNotFoundException("Cannot read directory: " + dir.getAbsolutePath());
            }
            // set up server
            HTTPServer server = new HTTPServer(port);
            VirtualHost host = server.getVirtualHost(null); // default host
            host.setAllowGeneratedIndex(true); // with directory index pages
            host.addContext("/", new FileContextHandler(dir));
            host.addContext("/api/time", new ContextHandler() {
                public int serve(Request req, Response resp) throws IOException {
                    long now = System.currentTimeMillis();
                    resp.getHeaders().add("Content-Type", "text/plain");
                    resp.send(200, String.format("%tF %<tT", now));
                    return 0;
                }
            });
            // Add new context to serve the VERSION environment variable
            host.addContext("/api/version", new ContextHandler() {
                public int serve(Request req, Response resp) throws IOException {
                    String version = System.getenv("VERSION");
                    resp.getHeaders().add("Content-Type", "text/plain");
                    resp.send(200, version != null ? version : "Version not set");
                    return 0;
                }
            });
            
            server.start();
            System.out.println("Server started on port " + port);
        } catch (Exception e) {
            System.err.println("error: " + e);
        }
    }
}
