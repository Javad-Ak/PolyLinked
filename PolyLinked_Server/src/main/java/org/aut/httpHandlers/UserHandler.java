package org.aut.httpHandlers;

import org.aut.controllers.UserController;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.aut.dataAccessors.MediaAccessor;
import org.aut.dataAccessors.UserAccessor;
import org.aut.models.User;
import org.aut.utils.JsonHandler;
import org.aut.utils.MultipartHandler;
import org.aut.utils.exceptions.NotAcceptableException;
import org.aut.utils.exceptions.NotFoundException;
import org.aut.utils.exceptions.UnauthorizedException;
import org.json.JSONObject;

import java.io.*;
import java.sql.SQLException;

public class UserHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        try {
            User user = null;
            if (method.equals("PUT") || method.equals("DELETE")) {
                String jwt = exchange.getRequestHeaders().getFirst("Authorization");
                user = LoginHandler.getUserByToken(jwt);
            }
            switch (method) {
                case "POST": {
                    JSONObject jsonObject = JsonHandler.getObject(exchange.getRequestBody());
                    User newUser = new User(jsonObject);
                    if (!jsonObject.isEmpty()) {
                        UserController.addUser(newUser);
                        exchange.sendResponseHeaders(200, 0);
                    } else {
                        throw new NotAcceptableException("json object is empty");
                    }
                    break;
                }
                case "PUT": {
                    JSONObject jsonObject = JsonHandler.getObject(exchange.getRequestBody());
                    User newUser = new User(jsonObject);
                    if (!user.getUserId().equals(newUser.getUserId())) {

                        throw new UnauthorizedException("Unauthorized");
                    }
                    if (!jsonObject.isEmpty()) {
                        UserController.updateUser(newUser);
                        exchange.sendResponseHeaders(200, 0);
                    }
                    break;
                }
                case "DELETE": {
                    UserController.deleteUser(user);
                    exchange.sendResponseHeaders(200, 0);
                    break;
                }
                case "GET": {
                    String[] splitPath = exchange.getRequestURI().getPath().split("/");
                    if (splitPath.length != 3) {
                        throw new NotAcceptableException("Invalid path");
                    }
                    String path = splitPath[2];
                    User seekedUser = UserAccessor.getUserById(path);

                    exchange.sendResponseHeaders(200, 0);
                    OutputStream outputStream = exchange.getResponseBody();
                    JsonHandler.sendObject(outputStream, seekedUser.toJson());

                    outputStream.close();
                    break;
                }
                default:
                    exchange.sendResponseHeaders(405, 0);
                    break;
            }
        } catch (SQLException e) {
            exchange.sendResponseHeaders(500, 0);
        } catch (NotAcceptableException e) {
            exchange.sendResponseHeaders(406, 0);
        } catch (NotFoundException e) {
            exchange.sendResponseHeaders(404, 0);
        } catch (UnauthorizedException e) {
            exchange.sendResponseHeaders(401, 0);
        }
        exchange.close();
    }
}
