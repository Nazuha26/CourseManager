package com.coursemanagerfx.logic.utilities;

import com.coursemanagerfx.logic.basic.Group;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class ExcelExportUtility {
    private static String fileName;

    public static void exportToExcel(Group[] course, String courseName, File path) {
        DateTimeFormatter monthFormatter =
                DateTimeFormatter.ofPattern("LLLL", Locale.forLanguageTag("uk"));
        LocalDate now = LocalDate.now();
        String caption  = courseName + " за " +
                now.format(monthFormatter) + " " +
                now.getYear() + " року";
        String        fileName = caption + ".xlsx";
        ExcelExportUtility.fileName = fileName;

        File outFile = new File(path, fileName);

        try (XSSFWorkbook wb = new XSSFWorkbook();
             FileOutputStream fos = new FileOutputStream(outFile)) {

            XSSFFont defaultFont = wb.createFont();
            defaultFont.setFontName("Times New Roman");

            XSSFSheet sheet = wb.createSheet("Лист1");
            sheet.setZoom(70);


            // B (620 px)
            sheet.setColumnWidth(1, 19000);

            // C–R (90 px)
            //for (int i = 2; i <= 17; i++) sheet.setColumnWidth(i, 2500);

            // Объединение A1:R1
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 17));

            // Создание строки и ячейки
            XSSFRow row  = sheet.createRow(0);
            row.setHeightInPoints(30);
            XSSFCell cell = row.createCell(0);
            cell.setCellValue(caption);

            // Стиль: шрифт 16 pt, выравнивание по центру
            XSSFCellStyle style = wb.createCellStyle();
            XSSFFont font = wb.createFont();
            font.setFontName("Times New Roman");
            font.setFontHeightInPoints((short) 16);
            style.setFont(font);
            style.setAlignment(HorizontalAlignment.CENTER);
            style.setVerticalAlignment(VerticalAlignment.CENTER);
            cell.setCellStyle(style);

            // Заголовки
            String[] headers = {
                    "№", "П.І.Б.", "Сесія", "СФП", "СК, КГ, КВ", "Заохочення", "Стягнення",
                    "Секретники", "Редколегія", "Журналісти", "Спорторги", "Наукова діяльність",
                    "Сертифікати", "Призери змагань", "Волонтерська діяльність",
                    "Громадське життя", "Додаткові бали", "Заг. к-ть. балів"
            };

            // Стиль для заголовков: жирный, 18 pt
            XSSFCellStyle headerStyle = wb.createCellStyle();
            XSSFFont headerFont = wb.createFont();
            headerFont.setFontName("Times New Roman");
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 18);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // Стиль с поворотом текста на 90° (для тех, кто правее "Сесія")
            XSSFCellStyle rotatedStyle = wb.createCellStyle();
            rotatedStyle.cloneStyleFrom(headerStyle);
            rotatedStyle.setRotation((short) 90); // вертикальный текст

            XSSFRow headerRow = sheet.createRow(1);
            for (int i = 0; i < headers.length; i++) {
                XSSFCell headerRowCell = headerRow.createCell(i);
                headerRowCell.setCellValue(headers[i]);
                // начиная с "Сесія" (index 2), поворачиваем
                if (i >= 2) {
                    headerRowCell.setCellStyle(rotatedStyle);
                } else {
                    headerRowCell.setCellStyle(headerStyle);
                }
            }


            wb.write(fos);
        } catch (IOException e) {
            throw new RuntimeException("=== FATAL ERROR EXPORTING TO EXCEL ===", e);
        }
    }
}