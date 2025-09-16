package Shared.Models.Message;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "text_messages")
@PrimaryKeyJoinColumn(name = "message_id") // Specifies the join column to the base 'messages' table
public class TextMessage extends Message {

    @Column(nullable = false, columnDefinition = "TEXT")
    private String textContent;

    // --- Getters and Setters ---

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }
}