package nslookup_1;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;

import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;
import java.awt.Color;

public class DNSLookup3 {

	private JFrame frame;
	private JTextField tracuuTF;
	private JButton tracuuBT;
	private JTextArea consoleTA;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					DNSLookup3 window = new DNSLookup3();
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
	public DNSLookup3() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setResizable(false);
		frame.setBounds(100, 100, 581, 384);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JLabel lblNewLabel = new JLabel("TÊN MIỀN");
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setBounds(10, 10, 110, 40);
		frame.getContentPane().add(lblNewLabel);
		
		tracuuTF = new JTextField();
		tracuuTF.setFont(new Font("Tahoma", Font.PLAIN, 14));
		tracuuTF.setBounds(130, 10, 300, 40);
		frame.getContentPane().add(tracuuTF);
		tracuuTF.setColumns(10);
		
		tracuuBT = new JButton("Tra cứu IP");
		tracuuBT.setFont(new Font("Tahoma", Font.PLAIN, 16));
		tracuuBT.setBounds(441, 10, 115, 40);
		frame.getContentPane().add(tracuuBT);
		
		consoleTA = new JTextArea();
		consoleTA.setFont(new Font("Monospaced", Font.PLAIN, 12));
		consoleTA.setBackground(new Color(255, 255, 255));
		consoleTA.setEditable(false);
		consoleTA.setBounds(10, 60, 546, 280);
		frame.getContentPane().add(consoleTA);
		
		tracuuBT.addActionListener(new MyActionListener());
	}
	
    private class MyActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent evt) {
        	if (evt.getSource() == tracuuBT) {
        		String host = tracuuTF.getText().trim();
        		if (!host.isEmpty()) {
        			consoleTA.append("nslookup " + host + "\n");
        			
        			String result = nslookup_func1(host);
        			consoleTA.append(result + "\n");
        			
        			tracuuTF.setText("");
        		}
        	}
        }
    }
	
    private String nslookup_func1(String host) {
    	StringBuilder sb = new StringBuilder();
    	
    	try {
    		InetAddress inetAddress;
    		
    		inetAddress = InetAddress.getByName(host); 
            sb.append(inetAddress.getHostName())
            .append("/")
            .append(inetAddress.getHostAddress())
            .append("\n");
    	} catch (Exception e) {
    		consoleTA.append("Không tìm thấy địa chỉ IP của tên miền!\n");
    		consoleTA.append("note: chỉ hỗ trợ tên miền dạng chuỗi!\n");
    	}
    	
    	return sb.toString();
    }
}
