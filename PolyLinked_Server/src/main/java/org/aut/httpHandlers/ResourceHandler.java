package org.aut.httpHandlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.aut.dataAccessors.MediaAccessor;
import org.aut.utils.MultipartHandler;
import org.aut.utils.exceptions.NotAcceptableException;
import org.aut.utils.exceptions.NotFoundException;
import java.io.*;
import java.nio.file.Files;

public class ResourceHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String[] path = exchange.getRequestURI().getPath().split("/");
        String method = exchange.getRequestMethod();


        try {
            if (method.equals("HEAD") || method.equals("GET")) {
                if (path.length != 4) {
                    exchange.close();
                    return;
                }

                File file = MediaAccessor.getMedia(path[3], MediaAccessor.MediaPath.valueOf(path[2].toUpperCase()));
                if (file == null || file.length() < 1 || !file.isFile()) throw new NotFoundException("Media not found");

                int length = (int) file.length();
                String type = file.getName().substring(file.getName().lastIndexOf(".") + 1).toLowerCase();
                if (type.trim().isEmpty()) throw new NotFoundException("File corruption");

                if (MediaAccessor.VIDEO_EXTENSIONS.contains(type)) {
                    exchange.getResponseHeaders().add("Content-Type", "Video/" + type);
                } else if (MediaAccessor.AUDIO_EXTENSIONS.contains(type)) {
                    exchange.getResponseHeaders().add("Content-Type", "Audio/" + type);
                } else if (MediaAccessor.IMAGE_EXTENSIONS.contains(type)) {
                    exchange.getResponseHeaders().add("Content-Type", "Image/" + type);
                } else throw new NotFoundException("File format not supported");

                if (method.equals("HEAD")) {
                    exchange.sendResponseHeaders(200, -1);
                } else {
                    exchange.sendResponseHeaders(200, length);
                    try (OutputStream outputStream = exchange.getResponseBody();
                         FileInputStream inputStream = new FileInputStream(file)) {

                        MultipartHandler.linkStreams(inputStream, outputStream, length);
                    }
                }
            } else if (method.equals("POST")) {
                InputStream inputStream = exchange.getRequestBody();

                String root = exchange.getRequestHeaders().getFirst("Root");
                String id = exchange.getRequestHeaders().getFirst("ID");

                MediaAccessor.MediaPath rootPath = null;
                switch (root) {
                    case "Profile" -> rootPath = MediaAccessor.MediaPath.PROFILES;
                    case "Background" -> rootPath = MediaAccessor.MediaPath.BACKGROUNDS;
                }

                if (rootPath == null) {
                    exchange.sendResponseHeaders(404, -1);
                } else {
                    File oldFile = MediaAccessor.getMedia(id, rootPath);
                    File newFile = MultipartHandler.readToFile(inputStream, rootPath.value(), id);

                    if (oldFile != null && newFile != null) {
                        Files.delete(oldFile.toPath());
                    }

                    inputStream.close();
                    exchange.sendResponseHeaders(200, 0);
                }
            }
        } catch (NotFoundException e) {
            exchange.sendResponseHeaders(404, -1);
        } catch (NotAcceptableException e) {
            exchange.sendResponseHeaders(406, -1);
        }

        exchange.close();
    }
}
