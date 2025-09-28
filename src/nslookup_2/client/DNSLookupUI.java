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
    private JTextField inputTF;
    private JTextArea consoleTA;

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
        frame = new JFrame("DNS Lookup Client");
        frame.setBounds(100, 100, 600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        JLabel lbl = new JLabel("Domain:");
        lbl.setBounds(30, 30, 60, 20);
        frame.getContentPane().add(lbl);

        inputTF = new JTextField();
        inputTF.setBounds(100, 30, 200, 25);
        frame.getContentPane().add(inputTF);

        lookupBT = new JButton("Lookup");
        lookupBT.setBounds(320, 30, 80, 25);
        frame.getContentPane().add(lookupBT);

        clearBT = new JButton("Clear");
        clearBT.setBounds(410, 30, 70, 25);
        frame.getContentPane().add(clearBT);

        multiTestBT = new JButton("Multi Test");
        multiTestBT.setBounds(490, 30, 90, 25);
        frame.getContentPane().add(multiTestBT);

        showHistoryBT = new JButton("History");
        showHistoryBT.setBounds(590, 30, 80, 25);
        frame.getContentPane().add(showHistoryBT);

        consoleTA = new JTextArea();
        consoleTA.setEditable(false);
        consoleTA.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(consoleTA);
        scrollPane.setBounds(30, 80, 640, 250);
        frame.getContentPane().add(scrollPane);

        // Add action listeners
        lookupBT.addActionListener(new MyActionListener());
        clearBT.addActionListener(new MyActionListener());
        multiTestBT.addActionListener(new MyActionListener());
        showHistoryBT.addActionListener(new MyActionListener());
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
                        String response = DNSLookupClient_request(input);
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
                                String response = DNSLookupClient_request(domain);
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
                String result = DNSLookupClient_request(domain);
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

    private String DNSLookupClient_request(String host) {
        DNSLookupClient client = new DNSLookupClient("127.0.0.1", 5050);
        return client.lookup(host);
    }
}
