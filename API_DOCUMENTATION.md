# API Documentation - TikLive Platform

## 📝 Table of Contents
- [Introduction](#introduction)
- [Base URLs](#base-urls)
- [Authentication](#authentication)
- [API Endpoints](#api-endpoints)
  - [Authentication](#authentication-api)
  - [User Management](#user-management)
  - [Direct Messages](#direct-messages)
  - [Chat](#chat)
  - [Payment & Wallet](#payment--wallet)
  - [Post](#post)
  - [WebSocket Endpoints](#websocket-endpoints)
- [Response Format](#response-format)
- [Error Codes](#error-codes)
- [Rate Limits](#rate-limits)

## Introduction
This document provides a comprehensive guide to the TikLive Platform API. The API allows clients to interact with the TikLive backend services including authentication, user management, chat, direct messaging, payments, and more.

## Base URLs
- **Development**: `http://localhost:8080/api/v1`
- **Production**: TBD

## Authentication
Most endpoints require authentication using JWT tokens. Include the token in the Authorization header:
```
Authorization: Bearer YOUR_JWT_TOKEN
```

## API Endpoints

### Authentication API
Base path: `/auth`

#### Register
```http
POST /auth/register
Content-Type: application/json

{
  "username": "string",
  "email": "string",
  "password": "string",
  "fullName": "string"
}
```
**Response**: User information

#### Login
```http
POST /auth/login
Content-Type: application/json

{
  "username": "string",
  "password": "string"
}
```
**Response**: Authentication tokens (access and refresh tokens)

#### Verify Email
```http
POST /auth/verify-email
Content-Type: application/json

{
  "token": "string",
  "otp": "string"
}
```
**Response**: Success message

#### Forgot Password
```http
POST /auth/forgot-password
Content-Type: application/json

{
  "email": "string"
}
```
**Response**: Success message with instructions

#### Reset Password
```http
POST /auth/reset-password
Content-Type: application/json

{
  "token": "string",
  "password": "string",
  "confirmPassword": "string"
}
```
**Response**: Success message

#### Resend Verification Email
```http
POST /auth/resend-verification
Content-Type: application/json

{
  "email": "string"
}
```
**Response**: Success message

#### Logout
```http
POST /auth/logout
Content-Type: application/json

{
  "refreshToken": "string"
}
```
**Response**: Success message

#### Health Check
```http
GET /auth/health
```
**Response**: Service status

---

### User Management
Base path: `/users`

#### Get Current User Profile
```http
GET /users/profile
```
**Auth Required**: Yes  
**Response**: User profile information

#### Update Profile
```http
PUT /users/profile
Content-Type: application/json

{
  "fullName": "string",
  "bio": "string",
  "avatarUrl": "string",
  // Other profile fields
}
```
**Auth Required**: Yes  
**Response**: Updated user profile

#### Get User by ID
```http
GET /users/{id}
```
**Auth Required**: Yes  
**Response**: User information

#### Follow User
```http
POST /users/{id}/follow
```
**Auth Required**: Yes  
**Response**: Success message

#### Unfollow User
```http
DELETE /users/{id}/follow
```
**Auth Required**: Yes  
**Response**: Success message

#### Get User Followers
```http
GET /users/{id}/followers?page=0&size=10
```
**Auth Required**: Yes  
**Response**: Paginated list of followers

#### Get User Following
```http
GET /users/{id}/following?page=0&size=10
```
**Auth Required**: Yes  
**Response**: Paginated list of users being followed

#### Search Users
```http
GET /users/search?query=string&page=0&size=10
```
**Auth Required**: Yes  
**Response**: Paginated search results

#### Block User
```http
POST /users/block/{id}
```
**Auth Required**: Yes  
**Response**: Success message

#### Unblock User
```http
DELETE /users/block/{id}
```
**Auth Required**: Yes  
**Response**: Success message

---

### Direct Messages
Base path: `/api/v1/direct-messages`

#### Conversation Management

##### Create or Get Conversation
```http
POST /conversations
Content-Type: application/json

{
  "targetUserId": 123,
  "initialMessage": "Hello there!"
}
```
**Auth Required**: Yes  
**Response**: Conversation details with initial message

##### Get Conversations
```http
GET /conversations?page=0&size=20&sortBy=lastMessageAt&sortDir=desc
```
**Auth Required**: Yes  
**Response**: Paginated list of conversations

##### Get Conversation by ID
```http
GET /conversations/{conversationId}
```
**Auth Required**: Yes  
**Response**: Conversation details

##### Get Conversation with User
```http
GET /conversations/with/{userId}
```
**Auth Required**: Yes  
**Response**: Conversation details

##### Delete Conversation
```http
DELETE /conversations/{conversationId}
```
**Auth Required**: Yes  
**Response**: Success message

##### Block Conversation
```http
POST /conversations/{conversationId}/block
```
**Auth Required**: Yes  
**Response**: Success message

##### Unblock Conversation
```http
POST /conversations/{conversationId}/unblock
```
**Auth Required**: Yes  
**Response**: Success message

##### Search Conversations
```http
GET /conversations/search?query=string&page=0&size=20
```
**Auth Required**: Yes  
**Response**: Paginated search results

##### Get Unread Conversations
```http
GET /conversations/unread?page=0&size=20
```
**Auth Required**: Yes  
**Response**: Paginated list of conversations with unread messages

##### Get Unread Conversations Count
```http
GET /conversations/unread/count
```
**Auth Required**: Yes  
**Response**: Count of conversations with unread messages

##### Mark Conversation as Read
```http
POST /conversations/{conversationId}/mark-read
```
**Auth Required**: Yes  
**Response**: Success message

#### Message Management

##### Send Message
```http
POST /messages
Content-Type: application/json

{
  "conversationId": 123,
  "content": "Hello!",
  "messageType": "TEXT"  // TEXT, IMAGE, VIDEO, AUDIO, FILE
}
```
**Auth Required**: Yes  
**Response**: Message details

##### Send Media Message
```http
POST /messages
Content-Type: application/json

{
  "conversationId": 123,
  "content": "Check this out!",
  "messageType": "IMAGE",
  "mediaUrl": "https://example.com/image.jpg",
  "thumbnailUrl": "https://example.com/thumb.jpg",
  "mediaSize": 1024000,
  "mediaType": "image/jpeg"
}
```
**Auth Required**: Yes  
**Response**: Message details

##### Reply to Message
```http
POST /messages
Content-Type: application/json

{
  "conversationId": 123,
  "content": "I agree!",
  "messageType": "TEXT",
  "replyToMessageId": 456
}
```
**Auth Required**: Yes  
**Response**: Message details

##### Get Messages in Conversation
```http
GET /conversations/{conversationId}/messages?page=0&size=20&sortBy=createdAt&sortDir=desc
```
**Auth Required**: Yes  
**Response**: Paginated list of messages

##### Get Message by ID
```http
GET /messages/{messageId}
```
**Auth Required**: Yes  
**Response**: Message details

##### Edit Message
```http
PUT /messages
Content-Type: application/json

{
  "messageId": 123,
  "content": "Updated content"
}
```
**Auth Required**: Yes  
**Response**: Updated message details

##### Delete Message
```http
DELETE /messages/{messageId}
```
**Auth Required**: Yes  
**Response**: Success message

##### Mark Message as Read
```http
POST /messages/{messageId}/mark-read
```
**Auth Required**: Yes  
**Response**: Success message

##### Mark All Messages as Read
```http
POST /conversations/{conversationId}/messages/mark-all-read
```
**Auth Required**: Yes  
**Response**: Success message

##### Search Messages
```http
GET /conversations/{conversationId}/messages/search?query=string&page=0&size=20
```
**Auth Required**: Yes  
**Response**: Paginated search results

##### Get Media Messages
```http
GET /conversations/{conversationId}/messages/media?types=IMAGE,VIDEO&page=0&size=20
```
**Auth Required**: Yes  
**Response**: Paginated list of media messages

##### Get New Messages
```http
GET /conversations/{conversationId}/messages/new?after=2023-07-29T10:30:00
```
**Auth Required**: Yes  
**Response**: List of new messages after specified time

##### Get Unread Messages Count
```http
GET /conversations/{conversationId}/messages/unread/count
```
**Auth Required**: Yes  
**Response**: Count of unread messages

---

### Chat
Base path: `/api/v1/chat`

#### Get Chat Rooms
```http
GET /rooms?page=0&size=20&sortBy=createdAt&sortDir=desc
```
**Auth Required**: No  
**Response**: Paginated list of chat rooms

#### Create Chat Room
```http
POST /rooms
Content-Type: application/json

{
  "name": "string",
  "description": "string",
  "isPrivate": boolean
}
```
**Auth Required**: Yes (MODERATOR or ADMIN role)  
**Response**: Created chat room details

#### Get Messages in Room
```http
GET /rooms/{id}/messages?page=0&size=50
```
**Auth Required**: No  
**Response**: Paginated list of messages in the room

#### Send Message to Room
```http
POST /rooms/{id}/messages
Content-Type: application/json

{
  "content": "string",
  "messageType": "TEXT"
}
```
**Auth Required**: Yes  
**Response**: Message details

#### Direct Messages (Deprecated)
```http
GET /direct/{userId}
```
**Auth Required**: Yes  
**Response**: Redirection to Direct Messages API

#### Send Typing Indicator
```http
POST /typing
Content-Type: application/json

{
  "roomId": "number",
  "isTyping": boolean
}
```
**Auth Required**: Yes  
**Response**: Success message

#### Search Messages in Room
```http
GET /rooms/{id}/search?keyword=string&page=0&size=20
```
**Auth Required**: Yes  
**Response**: Paginated search results

---

### Payment & Wallet
Base path: `/payments`

#### Get Balance
```http
GET /balance
```
**Auth Required**: Yes  
**Response**: Balance information

#### Top-up Account
```http
POST /topup
Content-Type: application/json

{
  "amount": "number",
  "paymentMethod": "string",
  "returnUrl": "string"
}
```
**Auth Required**: Yes  
**Response**: Payment URL and transaction details

#### Withdraw Funds
```http
POST /withdraw
Content-Type: application/json

{
  "amount": "number",
  "bankAccount": "string",
  "bankName": "string",
  "accountName": "string"
}
```
**Auth Required**: Yes  
**Response**: Transaction details

#### Get Transactions
```http
GET /transactions?page=0&size=10&sortBy=createdAt&sortDir=desc&type=string&status=string&startDate=string&endDate=string&paymentMethod=string
```
**Auth Required**: Yes  
**Response**: Paginated list of transactions

#### Get Payment Statistics
```http
GET /statistics?startDate=2024-01-01&endDate=2024-12-31
```
**Auth Required**: Yes  
**Response**: Payment statistics for the given period

#### VNPay Callback
```http
GET/POST /vnpay/callback
```
**Auth Required**: No  
**Response**: Callback processing result

#### Get Payment Methods
```http
GET /methods
```
**Auth Required**: No  
**Response**: Available payment methods

---

### Post
Base path: `/posts`

#### Get Timeline Posts
```http
GET /posts?page=0&size=10
```
**Auth Required**: Optional  
**Response**: Paginated list of posts in timeline

#### Create Post
```http
POST /posts
Content-Type: application/json

{
  "content": "string",
  "mediaUrls": ["string"],
  "tags": ["string"]
}
```
**Auth Required**: Yes  
**Response**: Created post details

---

### WebSocket Endpoints

#### Chat WebSocket
```
Connection: ws://localhost:8080/ws/chat
Subscribe: /topic/livestream/{livestreamId}
Send: /app/chat.sendMessage
```

#### Direct Messages WebSocket
```
Connection: ws://localhost:8080/ws/direct-messages
Subscribe: /user/queue/messages
Send: /app/direct.sendMessage
```

#### Livestream WebSocket
```
Connection: ws://localhost:8080/ws/livestream
Subscribe: /topic/livestream/{livestreamId}
Send: /app/livestream.join
```

## Response Format
All endpoints return data in the following format:
```json
{
  "success": boolean,
  "message": "string",
  "data": object
}
```

For paginated responses:
```json
{
  "success": boolean,
  "message": "string",
  "data": {
    "content": [array of items],
    "page": number,
    "size": number,
    "totalElements": number,
    "totalPages": number,
    "first": boolean,
    "last": boolean
  }
}
```

## Error Codes
- `400`: Bad Request (validation errors)
- `401`: Unauthorized (not logged in)
- `403`: Forbidden (insufficient permissions)
- `404`: Not Found (resource not found)
- `429`: Too Many Requests (rate limit exceeded)
- `500`: Internal Server Error

## Rate Limits
- Authentication: 5 attempts per minute
- Direct Messages: 30 messages per minute per user
- API calls: 100 requests per minute per IP
