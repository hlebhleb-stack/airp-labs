package lab3.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Java-клиент получает HTML от FlightServlet и сохраняет таблицу рейсов. */
public class FlightClient {
    private static final String SERVLET_URL = "http://localhost:8080/FlightServlet";

    public static void main(String[] args) throws IOException {
        if (args.length > 2) {
            System.err.println("Запуск: java -cp out lab3.client.FlightClient [город или IATA] [0|6|12|18]");
            System.exit(1);
        }

        String destination = args.length > 0 ? args[0] : "";
        String period = args.length > 1 ? args[1] : "";
        String query = "?destination=" + URLEncoder.encode(destination, StandardCharsets.UTF_8)
                + "&period=" + URLEncoder.encode(period, StandardCharsets.UTF_8);

        HttpURLConnection connection = (HttpURLConnection) URI.create(SERVLET_URL + query)
                .toURL().openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5_000);
        connection.setReadTimeout(35_000);
        try {
            int status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                throw new IOException("FlightServlet вернул HTTP " + status
                        + ". Подробности: " + SERVLET_URL + query);
            }

            Path output = Path.of("ber-flights.html");
            try (InputStream in = connection.getInputStream()) {
                Files.copy(in, output, StandardCopyOption.REPLACE_EXISTING);
            }
            System.out.println("HTML сохранён: " + output.toAbsolutePath());
        } finally {
            connection.disconnect();
        }
    }
}
