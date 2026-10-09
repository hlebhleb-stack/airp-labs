package lab2;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

// Поток-сервер: принимает соединения и обрабатывает команды клиентов
class AccountServer extends Thread {
    ServerSocket server;

    public void run() {
        try {
            server = new ServerSocket(3001);
        } catch (Exception e) {
            System.out.println("Ошибка сервера: " + e);
        }
        while (true) {
            Socket s = null;
            try {
                s = server.accept(); // ожидание подключения клиента
                BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
                PrintStream ps = new PrintStream(s.getOutputStream());

                String command = br.readLine(); // читаем команду от клиента

                int amountcur = (int) (Math.random() * 1000);

                if ("ADD_WITHDRAW".equals(command)) {
                    // Клиент 1: случайно добавляет или снимает
                    if (Math.random() > 0.5)
                        BankApp.amount -= amountcur;
                    else
                        BankApp.amount += amountcur;
                } else if ("WITHDRAW".equals(command)) {
                    // Клиент 2: только снимает
                    BankApp.amount -= amountcur;
                }

                ps.println("Account:" + BankApp.amount); // отправляем новый баланс
                ps.flush();
                s.close();
            } catch (Exception e) {
                System.out.println("Ошибка обработки: " + e);
            }
        }
    }
}

// Поток клиента 1: добавляет или снимает деньги со счёта
class ClientThread1 extends Thread {
    BankApp app;
    String serverAddr;

    public ClientThread1(BankApp app, String serverAddr) {
        this.app = app;
        this.serverAddr = serverAddr;
    }

    public void run() {
        try {
            Socket s = new Socket(serverAddr, 3001);
            PrintStream ps = new PrintStream(s.getOutputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));

            ps.println("ADD_WITHDRAW"); // команда: добавить или снять
            ps.flush();

            String msg = br.readLine();
            if (msg != null && msg.startsWith("Account:")) {
                int newAmount = Integer.parseInt(msg.substring(8));
                app.updateBalance(newAmount, "Клиент 1");
            }
            s.close();
        } catch (Exception e) {
            System.out.println("Ошибка клиента 1: " + e);
        }
    }
}

// Поток клиента 2: только снимает деньги со счёта
class ClientThread2 extends Thread {
    BankApp app;
    String serverAddr;

    public ClientThread2(BankApp app, String serverAddr) {
        this.app = app;
        this.serverAddr = serverAddr;
    }

    public void run() {
        try {
            Socket s = new Socket(serverAddr, 3001);
            PrintStream ps = new PrintStream(s.getOutputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));

            ps.println("WITHDRAW"); // команда: только снять
            ps.flush();

            String msg = br.readLine();
            if (msg != null && msg.startsWith("Account:")) {
                int newAmount = Integer.parseInt(msg.substring(8));
                app.updateBalance(newAmount, "Клиент 2");
            }
            s.close();
        } catch (Exception e) {
            System.out.println("Ошибка клиента 2: " + e);
        }
    }
}

public class BankApp extends Frame {

    static int amount = 200; // общий счёт, разделяемый между клиентами

    TextField balanceField;
    TextField logField;
    Button btn1;
    Button btn2;
    String serverAddr;

    public BankApp(String serverAddr) {
        this.serverAddr = serverAddr;
        setTitle("Банковский счёт");
        setLayout(new FlowLayout());

        add(new Label("Текущий баланс:"));
        balanceField = new TextField("Счёт: " + amount, 25);
        balanceField.setEditable(false);
        add(balanceField);

        add(new Label("Последняя операция:"));
        logField = new TextField("—", 25);
        logField.setEditable(false);
        add(logField);

        btn1 = new Button("Клиент 1 (добавить / снять)");
        add(btn1);

        btn2 = new Button("Клиент 2 (только снять)");
        add(btn2);

        btn1.addActionListener(e -> new ClientThread1(this, serverAddr).start());
        btn2.addActionListener(e -> new ClientThread2(this, serverAddr).start());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }

    // Обновляет текстовые поля после ответа сервера
    public synchronized void updateBalance(int newAmount, String clientName) {
        balanceField.setText("Счёт: " + newAmount);
        logField.setText(clientName + " → баланс: " + newAmount);
    }

    public static void main(String[] args) {
        String serverAddr = args.length > 0 ? args[0] : "127.0.0.1";
        BankApp f = new BankApp(serverAddr);
        f.setSize(400, 220);
        f.setVisible(true);
        new AccountServer().start(); // запуск потока-сервера
    }
}
