package lab3.client;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

/** Java-клиент получает HTML от FlightServlet и сохраняет таблицу рейсов. */
public class FlightClient {
    private static final String SERVLET_URL = "http://localhost:8080/FlightServlet";
    private static final Path OUTPUT = Path.of("ber-flights.html");

    private record Period(String label, String value) {
        @Override
        public String toString() {
            return label;
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            SwingUtilities.invokeLater(FlightClient::showWindow);
            return;
        }
        if (args.length > 2) {
            System.err.println("Запуск: java -cp out lab3.client.FlightClient [город или IATA] [0|6|12|18]");
            System.exit(1);
        }

        String destination = args[0];
        String period = args.length > 1 ? args[1] : "";
        Files.writeString(OUTPUT, loadFlights(destination, period), StandardCharsets.UTF_8);
        System.out.println("HTML сохранён: " + OUTPUT.toAbsolutePath());
    }

    private static void showWindow() {
        JFrame frame = new JFrame("Вылеты из Берлина (BER)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTextField destination = new JTextField("AMS", 12);
        JComboBox<Period> period = new JComboBox<>(new Period[]{
                new Period("Текущий", ""),
                new Period("00:00–06:00", "0"),
                new Period("06:00–12:00", "6"),
                new Period("12:00–18:00", "12"),
                new Period("18:00–24:00", "18")
        });
        period.setSelectedIndex(3);
        JButton search = new JButton("Показать рейсы");
        JButton save = new JButton("Сохранить HTML");
        save.setEnabled(false);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEADING));
        controls.add(new JLabel("Город или код IATA"));
        controls.add(destination);
        controls.add(new JLabel("Время Берлина"));
        controls.add(period);
        controls.add(search);
        controls.add(save);

        JEditorPane result = new JEditorPane("text/html", "");
        result.setEditable(false);
        JLabel status = new JLabel("Введите город латиницей или код IATA. Пустое поле покажет все рейсы.");
        status.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        class FlightSearch {
            private String html;

            void start() {
                String city = destination.getText().trim();
                Period selected = (Period) period.getSelectedItem();
                search.setEnabled(false);
                save.setEnabled(false);
                html = null;
                result.setText("");
                status.setText("Загрузка табло…");

                new SwingWorker<String, Void>() {
                    @Override
                    protected String doInBackground() throws IOException {
                        return loadFlights(city, selected.value());
                    }

                    @Override
                    protected void done() {
                        try {
                            html = get();
                            result.setText(html);
                            result.setCaretPosition(0);
                            save.setEnabled(true);
                            status.setText("Табло загружено.");
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            status.setText("Загрузка прервана.");
                        } catch (ExecutionException e) {
                            status.setText("Ошибка: " + e.getCause().getMessage());
                        } finally {
                            search.setEnabled(true);
                        }
                    }
                }.execute();
            }

            void save() {
                try {
                    Files.writeString(OUTPUT, html, StandardCharsets.UTF_8);
                    status.setText("Файл сохранён: " + OUTPUT.toAbsolutePath());
                } catch (IOException e) {
                    status.setText("Не удалось сохранить HTML: " + e.getMessage());
                }
            }
        }

        FlightSearch flightSearch = new FlightSearch();
        search.addActionListener(event -> flightSearch.start());
        save.addActionListener(event -> flightSearch.save());
        frame.getRootPane().setDefaultButton(search);
        frame.add(controls, BorderLayout.NORTH);
        frame.add(new JScrollPane(result), BorderLayout.CENTER);
        frame.add(status, BorderLayout.SOUTH);
        frame.setSize(900, 450);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static String loadFlights(String destination, String period) throws IOException {
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
                        + ". Проверьте журнал сервера.");
            }

            try (InputStream in = connection.getInputStream()) {
                return new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (ConnectException e) {
            throw new IOException("Запустите сервер лабораторной на порту 8080.", e);
        } finally {
            connection.disconnect();
        }
    }
}
