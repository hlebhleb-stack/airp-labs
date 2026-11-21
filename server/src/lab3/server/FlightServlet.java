package lab3.server;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Objects;

/** Дополнительное задание: разбор HTML-табло вылетов BER. */
public class FlightServlet extends HttpServlet {
    private static final String BOARD_URL = "https://www.berlin-airport.com/departures";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String destination = Objects.toString(request.getParameter("destination"), "").trim().toLowerCase(Locale.ROOT);
        String period = Objects.toString(request.getParameter("period"), "");
        if (!period.isEmpty() && !period.matches("0|6|12|18")) {
            sendPage(response, 400, "Укажите начало периода: 0, 6, 12 или 18.", new Elements(), "");
            return;
        }

        String url = period.isEmpty() ? BOARD_URL : BOARD_URL + "?tp=" + period;
        Document board;
        try {
            board = Jsoup.connect(url).timeout(15_000).get();
        } catch (HttpStatusException e) {
            sendPage(response, 502, "Табло вернуло HTTP " + e.getStatusCode() + ".", new Elements(), "");
            return;
        } catch (IOException e) {
            System.err.println("[FlightServlet] " + e);
            sendPage(response, 502, "Не удалось загрузить HTML-табло BER.", new Elements(), "");
            return;
        }

        if (board.selectFirst(".flights-info .flight-row.flight-titol") == null) {
            sendPage(response, 502, "В HTML-ответе не найдено табло вылетов BER.", new Elements(), "");
            return;
        }

        Elements flights = board.select(".flights-info .flight-row:not(.flight-titol)");
        if (!destination.isEmpty()) {
            flights.removeIf(row -> !matchesDestination(row, destination));
        }
        String message = flights.isEmpty()
                ? "В выбранном периоде подходящих рейсов нет."
                : "Найдено рейсов: " + flights.size();
        sendPage(response, 200, message, flights, period);
    }

    private static boolean matchesDestination(Element row, String destination) {
        String city = row.select(".flight-col__dest-term b").text().toLowerCase(Locale.ROOT);
        String code = row.select(".flight-col__dest-term span").text().toLowerCase(Locale.ROOT);
        if (destination.matches("[a-z]{3}")) {
            return city.equals(destination) || code.equals("(" + destination + ")");
        }
        return city.contains(destination) || code.contains(destination);
    }

    private static void sendPage(HttpServletResponse response, int status, String message,
                                 Elements flights, String period) throws IOException {
        response.setStatus(status);
        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE html><html lang='ru'><head><meta charset='UTF-8'>");
        out.println("<title>Вылеты из Берлина (BER)</title></head><body>");
        out.println("<h1>Вылеты из Берлина (BER)</h1>");
        String sourceUrl = period.isEmpty() ? BOARD_URL : BOARD_URL + "?tp=" + period;
        out.println("<p>Источник: <a href='" + sourceUrl + "'>табло BER</a>. "
                + "Время местное, Берлин.</p>");
        out.println("<p>" + Entities.escape(message) + "</p>");
        if (!flights.isEmpty()) {
            out.println("<table border='1'><thead><tr><th>Пункт назначения</th><th>Вылет</th>"
                    + "<th>Рейс</th><th>Авиакомпания</th><th>Терминал</th><th>Статус</th>"
                    + "</tr></thead><tbody>");
            for (Element row : flights) {
                out.println("<tr>");
                writeCell(out, row.select(".flight-col__dest-term").text());
                writeCell(out, row.select(".flight-col__hour").text());
                writeCell(out, row.select(".flight-col__flight > a").text());
                writeCell(out, row.select(".flight-col__airline > a").text());
                writeCell(out, row.select(".flight-col__terminal").text());
                writeCell(out, row.select(".flight-col__status").text().replace(" [+]", ""));
                out.println("</tr>");
            }
            out.println("</tbody></table>");
        }
        out.println("</body></html>");
    }

    private static void writeCell(PrintWriter out, String value) {
        out.println("<td>" + Entities.escape(value) + "</td>");
    }
}
