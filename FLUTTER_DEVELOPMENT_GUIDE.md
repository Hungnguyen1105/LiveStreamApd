# 📱 TikLive Flutter Development Guide

## 🎯 Tổng quan dự án

TikLive là ứng dụng livestream tương tự TikTok với đầy đủ tính năng social media hiện đại. Guide này cung cấp hướng dẫn chi tiết để phát triển Flutter client dựa trên Spring Boot backend.

## 🏗️ Architecture Overview

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  Flutter Client │◄──►│ SpringBoot API  │◄──►│   PostgreSQL    │
│                 │    │                 │    │                 │
│  • BLoC Pattern │    │  • REST APIs    │    │  • User Data    │
│  • Clean Arch   │    │  • WebSocket    │    │  • Chat Data    │
│  • Dio HTTP     │    │  • JWT Auth     │    │  • Media Data   │
│  • WebSocket    │    │  • Redis Cache  │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
           │                        │                        │
           │            ┌─────────────────┐                  │
           └────────────┤   TencentRTC    ├──────────────────┘
                        │                 │
                        │ • Live Stream   │
                        │ • Video Call    │
                        │ • Audio/Video   │
                        └─────────────────┘
```

## 📋 Backend API Endpoints

### 🔐 Authentication APIs
```
POST   /api/auth/register              # Đăng ký tài khoản
POST   /api/auth/login                 # Đăng nhập
POST   /api/auth/refresh-token         # Refresh JWT token
POST   /api/auth/logout                # Đăng xuất
POST   /api/auth/forgot-password       # Quên mật khẩu
POST   /api/auth/reset-password        # Reset mật khẩu
POST   /api/auth/verify-email          # Xác thực email
POST   /api/auth/resend-otp           # Gửi lại OTP
POST   /api/auth/verify-otp           # Xác thực OTP
```

### 👤 User Management APIs
```
GET    /api/users/profile             # Lấy profile user hiện tại
PUT    /api/users/profile             # Cập nhật profile
GET    /api/users/{id}                # Lấy thông tin user theo ID
POST   /api/users/{id}/follow         # Follow user
DELETE /api/users/{id}/unfollow       # Unfollow user
GET    /api/users/{id}/followers      # Danh sách followers
GET    /api/users/{id}/following      # Danh sách following
POST   /api/users/search              # Tìm kiếm user
POST   /api/users/{id}/block          # Block user
DELETE /api/users/{id}/unblock        # Unblock user
```

### 💬 Direct Messages APIs
```
GET    /api/direct-messages/conversations              # Danh sách conversations
POST   /api/direct-messages/conversations              # Tạo conversation mới
GET    /api/direct-messages/{conversationId}/messages  # Lấy messages trong conversation
POST   /api/direct-messages/{conversationId}/messages  # Gửi message
PUT    /api/direct-messages/messages/{messageId}       # Edit message
DELETE /api/direct-messages/messages/{messageId}       # Delete message
POST   /api/direct-messages/messages/{messageId}/read  # Đánh dấu đã đọc
GET    /api/direct-messages/search                     # Tìm kiếm messages
POST   /api/direct-messages/{conversationId}/block     # Block conversation
DELETE /api/direct-messages/{conversationId}/block     # Unblock conversation
DELETE /api/direct-messages/{conversationId}           # Delete conversation
```

### 🎥 Livestream APIs
```
GET    /api/livestreams               # Danh sách livestreams đang live
POST   /api/livestreams               # Tạo livestream mới
GET    /api/livestreams/{id}          # Chi tiết livestream
PUT    /api/livestreams/{id}          # Cập nhật livestream
DELETE /api/livestreams/{id}          # Xóa livestream
POST   /api/livestreams/{id}/start    # Bắt đầu livestream
POST   /api/livestreams/{id}/end      # Kết thúc livestream
GET    /api/livestreams/{id}/viewers  # Danh sách viewers
POST   /api/livestreams/{id}/join     # Join xem livestream
POST   /api/livestreams/{id}/leave    # Leave livestream
```

### 🎁 Payment & Gifts APIs
```
GET    /api/payments/balance          # Lấy số dư tài khoản
POST   /api/payments/topup            # Nạp tiền
POST   /api/payments/withdraw         # Rút tiền
GET    /api/payments/transactions     # Lịch sử giao dịch
GET    /api/gifts                     # Danh sách gifts
POST   /api/gifts/send                # Tặng gift
GET    /api/gifts/received            # Gifts đã nhận
```

### 📝 Posts & Social APIs
```
GET    /api/posts                     # Danh sách posts (feed)
POST   /api/posts                     # Tạo post mới
GET    /api/posts/{id}                # Chi tiết post
PUT    /api/posts/{id}                # Cập nhật post
DELETE /api/posts/{id}                # Xóa post
POST   /api/posts/{id}/like           # Like/Unlike post
POST   /api/posts/{id}/comments       # Comment vào post
GET    /api/posts/{id}/comments       # Lấy comments của post
PUT    /api/comments/{id}             # Cập nhật comment
DELETE /api/comments/{id}             # Xóa comment
```

## 🔌 WebSocket Endpoints

### Chat WebSocket
```
Connection: ws://localhost:8080/ws/chat
Subscribe: /topic/livestream/{livestreamId}
Send: /app/chat.sendMessage
```

### Direct Messages WebSocket
```
Connection: ws://localhost:8080/ws/direct-messages
Subscribe: /user/queue/messages
Send: /app/direct.sendMessage
```

### Livestream WebSocket
```
Connection: ws://localhost:8080/ws/livestream
Subscribe: /topic/livestream/{livestreamId}
Send: /app/livestream.join
```

## 📱 Flutter Project Structure

```
tiklive_flutter/
├── lib/
│   ├── main.dart
│   ├── app.dart
│   │
│   ├── core/                         # Core utilities
│   │   ├── constants/
│   │   │   ├── api_constants.dart
│   │   │   ├── app_constants.dart
│   │   │   └── storage_keys.dart
│   │   ├── network/
│   │   │   ├── dio_client.dart
│   │   │   ├── api_result.dart
│   │   │   └── network_exceptions.dart
│   │   ├── websocket/
│   │   │   ├── websocket_client.dart
│   │   │   └── stomp_client.dart
│   │   ├── services/
│   │   │   ├── storage_service.dart
│   │   │   ├── notification_service.dart
│   │   │   └── permission_service.dart
│   │   └── utils/
│   │       ├── validators.dart
│   │       ├── formatters.dart
│   │       └── extensions.dart
│   │
│   ├── shared/                       # Shared components
│   │   ├── widgets/
│   │   ├── theme/
│   │   └── models/
│   │
│   ├── features/                     # Feature modules
│   │   ├── auth/
│   │   │   ├── data/
│   │   │   │   ├── datasources/
│   │   │   │   ├── models/
│   │   │   │   └── repositories/
│   │   │   ├── domain/
│   │   │   │   ├── entities/
│   │   │   │   ├── repositories/
│   │   │   │   └── usecases/
│   │   │   └── presentation/
│   │   │       ├── bloc/
│   │   │       ├── pages/
│   │   │       └── widgets/
│   │   │
│   │   ├── home/
│   │   ├── livestream/
│   │   ├── chat/
│   │   ├── profile/
│   │   ├── search/
│   │   ├── payment/
│   │   └── notifications/
│   │
│   ├── injection/                    # Dependency Injection
│   └── routes/                       # Navigation
│
├── assets/                           # Static assets
├── test/                            # Tests
└── pubspec.yaml                     # Dependencies
```

## 📦 Flutter Dependencies

### pubspec.yaml
```yaml
dependencies:
  flutter:
    sdk: flutter
  
  # State Management
  flutter_bloc: ^8.1.3
  bloc: ^8.1.2
  equatable: ^2.0.5
  
  # HTTP Client & API
  dio: ^5.3.2
  retrofit: ^4.0.3
  json_annotation: ^4.8.1
  pretty_dio_logger: ^1.3.1
  
  # WebSocket & Real-time
  web_socket_channel: ^2.4.0
  stomp_dart_client: ^1.0.0
  
  # TencentRTC for Livestreaming
  tencent_rtc_sdk: ^2.8.0
  
  # Local Storage
  hive: ^2.2.3
  hive_flutter: ^1.1.0
  sqflite: ^2.3.0
  shared_preferences: ^2.2.2
  
  # Firebase Services
  firebase_core: ^2.24.2
  firebase_messaging: ^14.7.10
  firebase_analytics: ^10.8.0
  firebase_crashlytics: ^3.4.8
  
  # UI & Media
  cached_network_image: ^3.3.0
  image_picker: ^1.0.4
  video_player: ^2.8.1
  permission_handler: ^11.0.1
  photo_view: ^0.14.0
  
  # Navigation
  go_router: ^12.1.3
  
  # Utilities
  get_it: ^7.6.4
  freezed_annotation: ^2.4.1
  intl: ^0.18.1
  uuid: ^4.2.1
  
