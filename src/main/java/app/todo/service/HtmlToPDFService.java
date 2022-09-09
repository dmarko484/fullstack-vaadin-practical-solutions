package app.todo.service;

import com.itextpdf.text.DocumentException;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class HtmlToPDFService {

  public static ByteArrayOutputStream createPdf(String html) {
    ITextRenderer renderer = new ITextRenderer();

    renderer.setDocumentFromString(html);
    renderer.layout();

    ByteArrayOutputStream fs = new ByteArrayOutputStream();
    try {
      renderer.createPDF(fs);

      return fs;
    } catch (DocumentException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

}
