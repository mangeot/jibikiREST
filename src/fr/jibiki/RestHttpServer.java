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
        int port = Integer.parseInt(appProps.getProperty("API_PORT"));
        String portEnv = System.getenv("API_PORT");
        if (portEnv != null && !portEnv.equals("")) {
            port = Integer.parseInt(portEnv);
        }
        String dbhost = appProps.getProperty("DATABASE_HOST");
        String dbhostEnv = System.getenv("DATABASE_HOST");
        if (dbhostEnv != null && !dbhostEnv.equals("")) {
            dbhost = dbhostEnv;
        }
        String dbname = appProps.getProperty("DATABASE_NAME");
        String dbnameEnv = System.getenv("DATABASE_NAME");
        if (dbnameEnv != null && !dbnameEnv.equals("")) {
            dbname = dbnameEnv;
        }
        String dburl = "jdbc:postgresql://"+dbhost+"/"+dbname;
        String dbuser = appProps.getProperty("DATABASE_USER");
        String dbuserEnv = System.getenv("DATABASE_USER");
        if (dbuserEnv != null && !dbuserEnv.equals("")) {
            dbuser = dbuserEnv;
        }
        String dbpassword = appProps.getProperty("DATABASE_PASSWORD");
        String dbpasswordEnv = System.getenv("DATABASE_PASSWORD");
        if (dbpasswordEnv != null && !dbpasswordEnv.equals("")) {
            dbpassword = dbpasswordEnv;
        }
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

     public static String encodeXMLEntities(String theString) {
        if (null != theString && !theString.equals("")) {
            String tmpString = "";
            while (null != theString && theString.indexOf("&") >= 0) {
                tmpString = theString.substring(0,theString.indexOf("&"))
                        + "&amp;";
                theString = theString.substring(theString.indexOf("&") + 1);
            }
            theString = tmpString + theString;

            while (theString.indexOf("'") >= 0 || theString.indexOf("\"") >= 0 
                || theString.indexOf("<") >= 0 ||theString.indexOf(">") >= 0) {
                if (theString.indexOf("'") >= 0) {
                    theString = theString.substring(0,theString.indexOf("'"))
                        + "&#39;" + theString.substring(theString.indexOf("'") + 1);
                }
                if (theString.indexOf("\"") >= 0) {
                    theString = theString.substring(0,theString.indexOf("\""))
                        + "&quot;" + theString.substring(theString.indexOf("\"") + 1);
                }
                if (theString.indexOf("<") >= 0) {
                    theString = theString.substring(0,theString.indexOf("<"))
                        + "&lt;" + theString.substring(theString.indexOf("<") + 1);
                }
                if (theString.indexOf(">") >= 0) {
                    theString = theString.substring(0,theString.indexOf(">"))
                        + "&gt;" + theString.substring(theString.indexOf(">") + 1);
                } 
            }
        }
    return theString;
    }    

}