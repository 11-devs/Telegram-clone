package Shared.Models.Message;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "poll_messages")
public class PollMessage extends Message {

    @Column(nullable = false)
    private String question;

    @ElementCollection
    @CollectionTable(name = "poll_options", joinColumns = @JoinColumn(name = "poll_message_id"))
    @Column(name = "option_text", nullable = false)
    private List<String> options;

    @Column(name = "is_anonymous", nullable = false)
    private boolean isAnonymous;

    @Column(name = "allows_multiple_answers", nullable = false)
    private boolean isMultipleAnswersAllowed;
}
