package tn.fst.backend.backend.dto;

import java.time.LocalDateTime;

public class ChatMessageResponse {

    private Long idMsg;
    private String question;
    private String answer;
    private LocalDateTime dateMsg;
    private Long conversationId;

    public ChatMessageResponse() {}

    public ChatMessageResponse(Long idMsg, String question, String answer, LocalDateTime dateMsg, Long conversationId) {
        this.idMsg = idMsg;
        this.question = question;
        this.answer = answer;
        this.dateMsg = dateMsg;
        this.conversationId = conversationId;
    }

    public Long getIdMsg() {
        return idMsg;
    }

    public void setIdMsg(Long idMsg) {
        this.idMsg = idMsg;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public LocalDateTime getDateMsg() {
        return dateMsg;
    }

    public void setDateMsg(LocalDateTime dateMsg) {
        this.dateMsg = dateMsg;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }
}
