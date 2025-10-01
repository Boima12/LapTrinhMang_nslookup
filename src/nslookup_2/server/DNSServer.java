package nslookup_2.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.SwingUtilities;

import com.google.gson.Gson;

import nslookup_2.server.common.Constants;
import nslookup_2.server.ui.ServerUI;
import nslookup_2.shared.DNSQuery;
import nslookup_2.shared.DNSResult;
import nslookup_2.shared.NetworkUtils;

public class DNSServer {
	
	private static ServerUI serverui = new ServerUI();
	
    public static void main(String[] args) {
    	int port = Constants.SERVER_PORT;
        String ipAddress = NetworkUtils.getLocalIPAddress();
        
        // show ServerUI.java UI
        SwingUtilities.invokeLater(() -> {
        	serverui.setServerInfo(ipAddress, port);
        	serverui.show(); 
        });
        
        System.out.println("DNS Server running on " + ipAddress + ":" + port);

        // Listen for any new query from Client
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            	String DNSQueryJson = in.readLine();
            	DNSQuery query = new Gson().fromJson(DNSQueryJson, DNSQuery.class);
                String domain = query.getDomain();

                serverui_query_start(domain);

                String jsonResult = DNSResolver.resolve(domain, query.getRecordTypes());
                DNSResult result = new Gson().fromJson(jsonResult, DNSResult.class);
                
                out.println(jsonResult);
                out.println("END"); // "END" signal
                
                String status = result.isSuccess() ? "SUCCESS" : "FAILED (" + result.getError() + ")";
                serverui_query_finish(domain, status);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        private void serverui_query_start(String domain) {
        	String query_task = socket.getInetAddress() + ": " + domain;
            serverui.queryList_add(query_task);
        }
        
        private void serverui_query_finish(String domain, String status) {
        	String query_task = socket.getInetAddress() + ": " + domain;
        	serverui.queryList_remove(query_task);
        	serverui.queryLog_add(socket.getInetAddress() + " - " + domain + " - " + status);
        }
    }
}
