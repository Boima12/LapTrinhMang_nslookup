## Quy trình hoạt động hệ thống DNS Lookup (nslookup_2)

Tài liệu này mô tả rõ ràng, cụ thể, tuần tự luồng hoạt động của hệ thống từ lúc khởi động server đến khi client gửi truy vấn và server xử lý phản hồi. Các mục liệt kê file, class, method liên quan kèm mô tả nhiệm vụ.

---

### 1) Thành phần chính (file/class)

- **Server** (thư mục `src/nslookup_2/server`):
    - `DNSServer.java`: Điểm vào chính của server, nhận kết nối, quản lý thread pool, vòng lặp accept, xử lý từng client qua `ClientHandler`.
    - `DNSResolver.java`: Thực hiện tra cứu DNS nhiều bản ghi (A/AAAA/CNAME/MX/NS/TXT), có cache, thống kê.
    - `ServerUI.java`, `ServerMonitor.java`: Hiển thị và giám sát server (được gọi từ `DNSServer`).

- **Client** (thư mục `src/nslookup_2/client`):
    - `DNSLookupClient.java`: Client kiểu one-shot cho mỗi truy vấn (mỗi query mở socket mới, gửi domain, đọc đến `END`).
    - `DNSLookupUI.java`: Ứng dụng GUI client, hỗ trợ kết nối giữ-alive (persistent) và gửi nhiều lệnh `LOOKUP` qua cùng một socket; có thêm các chức năng test/histor y.
    - `MultiClientTester.java`: Công cụ benchmark tạo nhiều client đồng thời để kiểm thử hiệu năng server.

---

### 2) Giao thức ứng dụng (Client ↔ Server)

- Kết nối TCP: Client kết nối tới server cổng mặc định `5050`.
- Dòng đầu tiên có thể là một trong hai:
    - `HELLO:<clientName>`: Đăng ký tên client (tùy chọn).
    - Hoặc trực tiếp là một domain (tương thích ngược) → server coi là truy vấn ngay.
- Trong phiên làm việc (với kết nối giữ-alive):
    - `LOOKUP <domain>`: Yêu cầu tra cứu DNS.
    - `QUIT`: Kết thúc phiên.
- Server trả về nhiều dòng kết quả, và luôn kết thúc một lần phản hồi bằng dòng `END`.

---

### 3) Trình tự khởi động Server

Các điểm mã quan trọng trong `DNSServer.java`:

- `main(String[] args)`:
    - Khởi tạo cấu hình thread pool (CORE/MAX threads, queue, policy).
    - Đăng ký shutdown hook để dừng thread pool an toàn.
    - Gọi `ServerMonitor.startMonitoring();` để bắt đầu giám sát.
    - Tạo và hiển thị `ServerUI` bằng `serverUI.showUI();`.
    - Tạo `ServerSocket` tại cổng `5050` và vào vòng lặp `accept()` chờ kết nối.
    - Mỗi khi có kết nối: tăng bộ đếm, log, cập nhật UI, sau đó `submit` một `ClientHandler` vào thread pool để xử lý song song.

- Lớp lồng `ClientHandler implements Runnable`:
    - Tạo `BufferedReader/PrintWriter` từ `socket` để giao tiếp dạng dòng.
    - Đọc dòng đầu tiên:
        - Nếu bắt đầu bằng `HELLO:` → lưu `clientName`, cập nhật hiển thị UI.
        - Nếu là domain (không rỗng) → xử lý tra cứu ngay (tương thích ngược): gọi `DNSResolver.resolve(domain)` và gửi kết quả + `END`.
    - Vòng lặp lệnh: đọc từng dòng tới khi `null` hoặc `QUIT`:
        - `LOOKUP <domain>` → gọi `DNSResolver.resolve(domain)`; gửi từng dòng kết quả; kết thúc bằng `END`.
        - `HELLO:<clientName>` (đổi tên trong phiên) → cập nhật nhãn client trong UI.
        - Lệnh không hỗ trợ → gửi `Error: Unknown command` + `END`.
    - Cuối cùng: đóng socket, giảm bộ đếm kết nối đang hoạt động, cập nhật UI.

---

### 4) Xử lý tra cứu DNS (Server)

Trong `DNSResolver.java`:

- `resolve(String input)`:
    - Chuẩn hóa input, tăng tổng số truy vấn.
    - Kiểm tra cache `ConcurrentHashMap` với TTL 5 phút. Nếu còn hạn → trả về ngay (cache hit).
    - Nếu miss → gọi `performDNSResolution(input)`, sau đó lưu kết quả vào cache và trả về.

- `performDNSResolution(String input)` (điểm chính thực hiện tra cứu):
    - Dùng `InetAddress.getByName(input)` để lấy `CanonicalHostName`/IP cơ bản (nếu có).
    - Tạo context JNDI DNS (`com.sun.jndi.dns.DnsContextFactory`) và lần lượt hỏi các record: `A, AAAA, CNAME, MX, NS, TXT`.
    - Ghi nhận kết quả từng loại record; nếu truy vấn một loại thất bại hoặc không có bản ghi thì ghi chú rõ.
    - Trả chuỗi tổng hợp kết quả cho gọi bên ngoài.

