package lab3.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

/**
 * Общее задание: клиентское Java-приложение (Swing), вызывающее DictionaryServlet по URL.
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

        // Строка 0: метка + поле русского слова
        c.gridx = 0; c.gridy = 0;
        add(new JLabel("Русское слово:"), c);
        c.gridx = 1;
        add(txtRu, c);

        // Строка 1: метка + поле английского слова
        c.gridx = 0; c.gridy = 1;
        add(new JLabel("English word:"), c);
        c.gridx = 1;
        add(txtEn, c);

        // Строка 2: кнопки
        JButton btnForward = new JButton("RU → EN (перевести)");
        JButton btnReverse = new JButton("EN → RU (обратный)");

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnPanel.add(btnForward);
        btnPanel.add(btnReverse);

        c.gridx = 0; c.gridy = 2; c.gridwidth = 2;
        add(btnPanel, c);

        // Строка 3: статус
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
            String word;
            String param;
            if (ruToEn) {
                word = txtRu.getText().trim();
                if (word.isEmpty()) { lblStatus.setText("Введите русское слово"); return; }
                param = "txt=" + URLEncoder.encode(word, "UTF-8");
            } else {
                word = txtEn.getText().trim();
                if (word.isEmpty()) { lblStatus.setText("Enter an English word"); return; }
                param = "trans=" + URLEncoder.encode(word, "UTF-8");
            }

            URL url = new URL(SERVLET_URL + "?" + param);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                String result = br.readLine();
                if (ruToEn) {
                    txtEn.setText(result);
                } else {
                    txtRu.setText(result);
                }
                lblStatus.setText("Готово.");
            }
        } catch (Exception ex) {
            lblStatus.setText("Ошибка: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DictionaryClient().setVisible(true));
    }
}
