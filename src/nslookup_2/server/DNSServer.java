package nslookup_2.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class DNSServer {
    public static void main(String[] args) {
        int port = 5050;
        System.out.println("DNS Server running on port " + port);

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

                String domain = in.readLine();
                System.out.println("Received query: " + domain);

                String result = DNSResolver.resolve(domain);

                // gửi nhiều dòng
                for (String line : result.split("\n")) {
                    out.println(line);
                }
                out.println("END"); // đánh dấu kết thúc

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
