package fr.jibiki;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import com.sun.net.httpserver.HttpExchange;

public class EntriesHandler implements com.sun.net.httpserver.HttpHandler {
    @Override
    public void handle(HttpExchange t) throws IOException {
        String method = t.getRequestMethod();
        String path = t.getRequestURI().getPath();
        String params = t.getRequestURI().getQuery();
        String response = "";
        String[] restStrings = null;
        path = path.substring(path.indexOf(RestHttpServer.entriesApi)+RestHttpServer.entriesApi.length());
        System.out.println("path:["+path+"]");
        restStrings = path.split("/");
  
        for (int i = 0; i < restStrings.length; i++) {
            try {
                restStrings[i] = java.net.URLDecoder.decode(restStrings[i], StandardCharsets.UTF_8.name());
            } catch (UnsupportedEncodingException e) {}
            System.out.println("rs:["+i+"]:["+restStrings[i] + "]");
        }
        System.out.println("Méthode : " + method + " URI : " + path + " params : " + params + " rs: " + restStrings.length );
        if (restStrings.length==1 && (restStrings[0].equals("") || restStrings[0].equals("*"))) {
            response =Database.getDictionaries();
        }
        else {
            String dictName = restStrings[0];
            System.out.println("dict: " + dictName);
            if (restStrings.length==1) {
                response = Database.getDictionary(dictName);
            }
            else if (restStrings.length==2) {
                String srclang=restStrings[1];
                response = Database.getVolume(dictName,srclang);
            }
           else if (restStrings.length==3) {
                String srclang=restStrings[1];
                String contribId=restStrings[2];
                response = Database.getEntryId(dictName,srclang,contribId);
            }
        }
        t.getResponseHeaders().set("Content-Type","text/xml; charset=UTF-8");
        //t.getResponseHeaders().put("Accept-Encoding", Arrays.asList("UTF-8"));
        OutputStream os = t.getResponseBody();
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        t.sendResponseHeaders(200, bytes.length);
        os.write(bytes);
        os.close();
    }
}