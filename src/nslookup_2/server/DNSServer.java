package nslookup_2.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DNSServer {
    private static final int MAX_THREADS = 50;
    private static final int CORE_THREADS = 10;
    private static final AtomicInteger clientCounter = new AtomicInteger(0);
    private static final AtomicInteger activeConnections = new AtomicInteger(0);

    // Thread pool để quản lý client connections
    private static final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
            CORE_THREADS,
            MAX_THREADS,
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(100), // Queue size limit
            new ThreadFactory() {
                private final AtomicInteger threadNumber = new AtomicInteger(1);
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "DNS-ClientHandler-" + threadNumber.getAndIncrement());
                    t.setDaemon(false);
                    return t;
                }
            },
            new ThreadPoolExecutor.CallerRunsPolicy() // Fallback policy
    );

    private static ServerUI serverUI;

    public static void main(String[] args) {
        int port = 5050;
        System.out.println("=== DNS Server Starting ===");
        System.out.println("Port: " + port);
        System.out.println("Max Threads: " + MAX_THREADS);
        System.out.println("Core Threads: " + CORE_THREADS);
        System.out.println("=============================");

        // Shutdown hook để graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n=== Shutting down DNS Server ===");
            threadPool.shutdown();
            try {
                if (!threadPool.awaitTermination(10, TimeUnit.SECONDS)) {
                    threadPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
            }
            System.out.println("DNS Server stopped.");
        }));

        // Start monitoring
        ServerMonitor.startMonitoring();

        // Start server UI
        serverUI = new ServerUI();
        serverUI.showUI();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("DNS Server running on port " + port);
            System.out.println("Waiting for connections...\n");

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    int clientId = clientCounter.incrementAndGet();
                    int activeCount = activeConnections.incrementAndGet();

                    logConnection(clientId, clientSocket, "CONNECTED", activeCount);
                    if (serverUI != null) {
                        serverUI.addClient(clientSocket.getInetAddress().getHostAddress());
                    }

                    // Submit client handler to thread pool
                    threadPool.submit(new ClientHandler(clientSocket, clientId));

                } catch (IOException e) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void logConnection(int clientId, Socket socket, String action, int activeCount) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String clientInfo = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
        System.out.printf("[%s] Client #%d %s from %s (Active: %d)\n",
                timestamp, clientId, action, clientInfo, activeCount);
    }

    private static class ClientHandler implements Runnable {
        private final Socket socket;
        private final int clientId;
        private String clientInfo;
        private String clientName = "Unknown";

        public ClientHandler(Socket socket, int clientId) {
            this.socket = socket;
            this.clientId = clientId;
            this.clientInfo = socket.getInetAddress().getHostAddress();
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                String baseInfo = socket.getInetAddress().getHostAddress();
                // Expect optional HELLO:<name> first
                socket.setSoTimeout(0);
                String line = in.readLine();
                if (line != null && line.startsWith("HELLO:")) {
                    clientName = line.substring("HELLO:".length()).trim();
                    String newInfo = clientName.isEmpty() ? baseInfo : (clientName + " (" + baseInfo + ")");
                    if (serverUI != null) {
                        serverUI.renameClient(baseInfo, newInfo);
                    }
                    clientInfo = newInfo;
                } else if (line != null && !line.isEmpty()) {
                    // Backward compatibility: treat first line as a domain query
                    long startTime = System.currentTimeMillis();
                    String result = DNSResolver.resolve(line);
                    long endTime = System.currentTimeMillis();
                    for (String rline : result.split("\n")) {
                        out.println(rline);
                    }
                    out.println("END");
                    System.out.printf("Client #%d %s queried %s in %dms\n", clientId, baseInfo, line, (endTime - startTime));
                }

                // Command loop
                while ((line = in.readLine()) != null) {
                    if (line.equalsIgnoreCase("QUIT")) {
                        break;
                    } else if (line.startsWith("LOOKUP ")) {
                        String domain = line.substring("LOOKUP ".length()).trim();
                        if (domain.isEmpty()) {
                            out.println("Error: Empty query");
                            out.println("END");
                            continue;
                        }
                        long startTime = System.currentTimeMillis();
                        String result = DNSResolver.resolve(domain);
                        long endTime = System.currentTimeMillis();
                        for (String rline : result.split("\n")) {
                            out.println(rline);
                        }
                        out.println("END");
                        System.out.printf("Client #%d %s queried %s in %dms\n", clientId, (clientInfo == null ? baseInfo : clientInfo), domain, (endTime - startTime));
                    } else if (line.startsWith("HELLO:")) {
                        clientName = line.substring("HELLO:".length()).trim();
                        String newInfo = clientName.isEmpty() ? baseInfo : (clientName + " (" + baseInfo + ")");
                        if (serverUI != null) {
                            serverUI.renameClient((clientInfo == null ? baseInfo : clientInfo), newInfo);
                        }
                        clientInfo = newInfo;
                    } else {
                        out.println("Error: Unknown command");
                        out.println("END");
                    }
                }

            } catch (IOException e) {
                System.err.printf("Client #%d error: %s\n", clientId, e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Error closing socket for client #" + clientId);
                }
                int activeCount = activeConnections.decrementAndGet();
                logConnection(clientId, socket, "DISCONNECTED", activeCount);
                if (serverUI != null) {
                    String baseInfo = socket.getInetAddress().getHostAddress();
                    serverUI.removeClient(baseInfo);
                    if (clientInfo != null && !clientInfo.equals(baseInfo)) {
                        serverUI.removeClient(clientInfo);
                    }
                }
            }
        }
    }

    // Method để monitor thread pool status
    public static void printThreadPoolStatus() {
        System.out.println("=== Thread Pool Status ===");
        System.out.println("Active Threads: " + threadPool.getActiveCount());
        System.out.println("Pool Size: " + threadPool.getPoolSize());
        System.out.println("Core Pool Size: " + threadPool.getCorePoolSize());
        System.out.println("Max Pool Size: " + threadPool.getMaximumPoolSize());
        System.out.println("Queue Size: " + threadPool.getQueue().size());
        System.out.println("Completed Tasks: " + threadPool.getCompletedTaskCount());
        System.out.println("=========================");
    }
}
