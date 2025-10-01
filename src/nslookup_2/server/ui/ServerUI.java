package nslookup_2.server.ui;

import java.awt.EventQueue;

import javax.swing.*;
import java.awt.Font;
import javax.swing.border.TitledBorder;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;


public class ServerUI {

	private JFrame frame;
	private DefaultListModel<String> queryModel;
	private JList<String> queryList;
	private JTextArea queryLog;
	private JLabel ServerIP;
	private JLabel ServerPort;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ServerUI window = new ServerUI();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ServerUI() {
		initialize();
	}
	
	
	// make ServerUI.java reuseable
	public void show() {
		frame.setVisible(true);
	}
	

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 800, 700);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("Proxy server");
		frame.getContentPane().setLayout(null);
		
		JPanel server_info = new JPanel();
		server_info.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		server_info.setBounds(10, 10, 766, 50);
		frame.getContentPane().add(server_info);
		server_info.setLayout(null);
		
		ServerIP = new JLabel("Server address: ");
		ServerIP.setFont(new Font("Tahoma", Font.PLAIN, 14));
		ServerIP.setBounds(10, 10, 296, 30);
		server_info.add(ServerIP);
		
		ServerPort = new JLabel("Port: ");
		ServerPort.setFont(new Font("Tahoma", Font.PLAIN, 14));
		ServerPort.setBounds(316, 10, 311, 30);
		server_info.add(ServerPort);
		
		JPanel server_main = new JPanel();
		server_main.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		server_main.setBounds(10, 70, 766, 583);
		frame.getContentPane().add(server_main);
		server_main.setLayout(null);
		
		JLabel querying_label = new JLabel("Querying");
		querying_label.setFont(new Font("Tahoma", Font.PLAIN, 14));
		querying_label.setBounds(10, 10, 194, 17);
		server_main.add(querying_label);
		
		queryModel = new DefaultListModel<>();
		queryList = new JList<>(queryModel);
		queryList.setCellRenderer(new QueryCellRenderer());	// comment this line if you don't want QueryCellRenderer style
		JScrollPane queryScroll = new JScrollPane(queryList);
		queryScroll.setBounds(10, 37, 300, 536);
		server_main.add(queryScroll);

		
		queryLog = new JTextArea();
		queryLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
		queryLog.setEditable(false);
		queryLog.setLineWrap(true);
		queryLog.setWrapStyleWord(true);
		JScrollPane logScroll = new JScrollPane(queryLog);
		logScroll.setBounds(320, 37, 436, 536);
		server_main.add(logScroll);
		
		JLabel query_log_label = new JLabel("Log");
		query_log_label.setFont(new Font("Tahoma", Font.PLAIN, 14));
		query_log_label.setBounds(320, 10, 194, 17);
		server_main.add(query_log_label);
	}
	
	public void setServerInfo(String ip, int port) {
	    ServerIP.setText("Server address: " + ip);
	    ServerPort.setText("Port: " + port);
	}
	
	public void queryList_add(String taskInfo) {
	    SwingUtilities.invokeLater(() -> {
	        if (!queryModel.contains(taskInfo)) {
	            queryModel.addElement(taskInfo);
	        }
	    });
	}

	public void queryList_remove(String taskInfo) {
	    SwingUtilities.invokeLater(() -> {
	        queryModel.removeElement(taskInfo);
	    });
	}
	
	public void queryLog_add(String taskSummaryInfo) {
	    SwingUtilities.invokeLater(() -> {
	        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
	        queryLog.append("[" + time + "] " + taskSummaryInfo + "\n");
	    });
	}
}