dev_dependencies:
  flutter_test:
    sdk: flutter
  
  # Code Generation
  build_runner: ^2.4.7
  json_serializable: ^6.7.1
  retrofit_generator: ^8.0.4
  hive_generator: ^2.0.1
  freezed: ^2.4.6
  
  # Testing
  bloc_test: ^9.1.5
  mocktail: ^1.0.1
```

## 🔧 Core Setup

### 1. API Constants
```dart
// lib/core/constants/api_constants.dart
class ApiConstants {
  static const String baseUrl = 'http://10.0.2.2:8080/api';
  static const String websocketUrl = 'ws://10.0.2.2:8080/ws';
  
  // Auth endpoints
  static const String login = '/auth/login';
  static const String register = '/auth/register';
  static const String refreshToken = '/auth/refresh-token';
  
  // User endpoints
  static const String profile = '/users/profile';
  static const String updateProfile = '/users/profile';
  static const String followUser = '/users/{id}/follow';
  static const String unfollowUser = '/users/{id}/unfollow';
  
  // Direct Messages endpoints
  static const String conversations = '/direct-messages/conversations';
  static const String messages = '/direct-messages/{conversationId}/messages';
  static const String sendMessage = '/direct-messages/{conversationId}/messages';
  
  // Livestream endpoints
  static const String livestreams = '/livestreams';
  static const String createLivestream = '/livestreams';
  static const String joinLivestream = '/livestreams/{id}/join';
  
