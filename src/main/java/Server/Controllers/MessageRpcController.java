package Server.Controllers;

import JSocket2.Protocol.Rpc.RpcControllerBase;
import JSocket2.Protocol.Rpc.RpcResponse;
import Server.DaoManager;
import Shared.Api.Models.MessageController.*;
import Shared.Models.Account.Account;
import Shared.Models.Chat.Chat;
import Shared.Models.Media.Media;
import Shared.Models.Message.MediaMessage;
import Shared.Models.Message.Message;
import Shared.Models.Message.MessageType;
import Shared.Models.Message.TextMessage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter; // Import for formatting
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class MessageRpcController extends RpcControllerBase {
    private final DaoManager daoManager;

    public MessageRpcController(DaoManager daoManager) {
        this.daoManager = daoManager;
    }


    private GetMessageOutputModel mapMessageToOutputModel(Message message) {
        if (message == null) {
            return null;
        }

        GetMessageOutputModel output = new GetMessageOutputModel();
        output.setMessageId(message.getId());
        if (message.getSender() != null) {
            output.setSenderId(message.getSender().getId());
            String senderName = message.getSender().getFirstName() +
                    (message.getSender().getLastName() != null ? " " + message.getSender().getLastName() : "");
            output.setSenderName(senderName.trim());
        }
        if (message.getChat() != null) {
            output.setChatId(message.getChat().getId());
        }
        // Convert LocalDateTime to String for the output model
        output.setTimestamp(message.getTimestamp() != null ? message.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
        output.setEdited(message.isEdited());
        output.setMessageType(message.getType());
        output.setOutgoing(Objects.equals(output.getSenderId().toString(), getCurrentUser().getUserId()));
        if (message instanceof TextMessage) {
            output.setTextContent(((TextMessage) message).getTextContent());
        } else if (message instanceof MediaMessage) {
            Media media = ((MediaMessage) message).getMedia();
            if (media != null) {
                output.setMediaId(media.getId());
                output.setFileId(media.getFileId());
            }
        }

        return output;
    }

    // Corrected method signature and logic for getMessageById
    // Changed return type from Object to GetMessageOutputModel
    public RpcResponse<Object> getMessageById(GetMessageByIdInputModel model) {
        Message message = daoManager.getMessageDAO().findById(model.getMessageId()); // Used model.getMessageId()
        if (message == null) {
            return BadRequest("Message not found.");
        }
        GetMessageOutputModel output = mapMessageToOutputModel(message);
        return Ok(output);
    }

    // Return type already correct
    public RpcResponse<List<GetMessageOutputModel>> getMessagesByChat(GetMessageByChatInputModel model) {
        List<Message> messages = daoManager.getMessageDAO().findAllByField("chat.id", model.getChatId());
        if (messages == null || messages.isEmpty()) {
            return Ok(List.of());
        }
        List<GetMessageOutputModel> outputList = messages.stream()
                .map(this::mapMessageToOutputModel)
                .collect(Collectors.toList());
        return Ok(outputList);
    }

    // Return type already correct
    public RpcResponse<Object> sendMessage(SendMessageInputModel model) {
        Account sender = daoManager.getAccountDAO().findById(UUID.fromString( getCurrentUser().getUserId()));
        if (sender == null) {
            return BadRequest("Sender account not found.");
        }

        Chat chat = daoManager.getChatDAO().findById(model.getChatId());
        if (chat == null) {
            return BadRequest("Chat not found.");
        }

        Message newMessage;

        switch (model.getMessageType()) {
            case TEXT:
                if (model.getTextContent() == null || model.getTextContent().trim().isEmpty()) {
                    return BadRequest("Text content cannot be empty for a TEXT message.");
                }
                TextMessage textMessage = new TextMessage();
                textMessage.setTextContent(model.getTextContent());
                newMessage = textMessage;
                break;

            case MEDIA:
                if (model.getMediaId() == null) {
                    return BadRequest("Media ID cannot be null for a MEDIA message.");
                }
                Media media = daoManager.getMediaDAO().findById(model.getMediaId());
                if (media == null) {
                    return BadRequest("Media not found.");
                }
                MediaMessage mediaMessage = new MediaMessage();
                mediaMessage.setMedia(media);
                newMessage = mediaMessage;
                break;

            default:
                return BadRequest("The specified message type is not supported yet.");
        }

        newMessage.setSender(sender);
        newMessage.setChat(chat);
        newMessage.setTimestamp(LocalDateTime.now());
        newMessage.setEdited(false);
        newMessage.setType(model.getMessageType());

        daoManager.getMessageDAO().insert(newMessage);

        // Pass LocalDateTime to the SendMessageOutputModel constructor, which now handles conversion to String
        SendMessageOutputModel output = new SendMessageOutputModel(
                newMessage.getId(),
                newMessage.getTimestamp(), // This will be converted to String inside the constructor
                "Message sent successfully"
        );

        return Ok(output);
    }
}