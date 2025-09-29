package nslookup_2.server;
import javax.swing.*;
import java.awt.*;
public class ServerUI {
    private JFrame frame;
    private DefaultListModel<String> clientsModel;
    private JList<String> clientsList;
    private JTextArea logArea;

    public ServerUI() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame("DNS Server Monitor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 500);
        frame.setLayout(new BorderLayout());

        clientsModel = new DefaultListModel<>();
        clientsList = new JList<>(clientsModel);
        logArea = new JTextArea();
        logArea.setEditable(false);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(new JLabel("Clients đang kết nối"), BorderLayout.NORTH);
        leftPanel.add(new JScrollPane(clientsList), BorderLayout.CENTER);
        leftPanel.setPreferredSize(new Dimension(260, 0));

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(new JLabel("Nhật ký server"), BorderLayout.NORTH);
        rightPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);

        frame.add(leftPanel, BorderLayout.WEST);
        frame.add(rightPanel, BorderLayout.CENTER);
    }
    public void showUI() {
        SwingUtilities.invokeLater(() -> frame.setVisible(true));
    }
    public void addClient(String clientInfo) {
        SwingUtilities.invokeLater(() -> {
            if (!clientsModel.contains(clientInfo)) {
                clientsModel.addElement(clientInfo);
            }
            appendLog("Client CONNECTED: " + clientInfo);
        });
    }
    public void removeClient(String clientInfo) {
        SwingUtilities.invokeLater(() -> {
            clientsModel.removeElement(clientInfo);
            appendLog("Client DISCONNECTED: " + clientInfo);
        });
    }
    public void renameClient(String oldInfo, String newInfo) {
        SwingUtilities.invokeLater(() -> {
            int idx = clientsModel.indexOf(oldInfo);
            if (idx >= 0) {
                clientsModel.set(idx, newInfo);
                appendLog("Client IDENTIFIED: " + newInfo);
            } else {
                // Fallback: add if old not found
                if (!clientsModel.contains(newInfo)) {
                    clientsModel.addElement(newInfo);
                }
                appendLog("Client IDENTIFIED (added): " + newInfo);
            }
        });
    }
    public void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
}
