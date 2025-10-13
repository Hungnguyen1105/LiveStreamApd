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
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "direct_messages")
@Getter
@Setter
@ToString(exclude = {"conversation", "sender", "replyToMessage"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DirectMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private DirectConversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type")
    @Builder.Default
    private MessageType messageType = MessageType.TEXT;

    @Column(name = "media_url")
    private String mediaUrl;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "media_size")
    private Long mediaSize;

    @Column(name = "media_type")
    private String mediaType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_id")
    private DirectMessage replyToMessage;

    @Column(name = "is_edited")
    @Builder.Default
    private Boolean isEdited = false;

    @Column(name = "edited_at")
    private LocalDateTime editedAt;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "is_read")
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum MessageType {
        TEXT,
        IMAGE,
        VIDEO,
        AUDIO,
        FILE,
        STICKER,
        GIF,
        VOICE,
        LOCATION,
        SYSTEM
    }

    // Helper methods
    public void markAsRead() {
        this.isRead = true;
        this.readAt = TimeUtil.nowUtc();
    }

    public void markAsEdited() {
        this.isEdited = true;
        this.editedAt = TimeUtil.nowUtc();
    }

    public void markAsDeleted() {
        this.isDeleted = true;
        this.deletedAt = TimeUtil.nowUtc();
    }

    public void restore() {
        this.isDeleted = false;
        this.deletedAt = null;
    }

    public boolean canBeEditedBy(User user) {
        return sender.getId().equals(user.getId()) && !isDeleted;
    }

    public boolean canBeDeletedBy(User user) {
        return sender.getId().equals(user.getId()) && !isDeleted;
    }

    public boolean isMediaMessage() {
        return messageType != null && 
               (messageType == MessageType.IMAGE || 
                messageType == MessageType.VIDEO || 
                messageType == MessageType.AUDIO || 
                messageType == MessageType.FILE ||
                messageType == MessageType.VOICE);
    }

    public boolean isSystemMessage() {
        return messageType == MessageType.SYSTEM;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DirectMessage that = (DirectMessage) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
