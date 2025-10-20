package com.example.livestream_apd.config;

import com.example.livestream_apd.presentation.websocket.ChatWebSocketHandler;
// import com.example.livestream_apd.presentation.websocket.LiveStreamWebSocketHandler; // REMOVED
import com.example.livestream_apd.infrastructure.websocket.DirectMessageWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatWebSocketHandler;
    private final DirectMessageWebSocketHandler directMessageWebSocketHandler;
    // private final LiveStreamWebSocketHandler liveStreamWebSocketHandler; // REMOVED

    @Override
    public void registerWebSocketHandlers(@NonNull WebSocketHandlerRegistry registry) {
        // Chat Room WebSocket endpoint (for livestream chat)
        registry.addHandler(chatWebSocketHandler, "/ws/chat/{roomId}")
                .setAllowedOrigins("*") // Configure this properly for production
                .withSockJS(); // Enable SockJS fallback

        // Direct Message WebSocket endpoint (for 1-1 chat)
        registry.addHandler(directMessageWebSocketHandler, "/ws/direct-messages/{conversationId}")
                .setAllowedOrigins("*")
                .withSockJS();

        // LiveStream WebSocket endpoint - REMOVED
        // registry.addHandler(liveStreamWebSocketHandler, "/ws/livestream")
        //         .setAllowedOrigins("*")
        //         .withSockJS();
    }
}
