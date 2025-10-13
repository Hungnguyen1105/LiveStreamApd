package com.example.livestream_apd.domain.entity;

import com.example.livestream_apd.utils.TimeUtil;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Entity
@Table(name = "chat_rooms")
@Getter
@Setter
@ToString(exclude = {"messages"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "livestream_id")
    private Long livestreamId;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type")
    @Builder.Default
    private RoomType roomType = RoomType.LIVESTREAM;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "chat_room_settings", joinColumns = @JoinColumn(name = "room_id"))
    @MapKeyColumn(name = "setting_key")
    @Column(name = "setting_value")
    @Builder.Default
    private Map<String, String> settings = new HashMap<>();

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ChatMessage> messages = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum RoomType {
        LIVESTREAM, PRIVATE, GROUP
    }

    // Helper methods
    public void addMessage(ChatMessage message) {
        messages.add(message);
        message.setChatRoom(this);
    }

    public void removeMessage(ChatMessage message) {
        messages.remove(message);
        message.setChatRoom(null);
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatRoom chatRoom = (ChatRoom) o;
        return Objects.equals(id, chatRoom.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
