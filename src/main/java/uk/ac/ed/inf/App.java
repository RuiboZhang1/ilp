package uk.ac.ed.inf;

public class App {
    private static final String NAME = "localhost";



    public static void main(String[] args) {
        // reading the args
        String day = args[0];
        String month = args[1];
        String year = args[2];
        String websitePort = args[3];
        String databasePort = args[4];

        // initialise objects
        HttpServer httpServer = new HttpServer(NAME, websitePort);
        JsonParser jsonParser = new JsonParser(httpServer);
        Database dataBase = new Database(NAME, databasePort);
        Drone drone = new Drone();
    }
}