  // TencentRTC Config
  static const String tencentAppId = 'YOUR_TENCENT_APP_ID';
  static const String tencentSecretKey = 'YOUR_TENCENT_SECRET_KEY';
}
```

### 2. Dio HTTP Client
```dart
// lib/core/network/dio_client.dart
@singleton
class DioClient {
  late Dio _dio;
  
  DioClient() {
    _dio = Dio(BaseOptions(
      baseUrl: ApiConstants.baseUrl,
      connectTimeout: const Duration(seconds: 30),
      receiveTimeout: const Duration(seconds: 30),
      headers: {
        'Content-Type': 'application/json',
      },
    ));
    
    _dio.interceptors.addAll([
      AuthInterceptor(),
      PrettyDioLogger(
        requestHeader: true,
        requestBody: true,
        responseBody: true,
        responseHeader: false,
        error: true,
        compact: true,
      ),
    ]);
  }
  
  Dio get dio => _dio;
}

class AuthInterceptor extends Interceptor {
  @override
  void onRequest(RequestOptions options, RequestInterceptorHandler handler) {
    final token = GetIt.instance<StorageService>().getAccessToken();
    if (token != null) {
      options.headers['Authorization'] = 'Bearer $token';
    }
    handler.next(options);
  }
  
  @override
  void onError(DioException err, ErrorInterceptorHandler handler) {
    if (err.response?.statusCode == 401) {
      // Handle token refresh
      GetIt.instance<AuthBloc>().add(AuthTokenExpired());
    }
    handler.next(err);
  }
}
```

### 3. WebSocket Client
```dart
// lib/core/websocket/websocket_client.dart
@singleton
class WebSocketClient {
  StompClient? _stompClient;
  final Map<String, StreamController> _controllers = {};
  
  Future<void> connect() async {
    final token = GetIt.instance<StorageService>().getAccessToken();
    
    _stompClient = StompClient(
      config: StompConfig.sockJS(
        url: ApiConstants.websocketUrl,
        onConnect: _onConnect,
        onWebSocketError: (error) => print('WebSocket error: $error'),
        stompConnectHeaders: {
          'Authorization': 'Bearer $token',
        },
      ),
    );
    
    _stompClient!.activate();
  }
  
  void _onConnect(StompFrame frame) {
    print('WebSocket connected');
  }
  
  void subscribe(String destination, Function(String) onMessage) {
    _stompClient?.subscribe(
      destination: destination,
      callback: (frame) {
        if (frame.body != null) {
          onMessage(frame.body!);
        }
      },
    );
  }
  
