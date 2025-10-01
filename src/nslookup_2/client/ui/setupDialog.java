package nslookup_2.client.ui;

import javax.swing.*;
import java.awt.*;

public class setupDialog {
    public static String[] askForServerInfo(Component parent) {
        return askForServerInfo(parent, "", "");
    }

    public static String[] askForServerInfo(Component parent, String defaultHost, String defaultPort) {
        JPanel panel = new JPanel(new GridLayout(2, 2, 8, 8));

        JLabel hostLabel = new JLabel("Server Host:");
        JTextField hostField = new JTextField(defaultHost, 20);

        JLabel portLabel = new JLabel("Server Port:");
        JTextField portField = new JTextField(defaultPort, 6);

        panel.add(hostLabel);
        panel.add(hostField);
        panel.add(portLabel);
        panel.add(portField);

        String[] options = {"OK", "Cancel"};
        int option = JOptionPane.showOptionDialog(
                parent,
                panel,
                "Setup Server Connection",
                JOptionPane.NO_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
        );

        if (option == 0) { // OK
            String host = hostField.getText().trim();
            String portStr = portField.getText().trim();

            if (host.isEmpty() || portStr.isEmpty()) {
                return null; // nothing entered
            }

            try {
                int port = Integer.parseInt(portStr);
                return new String[]{host, String.valueOf(port)};
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(parent,
                        "Port must be a number.",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
                return askForServerInfo(parent, defaultHost, defaultPort); // retry
            }
        } else {
            return null; // user cancelled
        }
    }
}
