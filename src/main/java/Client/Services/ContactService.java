package Client.Services;

import Client.RpcCaller;
import JSocket2.Protocol.Rpc.RpcResponse;
import Shared.Api.Models.ContactController.*;
import javafx.concurrent.Task;

import java.util.UUID;

public class ContactService {
    private final RpcCaller rpcCaller;

    public ContactService(RpcCaller rpcCaller) {
        this.rpcCaller = rpcCaller;
    }

    public Task<RpcResponse<GetContactsOutputModel>> fetchContacts() {
        return new Task<>() {
            @Override
            protected RpcResponse<GetContactsOutputModel> call() throws Exception {
                GetContactsInputModel input = new GetContactsInputModel();
                return rpcCaller.getContacts(input);
            }
        };
    }

    public Task<RpcResponse<AddContactOutputModel>> addContact(String firstName, String lastName, String phoneNumber) {
        return new Task<>() {
            @Override
            protected RpcResponse<AddContactOutputModel> call() throws Exception {
                AddContactInputModel input = new AddContactInputModel();
                String savedName = firstName.trim();
                if (lastName != null && !lastName.trim().isEmpty()) {
                    savedName += " " + lastName.trim();
                }
                input.setSavedName(savedName);
                input.setPhoneNumber(phoneNumber);

                return rpcCaller.addContact(input);
            }
        };
    }
}