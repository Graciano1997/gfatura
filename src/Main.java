import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.*;
import javax.print.attribute.standard.PrinterName;
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

    public static void sendToprint(PrintService selectedPrinter, String invoiceHTML, int copyNumber)throws Exception{
        System.out.println("=== DEBUG: Criando componente de impressão ===");
        JEditorPane pane = new JEditorPane("text/html", invoiceHTML);
        pane.setSize(400, 600);
        
        // Pequena pausa para renderização
        Thread.sleep(500);
        
        System.out.println("=== DEBUG: Configurando job de impressão ===");
        PrinterJob job = PrinterJob.getPrinterJob();

        PageFormat pageFormat = job.defaultPage();
        Paper paper = pageFormat.getPaper();

        // Definir margens em pontos (1 polegada = 72 pontos)
        double margin = 20; // 0.5 polegada (meia polegada)
        double width = paper.getWidth();
        double height = paper.getHeight();

        paper.setImageableArea(margin, margin, width - 2 * margin, height);
        pageFormat.setPaper(paper);

        // Usar o formato de página com margens definidas

        job.setPrintService(selectedPrinter);
        job.setPrintable(pane.getPrintable(null, null), pageFormat);        
        System.out.println("=== DEBUG: Enviando para impressão ===");
        for(int i = 0; i <= copyNumber; i++){
            job.print();
            Thread.sleep(1000);
        }
        System.out.println("=== DEBUG: Sucesso! ===");
    }

    public static PrintService getPrinter(String printerName) throws Exception{
        // Lista todas as impressoras disponíveis
        if (printerName == null) { printerName = "";}
    
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        System.out.println("=== DEBUG: Encontradas " + services.length + " impressoras ===");
        
        if (services.length == 0) {
            throw new Exception("Nenhuma impressora encontrada no sistema!");
        }
        
        // Lista e procura a impressora
        PrintService selectedPrinter = null;
        System.out.println("Impressoras disponíveis:");
        for (int i = 0; i < services.length; i++) {
            System.out.println((i+1) + ". '" + services[i].getName() + "'");
            
            // Busca por nome (case insensitive e com contains)
            if (!printerName.isEmpty()) {
                if (services[i].getName().toLowerCase().contains(printerName.toLowerCase()) ||
                    printerName.toLowerCase().contains(services[i].getName().toLowerCase()) ||
                    services[i].getName().equalsIgnoreCase(printerName)) {
                    selectedPrinter = services[i];
                    System.out.println(" >>> SELECIONADA! <<<");
                }
            }
        }
        
        // Se não encontrou por nome, usa a primeira
        if (selectedPrinter == null) {
            selectedPrinter = services[0];
            System.out.println("=== Impressora não encontrada por nome, usando padrão: " + selectedPrinter.getName() + " ===");
        } else {
            System.out.println("=== Impressora selecionada: " + selectedPrinter.getName() + " ===");
        }
        return selectedPrinter;
    }

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", 5000), 0);

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

                try {
                System.out.println("=== DEBUG SIMPLES: Iniciando ===");
                    
                BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8));
                StringBuilder jsonBuilder = new StringBuilder();
                
                String line;
                while ((line = br.readLine()) != null) {
                    jsonBuilder.append(line);
                }
                  
                br.close();

                ObjectMapper mapper = new ObjectMapper();
                JsonNode json = mapper.readTree(jsonBuilder.toString());
                
                // Acessar propriedades com segurança
                String printerName = json.has("printer") ? json.get("printer").asText() : "";
                System.out.println("Impressora solicitada: '" + printerName + "'");
                PrintService selectedPrinter = Main.getPrinter(printerName);
                    
                    String html = "<html><body style='font-family:monospace;'>";
                    html += "<strong><h1>HOSANA POS - TESTE DE IMPRESSÃO</h1></strong>";
                    html += "<p>Impressora: " + selectedPrinter.getName() + "</p>";
                    html += "<p>Data: " + new java.util.Date() + "</p>";
                    html += "<p>Status: FUNCIONANDO!</p>";
                    html += "</body></html>";
                    int copies = 1;
                    Main.sendToprint(selectedPrinter,html,copies);
                    
                    String response = "{ \"status\": \"ok\", \"message\": \"Teste simples funcionou\", \"printer\": \"" + selectedPrinter.getName() + "\" }";
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

                try {
                System.out.println("=== DEBUG SIMPLES: Iniciando ===");
                BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8));
                StringBuilder jsonBuilder = new StringBuilder();
                
                String line;
                while ((line = br.readLine()) != null) {
                    jsonBuilder.append(line);
                }
                  
                br.close();

                ObjectMapper mapper = new ObjectMapper();
                JsonNode json = mapper.readTree(jsonBuilder.toString());
                
                // Acessar propriedades com segurança
                String printerName = json.has("printer") ? json.get("printer").asText() : "";
                String invoiceTemplate = json.has("invoice") ? json.get("invoice").asText() : "";
                int copyNumber = json.has("copyNumber") ? json.get("copyNumber").asInt() : 1;
                System.out.println("Impressora solicitada: '" + printerName + "'");
                PrintService selectedPrinter = Main.getPrinter(printerName);
                
                Main.sendToprint(selectedPrinter,invoiceTemplate,copyNumber);
                
                String response = "{ \"status\": \"ok\", \"message\": \"Teste simples funcionou\", \"printer\": \"" + selectedPrinter.getName() + "\" }";
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
                List<Map<String, Object>> printers = new ArrayList<>();
                
                for (PrintService service : services) {
                    Map<String, Object> printerInfo = new HashMap<>();
                    printerInfo.put("name", service.getName());
                    printerInfo.put("status", "available");
                    
                    // Verifica suporte a PDF
                    DocFlavor[] supportedFlavors = service.getSupportedDocFlavors();
                    boolean supportsPDF = false;
                    for (DocFlavor flavor : supportedFlavors) {
                        if (flavor.equals(DocFlavor.INPUT_STREAM.PDF)) {
                            supportsPDF = true;
                            break;
                        }
                    }
                    printerInfo.put("supportsPDF", supportsPDF);
                    
                    printers.add(printerInfo);
                }

                // Converter para JSON
                String jsonResponse = new ObjectMapper().writeValueAsString(printers);

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
        System.out.println("Local print server running on port 5000");
        
        // Lista as impressoras disponíveis na inicialização
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        System.out.println("*****************Impressoras disponíveis:*************");
            for (PrintService service : services) {
                System.out.println("- " + service.getName());
            }
        System.out.println("*****************FIM Impressoras disponíveis:*************");
    }
}