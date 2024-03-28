package handler;
import library.LibraryManagementSystem;
import library.LibraryManagementSystemImpl;
import utils.DatabaseConnector;
public class Library {
    public static LibraryManagementSystem library;
    public static void SetLibrary(DatabaseConnector connector){
        library = new LibraryManagementSystemImpl(connector);
    }
}
