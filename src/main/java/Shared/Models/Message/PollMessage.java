package Shared.Models.Message;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "poll_messages")
@PrimaryKeyJoinColumn(name = "message_id") // Specifies the join column to the base 'messages' table
public class PollMessage extends Message {

    @Column(nullable = false)
    private String question;

    @ElementCollection(fetch = FetchType.EAGER) // Eager fetch is often suitable for small collections like this
    @CollectionTable(name = "poll_options", joinColumns = @JoinColumn(name = "poll_message_id"))
    @Column(name = "option_text", nullable = false)
    @OrderColumn(name = "option_order") // Good practice to maintain the order of options
    private List<String> options = new ArrayList<>();

    @Column(name = "is_anonymous", nullable = false)
    private boolean isAnonymous;

    @Column(name = "allows_multiple_answers", nullable = false)
    private boolean allowsMultipleAnswers;

    // --- Getters and Setters ---

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public boolean isAnonymous() {
        return isAnonymous;
    }

    public void setAnonymous(boolean anonymous) {
        isAnonymous = anonymous;
    }

    public boolean isMultipleAnswersAllowed() {
        return allowsMultipleAnswers;
    }

    public void setMultipleAnswersAllowed(boolean multipleAnswersAllowed) {
        this.allowsMultipleAnswers = multipleAnswersAllowed;
    }
}