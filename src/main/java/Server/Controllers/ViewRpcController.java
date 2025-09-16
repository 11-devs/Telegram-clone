package Server.Controllers;

import JSocket2.Protocol.Rpc.RpcControllerBase;
import JSocket2.Protocol.Rpc.RpcResponse;
import Server.DaoManager;
import Shared.Api.Models.ViewController.*;
import Shared.Models.Account.Account;
import Shared.Models.Membership.Membership;
import Shared.Models.Message.Message;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ViewRpcController extends RpcControllerBase {
    private final DaoManager daoManager;

    public ViewRpcController(DaoManager daoManager) {
        this.daoManager = daoManager;
    }

    public RpcResponse<Object> setViewOnMessage(SetViewOnMessageInputModel model) {
        Message message = daoManager.getMessageDAO().findById(model.getMessageId());
        if (message == null) {
            return BadRequest("Message not found.");
        }

        UUID chatId = message.getChat().getId();
        List<Membership> membershipsInChat = daoManager.getMembershipDAO().findAllByField("chat.id", chatId);
        Membership userMembership = membershipsInChat.stream()
                .filter(m -> m.getAccount().getId().equals(model.getUserId()))
                .findFirst()
                .orElse(null);

        if (userMembership == null) {
            return BadRequest("User is not a member of this chat.");
        }

        userMembership.setLastReadMessage(message);
        daoManager.getMembershipDAO().update(userMembership);

        return Ok(new SetViewOnMessageOutputModel("View registered successfully."));
    }

    public RpcResponse<Object> getMessageViews(GetMessageViewsInputModel model) {
        Message message = daoManager.getMessageDAO().findById(model.getMessageId());
        if (message == null) {
            return BadRequest("Message not found.");
        }

        UUID chatId = message.getChat().getId();
        List<Membership> allMemberships = daoManager.getMembershipDAO().findAllByField("chat.id", chatId);

        List<ViewerInfo> viewers = allMemberships.stream()
                .filter(m -> m.getLastReadMessage() != null &&
                        !m.getLastReadMessage().getTimestamp().isBefore(message.getTimestamp()))
                .map(m -> {
                    Account account = m.getAccount();
                    return new ViewerInfo(account.getId(), account.getFirstName(), account.getLastName());
                })
                .collect(Collectors.toList());

        return Ok(new GetMessageViewsOutputModel(viewers));
    }
}