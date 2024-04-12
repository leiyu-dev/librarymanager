package handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import entities.Borrow;
import library.Library;
import library.LibraryManagementSystem;
import log.Log;
import queries.ApiResult;

import java.io.*;

public class ReturnBookHandler implements HttpHandler {
    private static ObjectMapper objectMapper = new ObjectMapper();
    private LibraryManagementSystem library = Library.library;

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Headers headers = exchange.getResponseHeaders();
        headers.add("Access-Control-Allow-Origin", "*");
        headers.add("Access-Control-Allow-Methods", "POST");
        headers.add("Access-Control-Allow-Headers", "Content-Type");
        String requestMethod = exchange.getRequestMethod();
        if (requestMethod.equals("OPTIONS")) {
            exchange.sendResponseHeaders(200, -1);
        } else if (requestMethod.equals("POST")) {
            handlePostRequest(exchange);
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }

    private void handlePostRequest(HttpExchange exchange) throws IOException {
        Log.log.info("ReturnBookPost");
        InputStream requestBody = exchange.getRequestBody();
        BufferedReader reader = new BufferedReader(new InputStreamReader(requestBody));
        StringBuilder requestBodyBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            requestBodyBuilder.append(line);
        }
        try {
            Borrow borrow = objectMapper.readValue(requestBodyBuilder.toString(), Borrow.class);
            ApiResult rst = library.returnBook(borrow);
            if (!rst.ok) throw new Exception(rst.message);
            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(200, 0);
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write("Return book successfully".getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(400, 0);
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(e.getMessage().getBytes());
            outputStream.close();
        }
    }
}