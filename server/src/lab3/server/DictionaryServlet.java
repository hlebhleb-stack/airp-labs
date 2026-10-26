package lab3.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.*;
import java.net.URI;
import java.util.*;

/**
 * Общее задание: сервлет-словарь с прямым (RU→EN) и обратным (EN→RU) переводом.
 *
 * GET /DictionaryServlet?txt=<русское слово>   → возвращает английский перевод
 * GET /DictionaryServlet?trans=<english word>  → возвращает русский перевод
 */
public class DictionaryServlet implements HttpHandler {

    // "База данных" — словарь переводов
    private static final Map<String, String> RU_TO_EN = new LinkedHashMap<>();
    private static final Map<String, String> EN_TO_RU = new LinkedHashMap<>();

    static {
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
        for (String[] pair : pairs) {
            RU_TO_EN.put(pair[0].toLowerCase(), pair[1]);
            EN_TO_RU.put(pair[1].toLowerCase(), pair[0]);
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery(); // "txt=привет" или "trans=hello"
        String result;
        String direction;

        if (query == null) {
            result = "Укажите параметр: txt= (RU→EN) или trans= (EN→RU)";
            direction = "—";
        } else if (query.startsWith("txt=")) {
            String word = java.net.URLDecoder.decode(query.substring(4), "UTF-8").toLowerCase().trim();
            String translation = RU_TO_EN.get(word);
            result = (translation != null) ? translation : "Слово не найдено";
            direction = "RU→EN";
        } else if (query.startsWith("trans=")) {
            String word = java.net.URLDecoder.decode(query.substring(6), "UTF-8").toLowerCase().trim();
            String translation = EN_TO_RU.get(word);
            result = (translation != null) ? translation : "Word not found";
            direction = "EN→RU";
        } else {
            result = "Неизвестный параметр запроса";
            direction = "—";
        }

        String body = result;
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        byte[] bytes = body.getBytes("UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }

        System.out.println("[DictionaryServlet] " + direction + " | запрос: " + query + " | ответ: " + result);
    }
}