  void sendMessage(String destination, String message) {
    _stompClient?.send(
      destination: destination,
      body: message,
    );
  }
  
  void disconnect() {
    _stompClient?.deactivate();
  }
}
```

## 🔐 Authentication Implementation

### 1. Auth Models
```dart
// lib/features/auth/data/models/login_request.dart
@freezed
class LoginRequest with _$LoginRequest {
  const factory LoginRequest({
    required String username,
    required String password,
    @Default(false) bool rememberMe,
  }) = _LoginRequest;
  
  factory LoginRequest.fromJson(Map<String, dynamic> json) =>
      _$LoginRequestFromJson(json);
}

// lib/features/auth/data/models/auth_response.dart
@freezed
class AuthResponse with _$AuthResponse {
  const factory AuthResponse({
    required String accessToken,
    required String refreshToken,
    required String tokenType,
    required int expiresIn,
    required UserModel user,
  }) = _AuthResponse;
  
  factory AuthResponse.fromJson(Map<String, dynamic> json) =>
      _$AuthResponseFromJson(json);
}
```

### 2. Auth API Service
```dart
// lib/features/auth/data/datasources/auth_remote_datasource.dart
@RestApi()
abstract class AuthRemoteDataSource {
  factory AuthRemoteDataSource(Dio dio) = _AuthRemoteDataSource;
  
  @POST(ApiConstants.login)
  Future<ApiResponse<AuthResponse>> login(@Body() LoginRequest request);
  
  @POST(ApiConstants.register)
  Future<ApiResponse<AuthResponse>> register(@Body() RegisterRequest request);
  
  @POST(ApiConstants.refreshToken)
  Future<ApiResponse<AuthResponse>> refreshToken(@Body() RefreshTokenRequest request);
  
  @POST('/auth/logout')
  Future<ApiResponse<String>> logout();
}
```

### 3. Auth BLoC
```dart
// lib/features/auth/presentation/bloc/auth_bloc.dart
class AuthBloc extends Bloc<AuthEvent, AuthState> {
  final AuthRepository _authRepository;
  final StorageService _storageService;
  
  AuthBloc(this._authRepository, this._storageService) : super(AuthInitial()) {
    on<AuthLoginRequested>(_onLoginRequested);
    on<AuthRegisterRequested>(_onRegisterRequested);
    on<AuthLogoutRequested>(_onLogoutRequested);
    on<AuthTokenExpired>(_onTokenExpired);
  }
  
  Future<void> _onLoginRequested(
    AuthLoginRequested event,
    Emitter<AuthState> emit,
  ) async {
    emit(AuthLoading());
    
    final result = await _authRepository.login(event.request);
    
    result.fold(
      (failure) => emit(AuthFailure(failure.message)),
      (authResponse) {
        _storageService.saveTokens(
          authResponse.accessToken,
          authResponse.refreshToken,
        );
        emit(AuthSuccess(authResponse.user));
      },
    );
  }
}
```

## 💬 Chat Implementation

### 1. Chat Models
```dart
// lib/features/chat/data/models/message_model.dart
@freezed
class MessageModel with _$MessageModel {
  const factory MessageModel({
    required String id,
    required String conversationId,
    required String senderId,
    required String content,
    required MessageType type,
    String? mediaUrl,
    String? replyToMessageId,
    required DateTime createdAt,
    DateTime? updatedAt,
    required bool isRead,
    required bool isEdited,
  }) = _MessageModel;
  
  factory MessageModel.fromJson(Map<String, dynamic> json) =>
      _$MessageModelFromJson(json);
}

enum MessageType { TEXT, IMAGE, VIDEO, AUDIO, FILE, SYSTEM }
```

### 2. Chat WebSocket Service
```dart
// lib/features/chat/data/datasources/chat_websocket_datasource.dart
@singleton
class ChatWebSocketDataSource {
  final WebSocketClient _webSocketClient;
  final StreamController<MessageModel> _messageController = StreamController.broadcast();
  
  ChatWebSocketDataSource(this._webSocketClient);
  
  Stream<MessageModel> get messageStream => _messageController.stream;
  
  void subscribeToConversation(String conversationId) {
    _webSocketClient.subscribe(
      '/user/queue/messages',
      (message) {
        final messageModel = MessageModel.fromJson(jsonDecode(message));
        _messageController.add(messageModel);
      },
    );
  }
  
