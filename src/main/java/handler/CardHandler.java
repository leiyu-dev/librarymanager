package handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import entities.Card;
import library.Library;
import library.LibraryManagementSystem;
import log.Log;
import queries.ApiResult;
import queries.CardList;

import java.io.*;
import java.util.List;

public class CardHandler implements HttpHandler {
        private static ObjectMapper objectMapper= new ObjectMapper();
        private LibraryManagementSystem library = Library.library;
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // 允许所有域的请求，cors处理
            Headers headers = exchange.getResponseHeaders();
            headers.add("Access-Control-Allow-Origin", "*");
            headers.add("Access-Control-Allow-Methods", "GET, POST, DELETE");
            headers.add("Access-Control-Allow-Headers", "Content-Type");
            // 解析请求的方法，看GET还是POST
            String requestMethod = exchange.getRequestMethod();
            // 注意判断要用equals方法而不是==啊，java的小坑（
            Log.log.info("incard"+requestMethod);
            switch (requestMethod) {
                case "GET":
                    // 处理GET
                    handleGetRequest(exchange);
                    break;
                case "POST":
                    // 处理POST
                    handlePostRequest(exchange);
                    break;
                case "OPTIONS":
                    // 处理OPTIONS
                    exchange.sendResponseHeaders(200, -1);

                    break;
                case "DELETE":
                    handleDeleteRequest(exchange);
                    break;
                default:
                    Log.log.warning("carderror");
                    // 其他请求返回405 Method Not Allowed
                    exchange.sendResponseHeaders(405, -1);
                    break;
            }
        }


    private void handlePostRequest(HttpExchange exchange) throws IOException {
        Log.log.info("cardpost");
        InputStream requestBody = exchange.getRequestBody();
        BufferedReader reader = new BufferedReader(new InputStreamReader(requestBody));
        StringBuilder requestBodyBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            requestBodyBuilder.append(line);
        }

        try {
            Card card = objectMapper.readValue(requestBodyBuilder.toString(), Card.class);
            ApiResult rst = library.registerCard(card);
            if(!rst.ok)throw new Exception(rst.message);
            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(200, 0);
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write("Card created successfully".getBytes());
            outputStream.close();
        }
        catch (Exception e){
            e.printStackTrace();
            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(400, 0);
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(e.getMessage().getBytes());
            outputStream.close();
        }
    }

    private void handleGetRequest(HttpExchange exchange) throws IOException {
        Log.log.info("cardget");
        try{
            ApiResult rst = library.showCards();
            List<Card> cards = ((CardList)(rst.payload)).getCards();
            StringBuilder response = new StringBuilder("[");
            for(int i=0;i<cards.size();i++){
                Card card=cards.get(i);
                response.append(objectMapper.writeValueAsString(card));
                if(i!=cards.size()-1) response.append(",");
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
        Log.log.info("carddelete");
        InputStream requestBody = exchange.getRequestBody();
        BufferedReader reader = new BufferedReader(new InputStreamReader(requestBody));
        StringBuilder requestBodyBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            requestBodyBuilder.append(line);
        }
//        System.out.println(requestBodyBuilder.toString());
        try {
            Card card = objectMapper.readValue(requestBodyBuilder.toString(), Card.class);
//            System.out.println("I get"+card.getCardId());
            ApiResult rst = library.removeCard(card.getCardId());
            if(!rst.ok)throw new Exception(rst.message);
            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(200, 0);
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write("Card removed successfully".getBytes());
            outputStream.close();
        }
        catch (Exception e){
            e.printStackTrace();
            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(400, 0);
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(e.getMessage().getBytes());
            outputStream.close();
        }
    }

}
