package org.aut.httpHandlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.aut.controllers.NewsFeedController;
import org.aut.models.Post;
import org.aut.models.User;
import org.aut.utils.MultipartHandler;
import org.aut.utils.exceptions.NotFoundException;
import org.aut.utils.exceptions.UnauthorizedException;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.TreeMap;

public class NewsFeedHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if (!method.equals("GET")) {
            exchange.sendResponseHeaders(405, 0);
            exchange.close();
            return;
        }

        String jwt = exchange.getRequestHeaders().getFirst("Authorization");
        try {
            User user = LoginHandler.getUserByToken(jwt);

            TreeMap<Post, User> map = NewsFeedController.fetchFeed(user.getUserId());
            if (map.isEmpty()) throw new NotFoundException("Empty feed");

            exchange.getResponseHeaders().add("X-Total-Count", String.valueOf(map.size()));
            exchange.sendResponseHeaders(200, 0);

            OutputStream os = exchange.getResponseBody();
            MultipartHandler.writeMap(os, map);
            os.close();
        } catch (NotFoundException e) {
            exchange.sendResponseHeaders(404, 0);
        } catch (SQLException e) {
            exchange.sendResponseHeaders(500, 0);
        } catch (UnauthorizedException e) {
            exchange.sendResponseHeaders(401, 0);
        }
        exchange.close();
    }
}
