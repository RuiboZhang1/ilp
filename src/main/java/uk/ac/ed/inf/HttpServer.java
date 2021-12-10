package uk.ac.ed.inf;


import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Establish the connection to the http server
 */
public class HttpServer {

    // Just have one HttpServer, shared between all HttpRequests
    private static final HttpClient client = HttpClient.newHttpClient();

    // private variables
    private String name;
    private String port;
    private String jsonResponse;

    /**
     * Constructor of the Http Server
     * @param name machine name of the server
     * @param port port of the server
     */
    public HttpServer(String name, String port) {
        this.name = name;
        this.port = port;
    }

    // getter
    public String getHttpServer() {
        return ("http://" + this.name + ":" + this.port);
    }

    public String getJsonResponse() {
        return this.jsonResponse;
    }

    /**
     * launch http request given the url, and store the response in the variable.
     * @param url the url string for request
     */
    public void retrieveJsonFromServer(String url) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // request success
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
