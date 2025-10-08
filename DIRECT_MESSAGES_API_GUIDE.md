# 💬 DIRECT MESSAGES (CHAT 1-1) - HƯỚNG DẪN TEST POSTMAN

## 🔗 BASE URL
```
http://localhost:8080/api/v1/direct-messages
```

## 🔐 AUTHENTICATION
Tất cả endpoints đều yêu cầu JWT token trong header:
```
Authorization: Bearer YOUR_JWT_TOKEN
```

---

## 📁 1. CONVERSATION MANAGEMENT

### 1.1 Tạo hoặc lấy cuộc trò chuyện với user
```http
POST /conversations
Content-Type: application/json

{
  "targetUserId": 2,
  "initialMessage": "Xin chào! Bạn có khỏe không?"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Cuộc trò chuyện đã được tạo thành công",
  "data": {
    "id": 1,
    "otherUser": {
      "id": 2,
      "username": "user2",
      "fullName": "User Two",
      "avatarUrl": "https://example.com/avatar2.jpg",
      "isOnline": true,
      "isVerified": false
    },
    "lastMessage": {
      "id": 1,
      "content": "Xin chào! Bạn có khỏe không?",
      "messageType": "TEXT",
      "createdAt": "2025-07-29T10:30:00"
    },
    "unreadCount": 0,
    "isBlocked": false,
    "isBlockedByOther": false,
    "createdAt": "2025-07-29T10:30:00"
  }
}
```

### 1.2 Lấy danh sách cuộc trò chuyện
```http
GET /conversations?page=0&size=20&sortBy=lastMessageAt&sortDir=desc
```

### 1.3 Lấy cuộc trò chuyện với user cụ thể
```http
GET /conversations/with/2
```

### 1.4 Lấy thông tin cuộc trò chuyện
```http
GET /conversations/1
```

### 1.5 Xóa cuộc trò chuyện
```http
DELETE /conversations/1
```

### 1.6 Chặn cuộc trò chuyện
```http
POST /conversations/1/block
```

### 1.7 Bỏ chặn cuộc trò chuyện
```http
POST /conversations/1/unblock
```

### 1.8 Tìm kiếm cuộc trò chuyện
```http
GET /conversations/search?query=user2&page=0&size=20
```

### 1.9 Lấy cuộc trò chuyện có tin nhắn chưa đọc
```http
GET /conversations/unread?page=0&size=20
```

### 1.10 Đếm số cuộc trò chuyện chưa đọc
```http
GET /conversations/unread/count
```

### 1.11 Đánh dấu cuộc trò chuyện đã đọc
```http
POST /conversations/1/mark-read
```

---

## 💬 2. MESSAGE MANAGEMENT

### 2.1 Gửi tin nhắn text
```http
POST /messages
Content-Type: application/json

{
  "conversationId": 1,
  "content": "Chào bạn! Tôi khỏe, cảm ơn bạn đã hỏi.",
  "messageType": "TEXT"
}
```

### 2.2 Gửi tin nhắn với media
```http
POST /messages
Content-Type: application/json

{
  "conversationId": 1,
  "content": "Gửi bạn một hình ảnh",
  "messageType": "IMAGE",
  "mediaUrl": "https://example.com/image.jpg",
  "thumbnailUrl": "https://example.com/thumb.jpg",
  "mediaSize": 1024000,
  "mediaType": "image/jpeg"
}
```

### 2.3 Reply tin nhắn
```http
POST /messages
Content-Type: application/json

{
  "conversationId": 1,
  "content": "Cảm ơn bạn!",
  "messageType": "TEXT",
  "replyToMessageId": 1
}
```

### 2.4 Lấy danh sách tin nhắn trong cuộc trò chuyện
```http
GET /conversations/1/messages?page=0&size=20&sortBy=createdAt&sortDir=desc
```

### 2.5 Lấy thông tin tin nhắn
```http
GET /messages/1
```

### 2.6 Sửa tin nhắn
```http
PUT /messages
Content-Type: application/json

{
  "messageId": 1,
  "content": "Nội dung đã được sửa"
}
```

### 2.7 Xóa tin nhắn
```http
DELETE /messages/1
```

