package Shared.Models.Message;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "system_messages")
@PrimaryKeyJoinColumn(name = "message_id") // Specifies the join column to the base 'messages' table
public class SystemMessage extends Message {

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // --- Getters and Setters ---

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}