  void sendMessage(SendMessageRequest request) {
    _webSocketClient.sendMessage(
      '/app/direct.sendMessage',
      jsonEncode(request.toJson()),
    );
  }
}
```

### 3. Chat BLoC
```dart
// lib/features/chat/presentation/bloc/chat_bloc.dart
class ChatBloc extends Bloc<ChatEvent, ChatState> {
  final ChatRepository _chatRepository;
  final ChatWebSocketDataSource _webSocketDataSource;
  late StreamSubscription _messageSubscription;
  
  ChatBloc(this._chatRepository, this._webSocketDataSource) : super(ChatInitial()) {
    _messageSubscription = _webSocketDataSource.messageStream.listen((message) {
      add(ChatMessageReceived(message));
    });
    
    on<ChatLoadConversations>(_onLoadConversations);
    on<ChatLoadMessages>(_onLoadMessages);
    on<ChatSendMessage>(_onSendMessage);
    on<ChatMessageReceived>(_onMessageReceived);
  }
  
  Future<void> _onSendMessage(
    ChatSendMessage event,
    Emitter<ChatState> emit,
  ) async {
    // Send via WebSocket for real-time delivery
    _webSocketDataSource.sendMessage(event.request);
    
    // Also send via HTTP API for persistence
    await _chatRepository.sendMessage(event.request);
  }
}
```

## 🎥 Livestream Implementation

### 1. TencentRTC Integration
```dart
// lib/features/livestream/data/datasources/tencent_rtc_datasource.dart
@singleton
class TencentRTCDataSource {
  TRTCCloud? _trtcCloud;
  
  Future<void> initialize() async {
    _trtcCloud = await TRTCCloud.sharedInstance();
    _trtcCloud!.registerListener(this);
  }
  
  Future<void> startLivestream(String roomId, String userId) async {
    final params = TRTCParams();
    params.sdkAppId = int.parse(ApiConstants.tencentAppId);
    params.roomId = int.parse(roomId);
    params.userId = userId;
    params.userSig = await _generateUserSig(userId);
    params.role = TRTCRoleAnchor;
    
    await _trtcCloud!.enterRoom(params, TRTCAppSceneLIVE);
    await _trtcCloud!.startLocalPreview(true, null);
    await _trtcCloud!.startLocalAudio(TRTCAudioQualityDefault);
  }
  
  Future<void> joinLivestream(String roomId, String userId) async {
    final params = TRTCParams();
    params.sdkAppId = int.parse(ApiConstants.tencentAppId);
    params.roomId = int.parse(roomId);
    params.userId = userId;
    params.userSig = await _generateUserSig(userId);
    params.role = TRTCRoleAudience;
    
    await _trtcCloud!.enterRoom(params, TRTCAppSceneLIVE);
  }
  
  Future<String> _generateUserSig(String userId) async {
    // Generate UserSig using Tencent's algorithm
    // This should be done on your server for security
    return TencentImSDKPlugin.getGenerateTestUserSig(
      sdkAppId: int.parse(ApiConstants.tencentAppId),
      secretKey: ApiConstants.tencentSecretKey,
      userId: userId,
    );
  }
}
```

### 2. Livestream BLoC
```dart
// lib/features/livestream/presentation/bloc/livestream_bloc.dart
class LivestreamBloc extends Bloc<LivestreamEvent, LivestreamState> {
  final LivestreamRepository _livestreamRepository;
  final TencentRTCDataSource _rtcDataSource;
  
  LivestreamBloc(this._livestreamRepository, this._rtcDataSource) : super(LivestreamInitial()) {
    on<LivestreamStartRequested>(_onStartRequested);
    on<LivestreamJoinRequested>(_onJoinRequested);
    on<LivestreamEndRequested>(_onEndRequested);
  }
  
