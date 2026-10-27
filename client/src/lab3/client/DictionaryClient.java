package lab3.client;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.regex.*;

/**
 * Общее задание: клиентское Java-приложение (Swing), вызывающее DictionaryServlet по URL.
 * Аналог ApacheClient из методички — использует HttpURLConnection вместо Apache HTTP.
 * Поддерживает прямой перевод (RU→EN) и обратный (EN→RU).
 */
public class DictionaryClient extends JFrame {

    private static final String SERVLET_URL = "http://localhost:8080/DictionaryServlet";

    private final JTextField txtRu    = new JTextField(20);
    private final JTextField txtEn    = new JTextField(20);
    private final JLabel     lblStatus = new JLabel(" ");

    public DictionaryClient() {
        super("Словарь — клиент (АИРП Лаб3)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 8, 6, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; c.gridy = 0;
        add(new JLabel("Русское слово:"), c);
        c.gridx = 1;
        add(txtRu, c);

        c.gridx = 0; c.gridy = 1;
        add(new JLabel("English word:"), c);
        c.gridx = 1;
        add(txtEn, c);

        JButton btnForward = new JButton("RU → EN (перевести)");
        JButton btnReverse = new JButton("EN → RU (обратный)");

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnPanel.add(btnForward);
        btnPanel.add(btnReverse);

        c.gridx = 0; c.gridy = 2; c.gridwidth = 2;
        add(btnPanel, c);

        lblStatus.setForeground(Color.DARK_GRAY);
        c.gridy = 3;
        add(lblStatus, c);

        btnForward.addActionListener(e -> translate(true));
        btnReverse.addActionListener(e -> translate(false));

        pack();
        setLocationRelativeTo(null);
    }

    private void translate(boolean ruToEn) {
        try {
            String param;
            if (ruToEn) {
                String word = txtRu.getText().trim();
                if (word.isEmpty()) { lblStatus.setText("Введите русское слово"); return; }
                param = "txt=" + URLEncoder.encode(word, "UTF-8") + "&trans=";
            } else {
                String word = txtEn.getText().trim();
                if (word.isEmpty()) { lblStatus.setText("Enter an English word"); return; }
                param = "txt=&trans=" + URLEncoder.encode(word, "UTF-8");
            }

            // Вызов сервлета по URL (аналог ApacheClient из методички)
            URL url = new URI(SERVLET_URL + "?" + param).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) sb.append(line).append("\n");
            }

            String html = sb.toString();

            // Извлекаем значения полей txt и trans из HTML-ответа сервлета
            String resultTxt   = extractValue(html, "name='txt'");
            String resultTrans = extractValue(html, "name='trans'");

            if (ruToEn) {
                txtEn.setText(resultTrans);
            } else {
                txtRu.setText(resultTxt);
            }
            lblStatus.setText("Готово.");
        } catch (Exception ex) {
            lblStatus.setText("Ошибка: " + ex.getMessage());
        }
    }

    /** Извлекает value='...' из тега с заданным name='...' в HTML-ответе. */
    private static String extractValue(String html, String nameAttr) {
        // ищем value= после нужного name=
        Pattern p = Pattern.compile(nameAttr + "[^>]*value='([^']*)'", Pattern.DOTALL);
        Matcher m = p.matcher(html);
        if (m.find()) return m.group(1);
        // также ищем value= перед name=
        p = Pattern.compile("value='([^']*)'[^>]*" + nameAttr, Pattern.DOTALL);
        m = p.matcher(html);
        if (m.find()) return m.group(1);
        return "";
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DictionaryClient().setVisible(true));
    }
}
