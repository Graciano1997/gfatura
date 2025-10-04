import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.DocFlavor;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import javax.swing.*;
import java.awt.print.*;
import java.awt.print.PrinterJob;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;
import org.apache.pdfbox.printing.PDFPrintable;
import org.apache.pdfbox.printing.Scaling;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

public class Main {
    
    // Detecta o tipo de impressora e retorna o tamanho de papel apropriado
    public static String detectPaperSize(String printerNameOrType) {
        String name = printerNameOrType.toLowerCase();
        
        // Impressoras térmicas comuns
        if (name.contains("tsp") || name.contains("tm-") || name.contains("thermal") || 
            name.contains("80mm") || name.contains("58mm") || name.equals("thermal80") || name.equals("thermal58")) {
            
            if (name.contains("58")) {
                return "58mm";
            }
            return "80mm";
        }
        
        return "A4"; // default
    }
 
    // Preprocessa o HTML adicionando CSS apropriado baseado no tipo de papel
     private static String preprocessHTML(String html, String paperSize) {
        if (!html.trim().toLowerCase().startsWith("<!doctype")) {
            html = "<!DOCTYPE html>" + html;
        }
        
        String injectedCSS = "";
        
        switch(paperSize) {
            case "80mm":
                injectedCSS = "<style>"
                    + "@page { size: 80mm auto; margin: 2mm; }"
                    + "body { width: 76mm; margin: 0 auto; font-size: 10pt; font-family: monospace; }"
                    + "* { max-width: 76mm; word-wrap: break-word; box-sizing: border-box; }"
                    + "</style>";
                break;
                
            case "58mm":
                injectedCSS = "<style>"
                    + "@page { size: 58mm auto; margin: 2mm; }"
                    + "body { width: 54mm; margin: 0 auto; font-size: 8pt; font-family: monospace; }"
                    + "* { max-width: 54mm; word-wrap: break-word; box-sizing: border-box; }"
                    + "</style>";
                break;
                
            default: // A4
                injectedCSS = "<style>"
                    + "@page { size: A4; margin: 15mm; }"
                    + "body { font-family: Arial, sans-serif; }"
                    + "</style>";
                break;
        }
        
        if (html.toLowerCase().contains("<head>")) {
            html = html.replaceFirst("(?i)<head>", "<head>" + injectedCSS);
        } else if (html.toLowerCase().contains("<html>")) {
            html = html.replaceFirst("(?i)<html>", "<html><head>" + injectedCSS + "</head>");
        } else {
            html = "<html><head>" + injectedCSS + "</head><body>" + html + "</body></html>";
        }
        
        return html;
    }

    public static PrintService getPrinter(String printerName) throws Exception {
        if (printerName == null) { printerName = ""; }
    
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        System.out.println("=== Available Printer number: " + services.length + "===");
        
        if (services.length == 0) {
            Message.show("Sem Impressora","Nenhuma impressora encontrada neste computador, conecte e tente novamente",MessageType.Warning);            
            // throw new Exception("No printer connected on this computer!");
        }
        
        List<PrintService> physicalPrinters = new ArrayList<>();
        for (PrintService service : services) {
            String name = service.getName().toLowerCase();
            boolean isVirtual = name.equals("pdf") || 
                name.contains("microsoft print to pdf") ||
                name.contains("xps") ||
                name.contains("onenote") ||
                name.contains("fax");
            
            if (!isVirtual || printerName.toLowerCase().contains("pdf")) {
                physicalPrinters.add(service);
            } else {
                System.out.println("Ignoring Virtual Printer: " + service.getName());
            }
        }
        
        if (physicalPrinters.isEmpty()) {
                        Message.show("Sem Impressora","Nenhuma impressora encontrada neste computador, conecte e tente novamente",MessageType.Warning);       
            // throw new Exception("No printer connected available on this computer");
        }
        

        PrintService selectedPrinter = null;
        System.out.println("Available Printers:");
        for (int i = 0; i < physicalPrinters.size(); i++) {
            PrintService service = physicalPrinters.get(i);
            System.out.println((i+1) + ". '" + service.getName() + "'");
            
            if (!printerName.isEmpty()) {
                if (service.getName().toLowerCase().contains(printerName.toLowerCase()) ||
                    printerName.toLowerCase().contains(service.getName().toLowerCase()) ||
                    service.getName().equalsIgnoreCase(printerName)) {
                    selectedPrinter = service;
                    System.out.println(" >>> Selected a Printer! <<<");
                }
            }
        }
        
        if (selectedPrinter == null) {
            selectedPrinter = physicalPrinters.get(0);
            System.out.println("=== Using the System default printer: " + selectedPrinter.getName() + " ===");
        } else {
            System.out.println("=== The selected Printer: " + selectedPrinter.getName() + " ===");
        }
        return selectedPrinter;
    }

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", 5000), 0);
        System.out.println("Starting the print server...");
    