  Future<void> _onStartRequested(
    LivestreamStartRequested event,
    Emitter<LivestreamState> emit,
  ) async {
    emit(LivestreamLoading());
    
    try {
      // Create livestream on server
      final result = await _livestreamRepository.createLivestream(event.request);
      
      await result.fold(
        (failure) async => emit(LivestreamFailure(failure.message)),
        (livestream) async {
          // Start TencentRTC session
          await _rtcDataSource.startLivestream(
            livestream.roomId,
            event.userId,
          );
          
          emit(LivestreamBroadcasting(livestream));
        },
      );
    } catch (e) {
      emit(LivestreamFailure(e.toString()));
    }
  }
}
```

## 🎨 UI Components

### 1. Chat Message Bubble
```dart
// lib/features/chat/presentation/widgets/message_bubble.dart
class MessageBubble extends StatelessWidget {
  final MessageModel message;
  final bool isCurrentUser;
  
  const MessageBubble({
    Key? key,
    required this.message,
    required this.isCurrentUser,
  }) : super(key: key);
  
  @override
  Widget build(BuildContext context) {
    return Align(
      alignment: isCurrentUser ? Alignment.centerRight : Alignment.centerLeft,
      child: Container(
        margin: const EdgeInsets.symmetric(vertical: 4, horizontal: 8),
        padding: const EdgeInsets.symmetric(vertical: 8, horizontal: 12),
        decoration: BoxDecoration(
          color: isCurrentUser ? Colors.blue : Colors.grey[300],
          borderRadius: BorderRadius.circular(16),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _buildMessageContent(),
            const SizedBox(height: 4),
            _buildMessageInfo(),
          ],
        ),
      ),
    );
  }
  
  Widget _buildMessageContent() {
    switch (message.type) {
      case MessageType.TEXT:
        return Text(
          message.content,
          style: TextStyle(
            color: isCurrentUser ? Colors.white : Colors.black87,
          ),
        );
      case MessageType.IMAGE:
        return CachedNetworkImage(
          imageUrl: message.mediaUrl!,
          width: 200,
          height: 200,
          fit: BoxFit.cover,
        );
      // Handle other message types...
      default:
        return Text(message.content);
    }
  }
  
  Widget _buildMessageInfo() {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Text(
          DateFormat('HH:mm').format(message.createdAt),
          style: TextStyle(
            fontSize: 10,
            color: isCurrentUser ? Colors.white70 : Colors.grey[600],
          ),
        ),
        if (message.isEdited) ...[
          const SizedBox(width: 4),
          Icon(
            Icons.edit,
            size: 10,
            color: isCurrentUser ? Colors.white70 : Colors.grey[600],
          ),
        ],
        if (isCurrentUser && message.isRead) ...[
          const SizedBox(width: 4),
          Icon(
            Icons.done_all,
            size: 12,
            color: Colors.blue[300],
          ),
        ],
      ],
    );
  }
}
```

### 2. Livestream Player Widget
```dart
// lib/features/livestream/presentation/widgets/livestream_player.dart
class LivestreamPlayer extends StatefulWidget {
  final String roomId;
  final String userId;
  final bool isBroadcaster;
  
  const LivestreamPlayer({
    Key? key,
    required this.roomId,
    required this.userId,
    required this.isBroadcaster,
  }) : super(key: key);
  
  @override
  State<LivestreamPlayer> createState() => _LivestreamPlayerState();
}

class _LivestreamPlayerState extends State<LivestreamPlayer> {
  int? _localViewId;
  int? _remoteViewId;
  
  @override
  void initState() {
    super.initState();
    _initializeTRTC();
  }
  
  Future<void> _initializeTRTC() async {
    if (widget.isBroadcaster) {
      _localViewId = await TRTCCloud.sharedInstance()?.getVideoRenderManager()?.createVideoView();
      await TRTCCloud.sharedInstance()?.startLocalPreview(true, _localViewId);
    } else {
      // Join as viewer
      final bloc = context.read<LivestreamBloc>();
      bloc.add(LivestreamJoinRequested(widget.roomId, widget.userId));
    }
  }
  
  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      height: 300,
      color: Colors.black,
      child: Stack(
        children: [
          if (_localViewId != null)
            TRTCVideoView(viewId: _localViewId!),
          if (_remoteViewId != null)
            TRTCVideoView(viewId: _remoteViewId!),
          
          // Overlay controls
          Positioned(
            bottom: 16,
            right: 16,
            child: _buildControlButtons(),
          ),
        ],
      ),
    );
  }
  
  Widget _buildControlButtons() {
    return Column(
      children: [
        if (widget.isBroadcaster) ...[
          FloatingActionButton(
            onPressed: _toggleCamera,
            child: const Icon(Icons.videocam),
          ),
          const SizedBox(height: 8),
          FloatingActionButton(
            onPressed: _toggleMicrophone,
            child: const Icon(Icons.mic),
          ),
          const SizedBox(height: 8),
          FloatingActionButton(
            onPressed: _endLivestream,
            backgroundColor: Colors.red,
            child: const Icon(Icons.call_end),
          ),
        ],
      ],
    );
  }
  
  void _toggleCamera() {
    // Implement camera toggle
  }
  
  void _toggleMicrophone() {
    // Implement microphone toggle
  }
  
  void _endLivestream() {
    context.read<LivestreamBloc>().add(LivestreamEndRequested());
  }
}
```

## 📱 Main App Setup

### 1. Dependency Injection
```dart
// lib/injection/injection_container.dart
final GetIt getIt = GetIt.instance;

