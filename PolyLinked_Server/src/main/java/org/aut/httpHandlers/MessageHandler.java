package org.aut.httpHandlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.aut.controllers.MessageController;
import org.aut.dataAccessors.MediaAccessor;
import org.aut.dataAccessors.MessageAccessor;
import org.aut.models.Message;
import org.aut.models.User;
import org.aut.utils.MultipartHandler;
import org.aut.utils.exceptions.NotAcceptableException;
import org.aut.utils.exceptions.NotFoundException;
import org.aut.utils.exceptions.UnauthorizedException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;

public class MessageHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String jwt = exchange.getRequestHeaders().getFirst("Authorization");

        try {
            User user = LoginHandler.getUserByToken(jwt);

            switch (method) {
                case "POST": {
                    InputStream inputStream = exchange.getRequestBody();
                    Message message = MultipartHandler.readObject(inputStream, Message.class);
                    File media = MultipartHandler.readToFile(inputStream, MediaAccessor.MediaPath.MESSAGES.value(), message.getId());

                    if (message.getText().trim().isEmpty() && media == null) {
                        throw new NotAcceptableException("Not acceptable");
                    }
                    if (!message.getSenderId().equals(user.getUserId())) {
                        throw new UnauthorizedException("Unauthorized user");
                    }

                    MessageController.addMessage(message);
                    inputStream.close();
                    exchange.sendResponseHeaders(200, 0);
                }
                break;

                case "DELETE": {
                    String[] splitPath = exchange.getRequestURI().getPath().split("/");

                    if (splitPath.length != 3) {
                        throw new NotAcceptableException("Invalid path");
                    }

                    String path = splitPath[2];
                    Message message = MessageAccessor.getMessageById(path);

                    if (!message.getSenderId().equals(user.getUserId())) {
                        throw new UnauthorizedException("Unauthorized user");
                    }

                    File media = MediaAccessor.getMedia(message.getId(), MediaAccessor.MediaPath.MESSAGES);
                    if (media != null) Files.deleteIfExists(media.toPath());

                    MessageController.deleteMessage(message.getId());
                    exchange.sendResponseHeaders(200, 0);
                }
                break;

                case "GET":
                    String[] splitPath = exchange.getRequestURI().getPath().split("/");
                    if (splitPath.length != 3 || splitPath[2].split("&").length != 2) {
                        throw new NotAcceptableException("Invalid path");
                    }
                    String path = splitPath[2];
                    String senderId = path.split("&")[0];
                    String receiverId = path.split("&")[1];

                    if (!senderId.equals(user.getUserId()) && !receiverId.equals(user.getUserId()))
                        throw new UnauthorizedException("Unauthorized user");

                    ArrayList<Message> messages = MessageAccessor.getLastMessagesBetween(senderId, receiverId);
                    if (messages.isEmpty()) throw new NotFoundException("Messages not found");

                    exchange.getResponseHeaders().set("X-Total-Count", "" + messages.size());
                    exchange.sendResponseHeaders(200, 0);
                    OutputStream outputStream = exchange.getResponseBody();
                    MultipartHandler.writeObjectArray(outputStream, messages);
                    outputStream.close();
                    break;

                default:
                    exchange.sendResponseHeaders(405, 0);

            }


        } catch (UnauthorizedException e) {
            exchange.sendResponseHeaders(401, 0);
        } catch (SQLException e) {
            exchange.sendResponseHeaders(500, 0);
        } catch (NotAcceptableException e) {
            exchange.sendResponseHeaders(406, 0);
        } catch (NotFoundException e) {
            exchange.sendResponseHeaders(404, 0);
        }
        exchange.close();
    }
}
