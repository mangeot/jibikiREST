package fr.jibiki;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;

import com.sun.net.httpserver.HttpExchange;

public class UsersHandler implements com.sun.net.httpserver.HttpHandler {
    @Override
    public void handle(HttpExchange t) throws IOException {
        String method = t.getRequestMethod();
        String path = t.getRequestURI().getPath();
        String params = t.getRequestURI().getQuery();
        System.out.println("Méthode : " + method + " URI : " + path + " params : " + params);
        int responseCode = 200;
        String response = "";
        String auth = t.getRequestHeaders().getFirst("Authorization");
        if (auth != null) {
            byte[] decodedBytes = Base64.getDecoder().decode(auth.substring(6));
            String pair = new String(decodedBytes);
            String login = pair.split(":")[0];
            String password = pair.split(":")[1];
            User myUser = User.getUser(login);
            if (myUser != null) {
                if (myUser.HasCorrectPassword(password)) {
                    System.out.println("User " + login + " has correct password");
                    System.out.println("Groups " + myUser.groups);
                    if (myUser.groups.indexOf("#admin#") >=0) {
                        response = Database.getUsersForAdmin();
                    } 
                    else {
                        response = Database.getUsers();
                    }
                } else {
                    System.out.println("User " + login + " has wrong password");
                    responseCode = 401;
                }
            } else {
                System.out.println("User " + login + " is unknown");
                responseCode = 401;
            }
        }
        else {
            response = Database.getUsers();
        }

        /*
         * if (!isAuthorized(auth)) {
         * exchange.getResponseHeaders().add("WWW-Authenticate",
         * "Basic realm=\"User Visible Realm\"");
         * exchange.sendResponseHeaders(401, -1);
         * return;
         * }
         */

        t.sendResponseHeaders(responseCode, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
