package Shared.Models.Message;

import Shared.Models.Media.Media;
import jakarta.persistence.*;

@Entity
@Table(name = "media_messages")
public class MediaMessage extends Message {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_id", nullable = false)
    private Media media;

    public Media getMedia() {
        return media;
    }

    public void setMedia(Media media) {
        this.media = media;
    }
}
