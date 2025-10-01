package nslookup_2.client;

import javax.swing.SwingUtilities;
import nslookup_2.client.ui.ClientUI;

public class DNSClient {
    public static void main(String[] args) {
        // Launch ClientUI.java
        SwingUtilities.invokeLater(() -> {
            ClientUI window = new ClientUI();
            window.display(); 
        });
    }
}
