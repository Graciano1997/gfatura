import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(5000), 0);

        System.out.println("Starting the server...");

        server.createContext("/print", exchange -> {

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                Headers headers = exchange.getResponseHeaders();
                headers.add("Access-Control-Allow-Origin", "*");
                headers.add("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
                headers.add("Access-Control-Allow-Headers", "Content-Type");

                exchange.sendResponseHeaders(204, -1); 
                return;
            }

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Headers headers = exchange.getResponseHeaders();
                headers.add("Access-Control-Allow-Origin", "*");

                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(isr);
                StringBuilder jsonBuilder = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    jsonBuilder.append(line);
                }

                System.out.println("Recebido JSON: " + jsonBuilder.toString());

                Fatura fatura = new ObjectMapper().readValue(jsonBuilder.toString(), Fatura.class);
                ImpressorTermico.imprimir(fatura, true);

                String response = "Impressão recebida.";
                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);

                exchange.sendResponseHeaders(200, responseBytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(responseBytes);
                os.close();
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        });

        server.start();
        System.out.println("Local print server running on 5000 port");
    }
}
