package org.aut.utils;

import org.aut.models.JsonSerializable;
import org.aut.models.MediaLinked;
import org.aut.models.User;
import org.aut.utils.exceptions.NotAcceptableException;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class MultipartHandler {
    private MultipartHandler() {
    }

    public static <T extends JsonSerializable> void writeObject(OutputStream outputStream, T obj) throws IOException {
        writeHeaders(outputStream, obj.getClass().getSimpleName() + "/json", obj.toJson().toString().getBytes().length);
        outputStream.write(obj.toJson().toString().getBytes());
        outputStream.flush();
    }

    public static void writeFromFile(OutputStream outputStream, File file) throws IOException, NotAcceptableException {
        if (file == null || file.length() < 1) {
            writeHeaders(outputStream, "null/file", 0);
            return;
        }

        int length = (int) file.length();
        if (!file.getName().contains(".") || file.getName().endsWith("."))
            throw new NotAcceptableException("Invalid Type");

        writeHeaders(outputStream, file.getName().substring(file.getName().lastIndexOf(".") + 1) + "/file", length);

        FileInputStream inputStream = new FileInputStream(file);
        linkStreams(inputStream, outputStream, length);
        inputStream.close();
    }

    public static <T extends MediaLinked> void writeMap(OutputStream outputStream, TreeMap<T, User> map) throws IOException {
        for (T obj : map.keySet()) {
            writeObject(outputStream, obj);
            writeObject(outputStream, map.get(obj));
        }
        outputStream.flush();
    }

    public static <T extends JsonSerializable> void writeObjectArray(OutputStream outputStream, List<T> array) throws IOException {
        for (T obj : array) writeObject(outputStream, obj);
    }

    public static File readToFile(InputStream inputStream, Path dir, String fileName) throws IOException, NotAcceptableException {
        JSONObject headers = readJson(inputStream);
        String[] type = headers.getString("Content-Type").split("/");
        int length = headers.getInt("Content-Length");
        if (length == 0) return null;
        if (!type[1].equals("file")) throw new NotAcceptableException("Invalid Content-Type");

        File file = new File(dir + "/" + fileName + "." + type[0]);
        FileOutputStream outputStream = new FileOutputStream(file, false);
        int remained = length;
        byte[] buffer = new byte[1000000];
        while (remained > 0) {
            if (remained < 1100000) {
                byte[] bytes = new byte[remained];
                int read = inputStream.read(bytes);
                if (read == -1) break;

                outputStream.write(bytes, 0, read);
                remained -= read;
            } else {
                int read = inputStream.read(buffer);
                if (read == -1) break;

                outputStream.write(buffer, 0, read);
                remained -= read;
            }
        }
        outputStream.flush();
        outputStream.close();
        return file;
    }

    public static <T extends JsonSerializable> T readObject(InputStream inputStream, Class<T> cls) throws IOException, NotAcceptableException {
        JSONObject headers = readJson(inputStream);
        String[] type = headers.getString("Content-Type").split("/");
        if (type.length < 1 || (!type[1].equals("json") || !cls.getSimpleName().equals(type[0])))
            throw new NotAcceptableException("Invalid Content-Type");

        return JsonSerializable.fromJson(readJson(inputStream), cls);
    }

    public static <T extends JsonSerializable> List<T> readObjectArray(InputStream inputStream, Class<T> cls, int count) throws IOException, NotAcceptableException {
        ArrayList<T> array = new ArrayList<>();
        for (int i = 1; i <= count; i++) array.add(readObject(inputStream, cls));
        return array;
    }

    public static <T extends MediaLinked> TreeMap<T, User> readMap(InputStream inputStream, Class<T> cls, int count) throws NotAcceptableException, IOException {
        TreeMap<T, User> map = new TreeMap<>();
        for (int i = 0; i < count; i++) {
            T obj = readObject(inputStream, cls);
            map.put(obj, readObject(inputStream, User.class));
        }
        return map;
    }

    private static void writeHeaders(OutputStream outputStream, String type, int length) throws IOException {
        JSONObject headers = new JSONObject();
        headers.put("Content-Type", type);
        headers.put("Content-Length", length);

        outputStream.write(headers.toString().getBytes());
        outputStream.flush();
    }

    private static JSONObject readJson(InputStream inputStream) throws IOException, NotAcceptableException {
        StringBuilder res = new StringBuilder();
        int ch;
        while ((ch = inputStream.read()) != -1) {
            char character = (char) ch;
            res.append(character);
            if (character == '}') break;
        }
        if (res.isEmpty() || res.charAt(0) != '{' || res.charAt(res.length() - 1) != '}') {
            throw new NotAcceptableException("Invalid headers");
        }

        return new JSONObject(res.toString());
    }

    public static void linkStreams(InputStream inputStream, OutputStream outputStream, int length) throws IOException {
        int totalWrite = 0;
        byte[] buffer = new byte[1000000];
        while (totalWrite < length) {
            int read = inputStream.read(buffer);
            if (read == -1) break;

            outputStream.write(buffer, 0, read);
            totalWrite += read;
        }

        outputStream.flush();
    }
}
