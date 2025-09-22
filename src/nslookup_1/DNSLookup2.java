package nslookup_1;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;
import java.awt.Color;

// imports for DNS lookup
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class DNSLookup2 {

    private JFrame frame;
    private JTextField inputTF;
    private JLabel input_label;
    private JButton triggerBT;
    private JTextArea consoleTA;

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    DNSLookup2 window = new DNSLookup2();
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public DNSLookup2() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame();
        frame.setBounds(100, 100, 1000, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        inputTF = new JTextField();
        inputTF.setFont(new Font("Tahoma", Font.PLAIN, 14));
        inputTF.setBounds(71, 196, 250, 22);
        frame.getContentPane().add(inputTF);
        inputTF.setColumns(10);

        input_label = new JLabel("Host address/name");
        input_label.setFont(new Font("Tahoma", Font.PLAIN, 14));
        input_label.setBounds(71, 179, 179, 13);
        frame.getContentPane().add(input_label);

        triggerBT = new JButton("Find");
        triggerBT.setFont(new Font("Tahoma", Font.PLAIN, 12));
        triggerBT.setBounds(71, 224, 85, 21);
        frame.getContentPane().add(triggerBT);

        consoleTA = new JTextArea();
        consoleTA.setBorder(new LineBorder(new Color(0, 0, 0)));
        consoleTA.setFont(new Font("Tahoma", Font.PLAIN, 14));
        consoleTA.setLineWrap(true);
        consoleTA.setBounds(408, 23, 527, 511);
        frame.getContentPane().add(consoleTA);

        triggerBT.addActionListener(new MyActionListener());
    }

    private class MyActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent evt) {
            if (evt.getSource() == triggerBT) {
                String host = inputTF.getText().trim();
                if (!host.isEmpty()) {
                    consoleTA.append("nslookup " + host + "\n");
                    String result = nslookup_func1(host);  // perform DNS lookup
                    consoleTA.append(result + "\n");
                    inputTF.setText("");
                    inputTF.requestFocus(); // focus back to input
                }
            }
        }
    }

    // DNS lookup logic (from DNSLookup.java, modified to return a String)
    private String nslookup_func1(String host) {
        StringBuilder sb = new StringBuilder();
        try {
            InetAddress inetAddress;
            if (Character.isDigit(host.charAt(0))) {
                // treat as IP address
                String[] bytes = host.split("[.]");
                byte[] b = new byte[4];
                for (int i = 0; i < bytes.length; i++) {
                    b[i] = (byte) Integer.parseInt(bytes[i]);
                }
                inetAddress = InetAddress.getByAddress(b);
            } else {
                // treat as hostname
                inetAddress = InetAddress.getByName(host);
            }

            sb.append(inetAddress.getHostName())
              .append("/")
              .append(inetAddress.getHostAddress())
              .append("\n");

            InitialDirContext iDirC = new InitialDirContext();
            Attributes attributes = iDirC.getAttributes("dns:/" + inetAddress.getHostName());
            NamingEnumeration<?> attributeEnumeration = attributes.getAll();

            sb.append("-- DNS INFORMATION --\n");
            while (attributeEnumeration.hasMore()) {
                sb.append(attributeEnumeration.next()).append("\n");
            }
            attributeEnumeration.close();
        } catch (UnknownHostException e) {
            sb.append("ERROR: No Internet Address for '").append(host).append("'\n");
        } catch (NamingException e) {
            sb.append("ERROR: No DNS record for '").append(host).append("'\n");
        } catch (Exception e) {
            sb.append("Unexpected error: ").append(e.getMessage()).append("\n");
        }
        return sb.toString();
    }
}
