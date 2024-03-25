import entities.Book;
import entities.Borrow;
import entities.Card;
import queries.*;
import utils.DBInitializer;
import utils.DatabaseConnector;

import javax.xml.transform.Result;
import java.sql.*;
import java.util.List;

public class LibraryManagementSystemImpl implements LibraryManagementSystem {

    private final DatabaseConnector connector;
    public LibraryManagementSystemImpl(DatabaseConnector connector) {
        this.connector = connector;
    }

    @Override
    public ApiResult storeBook(Book book) {//register a book to database
        Connection conn= connector.getConn();

        try {
            String sql="select title from book where category=? and title=? and press=? and author=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, book.getCategory());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getPress());
            stmt.setString(4, book.getAuthor());
            ResultSet rs = stmt.executeQuery();
            if(rs.getString("title")==book.getTitle())throw new Exception("Duplicated Book");
        }
        catch (Exception e){
            return new ApiResult(false,"search error:"+e.getMessage());
        }

        try {
            String sql = "insert into book(category,title,press,publish_year,author,price,stock)" +
                    " values(?,?,?,?,?,?,?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1,book.getCategory());
            stmt.setString(2,book.getTitle());
            stmt.setString(3, book.getPress());
            stmt.setInt(4,book.getPublishYear());
            stmt.setString(5,book.getAuthor());
            stmt.setDouble(6,book.getPrice());
            stmt.setInt(7,book.getStock());
            stmt.execute();
            commit(conn);
        }
        catch (Exception e){
            rollback(conn);
            e.printStackTrace();
            return new ApiResult(false,e.getMessage());
        }
        return new ApiResult(true, "Successfully Store Book");
    }

    @Override
    public ApiResult incBookStock(int bookId, int deltaStock) {
        Connection conn= connector.getConn();
        int now_stock;
        try{
            String SqlSearchId="select * from book where book_id=?";
            PreparedStatement StateSearchId = conn.prepareStatement(SqlSearchId);
            StateSearchId.setInt(1,bookId);
            ResultSet rs=StateSearchId.executeQuery();
            if(rs.getInt("bookId")!=bookId)throw new Exception("Cannot find the book");
            now_stock=rs.getInt("stock");
            if(now_stock+deltaStock<0)throw new Exception("Do not have such amount of books");
        }
        catch (Exception e){
            return new ApiResult(false,  "check error:"+e.getMessage());
        }
        try{
            String sqlUpdateStock="update book set stock=? where book_id=?";
            PreparedStatement StateUpdateStock = conn.prepareStatement(sqlUpdateStock);
            StateUpdateStock.setInt(1,now_stock+deltaStock);
            StateUpdateStock.setInt(2,bookId);
            commit(conn);
        }
        catch (Exception e){
            rollback(conn);
            return new ApiResult(false,"add stock error:"+e.getMessage());
        }
        return new ApiResult(false, "Successfully Increase BookStock");
    }

    @Override
    public ApiResult storeBook(List<Book> books) {
        Connection conn= connector.getConn();
        try{

            for (int i=0;i<books.size();i++){

                Book book = books.get(i);

            }
        }
        catch (Exception e){
            return new ApiResult(false, "Add failed:"+e.getMessage());
        }
        return new ApiResult(false, "Successfully StoreMassiveBook");
    }

    @Override
    public ApiResult removeBook(int bookId) {
        Connection conn= connector.getConn();
        try{

        }
        catch (Exception e){
            return new ApiResult(false,  e.getMessage());
        }
        return new ApiResult(false, "Successfully RemoveBook");
    }

    @Override
    public ApiResult modifyBookInfo(Book book) {
        Connection conn= connector.getConn();
        try{

        }
        catch (Exception e){
            return new ApiResult(false,  e.getMessage());
        }
        return new ApiResult(false, "Successfully ModifyBookInfo");
    }

    @Override
    public ApiResult queryBook(BookQueryConditions conditions) {
        Connection conn= connector.getConn();
        try{

        }
        catch (Exception e){
            return new ApiResult(false,  e.getMessage());
        }
        return new ApiResult(false, "Successfully QueryBook");
    }

    @Override
    public ApiResult borrowBook(Borrow borrow) {
        Connection conn= connector.getConn();
        try{

        }
        catch (Exception e){
            return new ApiResult(false,  e.getMessage());
        }
        return new ApiResult(false, "Successfully BorrowBook");
    }

    @Override
    public ApiResult returnBook(Borrow borrow) {
        Connection conn= connector.getConn();
        try{

        }
        catch (Exception e){
            return new ApiResult(false,  e.getMessage());
        }
        return new ApiResult(false, "Successfully ReturnBook");
    }

    @Override
    public ApiResult showBorrowHistory(int cardId) {
        Connection conn= connector.getConn();
        try{

        }
        catch (Exception e){
            return new ApiResult(false,  e.getMessage());
        }
        return new ApiResult(false, "Successfully ShowBorrowHistory");
    }

    @Override
    public ApiResult registerCard(Card card) {
        Connection conn= connector.getConn();
        try{

        }
        catch (Exception e){
            return new ApiResult(false,  e.getMessage());
        }
        return new ApiResult(false, "Successfully RegisterCard");
    }

    @Override
    public ApiResult removeCard(int cardId) {
        Connection conn= connector.getConn();
        try{

        }
        catch (Exception e){
            return new ApiResult(false,  e.getMessage());
        }
        return new ApiResult(false, "Successfully RemoveCard");
    }

    @Override
    public ApiResult showCards() {
        Connection conn= connector.getConn();
        try{

        }
        catch (Exception e){
            return new ApiResult(false,  e.getMessage());
        }
        return new ApiResult(false, "Successfully ShowCards");
    }

    @Override
    public ApiResult resetDatabase() {
        Connection conn = connector.getConn();
        try {
            Statement stmt = conn.createStatement();
            DBInitializer initializer = connector.getConf().getType().getDbInitializer();
            stmt.addBatch(initializer.sqlDropBorrow());
            stmt.addBatch(initializer.sqlDropBook());
            stmt.addBatch(initializer.sqlDropCard());
            stmt.addBatch(initializer.sqlCreateCard());
            stmt.addBatch(initializer.sqlCreateBook());
            stmt.addBatch(initializer.sqlCreateBorrow());
            stmt.executeBatch();
            commit(conn);
        } catch (Exception e) {
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, null);
    }

    private void rollback(Connection conn) {
        try {
            conn.rollback();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void commit(Connection conn) {
        try {
            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
