package uta.cse3310;

import java.net.InetSocketAddress;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class App {
    public static void main(String[] args) {
        int httpPort = 9080;
        String httpPortEnv = System.getenv("HTTP_PORT");
        if (httpPortEnv != null) {
            httpPort = Integer.parseInt(httpPortEnv);
        }

        HttpServer httpServer = new HttpServer(httpPort, "src/main/webapp/html");
        httpServer.start();
        System.out.println("[App.java] HTTP Server started on port: " + httpPort);

        int wsPort = 9180;
        String wsPortEnv = System.getenv("WEBSOCKET_PORT");
        if (wsPortEnv != null) {
            wsPort = Integer.parseInt(wsPortEnv);
        }

        final int finalWsPort = wsPort; // Make a final copy for use in the inner class

        WebSocketServer webSocketServer = new WebSocketServer(new InetSocketAddress(finalWsPort)) {
            @Override
            public void onOpen(WebSocket conn, ClientHandshake handshake) {
                System.out.println("Client connected: " + conn.getRemoteSocketAddress());
            }

            @Override
            public void onClose(WebSocket conn, int code, String reason, boolean remote) {
                System.out.println("Client disconnected: " + conn.getRemoteSocketAddress());
            }

            @Override
            public void onMessage(WebSocket conn, String message) {
                System.out.println("Received message from client: " + message);
                 if ("bark".equals(message)) {
                    Game G = new Game();
                    String test_result = G.test();
                    conn.send(test_result);
                }

            }

            @Override
            public void onError(WebSocket conn, Exception ex) {
                System.err.println("WebSocket error with connection: " + conn + "\nError: " + ex.getMessage());
                ex.printStackTrace();
            }

            @Override
            public void onStart() {
                System.out.println("WebSocket server started at ws://localhost:" + finalWsPort + "/websocket");
            }
        };

        webSocketServer.start();
        System.out.println("WebSocket Server started on port: " + finalWsPort);
    }
}
