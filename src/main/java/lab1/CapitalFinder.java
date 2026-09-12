package lab1;

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
    private final Map<String, String> countryToDescription = new LinkedHashMap<>();
    private final Map<String, String> fileToContent = new LinkedHashMap<>();

    public CapitalFinder() {
        super("Поиск столицы");
        setLayout(null);
        setBackground(new Color(150, 200, 100));
        setSize(450, 260);

        Label label = new Label("Введите название государства:");
        label.setBounds(20, 30, 300, 20);
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

        loadCountries("data");

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                System.exit(0);
            }
        });
    }

    private void loadCountries(String dataDir) {
        File[] files = new File(dataDir).listFiles((dir, name) -> name.endsWith(".txt"));
        if (files == null) {
            resultArea.setText("Не удалось найти папку с данными: " + dataDir);
            return;
        }
        Arrays.sort(files, Comparator.comparing(File::getName));

        for (File file : files) {
            loadCountryFile(file);
        }
    }

    private void loadCountryFile(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            fileToContent.put(file.getName(), content.toString());

            String firstLine = content.toString().split("\n", 2)[0];
            if (firstLine.trim().isEmpty()) return;

            String[] parts = firstLine.trim().split(";");
            if (parts.length != 2) return;

            String country = parts[0].trim();
            String capital = parts[1].trim();
            String[] lines = content.toString().split("\n");
            String description = lines.length > 1 ? lines[1].trim() : "";

            countryToCapital.put(country, capital);
            countryToDescription.put(country, description);
        } catch (IOException e) {
            resultArea.setText("Ошибка чтения файла " + file.getName() + ": " + e.getMessage());
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
            String bestCapital = null;
            int bestDistance = Integer.MAX_VALUE;
            double bestRelativeDistance = Double.MAX_VALUE;
            for (Map.Entry<String, String> entry : countryToCapital.entrySet()) {
                String country = entry.getKey();
                int distance = levenshtein(query.toLowerCase(), country.toLowerCase());
                double relativeDistance = (double) distance / Math.max(query.length(), country.length());
                if (relativeDistance < bestRelativeDistance) {
                    bestRelativeDistance = relativeDistance;
                    bestDistance = distance;
                    bestCountry = country;
                    bestCapital = entry.getValue();
                }
            }

            if (bestCountry == null) {
                resultArea.setText("Список стран пуст");
                return;
            }

            StringBuilder matchesText = new StringBuilder();
            String bestFileName = null;
            int bestFileCount = 0;
            for (Map.Entry<String, String> entry : fileToContent.entrySet()) {
                int count = countOccurrences(entry.getValue().toLowerCase(), query.toLowerCase());
                matchesText.append(entry.getKey()).append(" - ").append(count).append("\n");
                if (bestFileName == null || count > bestFileCount) {
                    bestFileName = entry.getKey();
                    bestFileCount = count;
                }
            }

            String description = countryToDescription.getOrDefault(bestCountry, "");
            resultArea.setText(matchesText
                    + "\nПохоже, вы имели в виду: " + bestCountry
                    + "\nСтолица: " + bestCapital
                    + "\nОписание: " + description
                    + "\n(отличие от введённого текста: " + bestDistance + " символ(ов))");

            if (bestFileCount > 0) {
                openInBrowser(bestFileName, fileToContent.get(bestFileName));
            }
        }
    }

    private void openInBrowser(String title, String content) {
        try {
            File htmlFile = File.createTempFile("airp-lab1-", ".html");
            htmlFile.deleteOnExit();
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(htmlFile), "UTF-8")) {
                writer.write("<!DOCTYPE html><html><head><meta charset=\"UTF-8\">"
                        + "<title>" + title + "</title></head><body><pre>"
                        + content.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                        + "</pre></body></html>");
            }

            String os = System.getProperty("os.name", "").toLowerCase();
            String path = htmlFile.getAbsolutePath();
            if (os.contains("mac")) {
                new ProcessBuilder("open", "-a", "Safari", path).start();
            } else if (os.contains("win")) {
                new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", path).start();
            } else {
                new ProcessBuilder("xdg-open", path).start();
            }
        } catch (IOException e) {
            resultArea.append("\nНе удалось открыть файл в браузере: " + e.getMessage());
        }
    }

    private static final double WORD_MATCH_THRESHOLD = 0.34;

    private int countOccurrences(String text, String word) {
        int count = 0;
        for (String candidate : text.split("[^\\p{L}]+")) {
            if (candidate.isEmpty()) continue;
            int distance = levenshtein(candidate, word);
            double relativeDistance = (double) distance / Math.max(candidate.length(), word.length());
            if (relativeDistance <= WORD_MATCH_THRESHOLD) count++;
        }
        return count;
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
