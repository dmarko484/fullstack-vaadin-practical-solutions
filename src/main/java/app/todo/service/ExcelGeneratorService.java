package app.todo.service;

import app.todo.model.Todo;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ExcelGeneratorService {

  public Workbook createExcelFile(Set<Todo> todos) {
    Workbook wb = new XSSFWorkbook();

    Sheet sheet = wb.createSheet("Todo Items");

    Row row = sheet.createRow(0);
    Cell cell = row.createCell(0);
    cell.setCellValue("ToDo's List ... # "+todos.size());

    CellStyle style = wb.createCellStyle();
    style.setFillBackgroundColor(IndexedColors.LIGHT_GREEN.index);
    style.setBorderBottom(BorderStyle.THIN);

    Font font = wb.createFont();
    font.setColor(IndexedColors.BLUE.getIndex());
    font.setBold(true);
    style.setFont(font);

    cell.setCellStyle(style);

    CellStyle style2 = wb.createCellStyle();
    Font font2 = wb.createFont();
    font2.setBold(true);
    style2.setFont(font2);

    int rowNumber = 2;

    for (Todo item: todos) {
      row = sheet.createRow(rowNumber);

      cell = row.createCell(0);
      cell.setCellValue(item.getAuthor());

      cell = row.createCell(1);
      cell.setCellValue(item.getTitle());
      cell.setCellStyle(style2);

      cell = row.createCell(2);
      cell.setCellValue(item.getBody());

      rowNumber++;
    }

    return wb;
  }

}
