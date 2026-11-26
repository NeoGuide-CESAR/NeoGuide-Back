package neoguide.project.service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Gera o memorial em PDF sem dependência externa: texto em Helvetica (WinAnsi/Latin-1), paginado em A4.
 * O layout final deve seguir o modelo de memorial oficial, que ainda não está no repositório.
 */
public final class MemorialPdfWriter {

    private static final int LINHAS_POR_PAGINA = 52;
    private static final int LARGURA_MAXIMA = 92;

    private MemorialPdfWriter() {
    }

    public static byte[] gerar(List<String> linhas) {
        List<String> quebradas = new ArrayList<>();
        for (String linha : linhas) {
            quebradas.addAll(quebrar(linha == null ? "" : linha));
        }
        List<List<String>> paginas = new ArrayList<>();
        for (int i = 0; i < quebradas.size(); i += LINHAS_POR_PAGINA) {
            paginas.add(quebradas.subList(i, Math.min(i + LINHAS_POR_PAGINA, quebradas.size())));
        }
        if (paginas.isEmpty()) {
            paginas.add(List.of());
        }

        int n = paginas.size();
        // Objetos: 1 catálogo, 2 páginas, 3 fonte; para cada página: objeto de página e objeto de conteúdo.
        List<String> objetos = new ArrayList<>();
        objetos.add("<< /Type /Catalog /Pages 2 0 R >>");
        StringBuilder kids = new StringBuilder();
        for (int i = 0; i < n; i++) {
            kids.append(4 + 2 * i).append(" 0 R ");
        }
        objetos.add("<< /Type /Pages /Kids [" + kids.toString().trim() + "] /Count " + n + " >>");
        objetos.add("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>");
        for (int i = 0; i < n; i++) {
            int idConteudo = 5 + 2 * i;
            objetos.add("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] "
                    + "/Resources << /Font << /F1 3 0 R >> >> /Contents " + idConteudo + " 0 R >>");
            String conteudo = conteudoDaPagina(paginas.get(i));
            objetos.add("<< /Length " + conteudo.getBytes(StandardCharsets.ISO_8859_1).length + " >>\nstream\n"
                    + conteudo + "\nendstream");
        }

        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        escrever(saida, "%PDF-1.4\n%âãÏÓ\n");
        List<Integer> deslocamentos = new ArrayList<>();
        for (int i = 0; i < objetos.size(); i++) {
            deslocamentos.add(saida.size());
            escrever(saida, (i + 1) + " 0 obj\n" + objetos.get(i) + "\nendobj\n");
        }
        int inicioXref = saida.size();
        StringBuilder xref = new StringBuilder();
        xref.append("xref\n0 ").append(objetos.size() + 1).append('\n');
        xref.append("0000000000 65535 f \n");
        for (int off : deslocamentos) {
            xref.append(String.format("%010d 00000 n \n", off));
        }
        xref.append("trailer\n<< /Size ").append(objetos.size() + 1).append(" /Root 1 0 R >>\n");
        xref.append("startxref\n").append(inicioXref).append("\n%%EOF\n");
        escrever(saida, xref.toString());
        return saida.toByteArray();
    }

    private static String conteudoDaPagina(List<String> linhas) {
        StringBuilder s = new StringBuilder("BT\n/F1 10 Tf\n14 TL\n50 790 Td\n");
        for (String linha : linhas) {
            s.append('(').append(escapar(linha)).append(") Tj T*\n");
        }
        s.append("ET");
        return s.toString();
    }

    /** Escapa parênteses e barras invertidas; caracteres fora de Latin-1 viram '?'. */
    static String escapar(String texto) {
        StringBuilder s = new StringBuilder();
        for (char c : texto.toCharArray()) {
            if (c > 255) {
                s.append('?');
            } else if (c == '(' || c == ')' || c == '\\') {
                s.append('\\').append(c);
            } else if (c < 32) {
                s.append(' ');
            } else {
                s.append(c);
            }
        }
        return s.toString();
    }

    static List<String> quebrar(String linha) {
        List<String> partes = new ArrayList<>();
        if (linha.length() <= LARGURA_MAXIMA) {
            partes.add(linha);
            return partes;
        }
        StringBuilder atual = new StringBuilder();
        for (String palavra : linha.split(" ")) {
            if (atual.length() > 0 && atual.length() + 1 + palavra.length() > LARGURA_MAXIMA) {
                partes.add(atual.toString());
                atual.setLength(0);
            }
            if (atual.length() > 0) atual.append(' ');
            atual.append(palavra);
        }
        partes.add(atual.toString());
        return partes;
    }

    private static void escrever(ByteArrayOutputStream saida, String texto) {
        byte[] bytes = texto.getBytes(StandardCharsets.ISO_8859_1);
        saida.writeBytes(bytes);
    }
}
