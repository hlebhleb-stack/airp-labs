package lab3.client;

import java.awt.Desktop;
import java.io.*;
import java.net.*;
import java.net.URI;
import java.nio.file.*;

/**
 * Вариант 4: клиент запрашивает документ Word у сервлета,
 * сохраняет во временный файл и открывает автоматически.
 */
public class WordClient {

    private static final String SERVLET_URL = "http://localhost:8080/WordServlet";

    public static void main(String[] args) throws Exception {
        System.out.println("Подключение к сервлету: " + SERVLET_URL);

        URL url = new URI(SERVLET_URL).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();

        int status = conn.getResponseCode();
        System.out.println("HTTP статус: " + status);

        if (status != 200) {
            System.err.println("Ошибка: сервер вернул статус " + status);
            return;
        }

        // Определяем имя файла из заголовка Content-Disposition, если есть
        String filename = extractFilename(conn.getHeaderField("Content-Disposition"));

        // Сохраняем файл во временную директорию
        Path tempFile = Files.createTempFile("word_servlet_", filename);
        try (InputStream in = conn.getInputStream();
             OutputStream out = Files.newOutputStream(tempFile)) {
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) != -1) {
                out.write(buf, 0, n);
            }
        }

        System.out.println("Файл сохранён: " + tempFile);

        // Открываем документ системным приложением (Microsoft Word / LibreOffice)
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(tempFile.toFile());
            System.out.println("Документ открыт автоматически.");
        } else {
            System.out.println("Desktop API не поддерживается. Откройте вручную: " + tempFile);
        }
    }

    /** Вытаскивает имя файла из заголовка Content-Disposition. */
    private static String extractFilename(String header) {
        if (header != null && header.contains("filename=")) {
            String[] parts = header.split("filename=");
            if (parts.length > 1) {
                return parts[1].replace("\"", "").trim();
            }
        }
        return "document.docx";
    }
}
