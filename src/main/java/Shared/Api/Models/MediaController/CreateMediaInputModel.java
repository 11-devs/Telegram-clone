package Shared.Api.Models.MediaController;

import java.util.UUID;

public class CreateMediaInputModel {
    private UUID id;
    private String fileName;
    private long size;
    private String fileExtension;

    public CreateMediaInputModel(UUID id, String fileName, long size, String fileExtension) {
        this.id = id;
        this.fileName = fileName;
        this.size = size;
        this.fileExtension = fileExtension;
    }

    //<editor-fold desc="Getters and Setters">
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
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
    //</editor-fold>
}