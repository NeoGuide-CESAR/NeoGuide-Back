package neoguide.project.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MemorialPdfWriterTest {

    @Test
    @DisplayName("PDF com cabeçalho, xref e startxref apontando para a tabela de referências")
    void estruturaDoPdf() {
        byte[] pdf = MemorialPdfWriter.gerar(List.of("Linha de teste", "Protocolo: PRJ-2026-0001"));
        String texto = new String(pdf, StandardCharsets.ISO_8859_1);

        assertTrue(texto.startsWith("%PDF-1.4"));
        assertTrue(texto.endsWith("%%EOF\n"));
        int startxref = Integer.parseInt(texto.substring(texto.lastIndexOf("startxref\n") + 10,
                texto.lastIndexOf("\n%%EOF")).trim());
        assertEquals("xref", texto.substring(startxref, startxref + 4));
    }

    @Test
    @DisplayName("Cada objeto está no deslocamento declarado na tabela xref")
    void deslocamentosDoXrefCorretos() {
        byte[] pdf = MemorialPdfWriter.gerar(List.of("a", "b"));
        String texto = new String(pdf, StandardCharsets.ISO_8859_1);
        int startxref = Integer.parseInt(texto.substring(texto.lastIndexOf("startxref\n") + 10,
                texto.lastIndexOf("\n%%EOF")).trim());
        String xref = texto.substring(startxref);
        String[] linhas = xref.split("\n");
        // linhas[0] = "xref", linhas[1] = "0 N", linhas[2] = entrada livre; a partir de linhas[3] estão os objetos
        for (int obj = 1; obj <= 3; obj++) {
            int off = Integer.parseInt(linhas[2 + obj].substring(0, 10));
            assertTrue(texto.startsWith(obj + " 0 obj", off), "objeto " + obj + " fora do deslocamento declarado");
        }
    }

    @Test
    @DisplayName("Texto longo é quebrado em linhas e paginado; parênteses são escapados")
    void quebraEPaginacao() {
        List<String> linhas = new ArrayList<>();
        for (int i = 0; i < 120; i++) {
            linhas.add("Item (" + i + ") com texto suficiente para ocupar mais de uma linha do memorial de cálculo");
        }
        String texto = new String(MemorialPdfWriter.gerar(linhas), StandardCharsets.ISO_8859_1);
        assertTrue(texto.contains("/Count 3"));
        assertTrue(texto.contains("\\(0\\)") || texto.contains("\\(1\\)"));
    }

    @Test
    @DisplayName("Caracteres fora de Latin-1 viram '?' e não quebram o PDF")
    void caracteresForaDeLatin1() {
        assertEquals("a?b", MemorialPdfWriter.escapar("a—b"));
    }
}
