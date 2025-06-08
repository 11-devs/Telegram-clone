package Shared.Models.Message;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "text_messages")
public class TextMessage extends Message {

    @Column(nullable = false, columnDefinition = "TEXT")
    private String textContent;

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }
}