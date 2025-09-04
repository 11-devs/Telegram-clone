package Server.Controllers;

import JSocket2.Protocol.Rpc.RpcControllerBase;
import JSocket2.Protocol.Rpc.RpcResponse;
import JSocket2.Utils.FileUtil;
import Server.DaoManager;
import Shared.Api.Models.MediaController.CreateMediaInputModel;
import Shared.Models.Account.Account;
import Shared.Models.Media.Media;
import Shared.Models.Media.MediaType;

import java.util.UUID;

public class MediaRpcController extends RpcControllerBase {
    private final DaoManager daoManager;

    public MediaRpcController(DaoManager daoManager) {
        this.daoManager = daoManager;
    }

    /**
     * Creates a database entry for a new media file before the upload begins.
     * The ID of the media entry is the same as the file transfer ID.
     * @param model Contains the ID, size, and extension of the file.
     * @return RpcResponse indicating success or failure.
     */
    public RpcResponse<Object> createMediaEntry(CreateMediaInputModel model) {
        // It's safe to proceed if the entry already exists, supports client-side retries.
        if (daoManager.getMediaDAO().findById(model.getId()) != null) {
            return Ok();
        }

        Media media = new Media();
        media.setId(model.getId()); // Use the pre-generated ID from the file transfer system
        media.setSize(model.getSize());
        media.setType(determineMediaType(model.getFileExtension()));
        // The URL is ignored as per the requirement; a conventional path is stored instead.
        media.setUrl("/files/media/" + model.getId().toString() + "." + model.getFileExtension());
        media.setMimeType(determineMediaType(model.getFileExtension()).toString());

        daoManager.getMediaDAO().save(media);

        return Ok();
    }

    /**
     * Determines the MediaType based on the file extension.
     * @param extension The file extension.
     * @return The corresponding MediaType.
     */
    private MediaType determineMediaType(String extension) {
        if (extension == null || extension.isEmpty()) {
            return MediaType.DOCUMENT;
        }
        return switch (extension.toLowerCase()) {
            case "jpg", "jpeg", "png", "gif", "bmp" -> MediaType.IMAGE;
            case "mp4", "avi", "mkv", "mov" -> MediaType.VIDEO;
            case "mp3", "wav", "flac" -> MediaType.AUDIO;
            case "sticker" -> MediaType.STICKER;
            case "voice" -> MediaType.VOICE;
            default -> MediaType.DOCUMENT;
        };
    }
}