package org.aut.httpHandlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.aut.dataAccessors.EducationAccessor;
import org.aut.dataAccessors.ProfileAccessor;
import org.aut.models.Education;
import org.aut.models.User;
import org.aut.utils.JsonHandler;
import org.aut.utils.MultipartHandler;
import org.aut.utils.exceptions.NotAcceptableException;
import org.aut.utils.exceptions.NotFoundException;
import org.aut.utils.exceptions.UnauthorizedException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;


public class EducationHandler implements HttpHandler {
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

                    Education edu = new Education(JsonHandler.getObject(inputStream));
                    if (!edu.getUserId().equals(user.getUserId())) throw new UnauthorizedException("Unauthorized");

                    try {
                        EducationAccessor.getEducation(edu.getEducationId());
                        EducationAccessor.updateEducation(edu);
                    } catch (Exception e) {
                        EducationAccessor.addEducation(edu);
                    }

                    inputStream.close();
                    exchange.sendResponseHeaders(200, 0);
                }
                break;
                case "GET": {
                    if (path.length != 4) throw new NotAcceptableException("Invalid path");

                    ArrayList<Education> educations = EducationAccessor.getEducationsOf(path[3]);
                    if (educations.isEmpty()) throw new NotFoundException("Not Found");

                    exchange.getResponseHeaders().add("X-Total-Count", Integer.toString(educations.size()));
                    exchange.sendResponseHeaders(200, 0);

                    OutputStream outputStream = exchange.getResponseBody();
                    MultipartHandler.writeObjectArray(outputStream, educations);
                    outputStream.close();

                }
                break;
                case "DELETE": {
                    if (path.length != 4) throw new NotAcceptableException("Invalid path");
                    Education education = EducationAccessor.getEducation(path[3]);
                    if (!education.getUserId().equals(user.getUserId()))
                        throw new UnauthorizedException("Unauthorized");

                    EducationAccessor.deleteEducation(education.getEducationId());
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
