package nslookup_2.client;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class DNSLookupUI {

    private JFrame frame;
    private JButton lookupBT;
    private JButton clearBT;
    private JButton multiTestBT;
    private JButton showHistoryBT;
    private JButton connectBT;
    private JTextField inputTF;
    private JTextField serverIPTF;
    private JTextField serverPortTF;
    private JTextField clientNameTF;
    private JTextArea consoleTA;

    // Persistent connection state
    private java.net.Socket persistentSocket;
    private java.io.PrintWriter persistentOut;
    private java.io.BufferedReader persistentIn;
    private boolean isConnected = false;

    // Lưu trữ danh sách domain đã lookup
    private java.util.List<String> lookupHistory = new java.util.ArrayList<>();

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                DNSLookupUI window = new DNSLookupUI();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public DNSLookupUI() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame("DNS Lookup Client - Multi-Machine");
        frame.setBounds(100, 100, 700, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        // Server connection settings
        JLabel serverLbl = new JLabel("Server IP:");
        serverLbl.setBounds(30, 20, 80, 20);
        frame.getContentPane().add(serverLbl);

        serverIPTF = new JTextField("192.168.1.100");
        serverIPTF.setBounds(120, 20, 120, 25);
        frame.getContentPane().add(serverIPTF);

        JLabel portLbl = new JLabel("Port:");
        portLbl.setBounds(250, 20, 40, 20);
        frame.getContentPane().add(portLbl);

        serverPortTF = new JTextField("5050");
        serverPortTF.setBounds(290, 20, 60, 25);
        frame.getContentPane().add(serverPortTF);

        JLabel nameLbl = new JLabel("Client Name:");
        nameLbl.setBounds(360, 20, 90, 20);
        frame.getContentPane().add(nameLbl);

        clientNameTF = new JTextField("Client-1");
        clientNameTF.setBounds(450, 20, 120, 25);
        frame.getContentPane().add(clientNameTF);

        connectBT = new JButton("Connect");
        connectBT.setBounds(580, 20, 90, 25);
        frame.getContentPane().add(connectBT);

        // Domain input
        JLabel lbl = new JLabel("Domain:");
        lbl.setBounds(30, 60, 60, 20);
        frame.getContentPane().add(lbl);

        inputTF = new JTextField();
        inputTF.setBounds(100, 60, 200, 25);
        frame.getContentPane().add(inputTF);

        lookupBT = new JButton("Lookup");
        lookupBT.setBounds(320, 60, 80, 25);
        frame.getContentPane().add(lookupBT);

        clearBT = new JButton("Clear");
        clearBT.setBounds(410, 60, 70, 25);
        frame.getContentPane().add(clearBT);

        multiTestBT = new JButton("Multi Test");
        multiTestBT.setBounds(490, 60, 90, 25);
        frame.getContentPane().add(multiTestBT);

        showHistoryBT = new JButton("History");
        showHistoryBT.setBounds(590, 60, 80, 25);
        frame.getContentPane().add(showHistoryBT);

        consoleTA = new JTextArea();
        consoleTA.setEditable(false);
        consoleTA.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(consoleTA);
        scrollPane.setBounds(30, 100, 640, 350);
        frame.getContentPane().add(scrollPane);

        // Add action listeners
        lookupBT.addActionListener(new MyActionListener());
        clearBT.addActionListener(new MyActionListener());
        multiTestBT.addActionListener(new MyActionListener());
        showHistoryBT.addActionListener(new MyActionListener());
        connectBT.addActionListener(new MyActionListener());
    }

    private class MyActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent evt) {
            if (evt.getSource() == lookupBT) {
                String input = inputTF.getText().trim();
                if (!input.isEmpty()) {
                    // Kiểm tra xem có nhiều domain không (cách nhau bởi dấu phẩy hoặc space)
                    String[] domains = input.split("[,\\s]+");

                    if (domains.length == 1) {
                        // Chỉ có 1 domain
                        consoleTA.append("Querying: " + input + "\n");
                        String response = doLookup(input);
                        consoleTA.append(response + "\n");

                        // Lưu vào history
                        if (!lookupHistory.contains(input)) {
                            lookupHistory.add(input);
                        }
                    } else {
                        // Có nhiều domain, xử lý từng domain một
                        consoleTA.append("=== Multiple Domain Query ===\n");
                        consoleTA.append("Found " + domains.length + " domains to query\n");
                        consoleTA.append("================================\n\n");

                        for (int i = 0; i < domains.length; i++) {
                            String domain = domains[i].trim();
                            if (!domain.isEmpty()) {
                                consoleTA.append("[" + (i + 1) + "/" + domains.length + "] Querying: " + domain + "\n");
                                String response = doLookup(domain);
                                consoleTA.append(response + "\n");
                                consoleTA.append("--------------------------------\n\n");

                                // Lưu vào history
                                if (!lookupHistory.contains(domain)) {
                                    lookupHistory.add(domain);
                                }
                            }
                        }
                    }
                    inputTF.setText("");
                }
            } else if (evt.getSource() == clearBT) {
                consoleTA.setText("");
                lookupHistory.clear(); // Xóa history khi clear
            } else if (evt.getSource() == multiTestBT) {
                runMultiTest();
            } else if (evt.getSource() == showHistoryBT) {
                showHistory();
            } else if (evt.getSource() == connectBT) {
                if (!isConnected) {
                    performConnect();
                } else {
                    performDisconnect();
                }
            }
        }
    }

    private void runMultiTest() {
        // Kiểm tra xem có domain nào trong history không
        if (lookupHistory.isEmpty()) {
            consoleTA.append("=== Multi-Test Error ===\n");
            consoleTA.append("No domains found in history!\n");
            consoleTA.append("Please lookup some domains first.\n");
            consoleTA.append("========================\n\n");
            return;
        }

        // Chuyển history thành array
        String[] testDomains = lookupHistory.toArray(new String[0]);

        consoleTA.append("=== Multi-Test Started ===\n");
        consoleTA.append("Testing " + testDomains.length + " domains from history:\n");
        consoleTA.append("Domains: " + String.join(", ", testDomains) + "\n");
        consoleTA.append("==========================\n\n");

        long totalStart = System.currentTimeMillis();
        int success = 0;
        int failed = 0;

        for (int i = 0; i < testDomains.length; i++) {
            String domain = testDomains[i];
            consoleTA.append("[" + (i + 1) + "/" + testDomains.length + "] Testing: " + domain + "\n");

            try {
                long start = System.currentTimeMillis();
                String result = doLookup(domain);
                long end = System.currentTimeMillis();

                if (result != null && !result.contains("Error")) {
                    success++;
                    consoleTA.append("✓ SUCCESS (" + (end - start) + "ms)\n");
                } else {
                    failed++;
                    consoleTA.append("✗ FAILED\n");
                }
            } catch (Exception e) {
                failed++;
                consoleTA.append("✗ EXCEPTION: " + e.getMessage() + "\n");
            }

            consoleTA.append("--------------------------------\n\n");
        }

        long totalEnd = System.currentTimeMillis();
        long totalTime = totalEnd - totalStart;

        consoleTA.append("=== Multi-Test Results ===\n");
        consoleTA.append("Total time: " + totalTime + "ms\n");
        consoleTA.append("Successful: " + success + "\n");
        consoleTA.append("Failed: " + failed + "\n");
        consoleTA.append("Average time: " + (totalTime / (double)testDomains.length) + "ms\n");
        consoleTA.append("==========================\n\n");
    }

    private void showHistory() {
        consoleTA.append("=== Lookup History ===\n");
        if (lookupHistory.isEmpty()) {
            consoleTA.append("No domains in history.\n");
            consoleTA.append("Lookup some domains first.\n");
        } else {
            consoleTA.append("Total domains: " + lookupHistory.size() + "\n");
            consoleTA.append("Domains:\n");
            for (int i = 0; i < lookupHistory.size(); i++) {
                consoleTA.append("  " + (i + 1) + ". " + lookupHistory.get(i) + "\n");
            }
        }
        consoleTA.append("=====================\n\n");
    }

    private void performConnect() {
        String serverIP = serverIPTF.getText().trim();
        String serverPort = serverPortTF.getText().trim();
        String clientName = clientNameTF.getText().trim();
        try {
            int port = Integer.parseInt(serverPort);
            consoleTA.append("Connecting to " + serverIP + ":" + port + "...\n");
            persistentSocket = new java.net.Socket(serverIP, port);
            persistentOut = new java.io.PrintWriter(persistentSocket.getOutputStream(), true);
            persistentIn = new java.io.BufferedReader(new java.io.InputStreamReader(persistentSocket.getInputStream()));
            // Send HELLO with client name
            persistentOut.println("HELLO:" + clientName);
            isConnected = true;
            connectBT.setText("Disconnect");
            JOptionPane.showMessageDialog(frame,
                    "Kết nối thành công tới " + serverIP + ":" + port,
                    "Kết nối thành công",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            consoleTA.append("Connection error: " + e.getMessage() + "\n");
            JOptionPane.showMessageDialog(frame,
                    "Lỗi kết nối: " + e.getMessage(),
                    "Kết nối thất bại",
                    JOptionPane.ERROR_MESSAGE);
            cleanupConnection();
        }
        consoleTA.append("--------------------------------\n\n");
    }

    private void performDisconnect() {
        try {
            if (persistentOut != null) {
                persistentOut.println("QUIT");
            }
        } catch (Exception ignored) {}
        cleanupConnection();
        JOptionPane.showMessageDialog(frame,
                "Đã ngắt kết nối",
                "Disconnected",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void cleanupConnection() {
        isConnected = false;
        connectBT.setText("Connect");
        try { if (persistentIn != null) persistentIn.close(); } catch (Exception ignored) {}
        try { if (persistentOut != null) persistentOut.close(); } catch (Exception ignored) {}
        try { if (persistentSocket != null) persistentSocket.close(); } catch (Exception ignored) {}
        persistentIn = null;
        persistentOut = null;
        persistentSocket = null;
    }

    private String doLookup(String host) {
        try {
            if (isConnected && persistentOut != null && persistentIn != null) {
                // Use persistent connection
                persistentOut.println("LOOKUP " + host);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = persistentIn.readLine()) != null) {
                    if ("END".equals(line)) break;
                    sb.append(line).append("\n");
                }
                return sb.toString();
            } else {
                // Fallback to one-shot
                String serverIP = serverIPTF.getText().trim();
                int port = Integer.parseInt(serverPortTF.getText().trim());
                DNSLookupClient client = new DNSLookupClient(serverIP, port);
                return client.lookup(host);
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
