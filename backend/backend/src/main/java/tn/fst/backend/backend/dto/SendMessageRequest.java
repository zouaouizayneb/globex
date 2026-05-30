package tn.fst.backend.backend.dto;

public class SendMessageRequest {
    private String question;

    public SendMessageRequest() {}

    public SendMessageRequest(String question) {
        this.question = question;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}
