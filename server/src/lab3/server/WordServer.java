package lab3.server;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class WordServer {
    public static void main(String[] args) throws Exception {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/WordServlet",       new WordServlet());
        server.createContext("/DictionaryServlet", new DictionaryServlet());
        server.start();
        System.out.println("Сервер запущен на порту " + port);
        System.out.println("  /WordServlet       — документ Word (вариант 4)");
        System.out.println("  /DictionaryServlet — словарь RU↔EN (общее задание)");
    }
}
