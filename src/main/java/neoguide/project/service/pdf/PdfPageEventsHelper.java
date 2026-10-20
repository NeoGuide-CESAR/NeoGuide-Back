package neoguide.project.service.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;

public class PdfPageEventsHelper extends PdfPageEventHelper {

    private final String protocolo;
    private PdfTemplate totalPagesTemplate;
    private final Font footerFont;

    public PdfPageEventsHelper(String protocolo) {
        this.protocolo = (protocolo != null && !protocolo.isBlank()) ? protocolo : "N/A";
        this.footerFont = new Font(Font.HELVETICA, 8, Font.NORMAL, new Color(100, 100, 100));
    }

    @Override
    public void onOpenDocument(PdfWriter writer, Document document) {
        totalPagesTemplate = writer.getDirectContent().createTemplate(30, 16);
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        // Linha separadora do rodapé
        writer.getDirectContent().setColorStroke(new Color(220, 220, 220));
        writer.getDirectContent().moveTo(document.left(), document.bottom() - 10);
        writer.getDirectContent().lineTo(document.right(), document.bottom() - 10);
        writer.getDirectContent().stroke();

        // Texto do lado esquerdo: Sistema e Protocolo
        String footerTextLeft = "NeoGuide — Sistema de Dimensionamento MUC | Protocolo: " + protocolo;
        ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_LEFT,
                new Phrase(footerTextLeft, footerFont),
                document.left(), document.bottom() - 22, 0);

        // Texto do lado direito: Página X de
        String pageText = "Página " + writer.getPageNumber() + " de ";
        float pageTextPosition = document.right() - 20;

        ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_RIGHT,
                new Phrase(pageText, footerFont),
                pageTextPosition, document.bottom() - 22, 0);

        // Adiciona template com o número total ao lado direito
        writer.getDirectContent().addTemplate(totalPagesTemplate, pageTextPosition, document.bottom() - 22);
    }

    @Override
    public void onCloseDocument(PdfWriter writer, Document document) {
        totalPagesTemplate.beginText();
        totalPagesTemplate.setFontAndSize(footerFont.getCalculatedBaseFont(true), 8);
        totalPagesTemplate.setColorFill(new Color(100, 100, 100));
        totalPagesTemplate.setTextMatrix(0, 0);
        totalPagesTemplate.showText(String.valueOf(writer.getPageNumber() - 1));
        totalPagesTemplate.endText();
    }
}
