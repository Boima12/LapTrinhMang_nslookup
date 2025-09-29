package nslookup_2.client;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MultiClientTester - Tool để test khả năng xử lý nhiều client cùng lúc
 */
public class MultiClientTester {
    // Có thể thêm domain tùy chỉnh hoặc để trống để nhập từ command line
    private static String[] customDomains = {};

    private static final AtomicInteger successCount = new AtomicInteger(0);
    private static final AtomicInteger errorCount = new AtomicInteger(0);
    private static final AtomicInteger completedCount = new AtomicInteger(0);

    public static void main(String[] args) {
        int numClients = 20; // Số client đồng thời
        int queriesPerClient = 3; // Số query mỗi client
        String[] testDomains = {}; // Domains để test
        String serverIP = "127.0.0.1"; // Server IP mặc định
        int serverPort = 5050; // Server port mặc định

        // Parse arguments
        if (args.length >= 1) {
            numClients = Integer.parseInt(args[0]);
        }
        if (args.length >= 2) {
            queriesPerClient = Integer.parseInt(args[1]);
        }
        if (args.length >= 3) {
            serverIP = args[2]; // Server IP
        }
        if (args.length >= 4) {
            serverPort = Integer.parseInt(args[3]); // Server port
        }
        if (args.length >= 5) {
            // Nếu có domain từ command line
            testDomains = new String[args.length - 4];
            System.arraycopy(args, 4, testDomains, 0, args.length - 4);
        } else {
            // Nếu không có domain từ command line, hỏi user nhập
            testDomains = getDomainsFromUser();
        }

        System.out.println("=== Multi-Client DNS Test ===");
        System.out.println("Server: " + serverIP + ":" + serverPort);
        System.out.println("Clients: " + numClients);
        System.out.println("Queries per client: " + queriesPerClient);
        System.out.println("Total queries: " + (numClients * queriesPerClient));
        System.out.println("Test domains: " + java.util.Arrays.toString(testDomains));
        System.out.println("=============================");

        ExecutorService executor = Executors.newFixedThreadPool(numClients);
        CountDownLatch latch = new CountDownLatch(numClients);

        long startTime = System.currentTimeMillis();

        // Tạo và chạy các client
        for (int i = 0; i < numClients; i++) {
            final int clientId = i + 1;
            executor.submit(new ClientTestWorker(clientId, queriesPerClient, testDomains, serverIP, serverPort, latch));
        }

        // Monitor progress
        startProgressMonitor();

        try {
            // Đợi tất cả client hoàn thành
            latch.await();
            executor.shutdown();

            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;

            System.out.println("\n=== Test Results ===");
            System.out.println("Total time: " + totalTime + "ms");
            System.out.println("Successful queries: " + successCount.get());
            System.out.println("Failed queries: " + errorCount.get());
            System.out.println("Total completed: " + completedCount.get());
            System.out.println("Average time per query: " + (totalTime / (double)(numClients * queriesPerClient)) + "ms");
            System.out.println("Queries per second: " + (numClients * queriesPerClient * 1000.0 / totalTime));
            System.out.println("===================");

        } catch (InterruptedException e) {
            System.err.println("Test interrupted: " + e.getMessage());
            executor.shutdownNow();
        }
    }

    private static String[] getDomainsFromUser() {
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        System.out.println("\nEnter domains to test (separated by comma):");
        System.out.println("Example: google.com,facebook.com,youtube.com");
        System.out.print("Domains: ");

        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            // If empty, ask again
            System.out.println("Cannot be empty! Please enter at least one domain.");
            System.out.print("Domains: ");
            input = scanner.nextLine().trim();

            // If still empty, exit program
            if (input.isEmpty()) {
                System.out.println("No domains entered. Exiting program.");
                System.exit(0);
            }
        }

        String[] domains = input.split(",");
        java.util.List<String> validDomains = new java.util.ArrayList<>();

        for (int i = 0; i < domains.length; i++) {
            String domain = domains[i].trim();
            if (!domain.isEmpty()) {
                validDomains.add(domain);
            }
        }

        // Check if there are any valid domains
        if (validDomains.isEmpty()) {
            System.out.println("No valid domains entered. Exiting program.");
            System.exit(0);
        }

        return validDomains.toArray(new String[0]);
    }

    private static void startProgressMonitor() {
        Thread monitor = new Thread(() -> {
            while (completedCount.get() < successCount.get() + errorCount.get()) {
                try {
                    Thread.sleep(1000);
                    int completed = completedCount.get();
                    int success = successCount.get();
                    int errors = errorCount.get();
                    System.out.printf("[%s] Progress: %d completed (%d success, %d errors)\n",
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                            completed, success, errors);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        monitor.setDaemon(true);
        monitor.start();
    }

    private static class ClientTestWorker implements Runnable {
        private final int clientId;
        private final int queriesPerClient;
        private final String[] testDomains;
        private final String serverIP;
        private final int serverPort;
        private final CountDownLatch latch;

        public ClientTestWorker(int clientId, int queriesPerClient, String[] testDomains, String serverIP, int serverPort, CountDownLatch latch) {
            this.clientId = clientId;
            this.queriesPerClient = queriesPerClient;
            this.testDomains = testDomains;
            this.serverIP = serverIP;
            this.serverPort = serverPort;
            this.latch = latch;
        }

        @Override
        public void run() {
            try {
                for (int i = 0; i < queriesPerClient; i++) {
                    String domain = testDomains[i % testDomains.length];

                    long queryStart = System.currentTimeMillis();
                    String result = testSingleQuery(domain);
                    long queryEnd = System.currentTimeMillis();

                    if (result != null && !result.contains("Error")) {
                        successCount.incrementAndGet();
                        System.out.printf("Client #%d: %s -> %dms\n",
                                clientId, domain, (queryEnd - queryStart));
                    } else {
                        errorCount.incrementAndGet();
                        System.err.printf("Client #%d: %s -> ERROR\n", clientId, domain);
                    }

                    completedCount.incrementAndGet();

                    // Random delay để simulate real usage
                    Thread.sleep(50 + (int)(Math.random() * 100));
                }
            } catch (Exception e) {
                System.err.printf("Client #%d error: %s\n", clientId, e.getMessage());
                errorCount.addAndGet(queriesPerClient);
                completedCount.addAndGet(queriesPerClient);
            } finally {
                latch.countDown();
            }
        }

        private String testSingleQuery(String domain) {
            try {
                DNSLookupClient client = new DNSLookupClient(serverIP, serverPort);
                return client.lookup(domain);
            } catch (Exception e) {
                return "Error: " + e.getMessage();
            }
        }
    }
}
