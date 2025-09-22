# ứng dụng Java giả lập nslookup dựa trên mô hình Client Server

## Giải thích các file:
I. thư mục nslookup_1 (nslookup cơ bản, chỉ là 1 client)
1. DNSLookup.java - Nguyên mẫu code từ video https://youtu.be/AlAV8jJ1JzM?si=wPWujX5epdSCd_Mz
2. DNSLookup2.java - phát triển từ DNSLookup.java, bỏ vào Java Swing
3. DNSLookup3.java - phiên bản Java Swing từ video https://youtu.be/SpDSG09Nt8c?si=rL6Gi8qOQsRl9sJ_

II. thư mục nslookup_2 (app nslookup mô hình Client - Server)

note: thay vì sử dụng InitialDirContext như trong DNSLookup2.java, ở đây tui chuyển sang dnsjava
1. DNSLookupClient.java - file helper cho DNSLookupUI.java
2. DNSLookupUI.java - Java Swing entry file, hay còn gọi là file Client chính
3. DNSServer.java - file Server chính
4. DNSResolver.java - file logic cho DNSServer.java

> Ở thư mục 2 này ông chạy DNSServer.java để mở server trước, sau đó mới chạy DNSLookupUI.java

## hướng dẫn đọc hiểu
### 1
ông vào đọc code trong thư mục nslookup_1 trước, các file code trong đó là từ mẫu 2 video youtube bữa trước tui gửi cho ông, trong đó bao gồm làm một file Java giả lập nslookup đơn giản chỉ có Client

### 2 
ông vào đọc code trong thư mục nslookup_2, trong đó tui phát triển từ thư mục nslookup_1 để xây dựng nslookup mô hình Client - Server, ông chạy lần lượt DNSServer.java và DNSLookupUI.java

để hiểu dễ hơn thì ông có thể tự code lại cả 4 files trong nslookup_2, tui có chuẩn bị sẵn một git branch code template để ông code lại bằng cách sử dụng:
	
```
git branch
git checkout starter
```

ông có thể code lại toàn bộ 4 files, và dựa trên nhánh main để đối chiếu lại

```
git checkout main
```

sau khi ông hiểu rõ ứng dụng hiện tại, ông tạo thêm nhánh branch mới riêng ông

```
git branch Chien
git checkout Chien
```

để làm và bổ sung thêm các tính năng nâng cao hí, sau đó có chi tụi mình sẽ gộp phần code của 2 đứa lại

ứng dụng hiện tại mình chỉ đơn thuần là nslookup cực kì đơn giản, ông có thể nghiên cứu và phát triển thêm các tính năng:
- Xử lý nhiều Client cùng lúc (multi-thread hoặc non-blocking socket).
- Ghi log truy vấn (domain nào, lúc nào, IP nào).
- Cache kết quả để nếu client khác hỏi cùng domain thì trả lời nhanh.
- Hiển thị thông tin server DNS đang dùng (giống nslookup thật).


