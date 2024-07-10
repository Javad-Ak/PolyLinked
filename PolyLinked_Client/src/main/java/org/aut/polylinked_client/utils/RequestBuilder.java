package org.aut.polylinked_client.utils;

import org.aut.polylinked_client.model.JsonSerializable;
import org.aut.polylinked_client.model.MediaLinked;
import org.aut.polylinked_client.model.User;
import org.aut.polylinked_client.utils.exceptions.NotAcceptableException;
import org.aut.polylinked_client.utils.exceptions.UnauthorizedException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;


public class RequestBuilder {
    private static final String SERVER_ADDRESS = "http://localhost:8080/";

    private RequestBuilder() {
    }

    public static HttpURLConnection buildConnection
            (String method, String endPoint, JSONObject headers, boolean doOutput) throws IOException {

        URL url = URI.create(SERVER_ADDRESS + endPoint).toURL();
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod(method);
        if (headers != null) headers.toMap().forEach((k, v) -> con.setRequestProperty(k, v.toString()));
        con.setDoOutput(doOutput);
        return con;
    }

    public static JSONObject jsonFromGetRequest(String endPoint, JSONObject headers) throws UnauthorizedException {
        HttpURLConnection con = null;
        try {
            con = buildConnection("GET", endPoint, headers, false);
            if (con.getResponseCode() / 100 == 2) {
                return JsonHandler.getObject(con.getInputStream());
            } else if (con.getResponseCode() == 401) {
                throw new UnauthorizedException("JWT invalid");
            }
        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception ignored) {
        } finally {
            if (con != null) con.disconnect();
        }
        return null;
    }

    public static <T extends MediaLinked> TreeMap<T, User> mapFromGetRequest(Class<T> cls, String endPoint, JSONObject headers) throws UnauthorizedException {
        TreeMap<T, User> map = new TreeMap<>();
        HttpURLConnection con = null;
        try {
            con = buildConnection("GET", endPoint, headers, false);
            if (con.getResponseCode() / 100 == 2) {
                int size = Integer.parseInt(con.getHeaderField("X-Total-Count"));
                map = MultipartHandler.readMap(con.getInputStream(), cls, size);
            } else if (con.getResponseCode() == 401) {
                throw new UnauthorizedException("JWT invalid");
            }
        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception ignored) {

        } finally {
            if (con != null) con.disconnect();
        }
        return map;
    }

    public static <T extends JsonSerializable> List<T> arrayFromGetRequest(Class<T> cls, String endPoint, JSONObject headers) throws UnauthorizedException {
        List<T> map = new ArrayList<>();
        HttpURLConnection con = null;
        try {
            con = buildConnection("GET", endPoint, headers, false);
            if (con.getResponseCode() / 100 == 2) {
                int size = Integer.parseInt(con.getHeaderField("X-Total-Count"));
                map = MultipartHandler.readObjectArray(con.getInputStream(), cls, size);

            } else if (con.getResponseCode() == 401) {
                throw new UnauthorizedException("JWT invalid");
            }
        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception ignored) {
        } finally {
            if (con != null) con.disconnect();
        }
        return map;
    }

    public static void sendMediaLinkedRequest(String method, String endPoint, JSONObject headers, MediaLinked mediaLinked, File file) throws NotAcceptableException, UnauthorizedException {
        HttpURLConnection con = null;
        try {
            con = buildConnection(method, endPoint, headers, true);
            OutputStream os = con.getOutputStream();
            MultipartHandler.writeObject(os, mediaLinked);
            MultipartHandler.writeFromFile(os, file);
            os.close();

            if (con.getResponseCode() == 401) {
                throw new UnauthorizedException("JWT invalid");
            } else if (con.getResponseCode() / 100 != 2) {
                throw new NotAcceptableException("Unknown");
            }
        } catch (IOException e) {
            throw new NotAcceptableException("Unknown");
        } finally {
            if (con != null) con.disconnect();
        }
    }

    public static void sendFileRequest(String method, String endPoint, JSONObject headers, File file) throws NotAcceptableException, UnauthorizedException {
        HttpURLConnection con = null;
        try {
            con = buildConnection(method, endPoint, headers, true);
            OutputStream os = con.getOutputStream();
            MultipartHandler.writeFromFile(os, file);
            os.close();

            if (con.getResponseCode() == 401) {
                throw new UnauthorizedException("JWT invalid");
            } else if (con.getResponseCode() / 100 != 2) {
                throw new NotAcceptableException("Unknown");
            }
        } catch (IOException e) {
            throw new NotAcceptableException("Unknown");
        } finally {
            if (con != null) con.disconnect();
        }
    }

    public static void sendJsonRequest(String method, String endPoint, JSONObject headers, JSONObject obj) throws UnauthorizedException, NotAcceptableException {
        HttpURLConnection con = null;
        try {
            con = buildConnection(method, endPoint, headers, true);
            JsonHandler.sendObject(con.getOutputStream(), obj);
            con.getOutputStream().close();

            if (con.getResponseCode() == 401) {
                throw new UnauthorizedException("JWT invalid");
            } else if (con.getResponseCode() / 100 != 2) {
                throw new NotAcceptableException("Unknown");
            }
        } catch (IOException | NotAcceptableException e) {
            throw new NotAcceptableException("Unknown");
        } finally {
            if (con != null) con.disconnect();
        }
    }

    public static JSONObject buildHeadRequest(String endPoint, JSONObject headers) {
        HttpURLConnection con = null;
        try {
            con = buildConnection("HEAD", endPoint, headers, false);

            if (con.getResponseCode() == 200) {
                JSONObject jsonObject = new JSONObject();
                con.getHeaderFields().forEach((k, v) -> {
                    if (k != null && !k.equalsIgnoreCase("null")) jsonObject.put(k, v.getFirst());
                });
                return jsonObject;
            } else if (con.getResponseCode() == 401) {
                throw new UnauthorizedException("Unknown");
            }
        } catch (Exception ignored) {
        } finally {
            if (con != null) con.disconnect();
        }
        return null;
    }
}
