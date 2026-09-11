package airp_lab1;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class CapitalFinder extends Frame implements ActionListener {

    Button exitButton = new Button("Exit");
    Button searchButton = new Button("Search");
    TextField countryField = new TextField();
    TextArea resultArea = new TextArea();

    private final Map<String, String> countryToCapital = new LinkedHashMap<>();

    public CapitalFinder() {
        super("Поиск столицы");
        setLayout(null);
        setBackground(new Color(150, 200, 100));
        setSize(450, 260);

        Label label = new Label("Введите название государства:");
        label.setBounds(20, 20, 300, 20);
        add(label);

        countryField.setBounds(20, 45, 300, 25);
        add(countryField);

        searchButton.setBounds(20, 80, 100, 25);
        searchButton.addActionListener(this);
        add(searchButton);

        exitButton.setBounds(130, 80, 100, 25);
        exitButton.addActionListener(this);
        add(exitButton);

        resultArea.setBounds(20, 120, 400, 100);
        resultArea.setEditable(false);
        add(resultArea);

        setLocationRelativeTo(null);
        setVisible(true);

        loadCountries("data/countries.txt");

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                System.exit(0);
            }
        });
    }

    private void loadCountries(String path) {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(";");
                if (parts.length != 2) continue;
                countryToCapital.put(parts[0].trim(), parts[1].trim());
            }
        } catch (IOException e) {
            resultArea.setText("Ошибка чтения файла: " + e.getMessage());
        }
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == exitButton) {
            System.exit(0);
        } else if (ae.getSource() == searchButton) {
            String query = countryField.getText().trim();
            if (query.isEmpty()) {
                resultArea.setText("Введите название государства");
                return;
            }

            String bestCountry = null;
            int bestDistance = Integer.MAX_VALUE;
            double bestRelativeDistance = Double.MAX_VALUE;
            for (String country : countryToCapital.keySet()) {
                int distance = levenshtein(query.toLowerCase(), country.toLowerCase());
                double relativeDistance = (double) distance / Math.max(query.length(), country.length());
                if (relativeDistance < bestRelativeDistance) {
                    bestRelativeDistance = relativeDistance;
                    bestDistance = distance;
                    bestCountry = country;
                }
            }

            if (bestCountry == null) {
                resultArea.setText("Список стран пуст");
                return;
            }

            String capital = countryToCapital.get(bestCountry);
            resultArea.setText("Похоже, вы имели в виду: " + bestCountry
                    + "\nСтолица: " + capital
                    + "\n(отличие от введённого текста: " + bestDistance + " символ(ов))");
        }
    }

    private int levenshtein(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }
        return dp[a.length()][b.length()];
    }

    public static void main(String[] args) {
        new CapitalFinder();
    }
}
