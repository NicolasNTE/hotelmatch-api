package pe.edu.upc.hotelmatch.report;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Small, dependency-free single-page PDF for the fixed report summary. */
final class SimplePdf {
    private SimplePdf() {}
    static byte[] create(List<String> lines) {
        StringBuilder stream = new StringBuilder("BT /F1 12 Tf 50 780 Td 22 TL\n");
        for (String line : lines) stream.append('(').append(line.replace("\\", "\\\\")
                .replace("(", "\\(").replace(")", "\\)")).append(") Tj T*\n");
        stream.append("ET\n");
        List<String> objects = List.of("<< /Type /Catalog /Pages 2 0 R >>",
                "<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
                "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>",
                "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>",
                "<< /Length " + stream.toString().getBytes(StandardCharsets.US_ASCII).length + " >>\nstream\n" + stream + "endstream");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<Integer> offsets = new ArrayList<>();
        write(out, "%PDF-1.4\n");
        for (int i = 0; i < objects.size(); i++) {
            offsets.add(out.size()); write(out, (i + 1) + " 0 obj\n" + objects.get(i) + "\nendobj\n");
        }
        int xref = out.size();
        write(out, "xref\n0 6\n0000000000 65535 f \n");
        for (int offset : offsets) write(out, String.format(java.util.Locale.ROOT, "%010d 00000 n \n", offset));
        write(out, "trailer\n<< /Size 6 /Root 1 0 R >>\nstartxref\n" + xref + "\n%%EOF\n");
        return out.toByteArray();
    }
    private static void write(ByteArrayOutputStream out, String s) { out.writeBytes(s.getBytes(StandardCharsets.US_ASCII)); }
}
