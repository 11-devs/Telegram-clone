package JSocket2.Protocol.Rpc;

public class RpcCallMetadata {
    private String controller;
    private String action;
    public RpcCallMetadata(String controller,String action){
        this.controller = controller;
        this.action = action;
    }
    public String getController() {
        return controller;
    }

    public void setController(String controller) {
        this.controller = controller;
    }
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
