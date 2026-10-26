package lab3.server;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class WordServer {
    public static void main(String[] args) throws Exception {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/WordServlet", new WordServlet());
        server.start();
        System.out.println("Сервер запущен на порту " + port);
        System.out.println("Обращайтесь: http://localhost:" + port + "/WordServlet");
    }
}
