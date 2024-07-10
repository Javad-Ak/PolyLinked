package org.aut;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import org.aut.dataAccessors.DataBaseAccessor;
import org.aut.httpHandlers.FollowHandler;
import org.aut.httpHandlers.LoginHandler;
import org.aut.httpHandlers.ProfileHandler;
import org.aut.httpHandlers.UserHandler;
import org.aut.httpHandlers.*;

/**
 * Server of PolyLinked - A simulation of LinkedIn
 *
 * @author AliReza Atharifard, MohammadJavad Akbari
 * @version 2.0
 */
public class Server {
    public static void main(String[] args) {
        try {
            DataBaseAccessor.create();
            HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 8080), 8);

            server.createContext("/users", new UserHandler());
            server.createContext("/users/login", new LoginHandler());
            server.createContext("/users/profiles", new ProfileHandler());
            server.createContext("/users/educations", new EducationHandler());
            server.createContext("/users/skills", new SkillHandler());
            server.createContext("/users/followers", new FollowersHandler());
            server.createContext("/users/followings" , new FollowingsHandler());
            server.createContext("/users/callInfo" , new CallInfoHandler());

            server.createContext("/follows", new FollowHandler());
            server.createContext("/connections", new ConnectHandler());
            server.createContext("/posts", new PostHandler());
            server.createContext("/likes", new LikeHandler());
            server.createContext("/comments", new CommentHandler());
            server.createContext("/messages", new MessageHandler());
            server.createContext("/newsfeed", new NewsFeedHandler());

            server.createContext("/search", new SearchHandler());
            server.createContext("/resources", new ResourceHandler());

            server.setExecutor(Executors.newFixedThreadPool(8));
            server.start();
            Runtime.getRuntime().addShutdownHook(new DataBaseAccessor("atExit"));

            System.out.println("Server is running at: " + server.getAddress());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}