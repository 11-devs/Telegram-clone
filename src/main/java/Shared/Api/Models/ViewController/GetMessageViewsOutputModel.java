package Shared.Api.Models.ViewController;

import java.util.List;

public class GetMessageViewsOutputModel {
    private List<ViewerInfo> viewers;

    public GetMessageViewsOutputModel(List<ViewerInfo> viewers) {
        this.viewers = viewers;
    }

    public List<ViewerInfo> getViewers() {
        return viewers;
    }
}