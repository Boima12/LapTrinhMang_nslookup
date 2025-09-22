package nslookup_2.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class DNSServer {
    public static void main(String[] args) {

    }

    private static class ClientHandler implements Runnable {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {

        }
    }
}
