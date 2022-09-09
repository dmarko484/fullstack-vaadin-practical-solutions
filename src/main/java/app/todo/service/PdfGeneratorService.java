package app.todo.service;

import app.todo.model.Todo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.io.ByteArrayOutputStream;
import java.util.Set;

@Service
public class PdfGeneratorService {

  @Autowired
  SpringTemplateEngine springTemplateEngine;

  public ByteArrayOutputStream createdPdf(Set<Todo> todos) {
    return HtmlToPDFService.createPdf(renderHtmlForPDF(todos));
  }

  private String renderHtmlForPDF(Set<Todo> todos) {
    Context context = new Context();
    context.setVariable("items", todos);
    context.setVariable("title", "ToDo's List: "+todos.size());
    context.setVariable("image1", ImageUtils.convertImageToBase64("pdf/images/image1.png"));
    // barcode image
    context.setVariable("barcode1", BarCodeService.getBarCodeAsBase64("ToDo-List-123456789"));

    return springTemplateEngine.process("pdf/todos.html", context);
  }

}