    server.createContext("/print_pdf", exchange -> {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            Headers headers = exchange.getResponseHeaders();
            headers.add("Access-Control-Allow-Origin", "*");
            headers.add("Access-Control-Allow-Methods", "POST, OPTIONS");
            headers.add("Access-Control-Allow-Headers", "Content-Type");
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            Headers headers = exchange.getResponseHeaders();
            headers.add("Access-Control-Allow-Origin", "*");

            try {
                InputStream is = exchange.getRequestBody();
                String jsonString = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                is.close();
                
                System.out.println("JSON received (Size): " + jsonString.length());
                System.out.println("First 200 chars: " + jsonString.substring(0, Math.min(200, jsonString.length())));

                ObjectMapper mapper = new ObjectMapper();
                JsonNode json = mapper.readTree(jsonString);
            
                System.out.println("JSON present Fields: " + json.fieldNames());
            
                if (!json.has("pdfBase64") || json.get("pdfBase64").asText().isEmpty()) {
                   throw new Exception("pdfBase64 is empty or absent!");
                }

                String pdfBase64 = json.get("pdfBase64").asText();
                System.out.println(pdfBase64);
                String printerName = json.has("printer") ? json.get("printer").asText() : "";
                int copyNumber = json.has("copyNumber") ? json.get("copyNumber").asInt() : 1;
                
                System.out.println("Received PDF base64 (size): " + pdfBase64.length());
                
                byte[] pdfBytes = Base64.getDecoder().decode(pdfBase64);
                System.out.println("Decoded PDF : " + pdfBytes.length + " bytes");
                
                // Salvar PDF
                String folderPath = "/home/grasoft/Documents/invoices/";
                File folder = new File(folderPath);
                if (!folder.exists()) folder.mkdirs();
                
                File pdfFile = new File(folder, "invoice-" + System.currentTimeMillis() + ".pdf");
                try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
                    fos.write(pdfBytes);
                }
                System.out.println("PDF salvo: " + pdfFile.getAbsolutePath());
                
                // Print if there is a physic printer 
                PrintService printer = Main.getPrinter(printerName);
                
                if (!printer.getName().toLowerCase().contains("pdf")) {
                    PDDocument document = PDDocument.load(pdfFile);
                    PrinterJob job = PrinterJob.getPrinterJob();
                    job.setPrintService(printer);
                    
                    PrintRequestAttributeSet attr = new HashPrintRequestAttributeSet();
                    attr.add(new Copies(copyNumber));
                    
                    job.setPageable(new PDFPageable(document));
                    job.print(attr);
                    
                    document.close();
                    System.out.println("✅ Printing...!");
                } else {
                    System.out.println("PDF Printer - only saving the file");
                }
                
                String response = "{\"status\":\"ok\",\"message\":\"PDF impresso\",\"file\":\"" + pdfFile.getName() + "\"}";
                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, responseBytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(responseBytes);
                os.close();

            } catch (Exception e) {
                Message.show("A impressão Falhou","Porfavor verifique se a impressora escolhida esta conectada ao computador e tente novamente",MessageType.Error);
                e.printStackTrace();
                String response = "{\"status\":\"error\",\"message\":\"" + e.getMessage().replace("\"", "'") + "\"}";
                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(500, responseBytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(responseBytes);
                os.close();

            }
        }
    });

    server.createContext("/print_termal", exchange -> {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
        Headers headers = exchange.getResponseHeaders();
        headers.add("Access-Control-Allow-Origin", "*");
        headers.add("Access-Control-Allow-Methods", "POST, OPTIONS");
        headers.add("Access-Control-Allow-Headers", "Content-Type");
        exchange.sendResponseHeaders(204, -1);
        return;
    }
    if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
        Headers headers = exchange.getResponseHeaders();
        headers.add("Access-Control-Allow-Origin", "*");

        try {
            InputStream is = exchange.getRequestBody();
            String jsonString = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            is.close();
            
            System.out.println("JSON received (size): " + jsonString.length());
            System.out.println("First 200 chars: " + jsonString.substring(0, Math.min(200, jsonString.length())));

            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(jsonString);
            
            String printerName = json.has("printer") ? json.get("printer").asText() : "";
            int copyNumber = json.has("copyNumber") ? json.get("copyNumber").asInt() : 1;
            String invoiceHtml = json.get("invoiceHtml").asText();
            
            String paperSize = detectPaperSize(printerName);

            invoiceHtml = preprocessHTML(invoiceHtml, paperSize);

            PrintService printer = Main.getPrinter(printerName);
            
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setPrintService(printer);
            
            PrintRequestAttributeSet attr = new HashPrintRequestAttributeSet();
            attr.add(new Copies(copyNumber));
            
            JEditorPane editorPane = new JEditorPane("text/html", "");
            editorPane.setText(invoiceHtml);
            editorPane.setSize(500, 500);
            editorPane.setEditable(false);

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
            
            job.print(attr);
                            
            String response = "{\"status\":\"ok\",\"message\":\"PDF impresso\",\"file\":\""  + "\"}";
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, responseBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(responseBytes);
            os.close();

        } catch (Exception e) {
            Message.show("A impressão Falhou","Porfavor verifique se a impressora escolhida esta conectada ao computador e tente novamente",MessageType.Error);
            e.printStackTrace();
            String response = "{\"status\":\"error\",\"message\":\"" + e.getMessage().replace("\"", "'") + "\"}";
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(500, responseBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(responseBytes);
            os.close();
        }
    }
    });

    // Endpoint para listar impressoras
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
                
                printers.add(printerInfo);
            }

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
    System.out.println("✅ Local print server running on port 5000");
    System.out.println("📄 Use [GET] /printers to get all the system, available printers.");
    System.out.println("🖨️ Use [POST] /print_pdf to print a pdf  item.");
    System.out.println("🖨️ Use [POST] /print_termal to print to any thermal printer.");
    
    PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
    System.out.println("\n*************** Detected printers ***************");
    for (PrintService service : services) {
        System.out.println("- " + service.getName());
    }
    System.out.println("******************************************************\n");
    }
}