import entities.Book;
import queries.ApiResult;
import queries.BookQueryConditions;
import utils.ConnectConfig;
import utils.DatabaseConnector;

import java.util.logging.Logger;

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
            LibraryManagementSystemImpl libmana = new LibraryManagementSystemImpl(connector);
            libmana.resetDatabase();
            ApiResult rlt = libmana.storeBook(new Book("Com. Sci. ", "Database System", "Leiyu PresS",
                    1998, "Lei_Yu",  114.514, 1));
            System.out.println(rlt.message);
            BookQueryConditions bq = new BookQueryConditions();
            bq.setAuthor("Lei_Yu");
            rlt = libmana.queryBook(bq);
            System.out.println(rlt.message);
            // release database connection handler
            if (connector.release()) {
                log.info("Success to release connection.");
            } else {
                log.warning("Failed to release connection.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
