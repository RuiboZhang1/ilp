package uk.ac.ed.inf;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;

public class HttpServer {

    // Just have one HttpServer, shared between all HttpRequests
    private static final HttpClient client = HttpClient.newHttpClient();

    // variables
    private String name;
    private String port;
    private String jsonResponse;

    public HttpServer(String name, String port) {
        this.name = name;
        this.port = port;
    }

    public String getHttpServer() {
        return ("http://" + this.name + ":" + this.port);
    }

    public String getJsonResponse() {
        return this.jsonResponse;
    }

    public void retrieveJsonFromServer(String url) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                this.jsonResponse = response.body();
            } else {
                System.err.println("Status code:" + response.statusCode() +
                        ". Unable to retrieve data from the server, please check the status code.");
                System.exit(1);
            }
        } catch (InterruptedException | IOException e) {
            System.err.println("Fatal error: Unable to connect to " + this.name + " at port " +
                    this.port + ".");
            System.exit(1);
        }
    }
}
