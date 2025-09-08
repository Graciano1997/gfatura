import javax.swing.*;
import java.awt.print.*;
import javax.print.*;
import java.io.File;
import java.util.Map;
import java.util.List;
import java.io.StringReader;
import javax.print.attribute.*;
import javax.print.attribute.standard.PrinterName;


public class ImpressorTermico {
    
    public static void imprimir(Fatura fatura, boolean show){
        try{
        String faturaHTML = gerarHTMLFaturaTermica(fatura,show); 

        JEditorPane editorPane = new JEditorPane("text/html", "");
        editorPane.setText(faturaHTML);
        editorPane.setSize(500, 500);
        editorPane.setEditable(false);
        
        Thread.sleep(500);

        PrinterJob job = PrinterJob.getPrinterJob();

        AttributeSet attrSet = new HashPrintServiceAttributeSet();
        attrSet.add(new PrinterName("PDF", null));
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, attrSet);

        if (services.length > 0) {
            System.out.println("Impressora encontrada: " + services[0].getName());
            job.setPrintService(services[0]);
        } else {
            System.out.println("Impressora não encontrada!");
        }

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
        if(job.printDialog()){
        job.print();    
        }   
    }catch(Exception e){
        JOptionPane.showMessageDialog(null,"There is not a connected print. \n Please ensure to connect a print.", "No printer Connected",JOptionPane.WARNING_MESSAGE);
        }
    }

    private static String gerarHTMLFaturaTermica(Fatura fatura, boolean show) {
        String dup = show ? "Original" : "Duplicado";
        String faturaHTML = "";
        faturaHTML += "<html><head><style>";
        faturaHTML += "body{margin:5px; font-family: monospace;} h2{text-align:center; margin:5px;} div{font-size: 10pt; margin:2px;} b{text-align:center;} span{padding:2px; text-align:center; display:block;} table{font-size:10pt; width:100%; border-collapse:collapse;} th,td{text-align:left; padding:2px;}";
        faturaHTML += "</style></head><body>";
        faturaHTML += "<h2>"+ fatura.getEmpresa() +"</h2>";
        faturaHTML += "<div><b>Nif:"+fatura.getNif()+"   Tel:."+fatura.getEmpresaPhone()+"</b></div>";
        faturaHTML += "<div><b>Email:"+fatura.getEmail()+"   Local:."+fatura.getLocal()+"</b></div>";
        faturaHTML += "<span>"+dup+"</span>";
        faturaHTML += "<div>Número do Recibo: " + fatura.getNumeroRecibo() + "</div>";
        faturaHTML += "<div>Emitido em: " + fatura.getDataEmissao() + "</div>";
        faturaHTML += "<div>--------------------------------------------------------------</div>";
        faturaHTML += "<div>Cliente: " + fatura.getCliente() + "</div>";
        faturaHTML += "<div>Telefone: " + fatura.getTelefone() + "</div>";
        faturaHTML += "<div>--------------------------------------------------------------</div>";
        faturaHTML += "<table>";
        faturaHTML += "<tr><th>Nome</th><th>Qtd</th><th>Preço</th></tr>";
        for(Produto p: fatura.getProduto())
            faturaHTML += "<tr><td>"+p.getNome()+"</td><td>"+p.getQtd()+"</td><td>"+p.getPreco()+"</td></tr>";
        faturaHTML += "</table><div>--------------------------------------------------------------</div>";
        faturaHTML += "<div>Desconto: " + fatura.getDesconto() + "</div>";
        faturaHTML += "<div>Troco: " + fatura.getTroco() + "</div>";
        faturaHTML += "<div><b>Total: " + fatura.getTotal() + "</b></div>";
        faturaHTML += "<div>--------------------------------------------------------------</div>";
        faturaHTML += "<div>Forma de Pagamento: " + fatura.getFormaPagamento() + "</div>";
        faturaHTML += "<div>Operador: " + fatura.getVendedor() + "</div>";
        faturaHTML += "<div>--------------------------------------------------------------</div>";
        faturaHTML += "<div>Observações:</div>";
        faturaHTML += "<div>" + fatura.getObservacoes() + "</div>";
        faturaHTML += "<div>--------------------------------------------------------------</div>";
        faturaHTML += "<div><b>OBRIGADO PELA PREFERÊNCIA!</b></div>";
        faturaHTML += "<div>--------------------------------------------------------------</div>";
        faturaHTML += "</body></html>";
        
        return faturaHTML;
    }

    private static String gerarHTMLFaturaA4(Fatura fatura, boolean show) {
        String dup = show ? "Original" : "Duplicado";
        String faturaHTML = "";
        faturaHTML += "<html><head><style>";
        faturaHTML += "body{margin:5px; font-family: monospace;} h2{text-align:center; margin:5px;} div{font-size: 10pt; margin:2px;} b{text-align:center;} span{padding:2px; text-align:center; display:block;} table{font-size:10pt; width:100%; border-collapse:collapse;} th,td{text-align:left; padding:2px;}";
        faturaHTML += "</style></head><body>";
        faturaHTML += "<h2>"+ fatura.getEmpresa() +"</h2>";
        faturaHTML += "<div><b>Nif:"+fatura.getNif()+"   Tel:."+fatura.getEmpresaPhone()+"</b></div>";
        faturaHTML += "<div><b>Email:"+fatura.getEmail()+"   Local:."+fatura.getLocal()+"</b></div>";
        faturaHTML += "<span>"+dup+"</span>";
        faturaHTML += "<div>Número do Recibo: " + fatura.getNumeroRecibo() + "</div>";
        faturaHTML += "<div>Emitido em: " + fatura.getDataEmissao() + "</div>";
        faturaHTML += "<div>--------------------------------------------------------------</div>";
        faturaHTML += "<div>Cliente: " + fatura.getCliente() + "</div>";
        faturaHTML += "<div>Telefone: " + fatura.getTelefone() + "</div>";
        faturaHTML += "<div>--------------------------------------------------------------</div>";
        faturaHTML += "<table>";
        faturaHTML += "<tr><th>Nome</th><th>Qtd</th><th>Preço</th></tr>";
        for(Produto p: fatura.getProduto())
            faturaHTML += "<tr><td>"+p.getNome()+"</td><td>"+p.getQtd()+"</td><td>"+p.getPreco()+"</td></tr>";
        faturaHTML += "</table><div>--------------------------------------------------------------</div>";
        faturaHTML += "<div>Desconto: " + fatura.getDesconto() + "</div>";
        faturaHTML += "<div>Troco: " + fatura.getTroco() + "</div>";
        faturaHTML += "<div><b>Total: " + fatura.getTotal() + "</b></div>";
        faturaHTML += "<div>--------------------------------------------------------------</div>";
        faturaHTML += "<div>Forma de Pagamento: " + fatura.getFormaPagamento() + "</div>";
        faturaHTML += "<div>Operador: " + fatura.getVendedor() + "</div>";
        faturaHTML += "<div>--------------------------------------------------------------</div>";
        faturaHTML += "<div>Observações:</div>";
        faturaHTML += "<div>" + fatura.getObservacoes() + "</div>";
        faturaHTML += "<div>--------------------------------------------------------------</div>";
        faturaHTML += "<div><b>OBRIGADO PELA PREFERÊNCIA!</b></div>";
        faturaHTML += "<div>--------------------------------------------------------------</div>";
        faturaHTML += "</body></html>";
        
        return faturaHTML;
}
}
