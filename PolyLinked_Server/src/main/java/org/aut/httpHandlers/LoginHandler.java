package org.aut.httpHandlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import io.jsonwebtoken.Claims;
import org.aut.controllers.UserController;
import org.aut.dataAccessors.UserAccessor;
import org.aut.models.User;
import org.aut.utils.JsonHandler;
import org.aut.utils.JwtHandler;
import org.aut.utils.exceptions.NotFoundException;
import org.aut.utils.exceptions.UnauthorizedException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.SQLException;

public class LoginHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        JSONObject response = new JSONObject();
        int code;

        try {
            if (!exchange.getRequestMethod().equals("POST")) {
                code = 405; // method not found
            } else {
                JSONObject received = JsonHandler.getObject(exchange.getRequestBody()); // A json with email and password
                if (UserController.authenticate(received.getString("email"), received.getString("password"))) {
                    code = 200;
                    User user = UserAccessor.getUserByEmail(received.getString("email"));
                    response.put("Authorization", JwtHandler.generateToken(user.getUserId()));
                    response.put("userId", user.getUserId());
                    response.put("fullName", user.getFirstName() + " " + user.getLastName());
                } else {
                    throw new UnauthorizedException("Invalid credentials");
                }
            }
        } catch (UnauthorizedException | NotFoundException e) {
            code = 401;
        } catch (SQLException e) {
            code = 500;
        }


        if (response.isEmpty()) {
            exchange.sendResponseHeaders(code, 0);
        } else {
            exchange.sendResponseHeaders(code, response.toString().getBytes().length);
            JsonHandler.sendObject(exchange.getResponseBody(), response);
        }
        exchange.close();
    }

    public static User getUserByToken(String token) throws SQLException, UnauthorizedException {
        try {
            Claims claims = JwtHandler.verifyToken(token);
            return UserAccessor.getUserById(claims.getSubject());
        } catch (Exception e) {
            throw new UnauthorizedException("Authentication failed.");
        }
    }
}
