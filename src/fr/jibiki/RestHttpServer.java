/*
 * In order to compile :
 * javac *.java
 * javac -d . *.java
 * java -cp postgresql-42.7.8.jar:.  fr.jibiki.RestHttpServer
 */

package fr.jibiki;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.sun.net.httpserver.HttpServer;

public class RestHttpServer {

    public static String usersApi = "/apiusers/";
    public static String entriesApi = "/api/";

    public static String JSON_CONTENTTYPE = "text/json";
    public static String XML_CONTENTTYPE = "text/xml";

    public static void main(String[] args) throws Exception {
        Properties appProps = new Properties();
        appProps.load(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("fr/jibiki/RestHttpServer.properties"));
        int port = Integer.parseInt(appProps.getProperty("port"));
        String dburl = appProps.getProperty("dburl");
        String dbuser = appProps.getProperty("dbuser");
        String dbpassword = appProps.getProperty("dbpassword");
        Database.connect(dburl, dbuser, dbpassword);
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext(usersApi, new UsersHandler());
        server.createContext(entriesApi, new EntriesHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("Server started at port: " + server.getAddress().getPort());
    }

    public static HashMap parseQueryString(String queryString) {
        if (queryString == null) {
            return null;
        }

        HashMap resultMap = new HashMap();

        for (String param : queryString.split("&")) {
            String[] keyValue = param.split("=");

            if (keyValue.length > 1) {
                resultMap.put(keyValue[0], keyValue[1]);
            } else {
                resultMap.put(keyValue[0], "");
            }
        }

        return resultMap;
    }

}