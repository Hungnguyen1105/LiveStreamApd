Day 16 - Integration tests

Mục tiêu:
- Thêm các bài test tích hợp cho các service chính (streaming, session management)
- Kiểm tra flow end-to-end: tạo session -> bắt đầu stream -> kết thúc stream
- Chạy trên môi trường local với in-memory DB (H2) hoặc cấu hình test

Các file liên quan (gợi ý):
- src/test/java/.../IntegrationTests.java
- src/test/resources/application-test.yml

Ghi chú: Nếu bạn muốn, tôi có thể tạo test skeletons cho bạn.