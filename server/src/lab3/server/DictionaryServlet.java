package lab3.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.*;
import java.net.URLDecoder;
import java.sql.*;
import java.util.*;

/**
 * Общее задание: сервлет-словарь с прямым (RU→EN) и обратным (EN→RU) переводом.
 * База данных: SQLite (файл dictionary.db рядом со скриптом запуска).
 */
public class DictionaryServlet implements HttpHandler {

    private static final String DB_URL = "jdbc:sqlite:dictionary.db";

    // Инициализация БД при загрузке класса
    static {
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 Statement stmt = conn.createStatement()) {

                stmt.execute(
                    "CREATE TABLE IF NOT EXISTS words (" +
                    "  ru TEXT PRIMARY KEY," +
                    "  en TEXT NOT NULL" +
                    ")"
                );

                // Заполняем только если таблица пустая
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM words");
                if (rs.next() && rs.getInt(1) == 0) {
                    String[][] pairs = {
                        {"привет",      "hello"},
                        {"мир",         "world"},
                        {"кот",         "cat"},
                        {"собака",      "dog"},
                        {"дом",         "house"},
                        {"книга",       "book"},
                        {"стол",        "table"},
                        {"окно",        "window"},
                        {"вода",        "water"},
                        {"огонь",       "fire"},
                        {"солнце",      "sun"},
                        {"луна",        "moon"},
                        {"дерево",      "tree"},
                        {"дорога",      "road"},
                        {"город",       "city"},
                        {"школа",       "school"},
                        {"компьютер",   "computer"},
                        {"программа",   "program"},
                        {"студент",     "student"},
                        {"университет", "university"},
                    };
                    PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO words(ru, en) VALUES(?, ?)"
                    );
                    for (String[] pair : pairs) {
                        ps.setString(1, pair[0]);
                        ps.setString(2, pair[1]);
                        ps.executeUpdate();
                    }
                    System.out.println("[DictionaryServlet] БД заполнена: " + pairs.length + " слов.");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка инициализации БД: " + e.getMessage(), e);
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if ("GET".equalsIgnoreCase(method)) {
            doGet(exchange);
        } else if ("POST".equalsIgnoreCase(method)) {
            doPost(exchange);
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }

    private void doGet(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        processRequest(exchange, query);
    }

    private void doPost(HttpExchange exchange) throws IOException {
        String query = new String(exchange.getRequestBody().readAllBytes(), "UTF-8");
        processRequest(exchange, query);
    }

    private void processRequest(HttpExchange exchange, String query) throws IOException {
        Map<String, String> params = parseQuery(query);

        String txt   = params.getOrDefault("txt",   "").trim();
        String trans = params.getOrDefault("trans", "").trim();

        String resultTxt   = txt;
        String resultTrans = trans;

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            if (!txt.isEmpty()) {
                // Прямой перевод RU→EN
                PreparedStatement ps = conn.prepareStatement(
                    "SELECT en FROM words WHERE LOWER(ru) = LOWER(?)"
                );
                ps.setString(1, txt);
                ResultSet rs = ps.executeQuery();
                resultTrans = rs.next() ? rs.getString("en") : "Слово не найдено";
                resultTxt   = txt;

            } else if (!trans.isEmpty()) {
                // Обратный перевод EN→RU
                PreparedStatement ps = conn.prepareStatement(
                    "SELECT ru FROM words WHERE LOWER(en) = LOWER(?)"
                );
                ps.setString(1, trans);
                ResultSet rs = ps.executeQuery();
                resultTxt   = rs.next() ? rs.getString("ru") : "Word not found";
                resultTrans = trans;
            }
        } catch (SQLException e) {
            resultTrans = "Ошибка БД: " + e.getMessage();
        }

        System.out.println("[DictionaryServlet] txt='" + txt + "' trans='" + trans
                + "' → txtResult='" + resultTxt + "' transResult='" + resultTrans + "'");

        String responseHtml =
            "<html>\n" +
            "<head><title>Servlet DictionaryServlet</title>\n" +
            "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" +
            "</head>\n" +
            "<body bgcolor='#aaccff'>\n" +
            "<form name='frm' method='Get' action='DictionaryServlet'>\n" +
            "<Font color='green' size='10'>Форма для работы со словарем</Font><br><br>\n" +
            "<Font color='blue' size='6'>Введите русское слово: </Font>" +
            "<Input type='Text' name='txt' value='" + htmlEscape(resultTxt) + "'/><br><br>\n" +
            "<Font color='blue' size='6'>Перевод: </Font>" +
            "<input type='text' name='trans' value='" + htmlEscape(resultTrans) + "'/><br>\n" +
            "<h4>Кликни здесь для получения перевода: " +
            "<Input type='submit' value='Перевести'/></h4>\n" +
            "</form>\n" +
            "</body>\n" +
            "</html>";

        byte[] bytes = responseHtml.getBytes("UTF-8");
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static Map<String, String> parseQuery(String query) throws UnsupportedEncodingException {
        Map<String, String> result = new HashMap<>();
        if (query == null || query.isEmpty()) return result;
        for (String pair : query.split("&")) {
            int idx = pair.indexOf('=');
            if (idx > 0) {
                String key = URLDecoder.decode(pair.substring(0, idx), "UTF-8");
                String val = URLDecoder.decode(pair.substring(idx + 1), "UTF-8").trim();
                result.put(key, val);
            }
        }
        return result;
    }

    private static String htmlEscape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace("'", "&#39;");
    }
}
