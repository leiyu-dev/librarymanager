package handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import entities.Card;
import library.Library;
import library.LibraryManagementSystem;
import queries.ApiResult;
import queries.BorrowHistories;

import java.io.*;
import java.util.List;

public class ShowBorrowHistoryHandler implements HttpHandler {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final LibraryManagementSystem library = Library.library;

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Headers headers = exchange.getResponseHeaders();
        headers.add("Access-Control-Allow-Origin", "*");
        headers.add("Access-Control-Allow-Methods", "GET");
        headers.add("Access-Control-Allow-Headers", "Content-Type");
        String requestMethod = exchange.getRequestMethod();
        if (requestMethod.equals("OPTIONS")) {
            exchange.sendResponseHeaders(400, -1);
        } else if (requestMethod.equals("GET")) {
            handleGetRequest(exchange);
        } else {
            exchange.sendResponseHeaders(405, -1);
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
        try {
            Card card = objectMapper.readValue(requestBodyBuilder.toString(), Card.class);
            ApiResult rst = library.showBorrowHistory(card.getCardId());
            if (!rst.ok) throw new Exception(rst.message);
            List<BorrowHistories.Item> items = ((BorrowHistories)(rst.payload)).getItems();
            StringBuilder response = new StringBuilder("[");
            for(int i=0;i<items.size();i++){
                BorrowHistories.Item item=items.get(i);
                response.append(objectMapper.writeValueAsString(item));
                if(i!=items.size()-1) response.append(",");
            }
            response.append("]");
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, 0);
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(response.toString().getBytes());
            outputStream.close();
        } catch (Exception e) {
            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(400, 0);
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(e.getMessage().getBytes());
            outputStream.close();
        }
    }
}
