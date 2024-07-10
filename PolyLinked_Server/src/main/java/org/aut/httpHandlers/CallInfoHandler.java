package org.aut.httpHandlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.aut.controllers.CallInfoController;
import org.aut.dataAccessors.CallInfoAccessor;
import org.aut.dataAccessors.EducationAccessor;
import org.aut.dataAccessors.ProfileAccessor;
import org.aut.models.CallInfo;
import org.aut.models.User;
import org.aut.utils.JsonHandler;
import org.aut.utils.exceptions.NotAcceptableException;
import org.aut.utils.exceptions.NotFoundException;
import org.aut.utils.exceptions.UnauthorizedException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;

public class CallInfoHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String jwt = exchange.getRequestHeaders().getFirst("Authorization");

        try {
            User requester = LoginHandler.getUserByToken(jwt);

            switch (method) {
                case "POST":
                case "PUT": {
                    JSONObject jsonObject = JsonHandler.getObject(exchange.getRequestBody());
                    CallInfo newCallInfo = new CallInfo(jsonObject);

                    if (!requester.getUserId().equals(newCallInfo.getUserId()))
                        throw new UnauthorizedException("User unauthorized");

                    try {
                        CallInfoAccessor.getCallInfoByUserId(newCallInfo.getUserId());
                        CallInfoAccessor.updateCallInfo(newCallInfo);
                    } catch (Exception e) {
                        CallInfoAccessor.addCallInfo(newCallInfo);
                    }
                    exchange.sendResponseHeaders(200, 0);
                }
                break;

                case "DELETE": {
                    CallInfoController.deleteCallInfo(requester.getUserId());
                    exchange.sendResponseHeaders(200, 0);

                }
                case "GET": {
                    String[] path = exchange.getRequestURI().getPath().split("/");
                    if (path.length != 4) throw new NotAcceptableException("Invalid path");

                    CallInfo callinfo = CallInfoController.getCallInfo(path[3], requester.getUserId());
                    exchange.sendResponseHeaders(200, 0);

                    OutputStream outputStream = exchange.getResponseBody();
                    JsonHandler.sendObject(outputStream, callinfo.toJson());
                    outputStream.close();
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
