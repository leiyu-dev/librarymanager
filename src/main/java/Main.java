import com.sun.net.httpserver.HttpServer;
import handler.*;
import library.Library;
import utils.ConnectConfig;
import utils.DatabaseConnector;

import java.net.InetSocketAddress;
import java.util.Scanner;
import java.util.logging.Logger;
import static library.Library.SetLibrary;

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

            HttpServer server = HttpServer.create(new InetSocketAddress(8010),0);
            server.createContext("/card",new CardHandler());
            server.createContext("/book",new BookHandler());
            server.createContext("/book/inc",new IncBookHandler());
            server.createContext("/book/mstore",new MassiveStoreBookHandler());
            server.createContext("/book/borrow",new BorrowBookHandler());
            server.createContext("/book/return",new ReturnBookHandler());
            server.createContext("/borrow/history",new ShowBorrowHistoryHandler());
            server.start();
            System.out.println("Opened");

            Scanner scanner = new Scanner(System.in);
            String get ;
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
                if(get.equals("reset database")){
                    Library.library.resetDatabase();
                    System.out.println("Reset Successfully");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
