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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@ToString(exclude = {"user", "chatRoom", "parentMessage", "reactions"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_message_id")
    private ChatMessage parentMessage;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type")
    @Builder.Default
    private MessageType messageType = MessageType.TEXT;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "chat_message_metadata", joinColumns = @JoinColumn(name = "message_id"))
    @MapKeyColumn(name = "meta_key")
    @Column(name = "meta_value")
    @Builder.Default
    private Map<String, String> metadata = new HashMap<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "chat_message_reactions", joinColumns = @JoinColumn(name = "message_id"))
    @MapKeyColumn(name = "reaction_type")
    @Column(name = "user_count")
    @Builder.Default
    private Map<String, Integer> reactions = new HashMap<>();

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "edited_at")
    private LocalDateTime editedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum MessageType {
        TEXT, EMOJI, GIF, STICKER, SYSTEM
    }

    // Helper methods
    public void delete() {
        this.isDeleted = true;
        this.content = "Tin nhắn đã bị xóa";
    }

    public void edit(String newContent) {
        this.content = newContent;
        this.editedAt = TimeUtil.nowUtc();
    }

    public void addReaction(String reactionType) {
        reactions.put(reactionType, reactions.getOrDefault(reactionType, 0) + 1);
    }

    public void removeReaction(String reactionType) {
        int count = reactions.getOrDefault(reactionType, 0);
        if (count > 1) {
            reactions.put(reactionType, count - 1);
        } else {
            reactions.remove(reactionType);
        }
    }

    public boolean isEdited() {
        return editedAt != null;
    }

    public boolean canEdit(User user) {
        return this.user.getId().equals(user.getId()) && !isDeleted;
    }

    public boolean canDelete(User user) {
        return this.user.getId().equals(user.getId()) || user.hasRole(Role.RoleName.MODERATOR);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatMessage that = (ChatMessage) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
