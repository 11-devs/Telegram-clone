package Shared.Models.Interaction;

import Shared.Models.Message.Message;
import jakarta.persistence.*;

@Entity
@Table(name = "views")
public class View extends Interaction {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;
}
