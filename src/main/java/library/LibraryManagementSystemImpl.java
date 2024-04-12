package library;

import entities.Book;
import entities.Borrow;
import entities.Card;
import queries.*;
import utils.DBInitializer;
import utils.DatabaseConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LibraryManagementSystemImpl implements LibraryManagementSystem {

    private final DatabaseConnector connector;

    public LibraryManagementSystemImpl(DatabaseConnector connector) {
        this.connector = connector;
    }


    private Book getABook(ResultSet rst) {//get one book from the result set
        try {
            Book book = new Book(rst.getString("category"),
                    rst.getString("title"),
                    rst.getString("press"),
                    rst.getInt("publish_year"),
                    rst.getString("author"),
                    rst.getDouble("price"),
                    rst.getInt("stock"));
            book.setBookId(rst.getInt("book_id"));
            return book;
        } catch (Exception e) {

            return null;
        }
    }

    private Borrow getABorrow(ResultSet rst) {
        try {
            Borrow borrow = new Borrow(
                    rst.getInt("book_id"),
                    rst.getInt("card_id")
            );
            borrow.setBorrowTime(rst.getLong("borrow_time"));
            borrow.setReturnTime(rst.getLong("return_time"));
            return borrow;
        } catch (Exception e) {

            return null;
        }
    }

    private List<Book> toBook(ResultSet rst) {
        try {
            List<Book> books = new ArrayList<>();
            while (rst.next()) {
                Book book = getABook(rst);
                books.add(book);
            }
            return books;
        } catch (Exception e) {

            System.out.println(e.getMessage());
            return null;
        }
    }

    private List<Card> toCard(ResultSet rst) {
        try {
            List<Card> cards = new ArrayList<>();
            while (rst.next()) {
                Card card = new Card(rst.getInt("card_id"),
                        rst.getString("name"),
                        rst.getString("department"),
                        Card.CardType.values(rst.getString("type")));
                cards.add(card);
            }
            return cards;
        } catch (Exception e) {

            return null;
        }
    }

    private List<BorrowHistories.Item> toItem(ResultSet rst) {
        Connection conn = connector.getConn();
        try {
            conn.setAutoCommit(false);
            List<BorrowHistories.Item> items = new ArrayList<>();
            while (rst.next()) {
                Statement stmt = conn.createStatement();
                Borrow borrow = getABorrow(rst);
                ResultSet rsBook = stmt.executeQuery("select * from book where book_id=" + borrow.getBookId());
                if (!rsBook.next()) throw new Exception("Invalid Book");
                Book book = getABook(rsBook);
                if (book == null) throw new Exception("Invalid Book");
                BorrowHistories.Item item = new BorrowHistories.Item(rst.getInt("card_id"), book, borrow);
                items.add(item);
            }
            return items;
        } catch (Exception e) {

            return null;
        }
    }

    private ResultSet findEqualBook(Book book) throws Exception {
        Connection conn = connector.getConn();
        try {
            conn.setAutoCommit(false);
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
        } catch (Exception e) {

            rollback(conn);
            throw e;
        }
    }

    private ResultSet findEqualCard(Card card) throws Exception {
        Connection conn = connector.getConn();
        try {
            conn.setAutoCommit(false);
            String sql = "select * from card where name=? and department=? and type=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, card.getName());
            stmt.setString(2, card.getDepartment());
            stmt.setString(3, card.getType().getStr());
            ResultSet rs = stmt.executeQuery();
            commit(conn);
            return rs;
        } catch (Exception e) {

            rollback(conn);
            throw e;
        }
    }

    @Override
    public ApiResult storeBook(Book book) {//register a book to database
        Connection conn = connector.getConn();
        try {
            conn.setAutoCommit(false);
            ResultSet rs = findEqualBook(book);
            if (rs != null && rs.next()) throw new Exception("Duplicated Book");
        } catch (Exception e) {

            rollback(conn);
            return new ApiResult(false, "search error:" + e.getMessage());
        }
        try {
            if(Objects.equals(book.getCategory(), ""))throw new Exception("invalid category");
            if(Objects.equals(book.getTitle(), ""))throw new Exception("invalid title");
            if(Objects.equals(book.getPress(), ""))throw new Exception("invalid press");
            if(book.getPublishYear()<0)throw new Exception("invalid publish year");
            if(Objects.equals(book.getAuthor(), ""))throw new Exception("invalid author");
            if(book.getPrice()<0)throw new Exception("invalid price");
            if(book.getStock()<0)throw new Exception("invalid stock");
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

            return new ApiResult(false, e.getMessage());
        }

    }

    @Override
    public ApiResult incBookStock(int bookId, int deltaStock) {
        Connection conn = connector.getConn();
        int now_stock;
        try {
            conn.setAutoCommit(false);
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

            return new ApiResult(false, "add stock error:" + e.getMessage());
        }
        return new ApiResult(true, "Successfully Increase BookStock");
    }

    @Override
    public ApiResult storeBook(List<Book> books) {
        Connection conn = connector.getConn();
        String sqlAdd = "insert into book(category,title,press,publish_year,author,price,stock)" +
                " values(?,?,?,?,?,?,?)";
        PreparedStatement StmtAdd;
        try {
            conn.setAutoCommit(false);
            StmtAdd = conn.prepareStatement(sqlAdd);
        } catch (Exception e) {

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

                return new ApiResult(false, "add book error at " + i + e.getMessage());
            }
        }
        try {
            StmtAdd.executeBatch();
            for (Book book : books) {
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
        Connection conn = connector.getConn();
        try {
            conn.setAutoCommit(false);
            Statement stmtQueryBorrow = conn.createStatement();
            ResultSet rstBorrow = stmtQueryBorrow.executeQuery("select * from borrow where book_id=" + bookId + " and return_time=0");
            if (rstBorrow.next()) throw new Exception("Book not returned");
            Statement stmtQueryBook = conn.createStatement();
            ResultSet rstBook = stmtQueryBook.executeQuery("select * from book where book_id=" + bookId);
            if (!rstBook.next()) throw new Exception("Invalid book");
            String sql = "delete from book where book_id=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, bookId);
            stmt.execute();
            commit(conn);
            return new ApiResult(true, "Successfully RemoveBook");
        } catch (Exception e) {

            rollback(conn);
            return new ApiResult(false, "Remove error:" + e.getMessage());
        }
    }

    @Override
    public ApiResult modifyBookInfo(Book book) {
        Connection conn = connector.getConn();
        try {
            conn.setAutoCommit(false);
            Statement StmtFind = conn.createStatement();
            ResultSet rst = StmtFind.executeQuery("select * from book where book_id=" + book.getBookId());
            if (!rst.next()) throw new Exception("can not find the book");
            String UpdateSql = "update book set category=?,title=?,press=?,publish_year=?,author=?,price=? where book_id=?";
            PreparedStatement StmtUpdate = conn.prepareStatement(UpdateSql);
            if(book.getCategory()==null)book.setCategory(rst.getString("category"));
            if(book.getTitle()==null)book.setTitle(rst.getString("title"));
            if(book.getPress()==null)book.setPress(rst.getString("press"));
            if(book.getPublishYear()<0)book.setPublishYear(rst.getInt("publish_year"));
            if(book.getAuthor()==null)book.setAuthor(rst.getString("author"));
            if(book.getPrice()<0)book.setPrice(rst.getFloat("price"));
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
    public ApiResult queryBook(BookQueryConditions conditions) {
        String sql = "select * from book ";
        int now_place = 1;
        Connection conn = connector.getConn();
        try {
            conn.setAutoCommit(false);
            String SelectCategory = " category=? ";
            String SelectTitle = " title like ?";
            String SelectPress = " Press like ?";
            String SelectMinYear = "?<=publish_year";
            String SelectMaxYear = "publish_year <=?";
            String SelectAuthor = " author like ?";
            String SelectMinPrice = "?<=price";
            String SelectMaxPrice = "price<=?";
            String SelectSortBy = " order by " + conditions.getSortBy() + " " + conditions.getSortOrder() + " ,book_id asc";
            ResultSet rst;
            boolean FirstCondition = true;
            if (conditions.getCategory() != null) {
                if (!FirstCondition) sql += " and ";
                else sql += " where ";
                FirstCondition = false;
                sql += SelectCategory;
            }
            if (conditions.getTitle() != null) {
                if (!FirstCondition) sql += " and ";
                else sql += " where ";
                FirstCondition = false;
                sql += SelectTitle;
            }
            if (conditions.getPress() != null) {
                if (!FirstCondition) sql += " and ";
                else sql += " where ";
                FirstCondition = false;
                sql += SelectPress;
            }
            if (conditions.getMinPublishYear() != null) {
                if (!FirstCondition) sql += " and ";
                else sql += " where ";
                FirstCondition = false;
                sql += SelectMinYear;
            }
            if (conditions.getMaxPublishYear() != null) {
                if (!FirstCondition) sql += " and ";
                else sql += " where ";
                FirstCondition = false;
                sql += SelectMaxYear;
            }
            if (conditions.getAuthor() != null) {
                if (!FirstCondition) sql += " and ";
                else sql += " where ";
                FirstCondition = false;
                sql += SelectAuthor;
            }
            if (conditions.getMinPrice() != null) {
                if (!FirstCondition) sql += " and ";
                else sql += " where ";
                FirstCondition = false;
                sql += SelectMinPrice;
            }
            if (conditions.getMaxPrice() != null) {
                if (!FirstCondition) sql += " and ";
                else sql += " where ";
                FirstCondition = false;
                sql += SelectMaxPrice;
            }
            if (conditions.getSortBy() != null) sql += SelectSortBy;

            PreparedStatement StmtFind = conn.prepareStatement(sql);


            if (conditions.getCategory() != null) StmtFind.setString(now_place++, conditions.getCategory());
            if (conditions.getTitle() != null) StmtFind.setString(now_place++, "%" + conditions.getTitle() + "%");
            if (conditions.getPress() != null) StmtFind.setString(now_place++, "%" + conditions.getPress() + "%");
            if (conditions.getMinPublishYear() != null) StmtFind.setInt(now_place++, conditions.getMinPublishYear());
            if (conditions.getMaxPublishYear() != null) StmtFind.setInt(now_place++, conditions.getMaxPublishYear());
            if (conditions.getAuthor() != null) StmtFind.setString(now_place++, "%" + conditions.getAuthor() + "%");
            if (conditions.getMinPrice() != null) StmtFind.setDouble(now_place++, conditions.getMinPrice());
            if (conditions.getMaxPrice() != null) StmtFind.setDouble(now_place++, conditions.getMaxPrice());
            rst = StmtFind.executeQuery();
            List<Book> books = toBook(rst);
            return new ApiResult(true, "Successfully Query " + books.size() + " Books", new BookQueryResults(books));
        } catch (Exception e) {


            return new ApiResult(false, "Query Failed:" + e.getMessage() + "\n with the query sql:" + sql);
        }
    }

    @Override
    public ApiResult borrowBook(Borrow borrow) {
        Connection conn = connector.getConn();
        try {
            conn.setAutoCommit(false);
            Statement QueryBorrowStmt = conn.createStatement();
            ResultSet rsBorrow = QueryBorrowStmt.executeQuery("select * from borrow where return_time=0 and " +
                    "card_id=" + borrow.getCardId() + " and " + "book_id=" + borrow.getBookId() + " for update");
            if (rsBorrow.next()) throw new Exception("Duplicated Borrow");
            Statement QueryBook = conn.createStatement();
            ResultSet rsBook = QueryBook.executeQuery("select * from book where book_id=" + borrow.getBookId() + " for update ");
            if (!rsBook.next()) throw new Exception("Invalid book_id");
            int now_stock = rsBook.getInt("stock");
            if (now_stock - 1 < 0) throw new Exception("out of stock");
            String InsertSql = "insert into borrow(card_id, book_id, borrow_time) values(?,?,?)";
            PreparedStatement stmt = conn.prepareStatement(InsertSql);
            stmt.setInt(1, borrow.getCardId());
            stmt.setInt(2, borrow.getBookId());
            stmt.setLong(3, borrow.getBorrowTime());
            stmt.execute();
            Statement UpdateBook = conn.createStatement();
            UpdateBook.execute("update book set stock=" + (now_stock - 1) + " where book_id=" + borrow.getBookId());
            commit(conn);
            return new ApiResult(true, "Successfully BorrowBook");
        } catch (Exception e) {
            rollback(conn);

//            System.out.println(e.getMessage());
            return new ApiResult(false, e.getMessage());
        }
    }

    @Override
    public ApiResult returnBook(Borrow borrow) {
        Connection conn = connector.getConn();
        try {
            conn.setAutoCommit(false);
            String QueryBorrowSql = "select * from borrow where book_id=? and card_id=? and return_time=0 for update ";
            PreparedStatement stmt = conn.prepareStatement(QueryBorrowSql);
            stmt.setInt(1, borrow.getBookId());
            stmt.setInt(2, borrow.getCardId());
            ResultSet rsBorrow = stmt.executeQuery();
            if (!rsBorrow.next()) throw new Exception("Cannot find Borrow History");
            if (rsBorrow.getLong("borrow_time") >= borrow.getReturnTime()) throw new Exception("Invalid return time");
            if (rsBorrow.getLong("return_time") != 0) throw new Exception("Book has been returned before");
            Statement QueryBookStmt = conn.createStatement();
            ResultSet rsBook = QueryBookStmt.executeQuery("select * from book where book_id=" + borrow.getBookId() + " for update ");
            if (!rsBook.next()) throw new Exception("Invalid book id");
            Statement QueryCardStmt = conn.createStatement();
            ResultSet rsCard = QueryCardStmt.executeQuery("select * from card where card_id=" + borrow.getCardId() + " for update ");
            if (!rsCard.next()) throw new Exception("Invalid card id");
            int now_stock = rsBook.getInt("stock");
            String UpdateBorrowSql = "update borrow set return_time=? where card_id=? and book_id=? and borrow_time=?";
            PreparedStatement UpdateBorrowStmt = conn.prepareStatement(UpdateBorrowSql);
            UpdateBorrowStmt.setLong(1, borrow.getReturnTime());
            UpdateBorrowStmt.setInt(2, borrow.getCardId());
            UpdateBorrowStmt.setInt(3, borrow.getBookId());
            UpdateBorrowStmt.setLong(4, rsBorrow.getLong("borrow_time"));
            UpdateBorrowStmt.execute();

            Statement UpdateBookStmt = conn.createStatement();
            UpdateBookStmt.execute("update book set stock=" + (now_stock + 1) + " where book_id=" + borrow.getBookId());
            commit(conn);
        } catch (Exception e) {
            rollback(conn);

            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, "Successfully ReturnBook");
    }

    @Override
    public ApiResult showBorrowHistory(int cardId) {
        Connection conn = connector.getConn();
        try {
            conn.setAutoCommit(false);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("select * from borrow where card_id=" + cardId + " order by borrow_time DESC, book_id ASC");
            List<BorrowHistories.Item> items = toItem(rs);
            return new ApiResult(true, "Successfully Show " + items.size() + " Borrow History", new BorrowHistories(items));

        } catch (Exception e) {

            return new ApiResult(false, e.getMessage());
        }
    }

    @Override
    public ApiResult registerCard(Card card) {
        Connection conn = connector.getConn();
        try {
            conn.setAutoCommit(false);
            ResultSet rs = findEqualCard(card);
            if (rs != null && rs.next()) throw new Exception("Duplicated card");
        } catch (Exception e) {

            rollback(conn);
            return new ApiResult(false, "search error:" + e.getMessage());
        }
        try {
            String sql = "insert into card(name, department, type)" +
                    " values(?,?,?)";
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

            return new ApiResult(false, e.getMessage());
        }
    }

    @Override
    public ApiResult removeCard(int cardId) {
        Connection conn = connector.getConn();
        try {
            conn.setAutoCommit(false);
            Statement stmtQueryBorrow = conn.createStatement();
            ResultSet rstBorrow = stmtQueryBorrow.executeQuery("select * from borrow where card_id=" + cardId + " and return_time=0");
            if (rstBorrow.next()) {

                throw new Exception("Some book not returned");
            }
            Statement stmtQueryCard = conn.createStatement();
            ResultSet rstCard = stmtQueryCard.executeQuery("select * from card where card_id=" + cardId);
            if (!rstCard.next()) throw new Exception("Invalid Card");
            String sql = "delete from card where card_id=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, cardId);
            stmt.execute();
            commit(conn);
            return new ApiResult(true, "Successfully RemoveCard");
        } catch (Exception e) {
            rollback(conn);

            return new ApiResult(false, "Remove error:" + e.getMessage());
        }
    }

    @Override
    public ApiResult showCards() {
        Connection conn = connector.getConn();
        try {
            conn.setAutoCommit(false);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("select * from card order by card_id ASC ");
            List<Card> cards = toCard(rs);
            return new ApiResult(true, "Successfully Show " + cards.size() + " Cards", new CardList(cards));
        } catch (Exception e) {

            return new ApiResult(false, e.getMessage());
        }
    }

    @Override
    public ApiResult resetDatabase() {
        Connection conn = connector.getConn();
        try {
            conn.setAutoCommit(false);
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

        }
    }

    private void commit(Connection conn) {
        try {
            conn.commit();
        } catch (Exception e) {

        }
    }


}
