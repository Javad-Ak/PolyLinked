package org.aut.httpHandlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.aut.controllers.FollowController;
import org.aut.dataAccessors.FollowAccessor;
import org.aut.dataAccessors.UserAccessor;
import org.aut.models.User;
import org.aut.utils.MultipartHandler;
import org.aut.utils.exceptions.NotAcceptableException;
import org.aut.utils.exceptions.NotFoundException;
import org.aut.utils.exceptions.UnauthorizedException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class FollowersHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String jwt = exchange.getRequestHeaders().getFirst("Authorization");

        try {
            if (method.equals("GET")) {
                LoginHandler.getUserByToken(jwt);
                String[] splitURI = exchange.getRequestURI().getPath().split("/");
                if (splitURI.length != 4 && !splitURI[2].equals("followers")) {
                    throw new NotAcceptableException("Invalid request");
                }

                String path = splitURI[3];
                User seekedUser = UserAccessor.getUserById(path);

                ArrayList<User> followers = FollowAccessor.getFollowers(seekedUser.getUserId());
                if (followers.isEmpty()) throw new NotFoundException("No user not found");

                exchange.getResponseHeaders().set("X-Total-Count", "" + followers.size());
                exchange.sendResponseHeaders(200, 0);
                OutputStream outputStream = exchange.getResponseBody();

                MultipartHandler.writeObjectArray(outputStream, followers);
                outputStream.close();
            } else {
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
