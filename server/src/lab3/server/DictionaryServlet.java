package lab3.server;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Path;
import java.sql.*;
import java.util.Locale;
import java.util.Objects;

/**
 * Общее задание: сервлет-словарь с прямым (RU→EN) и обратным (EN→RU) переводом.
 * База данных: SQLite (файл dictionary.db в каталоге Tomcat или по пути lab3.dictionary.path).
 */
public class DictionaryServlet extends HttpServlet {

    private static final String DB_URL = "jdbc:sqlite:" + System.getProperty(
            "lab3.dictionary.path", Path.of(System.getProperty("catalina.base", "."), "dictionary.db").toString());

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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        String txt = Objects.toString(request.getParameter("txt"), "").trim();
        String trans = Objects.toString(request.getParameter("trans"), "").trim();

        String resultTxt   = txt;
        String resultTrans = trans;

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            if (!txt.isEmpty()) {
                // Прямой перевод RU→EN
                PreparedStatement ps = conn.prepareStatement(
                    "SELECT en FROM words WHERE LOWER(ru) = LOWER(?)"
                );
                ps.setString(1, txt.toLowerCase(Locale.ROOT));
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

        response.setContentType("text/html; charset=UTF-8");
        response.getWriter().print(responseHtml);
    }

    private static String htmlEscape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace("'", "&#39;");
    }
}
