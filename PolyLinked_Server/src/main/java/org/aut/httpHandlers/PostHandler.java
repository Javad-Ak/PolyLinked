package org.aut.httpHandlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.aut.controllers.PostController;
import org.aut.dataAccessors.MediaAccessor;
import org.aut.dataAccessors.PostAccessor;
import org.aut.models.Post;
import org.aut.models.User;
import org.aut.utils.JsonHandler;
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

public class PostHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String jwt = exchange.getRequestHeaders().getFirst("Authorization");
        String[] path = exchange.getRequestURI().getPath().split("/");

        try {
            User user = LoginHandler.getUserByToken(jwt);
            switch (method) {
                case "PUT":
                case "POST": {
                    InputStream inputStream = exchange.getRequestBody();

                    Post post = MultipartHandler.readObject(inputStream, Post.class);
                    if (!post.getUserId().equals(user.getUserId())) throw new UnauthorizedException("Unauthorized");

                    File newMedia = MultipartHandler.readToFile(inputStream, MediaAccessor.MediaPath.POSTS.value(), post.getPostId());
                    if (post.getText().trim().isEmpty() && newMedia == null)
                        throw new NotAcceptableException("Not acceptable");

                    if (method.equals("POST")) PostAccessor.addPost(post);
                    else {
                        PostAccessor.updatePost(post);
                        File oldMedia = MediaAccessor.getMedia(post.getUserId(), MediaAccessor.MediaPath.POSTS);
                        if (newMedia != null && oldMedia != null)
                            Files.delete(oldMedia.toPath());
                    }

                    inputStream.close();
                    exchange.sendResponseHeaders(200, 0);
                }
                break;
                case "GET": {
                    if (path.length == 3) {
                        Post post = PostAccessor.getPostById(path[2]);
                        exchange.sendResponseHeaders(200, 0);
                        OutputStream outputStream = exchange.getResponseBody();
                        JsonHandler.sendObject(outputStream, post.toJson());

                        outputStream.close();
                    } else if (path.length == 4) {
                        ArrayList<Post> posts = PostController.getPostsOf(user.getUserId());
                        if (posts.isEmpty()) throw new NotFoundException("Not found");

                        exchange.getResponseHeaders().add("X-Total-Count", Integer.toString(posts.size()));
                        exchange.sendResponseHeaders(200, 0);

                        OutputStream outputStream = exchange.getResponseBody();
                        MultipartHandler.writeObjectArray(outputStream, posts);
                        outputStream.close();
                    } else throw new NotAcceptableException("Invalid path");
                }
                break;
                case "DELETE": {
                    if (path.length != 3) throw new NotAcceptableException("Invalid path");

                    Post post = PostAccessor.getPostById(path[2]);

                    if (!post.getUserId().equals(user.getUserId())) throw new UnauthorizedException("Unauthorized");

                    File media = MediaAccessor.getMedia(post.getPostId(), MediaAccessor.MediaPath.POSTS);
                    if (media != null) Files.deleteIfExists(media.toPath());
                    PostAccessor.deletePost(post.getPostId());

                    exchange.sendResponseHeaders(200, 0);
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
