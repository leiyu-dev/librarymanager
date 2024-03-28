package handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import entities.Book;
import library.Library;
import library.LibraryManagementSystem;
import queries.ApiResult;
import queries.BookQueryConditions;
import queries.BookQueryResults;

import java.io.*;
import java.util.List;

public class BookHandler implements HttpHandler {
    private static ObjectMapper objectMapper= new ObjectMapper();
    private LibraryManagementSystem library = Library.library;
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // 允许所有域的请求，cors处理
        Headers headers = exchange.getResponseHeaders();
        headers.add("Access-Control-Allow-Origin", "*");
        headers.add("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT");
        headers.add("Access-Control-Allow-Headers", "Content-Type");
        // 解析请求的方法，看GET还是POST
        String requestMethod = exchange.getRequestMethod();
        // 注意判断要用equals方法而不是==啊，java的小坑（
        if (requestMethod.equals("GET")) {
            // 处理GET
            handleGetRequest(exchange);
        } else if (requestMethod.equals("POST")) {
            // 处理POST
            handlePostRequest(exchange);
        } else if (requestMethod.equals("OPTIONS")) {
            // 处理OPTIONS
            exchange.sendResponseHeaders(400, -1);
        } else if(requestMethod.equals("DELETE")){
            handleDeleteRequest(exchange);
        } else if(requestMethod.equals("PUT")) {
            handlePutRequest(exchange);
        }else
        {
            // 其他请求返回405 Method Not Allowed
            exchange.sendResponseHeaders(405, -1);
        }
    }


    private void handlePostRequest(HttpExchange exchange) throws IOException {
        InputStream requestBody = exchange.getRequestBody();
        BufferedReader reader = new BufferedReader(new InputStreamReader(requestBody));
        StringBuilder requestBodyBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            requestBodyBuilder.append(line);
        }

        try {
            Book book = objectMapper.readValue(requestBodyBuilder.toString(), Book.class);
            ApiResult rst = library.storeBook(book);
            if(!rst.ok)throw new Exception(rst.message);
            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(200, 0);
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write("Book stored successfully".getBytes());
            outputStream.close();
        }
        catch (Exception e){
            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(400, 0);
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(e.getMessage().getBytes());
            outputStream.close();
        }
    }

    private void handleGetRequest(HttpExchange exchange) throws IOException {
        InputStream requestBody = exchange.getRequestBody();
        BufferedReader reader = new BufferedReader(new InputStreamReader(requestBody));
        StringBuilder requestBodyBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            requestBodyBuilder.append(line);
        }
        try{
            BookQueryConditions bookQueryConditions = objectMapper.readValue(requestBodyBuilder.toString(), BookQueryConditions.class);
            ApiResult rst = library.queryBook(bookQueryConditions);
            List<Book> books = ((BookQueryResults)(rst.payload)).getResults();
            StringBuilder response = new StringBuilder("[");
            for(int i=0;i<books.size();i++){
                Book book=books.get(i);
                response.append(objectMapper.writeValueAsString(book));
                if(i!=books.size()-1) response.append(",");
            }
            response.append("]");
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, 0);
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(response.toString().getBytes());
            outputStream.close();
        }
        catch (Exception e) {
            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(400, 0);
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(e.getMessage().getBytes());
            outputStream.close();
        }
    }
    private void handleDeleteRequest(HttpExchange exchange) throws IOException {
        InputStream requestBody = exchange.getRequestBody();
        BufferedReader reader = new BufferedReader(new InputStreamReader(requestBody));
        StringBuilder requestBodyBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            requestBodyBuilder.append(line);
        }
        try {
            Book book = objectMapper.readValue(requestBodyBuilder.toString(), Book.class);
            ApiResult rst = library.removeBook(book.getBookId());
            if(!rst.ok)throw new Exception(rst.message);
            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(200, 0);
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write("Book removed successfully".getBytes());
            outputStream.close();
        }
        catch (Exception e){
            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(400, 0);
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(e.getMessage().getBytes());
            outputStream.close();
        }
    }
    private void handlePutRequest(HttpExchange exchange) throws IOException {//modifyBookInfo
        InputStream requestBody = exchange.getRequestBody();
        BufferedReader reader = new BufferedReader(new InputStreamReader(requestBody));
        StringBuilder requestBodyBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            requestBodyBuilder.append(line);
        }
        try {
            Book book = objectMapper.readValue(requestBodyBuilder.toString(), Book.class);
            ApiResult rst = library.modifyBookInfo(book);
            if(!rst.ok)throw new Exception(rst.message);
            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(200, 0);
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write("Book modified successfully".getBytes());
            outputStream.close();
        }
        catch (Exception e){
            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(400, 0);
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(e.getMessage().getBytes());
            outputStream.close();
        }

    }
}
