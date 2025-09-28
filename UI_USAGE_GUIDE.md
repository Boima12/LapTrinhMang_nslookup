### 1. **Single Domain Query** (Như cũ)
- Nhập: `google.com`
- Nhấn `Lookup`
- Kết quả: Hiển thị thông tin DNS của google.com

### 2. **Multiple Domain Query** (Mới!)
- Nhập: `google.com facebook.com youtube.com`
- Hoặc: `google.com,facebook.com,youtube.com`
- Nhấn `Lookup`
- Kết quả: Hiển thị thông tin DNS của từng domain riêng biệt

### 3. **Clear Button** (Mới!)
- Nhấn `Clear` để xóa toàn bộ console

### 4. **Multi Test Button** (Mới!)
- Nhấn `Multi Test` để test lại những domain đã lookup trước đó
- Chỉ test với domain trong history, không phải domain cố định
- Nếu chưa có domain nào trong history sẽ báo lỗi

### 5. **History Button** (Mới!)
- Nhấn `History` để xem danh sách domain đã lookup
- Hiển thị số lượng và danh sách domain

=== Thread Pool Status ===
Active Threads: 0          ← Số thread đang hoạt động
Pool Size: 10              ← Tổng số thread trong pool
Core Pool Size: 10         ← Số thread cố định (luôn chạy)
Max Pool Size: 50          ← Số thread tối đa có thể tạo
Queue Size: 0              ← Số task đang chờ trong queue
Completed Tasks: 79        ← Tổng số task đã hoàn thành
=========================
=== DNS Resolver Statistics ===
Total queries: 79          ← Tổng số DNS queries
Cache hits: 64             ← Số lần tìm thấy trong cache
Cache misses: 15           ← Số lần không tìm thấy trong cache
Cache hit rate: 81.01%     ← Tỷ lệ cache hit
Cached entries: 0          ← Số entries đang được cache
===============================