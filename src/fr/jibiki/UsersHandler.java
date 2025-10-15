package fr.jibiki;

import java.io.IOException;
import java.io.OutputStream;
import com.sun.net.httpserver.HttpExchange;

public class UsersHandler implements com.sun.net.httpserver.HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String method = t.getRequestMethod();
            String path = t.getRequestURI().getPath();
            String params = t.getRequestURI().getQuery();
            System.out.println("Méthode : "+method + " URI : " + path + " params : " + params);
            String response = "This is the response: ";
            response = response.concat(Database.getUsers());
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

}
