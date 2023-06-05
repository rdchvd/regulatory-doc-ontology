import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class DocxReader {
    public String read(String filePath) {
        StringBuilder textBuilder = new StringBuilder();

        try (FileInputStream fis = new FileInputStream(filePath);
             XWPFDocument document = new XWPFDocument(fis)) {

            List<XWPFParagraph> paragraphs = document.getParagraphs();

            for (XWPFParagraph paragraph : paragraphs) {
                String text = paragraph.getText();
                textBuilder.append(text).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return textBuilder.toString();
    }
}
