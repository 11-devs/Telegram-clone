package Client.Services;

import Client.RpcCaller;
import JSocket2.Protocol.Rpc.RpcResponse;
import Shared.Api.Models.ContactController.*;
import javafx.concurrent.Task;

import java.util.UUID;

public class ContactService {
    private final RpcCaller rpcCaller;
    private final UUID currentUserId; // Assuming you have a way to get the current user's ID

    public ContactService(RpcCaller rpcCaller, UUID currentUserId) {
        this.rpcCaller = rpcCaller;
        this.currentUserId = currentUserId;
    }

    public Task<RpcResponse<GetContactsOutputModel>> fetchContacts() {
        return new Task<>() {
            @Override
            protected RpcResponse<GetContactsOutputModel> call() throws Exception {
                GetContactsInputModel input = new GetContactsInputModel();
                input.setOwnerId(currentUserId);
                return rpcCaller.getContacts(input);
            }
        };
    }

    public Task<RpcResponse<AddContactOutputModel>> addContact(String firstName, String lastName, String phoneNumber) {
        return new Task<>() {
            @Override
            protected RpcResponse<AddContactOutputModel> call() throws Exception {
                // This is a simplified version. A real implementation would find the user by phone number first.
                // For this example, we'll assume a direct add is possible.
                // A more robust approach would be a server-side "findUserByPhone" RPC.
                // We are passing a placeholder UUID for contactId, server should resolve it.
                AddContactInputModel input = new AddContactInputModel();
                input.setOwnerId(currentUserId);
                // The server would need to find the Account ID for this phone number.
                // This part of the logic needs to be implemented on the server.
                // For now, let's assume the server handles finding the contact by phone.
                // We will pass the phone number inside the savedName for the server to process.
                input.setSavedName(firstName + " " + lastName + ";" + phoneNumber);

                return rpcCaller.addContact(input);
            }
        };
    }
}