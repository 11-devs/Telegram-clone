package Shared.Api.Models.MediaController;

import java.util.UUID;

public class CreateMediaInputModel {
    private UUID id;
    private long size;
    private String fileExtension;

    // No-arg constructor for JSON deserialization
    public CreateMediaInputModel() {}

    public CreateMediaInputModel(UUID id, long size, String fileExtension) {
        this.id = id;
        this.size = size;
        this.fileExtension = fileExtension;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }
}