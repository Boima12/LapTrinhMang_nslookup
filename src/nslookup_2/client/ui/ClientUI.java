package nslookup_2.client.ui;

import javax.swing.*;
import nslookup_2.client.DNSResolverClient;
import nslookup_2.client.common.HistoryRecord;

import java.awt.Color;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ClientUI extends JFrame {
	private static final long serialVersionUID = 1L;
	private JButton lookupBT;
    private JTextField inputTF;
    private JTextArea consoleTA;
    private JLabel serverInfo;
    private String[] serverData;
    private String serverHost;
    private int serverPort;
    private List<HistoryRecord> queryHistory = new ArrayList<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                ClientUI window = new ClientUI();
                window.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public ClientUI() {
        initialize();
    }
    
    public void display() { 
    	setVisible(true); 
    }

    private void initialize() {
        setTitle("DNS Lookup Client");
        setBounds(100, 100, 600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(null);
        
        // JmenuBar
        JMenuBar menuBar = new JMenuBar();
        JMenu menuHome = new JMenu("Home");
        menuBar.add(menuHome);

        JMenuItem menuItemChangeDestination = new JMenuItem("Change Destination");
        menuItemChangeDestination.addActionListener(e -> {
            String[] newData = setupDialog.askForServerInfo(this,
                    serverHost == null ? "" : serverHost,
                    serverPort == 0 ? "" : String.valueOf(serverPort));
            if (newData != null) {
                serverHost = newData[0];
                serverPort = Integer.parseInt(newData[1]);
                serverInfo.setText("Server destination: " + serverHost + ":" + serverPort);
            }
        });
        menuHome.add(menuItemChangeDestination);
        
        JMenuItem menuHistory = new JMenuItem("History");
        menuHistory.addActionListener(e -> {
            historyDialog();
        });
        menuHome.add(menuHistory);
        
        JMenuItem menuExit = new JMenuItem("Exit");
        menuExit.addActionListener(e -> {
            System.exit(0);
        });
        menuHome.add(menuExit);
        setJMenuBar(menuBar);
        
        // Main content
        JLabel lbl = new JLabel("Domain:");
        lbl.setBounds(30, 45, 60, 20);
        getContentPane().add(lbl);

        inputTF = new JTextField();
        inputTF.setBounds(100, 45, 340, 25);
        getContentPane().add(inputTF);

        lookupBT = new JButton("Lookup");
        lookupBT.setBounds(450, 43, 100, 25);
        getContentPane().add(lookupBT);

        consoleTA = new JTextArea();
        consoleTA.setEditable(false);
        consoleTA.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(consoleTA);
        scrollPane.setBounds(30, 80, 520, 250);
        getContentPane().add(scrollPane);
        
        serverInfo = new JLabel("Server destination: ");
        serverInfo.setForeground(new Color(123, 123, 123));
        serverInfo.setBounds(29, 22, 459, 13);
        getContentPane().add(serverInfo);

        inputTF.addActionListener(e -> sendQuery());
        lookupBT.addActionListener(e -> sendQuery());
        
        // open setupDialog.java to ask for serverHost and serverPost values.
        serverData = setupDialog.askForServerInfo(this);
        if (serverData != null) {
            serverHost = serverData[0];
            serverPort = Integer.parseInt(serverData[1]);
            serverInfo.setText("Server destination: " + serverHost + ":" + serverPort);
        }
    }

    private void sendQuery() {
        String host = inputTF.getText().trim();
        if (!host.isEmpty()) {
            consoleTA.append("Querying: " + host + "\n");
            
            String response = DNSLookupClient_request(host);
            consoleTA.append(response + "\n");
            inputTF.setText("");
            
            // add to history list
            String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            queryHistory.add(new HistoryRecord(time, host));
        }
    }

    private String DNSLookupClient_request(String host) {
        DNSResolverClient client = new DNSResolverClient(serverHost, serverPort);
        return client.lookup(host);
    }
    
    private void historyDialog() {
        HistoryRecord selected = historyDialog.showHistory(this, queryHistory);
        if (selected != null) {
            inputTF.setText(selected.getQuery());  // only put query into inputTF
        }
    }
}
