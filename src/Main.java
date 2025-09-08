import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.SimpleDoc;
import javax.print.PrintException;
import java.net.URL;
import java.io.InputStream;
import java.awt.print.*;
import javax.print.*;
import javax.swing.*;


public class Main {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(5000), 0);

        System.out.println("Starting the server...");
        

          server.createContext("/print_test", exchange -> {    
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

                // Lê JSON recebido
                BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8));
                StringBuilder jsonBuilder = new StringBuilder();
                
                String line;
                
                while ((line = br.readLine()) != null) {
                    jsonBuilder.append(line);
                }

                br.close();

                System.out.println("Recebido JSON: " + jsonBuilder.toString());

                // Classe auxiliar para mapear JSON
                class PrintRequest {
                    public String printer;
                }

                PrintRequest req = new ObjectMapper().readValue(jsonBuilder.toString(), PrintRequest.class);
              
                try {
 
                    String testHTML = "";
                    testHTML += "<html><head><style>";
                    testHTML += "body{display:flex; align-items:center; justify-content:center; margin:10px; font-family: monospace;}";
                    testHTML += "</style></head><body>";
                    testHTML += "<h1>THE HOSANA POS PRINTING API IS WORKING...</h1>";
                    testHTML += "</body></html>";

                    // Procura impressora
                    PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
                    PrintService selectedPrinter = null;
                    for (PrintService service : services) {
                        if (service.getName().equalsIgnoreCase(req.printer)) {
                            selectedPrinter = service;
                            break;
                        }
                    }

                    if (selectedPrinter == null) {
                        throw new Exception("Impressora não encontrada: " + req.printer);
                    }

                    
                JEditorPane editorPane = new JEditorPane("text/html", "");
                editorPane.setText(testHTML);
                editorPane.setSize(500, 500);
                editorPane.setEditable(false);
                
                Thread.sleep(500);

                PrinterJob job = PrinterJob.getPrinterJob();
                job.setPrintService(selectedPrinter);

                // Criar PageFormat personalizado
                PageFormat pageFormat = job.defaultPage();
                Paper paper = pageFormat.getPaper();

                // Definir margens em pontos (1 polegada = 72 pontos)
                double margin = 20; // 0.5 polegada (meia polegada)
                double width = paper.getWidth();
                double height = paper.getHeight();

                paper.setImageableArea(margin, margin, width - 2 * margin, height);
                pageFormat.setPaper(paper);

                // Usar o formato de página com margens definidas
                job.setPrintable(editorPane.getPrintable(null, null), pageFormat);

                // Imprimir diretamente
                job.print();             
 
                // Resposta de sucesso
                String response = "{ \"status\": \"ok\" }";
                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, responseBytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(responseBytes);
                os.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    String response = "{ \"status\": \"error\", \"message\": \"" + e.getMessage() + "\" }";
                    byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(500, responseBytes.length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(responseBytes);
                    os.close();
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        });

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

                // Lê JSON recebido
                BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8));
                StringBuilder jsonBuilder = new StringBuilder();
                
                String line;
                
                while ((line = br.readLine()) != null) {
                    jsonBuilder.append(line);
                }

                br.close();

                System.out.println("Recebido JSON: " + jsonBuilder.toString());

                // Classe auxiliar para mapear JSON
                class PrintRequest {
                    public String printer;
                    public String pdfUrl;
                    public int copyNumber;
                    public String queueId;
                }

                PrintRequest req = new ObjectMapper().readValue(jsonBuilder.toString(), PrintRequest.class);
              
                try {
                    // Faz download do PDF
                    InputStream inputStream = new URL(req.pdfUrl).openStream();

                    // Procura impressora
                    PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
                    PrintService selectedPrinter = null;
                    for (PrintService service : services) {
                        if (service.getName().equalsIgnoreCase(req.printer)) {
                            selectedPrinter = service;
                            break;
                        }
                    }

                    if (selectedPrinter == null) {
                        throw new Exception("Impressora não encontrada: " + req.printer);
                    }

                    // Cria job e imprime
                    DocPrintJob job = selectedPrinter.createPrintJob();
                    Doc doc = new SimpleDoc(inputStream, javax.print.DocFlavor.INPUT_STREAM.PDF, null);
                    
                    for(int i =0; i < req.copyNumber; i++){
                    job.print(doc, null);
                    Thread.sleep(500);
                    }

                    inputStream.close();

                    // Resposta de sucesso
                    String response = "{ \"status\": \"ok\", \"queueId\": \"" + req.queueId + "\" }";
                    byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(200, responseBytes.length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(responseBytes);
                    os.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    String response = "{ \"status\": \"error\", \"message\": \"" + e.getMessage() + "\" }";
                    byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(500, responseBytes.length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(responseBytes);
                    os.close();
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        });

        server.createContext("/printers", exchange -> {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                Headers headers = exchange.getResponseHeaders();
                headers.add("Access-Control-Allow-Origin", "*");
                headers.add("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
                headers.add("Access-Control-Allow-Headers", "Content-Type");
                exchange.sendResponseHeaders(204, -1); 
                return;
            }

            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                Headers headers = exchange.getResponseHeaders();
                headers.add("Access-Control-Allow-Origin", "*");

                PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
                List<String> printerNames = new ArrayList<>();
                for (PrintService service : services) {
                    printerNames.add(service.getName());
                }

                // Converter para JSON
                String jsonResponse = new ObjectMapper().writeValueAsString(printerNames);

                byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
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