Future<void> configureDependencies() async {
  // Core services
  getIt.registerSingleton<StorageService>(StorageService());
  getIt.registerSingleton<DioClient>(DioClient());
  getIt.registerSingleton<WebSocketClient>(WebSocketClient());
  
  // Data sources
  getIt.registerSingleton<AuthRemoteDataSource>(
    AuthRemoteDataSource(getIt<DioClient>().dio),
  );
  
  // Repositories
  getIt.registerSingleton<AuthRepository>(
    AuthRepositoryImpl(getIt<AuthRemoteDataSource>()),
  );
  
  // BLoCs
  getIt.registerFactory<AuthBloc>(
    () => AuthBloc(getIt<AuthRepository>(), getIt<StorageService>()),
  );
  
  getIt.registerFactory<ChatBloc>(
    () => ChatBloc(getIt<ChatRepository>(), getIt<ChatWebSocketDataSource>()),
  );
}
```

### 2. App Router
```dart
// lib/routes/app_router.dart
@AutoRouterConfig()
class AppRouter extends _$AppRouter {
  @override
  RouteType get defaultRouteType => const RouteType.adaptive();
  
  @override
  List<AutoRoute> get routes => [
    // Auth routes
    AutoRoute(
      page: LoginRoute.page,
      path: '/login',
      initial: true,
    ),
    AutoRoute(
      page: RegisterRoute.page,
      path: '/register',
    ),
    
    // Main app routes
    AutoRoute(
      page: HomeWrapperRoute.page,
      path: '/home',
      children: [
        AutoRoute(page: HomeRoute.page, path: '/feed'),
        AutoRoute(page: LivestreamListRoute.page, path: '/live'),
        AutoRoute(page: ChatListRoute.page, path: '/chat'),
        AutoRoute(page: ProfileRoute.page, path: '/profile'),
      ],
    ),
    
    // Chat routes
    AutoRoute(
      page: ChatDetailRoute.page,
      path: '/chat/:conversationId',
    ),
    
    // Livestream routes
    AutoRoute(
      page: LivestreamViewerRoute.page,
      path: '/livestream/:livestreamId',
    ),
    AutoRoute(
      page: LivestreamBroadcasterRoute.page,
      path: '/livestream/broadcast/:livestreamId',
    ),
  ];
}
```

### 3. Main App
```dart
// lib/main.dart
void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  
  // Initialize Firebase
  await Firebase.initializeApp();
  
  // Initialize dependencies
  await configureDependencies();
  
  // Initialize local storage
  await Hive.initFlutter();
  
  runApp(TikLiveApp());
}

class TikLiveApp extends StatelessWidget {
  final _appRouter = AppRouter();
  
  @override
  Widget build(BuildContext context) {
    return MultiBlocProvider(
      providers: [
        BlocProvider(create: (_) => getIt<AuthBloc>()),
        BlocProvider(create: (_) => getIt<ChatBloc>()),
        BlocProvider(create: (_) => getIt<LivestreamBloc>()),
      ],
      child: MaterialApp.router(
        title: 'TikLive',
        theme: AppTheme.lightTheme,
        darkTheme: AppTheme.darkTheme,
        routerConfig: _appRouter.config(),
      ),
    );
  }
}
```

## 🔑 Environment Configuration

### 1. Development Environment
```dart
// lib/core/config/environment_config.dart
class EnvironmentConfig {
  static const String environment = String.fromEnvironment(
    'ENVIRONMENT',
    defaultValue: 'development',
  );
  
  static bool get isDevelopment => environment == 'development';
  static bool get isProduction => environment == 'production';
  
