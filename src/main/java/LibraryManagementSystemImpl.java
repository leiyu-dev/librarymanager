import entities.Book;
import entities.Borrow;
import entities.Card;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import queries.*;
import utils.DBInitializer;
import utils.DatabaseConnector;

import javax.xml.transform.Result;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LibraryManagementSystemImpl implements LibraryManagementSystem {

    private final DatabaseConnector connector;

    public LibraryManagementSystemImpl(DatabaseConnector connector) {
        this.connector = connector;
    }
    private List<Book> ToBook(ResultSet rst){
        try {
            List<Book>books = new ArrayList<>();
            while(rst.next()) {
                Book book = new Book(rst.getString("category"),
                        rst.getString("title"),
                        rst.getString("press"),
                        rst.getInt("publish_year"),
                        rst.getString("author"),
                        rst.getDouble("price"),
                        rst.getInt("stock"));
                book.setBookId(rst.getInt("book_id"));
                books.add(book);
            }
            return books;
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    private ResultSet findEqualBook(Book book) {
        Connection conn = connector.getConn();
        try {
            String sql = "select * from book where category=? and title=? and press=? and author=? and publish_year=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, book.getCategory());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getPress());
            stmt.setString(4, book.getAuthor());
            stmt.setInt(5, book.getPublishYear());
            ResultSet rs = stmt.executeQuery();
            commit(conn);
            return rs;
        }
        catch (Exception e){
            rollback(conn);//todo: throw the error to the last function
            return null;
        }
    }
    private ResultSet findEqualCard(Card card){
        Connection conn = connector.getConn();
        try {
            String sql = "select * from card where name=? and department=? and type=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, card.getName());
            stmt.setString(2, card.getDepartment());
            stmt.setString(3, card.getType().getStr());
            ResultSet rs = stmt.executeQuery();
            commit(conn);
            return rs;
        }
        catch (Exception e){
            rollback(conn);//todo: throw the error to the last function
            return null;
        }
    }

    @Override
    public ApiResult storeBook(Book book) {//register a book to database
        Connection conn = connector.getConn();
        try {
            ResultSet rs = findEqualBook(book);
            if (rs != null && rs.next()) throw new Exception("Duplicated Book");
        } catch (Exception e) {
            rollback(conn);
            return new ApiResult(false, "search error:" + e.getMessage());
        }
        try {
            String sql = "insert into book(category,title,press,publish_year,author,price,stock)" +
                    " values(?,?,?,?,?,?,?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, book.getCategory());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getPress());
            stmt.setInt(4, book.getPublishYear());
            stmt.setString(5, book.getAuthor());
            stmt.setDouble(6, book.getPrice());
            stmt.setInt(7, book.getStock());
            stmt.execute();
            ResultSet rs = findEqualBook(book);
            if (rs == null || !rs.next()) throw new Exception("Can not find the inserted data");
            book.setBookId(rs.getInt("book_id"));
            commit(conn);
            return new ApiResult(true, "Successfully Store Book");
        } catch (Exception e) {
            rollback(conn);
            e.printStackTrace();
            return new ApiResult(false, e.getMessage());
        }

    }

    @Override
    public ApiResult incBookStock(int bookId, int deltaStock) {
        Connection conn = connector.getConn();
        int now_stock;
        try {
            String SqlSearchId = "select * from book where book_id=?";
            PreparedStatement StateSearchId = conn.prepareStatement(SqlSearchId);
            StateSearchId.setInt(1, bookId);
            ResultSet rs = StateSearchId.executeQuery();
            if (!rs.next()) throw new Exception("Cannot find the book");
            now_stock = rs.getInt("stock");
            if (now_stock + deltaStock < 0) throw new Exception("Do not have such amount of books");
        } catch (Exception e) {
            return new ApiResult(false, "check error:" + e.getMessage());
        }
        try {
            String sqlUpdateStock = "update book set stock=? where book_id=?";
            PreparedStatement StateUpdateStock = conn.prepareStatement(sqlUpdateStock);
            StateUpdateStock.setInt(1, now_stock + deltaStock);
            StateUpdateStock.setInt(2, bookId);
            StateUpdateStock.execute();
            commit(conn);
        } catch (Exception e) {
            rollback(conn);
            e.printStackTrace();
            return new ApiResult(false, "add stock error:" + e.getMessage());
        }
        return new ApiResult(true, "Successfully Increase BookStock");
    }

    @Override
    public ApiResult storeBook(List<Book> books) {
        Connection conn = connector.getConn();
        String Addsql = "insert into book(category,title,press,publish_year,author,price,stock)" +
                " values(?,?,?,?,?,?,?)";
        PreparedStatement StmtAdd;
        try {
            StmtAdd = conn.prepareStatement(Addsql);
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResult(false, "Statement error");
        }

        for (int i = 0; i < books.size(); i++) {
            Book book = books.get(i);
            try {
                ResultSet rs = findEqualBook(book);
                if (rs != null && rs.next()) throw new Exception("Duplicated Book");
            } catch (Exception e) {
                return new ApiResult(false, "search error at book :" + i + e.getMessage());
            }

            try {
                StmtAdd.setString(1, book.getCategory());
                StmtAdd.setString(2, book.getTitle());
                StmtAdd.setString(3, book.getPress());
                StmtAdd.setInt(4, book.getPublishYear());
                StmtAdd.setString(5, book.getAuthor());
                StmtAdd.setDouble(6, book.getPrice());
                StmtAdd.setInt(7, book.getStock());
                StmtAdd.addBatch();
            } catch (Exception e) {
                e.printStackTrace();
                return new ApiResult(false, "add book error at " + i + e.getMessage());
            }
        }
        try {
            StmtAdd.executeBatch();
            for (int i = 0; i < books.size(); i++) {
                Book book = books.get(i);
                ResultSet rs = findEqualBook(book);
                if (rs == null || !rs.next()) throw new Exception("Can not find the inserted data");
                book.setBookId(rs.getInt("book_id"));
            }
            commit(conn);
            return new ApiResult(true, "Successfully StoreMassiveBook");
        } catch (Exception e) {
            rollback(conn);
            return new ApiResult(false, "Add failed:" + e.getMessage());
        }

    }

    @Override
    public ApiResult removeBook(int bookId) {
        /*
        TODO: here we need to check if the book is not returned by someone
         */
        Connection conn = connector.getConn();
        try {
            String sql = "delete from book where book_id=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, bookId);
            stmt.execute();
        } catch (Exception e) {
            return new ApiResult(false, "Remove error:" + e.getMessage());
        }
        return new ApiResult(true, "Successfully RemoveBook");
    }

    @Override
    public ApiResult modifyBookInfo(Book book) {
        Connection conn = connector.getConn();
        try {
            Statement StmtFind = conn.createStatement();
            ResultSet rst = StmtFind.executeQuery("select * from book where book_id=" + book.getBookId());
            if(!rst.next()) throw new Exception("can not find the book");
            String UpdateSql = "update book set category=?,title=?,press=?,publish_year=?,author=?,price=? where book_id=?";
            PreparedStatement StmtUpdate = conn.prepareStatement(UpdateSql);
            StmtUpdate.setString(1, book.getCategory());
            StmtUpdate.setString(2, book.getTitle());
            StmtUpdate.setString(3, book.getPress());
            StmtUpdate.setInt(4, book.getPublishYear());
            StmtUpdate.setString(5, book.getAuthor());
            StmtUpdate.setDouble(6, book.getPrice());
            StmtUpdate.setInt(7, book.getBookId());
            StmtUpdate.execute();
            commit(conn);
        } catch (Exception e) {
            rollback(conn);
            return new ApiResult(false, "Modify error:" + e.getMessage());
        }
        return new ApiResult(true, "Successfully ModifyBookInfo");
    }

    @Override
    public ApiResult queryBook( BookQueryConditions conditions) {
        String sql = "select * from book ";
        try {
            Connection conn = connector.getConn();
            String SelectCategory = " category=\"" + conditions.getCategory()+"\" ";
            String SelectTitle = " title=\"" + conditions.getTitle()+"\" ";
            String SelectPress = " Press=\"" + conditions.getPress()+"\" ";
            String SelectYear = conditions.getMinPublishYear() + "<=publish_year and publish_year <=" + conditions.getMaxPublishYear();
            String SelectAuthor = " author=\"" + conditions.getAuthor()+"\" ";
            String SelectPrice = conditions.getMinPrice() + "<=price and price<=" + conditions.getMaxPrice();
            String SelectSortBy = " order by " + conditions.getSortBy() + " " + conditions.getSortOrder();
            ResultSet rst;
            boolean FirstCondition = true;
            if (conditions.getCategory() != null) {
                if (!FirstCondition) sql += " and ";
                else sql+=" where ";
                FirstCondition = false;
                sql += SelectCategory;
            }
            if (conditions.getTitle() != null) {
                if (!FirstCondition) sql += " and ";
                else sql+=" where ";
                FirstCondition = false;
                sql += SelectTitle;
            }
            if (conditions.getPress() != null) {
                if (!FirstCondition) sql += " and ";
                else sql+=" where ";
                FirstCondition = false;
                sql += SelectPress;
            }
            if (conditions.getMinPublishYear() != null) {
                if (!FirstCondition) sql += " and ";
                else sql+=" where ";
                FirstCondition = false;
                sql += SelectYear;
            }
            if (conditions.getAuthor() != null) {
                if (!FirstCondition) sql += " and ";
                else sql+=" where ";
                FirstCondition = false;
                sql += SelectAuthor;
            }
            if (conditions.getMinPrice() != null) {
                if (!FirstCondition) sql += " and ";
                else sql+=" where ";
                FirstCondition = false;
                sql += SelectPrice;
            }
            if (conditions.getSortBy() != null)
                sql += SelectSortBy;
            Statement StmtFind = conn.createStatement();
            rst = StmtFind.executeQuery(sql);
            List<Book>books=ToBook(rst);
            return new ApiResult(true, "Successfully Query "+books.size()+" Books",new BookQueryResults(books));
        } catch (Exception e) {
            return new ApiResult(false, "Query Failed:" + e.getMessage()+"\n with the query sql:"+ sql);
        }
    }
    @Override
    public ApiResult borrowBook(Borrow borrow) {
        Connection conn = connector.getConn();
        try {

        } catch (Exception e) {
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(false, "Successfully BorrowBook");
    }

    @Override
    public ApiResult returnBook(Borrow borrow) {
        Connection conn = connector.getConn();
        try {

        } catch (Exception e) {
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(false, "Successfully ReturnBook");
    }

    @Override
    public ApiResult showBorrowHistory(int cardId) {
        Connection conn = connector.getConn();
        try {

        } catch (Exception e) {
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(false, "Successfully ShowBorrowHistory");
    }

    @Override
    public ApiResult registerCard(Card card) {
        Connection conn = connector.getConn();
        try {
            ResultSet rs = findEqualCard(card);
            if (rs != null && rs.next()) throw new Exception("Duplicated card");
        } catch (Exception e) {
            rollback(conn);
            return new ApiResult(false, "search error:" + e.getMessage());
        }
        try {
            String sql = "insert into card(name, department, type)" +
                    " values(?,?,?,?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, card.getName());
            stmt.setString(2, card.getDepartment());
            stmt.setString(3, card.getType().getStr());
            stmt.execute();
            ResultSet rs = findEqualCard(card);
            if (rs == null || !rs.next()) throw new Exception("Can not find the inserted data");
            card.setCardId(rs.getInt("card_id"));
            commit(conn);
            return new ApiResult(true, "Successfully Add card");
        } catch (Exception e) {
            rollback(conn);
            e.printStackTrace();
            return new ApiResult(false, e.getMessage());
        }
    }

    @Override
    public ApiResult removeCard(int cardId) {
        Connection conn = connector.getConn();
        try {

        } catch (Exception e) {
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(false, "Successfully RemoveCard");
    }

    @Override
    public ApiResult showCards() {
        Connection conn = connector.getConn();
        try {

        } catch (Exception e) {
            return new ApiResult(false, e.getMessage());
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
