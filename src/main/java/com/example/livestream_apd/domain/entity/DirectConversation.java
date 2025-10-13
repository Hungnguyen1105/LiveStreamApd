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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "direct_conversations")
@Getter
@Setter
@ToString(exclude = {"user1", "user2", "messages"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DirectConversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user1_id", nullable = false)
    private User user1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user2_id", nullable = false)
    private User user2;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DirectMessage> messages = new ArrayList<>();

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "is_blocked_by_user1")
    @Builder.Default
    private Boolean isBlockedByUser1 = false;

    @Column(name = "is_blocked_by_user2")
    @Builder.Default
    private Boolean isBlockedByUser2 = false;

    @Column(name = "user1_deleted_at")
    private LocalDateTime user1DeletedAt;

    @Column(name = "user2_deleted_at")
    private LocalDateTime user2DeletedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper methods
    public void addMessage(DirectMessage message) {
        messages.add(message);
        message.setConversation(this);
        this.lastMessageAt = TimeUtil.nowUtc();
    }

    public void removeMessage(DirectMessage message) {
        messages.remove(message);
        message.setConversation(null);
    }

    public boolean isBlockedBy(User user) {
        if (user.getId().equals(user1.getId())) {
            return isBlockedByUser1;
        } else if (user.getId().equals(user2.getId())) {
            return isBlockedByUser2;
        }
        return false;
    }

    public void blockBy(User user) {
        if (user.getId().equals(user1.getId())) {
            this.isBlockedByUser1 = true;
        } else if (user.getId().equals(user2.getId())) {
            this.isBlockedByUser2 = true;
        }
    }

    public void unblockBy(User user) {
        if (user.getId().equals(user1.getId())) {
            this.isBlockedByUser1 = false;
        } else if (user.getId().equals(user2.getId())) {
            this.isBlockedByUser2 = false;
        }
    }

    public boolean isDeletedBy(User user) {
        if (user.getId().equals(user1.getId())) {
            return user1DeletedAt != null;
        } else if (user.getId().equals(user2.getId())) {
            return user2DeletedAt != null;
        }
        return false;
    }

    public void deleteBy(User user) {
        LocalDateTime now = TimeUtil.nowUtc();
        if (user.getId().equals(user1.getId())) {
            this.user1DeletedAt = now;
        } else if (user.getId().equals(user2.getId())) {
            this.user2DeletedAt = now;
        }
    }

    public void restoreBy(User user) {
        if (user.getId().equals(user1.getId())) {
            this.user1DeletedAt = null;
        } else if (user.getId().equals(user2.getId())) {
            this.user2DeletedAt = null;
        }
    }

    public User getOtherUser(User user) {
        if (user.getId().equals(user1.getId())) {
            return user2;
        } else if (user.getId().equals(user2.getId())) {
            return user1;
        }
        return null;
    }

    public boolean isParticipant(User user) {
        return user.getId().equals(user1.getId()) || user.getId().equals(user2.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DirectConversation that = (DirectConversation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
