import com.fasterxml.jackson.databind.ObjectMapper;
import javax.swing.*;
import java.awt.print.*;
import java.io.File;
import java.util.Map;
import java.util.List;
import java.io.StringReader;

public class Gfatura {

    public static void main(String[] args) {
        try {
            Imprimir(true);
            Thread.sleep(500);
            Imprimir(false);
      
        } catch (Exception e) {
            System.out.println("Erro ao imprimir: " + e.getMessage());
        }
    }
    public static void Imprimir(boolean show){
        try{
            ObjectMapper mapper = new ObjectMapper();

        // Lê o arquivo e transforma diretamente em um objeto Fatura
        Fatura fatura = mapper.readValue(new File("fatura.json"), Fatura.class);
            String dup = "";
            if(!show) dup = "Duplicado";
            else dup = "Original";
        String faturaHTML = "";
        faturaHTML += "<html><head><style>";
        faturaHTML += "body{margin-top:10px} h2{text-align:center;} div{font-size: 9pt;} b{text-align:center;} span{padding:5px 10px;text-align:center; float:right;} table{font-size:9pt;}";
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
        faturaHTML += "<div>**OBRIGADO PELA PREFERÊNCIA!**</div>";
        faturaHTML += "<div>--------------------------------------------------------------</div>";
        faturaHTML += "</body></html>";

        JEditorPane editorPane = new JEditorPane("text/html", "");
        editorPane.setText(faturaHTML);
        editorPane.setSize(500, 500);
        editorPane.setEditable(false);

        PrinterJob job = PrinterJob.getPrinterJob();

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
    
    }catch(Exception e){e.printStackTrace();}
    }
}
