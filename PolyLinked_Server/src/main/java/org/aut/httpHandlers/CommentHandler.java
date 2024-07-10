package org.aut.httpHandlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.aut.controllers.PostController;
import org.aut.dataAccessors.CommentAccessor;
import org.aut.dataAccessors.MediaAccessor;
import org.aut.models.Comment;
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
import java.util.TreeMap;

public class CommentHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String jwt = exchange.getRequestHeaders().getFirst("Authorization");

        try {
            User user = LoginHandler.getUserByToken(jwt);

            switch (method) {
                case "POST": {
                    InputStream inputStream = exchange.getRequestBody();
                    Comment comment = MultipartHandler.readObject(inputStream, Comment.class);
                    File media = MultipartHandler.readToFile(inputStream, MediaAccessor.MediaPath.COMMENTS.value(), comment.getId());

                    if (!comment.getUserId().equals(user.getUserId())) {
                        throw new UnauthorizedException("Unauthorized user");
                    }
                    if (comment.getText().trim().isEmpty() && media == null) {
                        throw new NotAcceptableException("Not acceptable");
                    }

                    CommentAccessor.addComment(comment);
                    inputStream.close();
                    exchange.sendResponseHeaders(200, 0);
                }
                break;
                case "DELETE": {
                    String[] path = exchange.getRequestURI().getPath().split("/");
                    if (path.length != 3) throw new NotAcceptableException("Invalid path");
                    Comment comment = CommentAccessor.getCommentById(path[2]);

                    if (!comment.getUserId().equals(user.getUserId()))
                        throw new UnauthorizedException("Unauthorized user");

                    File media = MediaAccessor.getMedia(comment.getId(), MediaAccessor.MediaPath.COMMENTS);
                    if (media != null) Files.deleteIfExists(media.toPath());

                    CommentAccessor.deleteComment(comment.getId());
                    exchange.sendResponseHeaders(200, 0);
                }
                break;
                case "GET": {
                    String[] path = exchange.getRequestURI().getPath().split("/");
                    if (path.length != 3) throw new NotAcceptableException("Invalid path");

                    TreeMap<Comment, User> map = PostController.getCommentsOfPost(path[2]);
                    if (map.isEmpty()) throw new NotFoundException("Not found");

                    exchange.getResponseHeaders().set("X-Total-Count", "" + map.size());
                    exchange.sendResponseHeaders(200, 0);

                    OutputStream outputStream = exchange.getResponseBody();
                    MultipartHandler.writeMap(outputStream, map);
                    outputStream.close();
                }
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
