import com.sun.net.httpserver.HttpServer;
import handler.CardHandler;
import utils.ConnectConfig;
import utils.DatabaseConnector;
import handler.Library;
import java.net.InetSocketAddress;
import java.util.Scanner;
import java.util.logging.Logger;
import static handler.Library.SetLibrary;

public class Main {


    private static final Logger log = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        try {
            // parse connection config from "resources/application.yaml"
            ConnectConfig conf = new ConnectConfig();
            log.info("Success to parse connect config. " + conf.toString());
            // connect to database
            DatabaseConnector connector = new DatabaseConnector(conf);
            boolean connStatus = connector.connect();
            if (!connStatus) {
                log.severe("Failed to connect database.");
                System.exit(1);
            }
            /* do somethings */
            SetLibrary(connector);
            Library.library.resetDatabase();

            HttpServer server = HttpServer.create(new InetSocketAddress(8010),0);
            server.createContext("/card",new CardHandler());
            server.start();
            System.out.println("Opened");

            Scanner scanner = new Scanner(System.in);
            String get = null;
            while(true)
            {
                get=scanner.nextLine();
                if(get.equals("stop")){
                    if (connector.release()) {
                        log.info("Success to release connection.");
                    } else {
                        log.warning("Failed to release connection.");
                    }
                    server.stop(0);
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
