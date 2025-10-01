package nslookup_2.client;

import java.io.*;
import java.net.Socket;

import com.google.gson.Gson;
import nslookup_2.shared.DNSResult;

public class DNSResolverClient {
    private String serverHost;
    private int serverPort;
    private static final Gson gson = new Gson();

    public DNSResolverClient(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    public String lookup(String domain) {
        try (
        	Socket socket = new Socket(serverHost, serverPort);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		) {
        	// send query domain to server
        	out.println(domain);
            
        	String DNSResultJson = in.readLine();
        	DNSResult result = gson.fromJson(DNSResultJson.toString(), DNSResult.class);
        	
            if (result.isSuccess()) {
                return result.getOutput();
            } else {
                return "Error: " + result.getError();
            }

        } catch (IOException e) {
            return "Client error: " + e.getMessage();
        }
    }
}