- Quản lý cache và thống kê:
    - `clearCache()`, `cleanupExpiredCache()`, `printCacheStats()` để theo dõi/điều khiển khi cần.

---

### 5) Hai kiểu Client và luồng hoạt động

1) Client one-shot (`DNSLookupClient.lookup`):
    - Mỗi truy vấn:
        - Mở `Socket(host, port)`.
        - Gửi ngay 1 dòng là domain cần tra cứu.
        - Đọc từng dòng cho đến khi nhận `END`.
        - Đóng kết nối, trả về chuỗi kết quả.

2) Client GUI giữ-alive (`DNSLookupUI`):
    - Nút Connect:
        - Tạo `persistentSocket`, `persistentOut`, `persistentIn`.
        - Gửi `HELLO:<clientName>`.
    - Khi người dùng bấm Lookup:
        - Nếu đang kết nối giữ-alive: gửi `LOOKUP <domain>`, đọc kết quả đến `END` rồi hiển thị.
        - Nếu chưa kết nối: fallback về one-shot bằng `DNSLookupClient.lookup`.
    - Nút Disconnect: gửi `QUIT` (nếu có thể) và dọn dẹp tài nguyên socket/stream.
    - Tính năng khác: nhập nhiều domain cùng lúc, lưu và hiển thị lịch sử tra cứu, chạy `Multi Test` trên danh sách lịch sử.

3) Công cụ kiểm thử tải (`MultiClientTester`):
    - Nhận tham số: số client đồng thời, số query mỗi client, IP/port server và danh sách domain (nhập qua tham số hoặc prompt).
    - Tạo `ExecutorService` với `numClients` luồng, mỗi worker lặp `queriesPerClient` lần:
        - Mỗi lần tạo một `DNSLookupClient` one-shot và gọi `lookup(domain)`.
        - Ghi nhận thành công/thất bại, thời gian phản hồi.
    - Dùng `CountDownLatch` chờ tất cả hoàn thành; in thống kê tổng thời gian, số lần thành công/thất bại, thời gian trung bình, QPS.

---

### 6) Sơ đồ luồng (mô tả tuần tự rút gọn)

- Server start (`DNSServer.main`):
    1. Khởi tạo thread pool, UI, monitor.
    2. Mở `ServerSocket(5050)` và lặp `accept()`.
    3. Với mỗi `Socket` mới: tạo `ClientHandler` và chạy trong thread pool.

- Client one-shot (`DNSLookupClient.lookup`):
    1. Kết nối TCP → gửi dòng: `<domain>`.
    2. Server `ClientHandler` đọc dòng đầu: coi là domain → `DNSResolver.resolve(domain)`.
    3. Server gửi nhiều dòng kết quả + `END` → client đọc đến `END` → đóng socket.

- Client giữ-alive (`DNSLookupUI`):
    1. Connect: mở socket, gửi `HELLO:<name>`.
    2. Lookup: gửi `LOOKUP <domain>`.
    3. Server xử lý và trả nhiều dòng + `END`.
    4. Người dùng có thể tiếp tục lookup nhiều lần trong cùng kết nối; `QUIT` để kết thúc.

---

### 7) Liên hệ mã nguồn cụ thể (trích đoạn ngắn)

Các vị trí quan trọng trong mã nguồn minh họa giao thức:

```115:121:src/nslookup_2/server/DNSServer.java
                String baseInfo = socket.getInetAddress().getHostAddress();
                // Expect optional HELLO:<name> first
                socket.setSoTimeout(0);
                String line = in.readLine();
                if (line != null && line.startsWith("HELLO:")) {
```

```145:159:src/nslookup_2/server/DNSServer.java
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
```

```15:27:src/nslookup_2/client/DNSLookupClient.java
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
```

```307:316:src/nslookup_2/client/DNSLookupUI.java
            if (isConnected && persistentOut != null && persistentIn != null) {
                // Use persistent connection
                persistentOut.println("LOOKUP " + host);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = persistentIn.readLine()) != null) {
                    if ("END".equals(line)) break;
                    sb.append(line).append("\n");
                }
                return sb.toString();
```

---

### 8) Cách chạy nhanh

- Chạy server: chạy `nslookup_2.server.DNSServer` (cổng mặc định 5050).
- Chạy client GUI: chạy `nslookup_2.client.DNSLookupUI`, nhập IP server, port, kết nối và tra cứu.
- Kiểm thử tải: chạy `nslookup_2.client.MultiClientTester` với tham số tùy chọn:
    - Ví dụ: `java nslookup_2.client.MultiClientTester 50 5 127.0.0.1 5050 google.com youtube.com`.

---

### 9) Ghi chú triển khai

- Server hỗ trợ cả one-shot (tương thích ngược) và phiên giữ-alive với lệnh `LOOKUP` để tối ưu hiệu năng khi nhiều truy vấn liên tiếp.
- `DNSResolver` có cache TTL 5 phút giúp giảm độ trễ và tải hệ thống; có API dọn dẹp/thống kê.
- Thread pool trong `DNSServer` bảo đảm mở rộng tốt khi nhiều client đồng thời, có queue và policy fallback.


