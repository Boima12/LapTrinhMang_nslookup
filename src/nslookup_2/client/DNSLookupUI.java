package nslookup_2.client;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class DNSLookupUI {

    private JFrame frame;
    private JButton lookupBT;
    private JTextField inputTF;
    private JTextArea consoleTA;

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
        lookupBT.setBounds(320, 30, 100, 25);
        frame.getContentPane().add(lookupBT);

        consoleTA = new JTextArea();
        consoleTA.setEditable(false);
        consoleTA.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(consoleTA);
        scrollPane.setBounds(30, 80, 520, 250);
        frame.getContentPane().add(scrollPane);

        lookupBT.addActionListener(new MyActionListener());
    }

    private class MyActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent evt) {

        }
    }

    private String DNSLookupClient_request(String host) {
        return "";
    }
}
