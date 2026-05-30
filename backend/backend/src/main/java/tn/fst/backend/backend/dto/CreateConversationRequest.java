package tn.fst.backend.backend.dto;

public class CreateConversationRequest {
    private String title;

    public CreateConversationRequest() {}

    public CreateConversationRequest(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
