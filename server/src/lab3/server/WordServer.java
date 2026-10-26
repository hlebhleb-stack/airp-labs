package lab3.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.*;

public class WordServer {
    public static void main(String[] args) throws Exception {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/WordServlet",       new WordServlet());
        server.createContext("/DictionaryServlet", new DictionaryServlet());
        // Стартовая страница — dictionary.html (welcome-file, как в web.xml)
        server.createContext("/", new StaticFileHandler("web"));
        server.start();
        System.out.println("Сервер запущен на порту " + port);
        System.out.println("  http://localhost:" + port + "/dictionary.html — форма словаря");
        System.out.println("  http://localhost:" + port + "/DictionaryServlet — сервлет словаря");
        System.out.println("  http://localhost:" + port + "/WordServlet       — документ Word");
    }

    /** Отдаёт статические файлы из папки webRoot относительно рабочей директории. */
    static class StaticFileHandler implements HttpHandler {
        private final String webRoot;

        StaticFileHandler(String webRoot) {
            this.webRoot = webRoot;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String uriPath = exchange.getRequestURI().getPath();
            // welcome-file: "/" → "/dictionary.html"
            if ("/".equals(uriPath)) uriPath = "/dictionary.html";

            Path filePath = Paths.get(webRoot, uriPath.substring(1));
            if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
                byte[] msg = "404 Not Found".getBytes("UTF-8");
                exchange.sendResponseHeaders(404, msg.length);
                exchange.getResponseBody().write(msg);
                exchange.getResponseBody().close();
                return;
            }

            String contentType = uriPath.endsWith(".html") ? "text/html; charset=UTF-8" : "application/octet-stream";
            byte[] bytes = Files.readAllBytes(filePath);
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }
}
