package app.todo;

import app.todo.model.Todo;
import app.todo.repo.InmemoryRepository;
import app.todo.service.*;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.shared.Registration;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;

@Route("t/:name")
public class TodoUI extends VerticalLayout implements BeforeEnterObserver, HasDynamicTitle {

  String author;

  @Autowired
  DbService dbService;

  @Autowired
  ExcelGeneratorService excelGeneratorService;

  @Autowired
  PdfGeneratorService pdfGeneratorService;

  @Autowired
  AttachmentsService attachmentsService;

  Grid<Todo> view;

  Registration broadcasterRegistration;

  Div downloadArea = new Div();

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);

    H2 title = new H2("TodoList Application: " + author.toUpperCase());
    add(title);

    Button btnAdd = new Button("Add new item");
    btnAdd.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
    btnAdd.addClickListener(buttonClickEvent -> {
      // click event implementation
      Dialog dialog = createAddDialogue();
      dialog.open();
    });

    Button btnRemove = new Button("Remove selected items");
    btnRemove.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
    btnRemove.addClickListener(buttonClickEvent -> {
      dbService.removeItems(view.getSelectedItems());
      Broadcaster.broadcast("Item(s) removed by " + author);
    });
    btnRemove.setVisible(true);

    Anchor downloadExcel = new Anchor(new StreamResource("excel.xlsx", () -> {
      try {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        excelGeneratorService.createExcelFile(view.getSelectedItems()).write(os);

        return new ByteArrayInputStream(os.toByteArray());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

    }), null);

    Button btnExport = new Button("Export selected to Excel");
    btnExport.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
    downloadExcel.add(btnExport);

    Anchor downloadPdf = new Anchor(new StreamResource("todo-list.pdf", () -> new ByteArrayInputStream(pdfGeneratorService.createdPdf(view.getSelectedItems()).toByteArray())), null);
    downloadPdf.setTarget("_blank");

    Button btnPdf = new Button("Export selected to PDF");
    btnPdf.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
    downloadPdf.add(btnPdf);

    add(new HorizontalLayout(btnAdd, btnRemove, downloadExcel, downloadPdf));

    view = new Grid();
    view.setAllRowsVisible(true);
    view.setSelectionMode(Grid.SelectionMode.MULTI);
    view.addColumn(Todo::getTitle);
    view.addColumn(Todo::getBody);
    view.addColumn(Todo::getAuthor);
    view.addColumn(Todo::getCreatedAt);
    refreshItems();
    add(view);

    Button btnSelectAll = new Button("select all");
    btnSelectAll.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    btnSelectAll.setIcon(VaadinIcon.PLUS.create());
    btnSelectAll.addClickListener(buttonClickEvent -> {
      view.asMultiSelect().select(dbService.getAllItems());
    });

    Button btnDeSelectAll = new Button("deselect all");
    btnDeSelectAll.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    btnDeSelectAll.setIcon(VaadinIcon.MINUS.create());
    btnDeSelectAll.addClickListener(buttonClickEvent -> {
      view.asMultiSelect().deselectAll();
    });

    add(new HorizontalLayout(btnSelectAll, btnDeSelectAll));

    MemoryBuffer memoryBuffer = new MemoryBuffer();
    Upload multiFileUpload = new Upload(memoryBuffer);
    multiFileUpload.addSucceededListener(event -> {
      attachmentsService.storeInGridFS(memoryBuffer.getInputStream(), event.getFileName(), event.getContentLength());
      Broadcaster.broadcast("New File " + event.getFileName() + " uploaded by " + author);
    });

    add(multiFileUpload, downloadArea);

    UI ui = attachEvent.getUI();
    broadcasterRegistration = Broadcaster.register(message -> {
      ui.access(() -> {
        refreshItems();
        Notification.show(message);
      });
    });

  }

  private VerticalLayout getDownloadPanel() {
    VerticalLayout root = new VerticalLayout();

    attachmentsService.listAllFiles()
            .forEach(file -> {
              Anchor download = new Anchor(new StreamResource(file.getFilename(), () -> {
                try {
                  return attachmentsService.getGridFsTemplate().getResource(file).getInputStream();
                } catch (IOException e) {
                  throw new RuntimeException(e);
                }
              }), null);
              download.setTarget("_blank");

              Button btn = new Button(file.getFilename(), new Icon(VaadinIcon.DOWNLOAD_ALT));
              btn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
              btn.getStyle().set("cursor", "pointer");
              download.add(btn);

              Button btnRemove = new Button(null, new Icon(VaadinIcon.FILE_REMOVE));
              btnRemove.getStyle().set("cursor", "pointer");
              btnRemove.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
              btnRemove.addClickListener(buttonClickEvent -> {
                attachmentsService.deleteAttachment(file.getObjectId().toString());
                Broadcaster.broadcast("File " + file.getFilename() + " removed by " + author);
              });

              root.add(new HorizontalLayout(download, btnRemove));

            });

    return root;
  }

  private Dialog createAddDialogue() {
    Dialog dialog = new Dialog();
    dialog.setHeaderTitle("New ToDo");

    VerticalLayout dialogLayout = new VerticalLayout();
    TextField title = new TextField("Title");
    dialogLayout.add(title);

    dialog.add(dialogLayout);

    Button btnSave = new Button("Add");
    btnSave.addClickListener(buttonClickEvent -> {
      dbService.addToItems(Todo.builder()
              .title(title.getValue())
              .body("Body from " + title.getValue())
              .author(author)
              .createdAt(LocalDateTime.now())
              .build());
      dialog.close();
      //refreshItems();
      Broadcaster.broadcast("Item added by " + author);
    });

    Button btnCancel = new Button("Cancel");
    btnCancel.addClickListener(buttonClickEvent -> dialog.close());

    dialog.getFooter().add(btnSave);
    dialog.getFooter().add(btnCancel);

    return dialog;
  }

  private void refreshItems() {
    view.setItems(dbService.getAllItems());

    downloadArea.removeAll();
    downloadArea.add(getDownloadPanel());
  }

  @Override
  public void beforeEnter(BeforeEnterEvent event) {
    author = event.getRouteParameters().get("name").get();
  }

  @Override
  public String getPageTitle() {
    return "ToDo: " + author.toUpperCase();
  }

  @Override
  protected void onDetach(DetachEvent detachEvent) {
    super.onDetach(detachEvent);

    if (broadcasterRegistration != null) {
      broadcasterRegistration.remove();
      broadcasterRegistration = null;
    }


  }
}
