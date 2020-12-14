package com.rahnemacollege.util.exceptions;


public class MessageException extends RuntimeException {

    private static final long serialVersionUID = 5489516240608806490L;
    private Message message;
    public MessageException(Message message) {
        this.message = message;
    }
    public Message getMessageStatus() {
        return message;
    }
    @Override
    public String getMessage() {
        return message.toString();
    }
}