  static String get baseUrl {
    switch (environment) {
      case 'development':
        return 'http://10.0.2.2:8080/api';
      case 'staging':
        return 'https://staging-api.tiklive.com/api';
      case 'production':
        return 'https://api.tiklive.com/api';
      default:
        return 'http://10.0.2.2:8080/api';
    }
  }
  
  static String get websocketUrl {
    switch (environment) {
      case 'development':
        return 'ws://10.0.2.2:8080/ws';
      case 'staging':
        return 'wss://staging-api.tiklive.com/ws';
      case 'production':
        return 'wss://api.tiklive.com/ws';
      default:
        return 'ws://10.0.2.2:8080/ws';
    }
  }
}
```

## 🧪 Testing

### 1. Unit Test Example
```dart
// test/features/auth/bloc/auth_bloc_test.dart
void main() {
  group('AuthBloc', () {
    late AuthBloc authBloc;
    late MockAuthRepository mockAuthRepository;
    late MockStorageService mockStorageService;
    
    setUp(() {
      mockAuthRepository = MockAuthRepository();
      mockStorageService = MockStorageService();
      authBloc = AuthBloc(mockAuthRepository, mockStorageService);
    });
    
    tearDown(() {
      authBloc.close();
    });
    
    blocTest<AuthBloc, AuthState>(
      'emits [AuthLoading, AuthSuccess] when login is successful',
      build: () {
        when(() => mockAuthRepository.login(any()))
            .thenAnswer((_) async => Right(mockAuthResponse));
        return authBloc;
      },
      act: (bloc) => bloc.add(AuthLoginRequested(mockLoginRequest)),
      expect: () => [
        AuthLoading(),
        AuthSuccess(mockUser),
      ],
      verify: (_) {
        verify(() => mockStorageService.saveTokens(any(), any())).called(1);
      },
    );
  });
}
```

## 🚀 Build & Deployment

### 1. Build Commands
```bash
# Development build
flutter build apk --debug --dart-define=ENVIRONMENT=development

# Production build
flutter build apk --release --dart-define=ENVIRONMENT=production

# iOS build
flutter build ios --release --dart-define=ENVIRONMENT=production
```

### 2. CI/CD Pipeline (GitHub Actions)
```yaml
# .github/workflows/flutter.yml
name: Flutter CI/CD

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    - uses: subosito/flutter-action@v2
      with:
        flutter-version: '3.16.0'
    - run: flutter pub get
    - run: flutter test
    - run: flutter analyze

  build:
    needs: test
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    - uses: subosito/flutter-action@v2
      with:
        flutter-version: '3.16.0'
    - run: flutter pub get
    - run: flutter build apk --release
```

## 📝 Development Guidelines

### 1. Code Style
- Sử dụng `dart format` để format code
- Follow [Effective Dart](https://dart.dev/guides/language/effective-dart) guidelines
- Sử dụng meaningful variable và function names
- Thêm comments cho complex logic

### 2. Git Workflow
- Feature branches: `feature/feature-name`
- Bug fixes: `bugfix/bug-description`
- Hotfixes: `hotfix/hotfix-description`
- Commit messages: `type(scope): description`

### 3. Testing Strategy
- Unit tests cho business logic
- Widget tests cho UI components
- Integration tests cho user flows
- Minimum 80% code coverage

## 🔧 Troubleshooting

### Common Issues

1. **WebSocket Connection Failed**
   - Kiểm tra server có đang chạy không
   - Verify WebSocket endpoint URL
   - Check network permissions

2. **TencentRTC Issues**
   - Verify AppID và SecretKey
   - Check camera/microphone permissions
   - Ensure proper UserSig generation

3. **Build Errors**
   - Run `flutter clean` và `flutter pub get`
   - Check dependencies versions
   - Verify Android/iOS configurations

## 📞 Support

- Backend API Documentation: Check Swagger at `http://localhost:8080/swagger-ui.html`
- TencentRTC Documentation: [https://cloud.tencent.com/document/product/647](https://cloud.tencent.com/document/product/647)
- Flutter Documentation: [https://flutter.dev/docs](https://flutter.dev/docs)

---

**Happy Coding! 🚀**

Dự án TikLive sẽ trở thành một ứng dụng livestream đầy đủ tính năng với architecture sạch sẽ và dễ maintain!
