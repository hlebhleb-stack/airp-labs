package lab3.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.*;
import java.net.URLDecoder;
import java.util.*;

/**
 * Общее задание: сервлет-словарь с прямым (RU→EN) и обратным (EN→RU) переводом.
 *
 * Аналог HttpServlet с doGet/doPost → processRequest.
 * Обрабатывает форму dictionary.html: поля txt (RU) и trans (EN).
 * Возвращает HTML-страницу с заполненными полями перевода.
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
        String method = exchange.getRequestMethod();
        // doGet и doPost — оба направляются в processRequest
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
        // При POST тело содержит параметры
        String query = new String(exchange.getRequestBody().readAllBytes(), "UTF-8");
        processRequest(exchange, query);
    }

    private void processRequest(HttpExchange exchange, String query) throws IOException {
        // Правильный разбор параметров: "txt=кот&trans=+cat" → Map
        Map<String, String> params = parseQuery(query);

        String txt   = params.getOrDefault("txt",   "").trim();
        String trans = params.getOrDefault("trans", "").trim();

        String resultTxt   = txt;
        String resultTrans = trans;

        // Если заполнено поле txt → прямой перевод RU→EN
        if (!txt.isEmpty()) {
            String found = RU_TO_EN.get(txt.toLowerCase());
            resultTrans = (found != null) ? found : "Слово не найдено";
            resultTxt   = txt;
        }
        // Если заполнено поле trans → обратный перевод EN→RU
        else if (!trans.isEmpty()) {
            String found = EN_TO_RU.get(trans.toLowerCase());
            resultTxt   = (found != null) ? found : "Word not found";
            resultTrans = trans;
        }

        System.out.println("[DictionaryServlet] txt='" + txt + "' trans='" + trans
                + "' → txtResult='" + resultTxt + "' transResult='" + resultTrans + "'");

        // Ответ — HTML-страница с заполненными полями (как в методичке)
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

    /** Разбирает строку вида "key1=val1&key2=val2" в Map. */
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
