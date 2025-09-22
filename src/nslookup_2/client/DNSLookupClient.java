package nslookup_2.client;

import java.io.*;
import java.net.Socket;

public class DNSLookupClient {
    private String serverHost;
    private int serverPort;

    public DNSLookupClient(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    public String lookup(String domain) {
        StringBuilder sb = new StringBuilder();
        try (Socket socket = new Socket(serverHost, serverPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println(domain); // gửi domain

            String line;
            while ((line = in.readLine()) != null) {
                if ("END".equals(line)) break; // server báo hết
                sb.append(line).append("\n");
            }

            return sb.toString();

        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }
}