### 2.8 Đánh dấu tin nhắn đã đọc
```http
POST /messages/1/mark-read
```

### 2.9 Đánh dấu tất cả tin nhắn đã đọc
```http
POST /conversations/1/messages/mark-all-read
```

### 2.10 Tìm kiếm tin nhắn trong cuộc trò chuyện
```http
GET /conversations/1/messages/search?query=chào&page=0&size=20
```

### 2.11 Lấy tin nhắn media
```http
GET /conversations/1/messages/media?types=IMAGE,VIDEO&page=0&size=20
```

### 2.12 Lấy tin nhắn mới
```http
GET /conversations/1/messages/new?after=2025-07-29T10:30:00
```

### 2.13 Đếm tin nhắn chưa đọc
```http
GET /conversations/1/messages/unread/count
```

---

## 🔌 3. WEBSOCKET TESTING

### 3.1 Kết nối WebSocket
```javascript
// URL: ws://localhost:8080/ws/direct-messages/1?userId=123
const socket = new WebSocket('ws://localhost:8080/ws/direct-messages/1?userId=123');

socket.onopen = function(event) {
    console.log('Connected to WebSocket');
};

socket.onmessage = function(event) {
    const message = JSON.parse(event.data);
    console.log('Received:', message);
};
```

### 3.2 Gửi tin nhắn qua WebSocket
```javascript
socket.send(JSON.stringify({
    type: "SEND_MESSAGE",
    data: {
        content: "Tin nhắn realtime",
        messageType: "TEXT"
    }
}));
```

### 3.3 Báo hiệu đang gõ
```javascript
// Bắt đầu gõ
socket.send(JSON.stringify({
    type: "TYPING_START"
}));

// Dừng gõ
socket.send(JSON.stringify({
    type: "TYPING_STOP"
}));
```

### 3.4 Đánh dấu đã đọc qua WebSocket
```javascript
socket.send(JSON.stringify({
    type: "MARK_AS_READ",
    data: {
        messageId: 1
    }
}));
```

### 3.5 Ping/Pong
```javascript
socket.send(JSON.stringify({
    type: "PING"
}));
```

---

## 📋 4. TEST SCENARIOS

### Scenario 1: Tạo cuộc trò chuyện và gửi tin nhắn
1. POST `/conversations` với `targetUserId: 2`
2. POST `/messages` với `conversationId` từ step 1
3. GET `/conversations/1/messages` để xem tin nhắn

### Scenario 2: Chat realtime
1. Kết nối WebSocket cho user 1
2. Kết nối WebSocket cho user 2  
3. Gửi tin nhắn từ user 1
4. Kiểm tra user 2 nhận được tin nhắn

### Scenario 3: Quản lý tin nhắn
1. Gửi tin nhắn
2. Sửa tin nhắn
3. Xóa tin nhắn
4. Kiểm tra trạng thái

### Scenario 4: Chặn và bỏ chặn
1. POST `/conversations/1/block`
2. Thử gửi tin nhắn (sẽ fail)
3. POST `/conversations/1/unblock`
4. Gửi tin nhắn thành công

---

## ⚠️ IMPORTANT NOTES

### Rate Limiting
- Tối đa 30 tin nhắn/phút per user
- Tin nhắn tối đa 4000 ký tự

### Error Codes
- `400`: Bad Request (validation lỗi)
- `401`: Unauthorized (chưa login)
- `403`: Forbidden (không có quyền)
- `404`: Not Found (không tìm thấy)
- `429`: Too Many Requests (rate limit)

### WebSocket Events
- `CONNECTION_ESTABLISHED`: Kết nối thành công
- `NEW_MESSAGE`: Tin nhắn mới
- `MESSAGE_UPDATED`: Tin nhắn được sửa
- `MESSAGE_DELETED`: Tin nhắn bị xóa
- `MESSAGE_READ`: Tin nhắn đã đọc
- `TYPING_START/STOP`: Trạng thái gõ
- `ERROR`: Lỗi

### Performance Tips
- Sử dụng pagination cho tin nhắn cũ
- Cache conversation list
- Sử dụng WebSocket cho realtime
- Giới hạn số tin nhắn load ban đầu
