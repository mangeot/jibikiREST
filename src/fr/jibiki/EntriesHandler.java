package fr.jibiki;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;

import com.sun.net.httpserver.HttpExchange;

public class EntriesHandler implements com.sun.net.httpserver.HttpHandler {

    protected static String STRATEGY_PARAMETER = "strategy";
    protected static String LIMIT_PARAMETER = "count";
    protected static String OFFSET_PARAMETER = "startIndex";
    protected static String ORDERBY_PARAMETER = "sortBy";

    @Override
    public void handle(HttpExchange t) throws IOException {
        String method = t.getRequestMethod();
        String path = t.getRequestURI().getPath();
        String params = t.getRequestURI().getQuery();
        String response = "";
        int responseCode = 200;
        String[] restStrings = null;
        path = path.substring(path.indexOf(RestHttpServer.entriesApi) + RestHttpServer.entriesApi.length());
        System.out.println("path:[" + path + "]");
        restStrings = path.split("/");

        for (int i = 0; i < restStrings.length; i++) {
            try {
                restStrings[i] = java.net.URLDecoder.decode(restStrings[i], StandardCharsets.UTF_8.name());
            } catch (UnsupportedEncodingException e) {
            }
            System.out.println("rs:[" + i + "]:[" + restStrings[i] + "]");
        }
        System.out.println("Méthode : " + method + " URI : " + path + " params : " + params + " rs: " + restStrings.length);
        if (method.equalsIgnoreCase("GET")) {
            if (restStrings.length == 1 && (restStrings[0].equals("") || restStrings[0].equals("*"))) {
                response = Database.getDictionaries();
            } else {
                String dictName = restStrings[0];
                System.out.println("dict: " + dictName);
                if (restStrings.length == 1) {
                    response = Database.getDictionary(dictName);
                    if (response==null || response.equals("")) {
                        responseCode = 404;
                        System.out.println("No dict " + dictName);
                    }
                } else if (restStrings.length == 2) {
                    String srclang = restStrings[1];
                    response = Database.getVolume(dictName, srclang);
                    if (response==null || response.equals("")) {
                        responseCode = 404;
                        System.out.println("No volume " + dictName + "|" + srclang);
                    }
                } else if (restStrings.length == 3) {
                    String srclang = restStrings[1];
                    String contribId = restStrings[2];
                    response = Database.getEntryId(dictName, srclang, contribId);
                    if (response==null || response.equals("")) {
                        responseCode = 404;
                        System.out.println("No entryid " + dictName + "|" + srclang + " id: " + contribId);
                    }
                } else if (restStrings.length > 3) {
                    System.out.println("taille reststring: " + restStrings.length);
                    String srclang = restStrings[1];
                    String mode = restStrings[2];
                    String string = restStrings[3];
                    String strategy = null;
                    String limit = null;
                    String offset = null;
                    String orderby = null;
                    String key = null;
                    HashMap paramsMap = RestHttpServer.parseQueryString(params);

                    if (paramsMap != null) {
                        System.out.println("paramsMap: " + paramsMap.size());
                        strategy = (String) paramsMap.get(STRATEGY_PARAMETER);
                        limit = (String) paramsMap.get(LIMIT_PARAMETER);
                        offset = (String) paramsMap.get(OFFSET_PARAMETER);
                        orderby = (String) paramsMap.get(ORDERBY_PARAMETER);
                    }
                    if (restStrings.length == 5) {
                        key = restStrings[4];
                    }
                    response = Database.getEntries(dictName, srclang, mode, string, key, strategy, limit, offset,
                            orderby);
                } else {
                    // erreur
                    System.out.println("Erreur ici !");
                }
            }
        }
        else if (method.equalsIgnoreCase("OPTIONS")) {
            t.getResponseHeaders().set("Access-Control-Allow-Methods","GET, OPTIONS");
            t.getResponseHeaders().set("Allow","GET, OPTIONS");
        }
        else {
            String errorMsg = "Error: method not implemented";
            responseCode = 501;
            response = "<?xml version='1.0'?><html><h1>Error: " + responseCode + "</h1><p>" + errorMsg + "</p></html>";
        }
        t.getResponseHeaders().set("Content-Type", "text/xml; charset=UTF-8");
        t.getResponseHeaders().put("Accept-Encoding", Arrays.asList("UTF-8"));
        OutputStream os = t.getResponseBody();
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        t.sendResponseHeaders(responseCode, bytes.length);
        os.write(bytes);
        os.close();
    }
}