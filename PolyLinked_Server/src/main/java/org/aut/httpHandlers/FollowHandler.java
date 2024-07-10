package org.aut.httpHandlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.aut.controllers.FollowController;
import org.aut.dataAccessors.FollowAccessor;
import org.aut.models.Follow;
import org.aut.models.User;
import org.aut.utils.JsonHandler;
import org.aut.utils.exceptions.NotFoundException;
import org.aut.utils.exceptions.NotAcceptableException;
import org.aut.utils.exceptions.UnauthorizedException;
import org.json.JSONObject;
import java.io.IOException;
import java.sql.SQLException;

public class FollowHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        try {
            String jwt = exchange.getRequestHeaders().getFirst("Authorization");
            User user = LoginHandler.getUserByToken(jwt);
            switch (method) {
                case "POST": {
                    JSONObject jsonObject = JsonHandler.getObject(exchange.getRequestBody());
                    Follow follow = new Follow(jsonObject);
                    if (!user.getUserId().equals(follow.getFollower_id()) && !user.getUserId().equals(follow.getFollowed_id())) {
                        throw new UnauthorizedException("User unauthorized");
                    }
                    if (!jsonObject.isEmpty() && !FollowAccessor.followExists(follow)) {
                        FollowController.addFollow(follow);
                        exchange.sendResponseHeaders(200, 0);
                    } else throw new NotFoundException("not found");
                }
                break;
                case "DELETE": {
                    JSONObject jsonObject = JsonHandler.getObject(exchange.getRequestBody());
                    Follow follow = new Follow(jsonObject);

                    if (!user.getUserId().equals(follow.getFollower_id()) && !user.getUserId().equals(follow.getFollowed_id())) {
                        throw new UnauthorizedException("User unauthorized");
                    }

                    if (!FollowAccessor.followExists(follow) || jsonObject.isEmpty()) {
                        throw new NotFoundException("not found");
                    } else {
                        FollowController.deleteFollow(follow);
                        exchange.sendResponseHeaders(200, 0);
                    }
                }
                break;
                case "HEAD": {
                    String[] path = exchange.getRequestURI().getPath().split("/");
                    if (path.length != 3) throw new NotAcceptableException("Invalid path");
                    String userId = path[2];
                    String exists = "false";
                    for (User obj : FollowAccessor.getFollowers(userId)) {
                        if (obj.getUserId().equals(user.getUserId())) {
                            exists = "true";
                            break;
                        }
                    }
                    exchange.getResponseHeaders().add("Exists", exists);
                    exchange.sendResponseHeaders(200, -1);
                }
                break;
                default:
                    exchange.sendResponseHeaders(405, 0);
                    break;
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
