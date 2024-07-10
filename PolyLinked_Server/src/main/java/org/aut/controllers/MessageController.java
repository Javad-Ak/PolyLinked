package org.aut.controllers;


import org.aut.dataAccessors.MessageAccessor;
import org.aut.models.Message;
import org.aut.utils.exceptions.NotAcceptableException;
import org.aut.utils.exceptions.NotFoundException;
import java.sql.SQLException;


public class MessageController {
    public static void addMessage(Message message) throws SQLException, NotFoundException, NotAcceptableException {
        if (message == null) {
            throw new NotAcceptableException("Null message");
        } else if (!UserController.userExistsById(message.getSenderId()) || !UserController.userExistsById(message.getReceiverId())) {
            throw new NotFoundException("User not found");
        }
        MessageAccessor.addMessage(message);
    }

    public static void deleteMessage(String messageId) throws SQLException, NotFoundException, NotAcceptableException {
        if (messageId == null || messageId.isEmpty()) {
            throw new NotAcceptableException("Null or empty messageId");
        } else if (!MessageAccessor.messageExists(messageId)) {
            throw new NotFoundException("Message not found");
        }
        MessageAccessor.deleteMessage(messageId);
    }
}
