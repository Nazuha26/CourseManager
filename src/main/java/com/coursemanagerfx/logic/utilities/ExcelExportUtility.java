/*
========================================
THIS FILE CREATED FOR "CourseManagerFX"
            Author: Nazuha26
========================================
*/

package com.coursemanagerfx.logic.utilities;

import com.coursemanagerfx.logic.basic.Group;
import com.coursemanagerfx.logic.basic.Student;
import com.coursemanagerfx.logic.basic.event.EventStatus;
import com.coursemanagerfx.logic.basic.event.category.EventCategories;
import com.coursemanagerfx.logic.basic.event.StudentEvent;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class ExcelExportUtility {

    /* === HEADERS === */
    private final static String[] HEADERS = {
            "№", "П.І.Б.", "Сесія", "СФП", "СК, КГ, КВ", "Заохочення", "Стягнення",
            "Секретники", "Редколегія", "Журналісти", "Спорторги", "Наукова діяльність",
            "Сертифікати", "Призери змагань", "Волонтерська діяльність",
            "Громадське життя", "Додаткові бали", "Заг. к-ть. балів"
    };
    /* =============== */

    /* ===== FONTS ===== */
    private static XSSFFont     font14;
    private static XSSFFont     font16;
    private static XSSFFont     font18;
    private static XSSFFont     font20;
    private static XSSFFont font16Bold;
    private static XSSFFont font18Bold;
    private static XSSFFont font20Bold;

    private static XSSFFont    tahoma9;
    /* ================= */

    public static boolean exportToExcel(Group[] course, String courseName, File path) {
        Locale locale_before = Locale.getDefault();                     // === remember current locale
        Locale.setDefault(Locale.of("uk", "UA"));       // === ALWAYS SET UKR LOCALE

        DateTimeFormatter monthFormatter =
                DateTimeFormatter.ofPattern("LLLL", Locale.forLanguageTag("uk"));

        LocalDate now = LocalDate.now();
        String headCaption  = courseName + " за " +
                now.format(monthFormatter) + " " +
                now.getYear() + " року";

        String fileName = headCaption + ".xlsx";

        File outFile = new File(path, fileName);

        try (XSSFWorkbook wb = new XSSFWorkbook();
             FileOutputStream fos = new FileOutputStream(outFile)) {

            /* --- FONTS --- */
            font14 = createFont(wb, "Times New Roman", 14, false);
            font16 = createFont(wb, "Times New Roman", 16, false);
            font18 = createFont(wb, "Times New Roman", 18, false);
            font20 = createFont(wb, "Times New Roman", 20, false);
            font16Bold = createFont(wb, "Times New Roman", 16, true);
            font18Bold = createFont(wb, "Times New Roman", 18, true);
            font20Bold = createFont(wb, "Times New Roman", 20, true);
            tahoma9 = createFont(wb, "Tahoma", 9, false);
            /* ------------- */

            // Styles
            XSSFCellStyle centerStyle = createStyle(wb, font16, HorizontalAlignment.CENTER);
            XSSFCellStyle captionStyle = createStyle(wb, font20, HorizontalAlignment.CENTER);
            XSSFCellStyle headerStyle = createStyle(wb, font18Bold, HorizontalAlignment.CENTER);
            XSSFCellStyle rotatedStyle = wb.createCellStyle();
            rotatedStyle.cloneStyleFrom(headerStyle);
            rotatedStyle.setRotation((short) 90);

            XSSFCellStyle pibStyle = createStyle(wb, font20Bold, HorizontalAlignment.LEFT);
            XSSFCellStyle numberStyle = createStyle(wb, font16Bold, HorizontalAlignment.CENTER);
            XSSFCellStyle signStyle = createStyle(wb, font18Bold, HorizontalAlignment.LEFT);
            XSSFCellStyle headOfCourseStyle = createStyle(wb, font18Bold, HorizontalAlignment.CENTER);

            /* ----------------------------------------------------------------- */

            XSSFSheet sheet = wb.createSheet("Лист1");
            sheet.setZoom(70);                                      // set zoom

            /* create first main caption row */
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 17));   // merge cols A-R
            XSSFRow row = sheet.createRow(0);//может динамически сделать в зависимости от количества категорий на случай если будет больше категорий?
            row.setHeightInPoints(30);
            XSSFCell cell = row.createCell(0);
            cell.setCellValue(headCaption);
            cell.setCellStyle(captionStyle);     // set center style



            /* === HEADING CELLS === */

            /* create HEADING cells */
            sheet.setColumnWidth(0, 1400);                 // set wight for column "№"
            sheet.setColumnWidth(1, 19000);                // set wight for column "ПІБ"

            /* set rotated heading style for cells which come after "ПІБ" */
            XSSFRow headerRow = sheet.createRow(1);
            for (int i = 0; i < HEADERS.length; i++) {
                XSSFCell headerRowCell = headerRow.createCell(i);
                headerRowCell.setCellValue(HEADERS[i]);
                if (i >= 2) headerRowCell.setCellStyle(rotatedStyle);
                else headerRowCell.setCellStyle(headerStyle);
            }

            /* ===================== */



            /* === CALCULATE STUDENTS INFO === */

            List<Student> allStudents = Arrays.stream(course)
                    .flatMap(g -> g.getStudents().stream())
                    .toList();

            List<StudentRow> studentRows = new ArrayList<>();

            for (Student student : allStudents) {
                StudentRow studentRow = new StudentRow();
                studentRow.name = student.getName();

                for (StudentEvent ev : student.getEvents())
                    if (ev.getStatus() == EventStatus.ACTIVE)
                        studentRow.add(ev.getCategory(), ev.getMark(), ev.getDescription());

                studentRows.add(studentRow);
            }

            /* =============================== */



            /* === SET STUDENTS INFO === */

            studentRows.sort(Comparator
                    .comparingDouble(StudentRow::total).reversed()     // sort by total mark
                    .thenComparing(sr -> sr.name)                      // sort by name if total mark equals
            );

            int startRow = 2;

            for (int i = 0; i < studentRows.size(); i++) {
                StudentRow sr = studentRows.get(i);
                XSSFRow studentRow = sheet.createRow(startRow + i);

                /* number "№" column */
                XSSFCell c0 = studentRow.createCell(0);
                c0.setCellValue(i + 1);
                c0.setCellStyle(numberStyle);
                /* ----------------- */

                /* name "ПІБ" column */
                XSSFCell c1 = studentRow.createCell(1);
                c1.setCellValue(sr.name);
                c1.setCellStyle(pibStyle);
                /* ----------------- */

                for (var entry : COL.entrySet()) {
                    EventCategories cat = entry.getKey();
                    int col_number = entry.getValue();
                    double mark = sr.sum(cat);

                    if (mark != 0) {    /* if 0 skip */
                        XSSFCell c = studentRow.createCell(col_number);
                        c.setCellValue(mark);
                        c.setCellStyle(centerStyle);
                        addHiddenCommentToCell(wb, sheet, c, sr.comment(cat));
                    }
                }

                /* total number of marks */
                XSSFCell c17 = studentRow.createCell(17);
                c17.setCellValue(sr.total());
                c17.setCellStyle(centerStyle);


                /*  === COLOR FILL from column " № " (0) to " total mark " (17) ===  */
                double total = sr.total();
                java.awt.Color fillColor;

                if (total <= 30)        fillColor = new java.awt.Color(255,0,0);
                else if (total <= 50)   fillColor = new java.awt.Color(255,255,0);
                else                    fillColor = new java.awt.Color(112,173,71);

                Cell topLeft = sheet.getRow(startRow + i).getCell(0);       // column "№"
                Cell bottomRight = sheet.getRow(startRow + i).getCell(17);  // column "Заг. к-ть. балів"

                fillColorArea(wb, sheet, topLeft, bottomRight, fillColor);
            }

            /* ========================= */



            /* apply all borders for cells */
            Cell topLeft = sheet.getRow(1).getCell(0);
            Cell bottomRight = sheet.getRow(startRow + studentRows.size() - 1).getCell(17);
            applyAllBorders(wb, sheet, topLeft, bottomRight);
            /* --------------------------- */



            /* === ADD SIGN UNDER THE TABLE === */
            int baseRow = startRow + studentRows.size() ;

            XSSFRow r1 = sheet.createRow(baseRow);
            XSSFCell c1 = r1.createCell(1);
            c1.setCellValue("Начальник курсу");
            c1.setCellStyle(signStyle);

            XSSFRow r2 = sheet.createRow(baseRow + 1);
            XSSFCell c2 = r2.createCell(1);
            c2.setCellValue("підполковник");
            c2.setCellStyle(signStyle);

            XSSFRow r3 = sheet.createRow(baseRow + 2);
            XSSFCell c3 = r3.createCell(1);
            String captionForSign = "___ " + now.format(monthFormatter) + " " + now.getYear() + " року";
            c3.setCellValue(captionForSign);
            c3.setCellStyle(signStyle);

            sheet.addMergedRegion(new CellRangeAddress(baseRow + 1, baseRow + 1, 12, 17));   // merge cols M-R
            XSSFCell nameCell = r2.createCell(12); // M
            nameCell.setCellValue("Віктор ФЕСЕНКО");
            nameCell.setCellStyle(headOfCourseStyle);
            /* ================================ */



            wb.write(fos);

            if (Desktop.isDesktopSupported())
                Desktop.getDesktop().open(outFile);     // open up exported file

            Locale.setDefault(locale_before);           // === set remembered default locale
            return true;
        } catch (IOException e) {
            throw new RuntimeException("=== FATAL ERROR EXPORTING TO EXCEL ===", e);
        }
    }

    /* ===== CORE ===== */
    private static final Map<EventCategories, Integer> COL = Map.ofEntries(
            Map.entry(EventCategories.MOD_1,  2),
            Map.entry(EventCategories.MOD_2,  3),
            Map.entry(EventCategories.MOD_3,  4),
            Map.entry(EventCategories.MOD_4,  5),
            Map.entry(EventCategories.MOD_5,  6),
            Map.entry(EventCategories.MOD_6,  7),
            Map.entry(EventCategories.MOD_7,  8),
            Map.entry(EventCategories.MOD_8,  9),
            Map.entry(EventCategories.MOD_9,  10),
            Map.entry(EventCategories.MOD_10, 11),
            Map.entry(EventCategories.MOD_11, 12),
            Map.entry(EventCategories.MOD_12, 13),
            Map.entry(EventCategories.MOD_13, 14),
            Map.entry(EventCategories.MOD_14, 15),
            Map.entry(EventCategories.CUSTOM, 16)
    );

    // WARNING не корректно подгоняет размеры примечания под количество текста
    private static void addHiddenCommentToCell(XSSFWorkbook workbook, XSSFSheet sheet, XSSFCell cell, String text) {
        CreationHelper factory = workbook.getCreationHelper();
        Drawing<?> drawing = sheet.createDrawingPatriarch();

        ClientAnchor anchor = factory.createClientAnchor();
        anchor.setCol1(cell.getColumnIndex());
        anchor.setCol2(cell.getColumnIndex() + 2); // width
        anchor.setRow1(cell.getRowIndex());
        anchor.setRow2(cell.getRowIndex() + 3);    // height

        Comment comment = drawing.createCellComment(anchor);

        XSSFRichTextString richText = new XSSFRichTextString(text);
        richText.applyFont(tahoma9);

        comment.setString(richText);
        comment.setVisible(false); // hide by default

        cell.setCellComment(comment);
    }

    private static void fillColorArea(XSSFWorkbook wb, XSSFSheet sheet,
                                      Cell topLeft, Cell bottomRight,
                                      java.awt.Color color) {

        Map<CellStyle, CellStyle> styleCache = new HashMap<>();

        /*  convert java.awt.Color to XSSFColor  */
        byte[] rgb = new byte[]{
                (byte) color.getRed(),
                (byte) color.getGreen(),
                (byte) color.getBlue()
        };
        XSSFColor xssfColor = new XSSFColor(rgb, null);

        int firstRow = topLeft.getRowIndex();
        int lastRow  = bottomRight.getRowIndex();
        int firstCol = topLeft.getColumnIndex();
        int lastCol  = bottomRight.getColumnIndex();

        for (int r = firstRow; r <= lastRow; r++) {
            Row row = sheet.getRow(r);
            if (row == null) row = sheet.createRow(r);

            for (int c = firstCol; c <= lastCol; c++) {
                Cell cell = row.getCell(c, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                CellStyle baseStyle = cell.getCellStyle();

                CellStyle filledStyle = styleCache.computeIfAbsent(baseStyle, s -> {
                    XSSFCellStyle st = wb.createCellStyle();
                    st.cloneStyleFrom(s);
                    st.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                    st.setFillForegroundColor(xssfColor);
                    return st;
                });

                cell.setCellStyle(filledStyle);
            }
        }
    }

    private static void applyAllBorders(XSSFWorkbook wb, XSSFSheet sheet, Cell topLeft, Cell bottomRight) {
        Map<CellStyle, CellStyle> styleCache = new HashMap<>();

        int firstRow = topLeft.getRowIndex();
        int lastRow  = bottomRight.getRowIndex();
        int firstCol = topLeft.getColumnIndex();
        int lastCol  = bottomRight.getColumnIndex();

        for (int r = firstRow; r <= lastRow; r++) {
            Row row = sheet.getRow(r);
            if (row == null) row = sheet.createRow(r);

            for (int c = firstCol; c <= lastCol; c++) {
                Cell cell = row.getCell(c, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                CellStyle baseStyle = cell.getCellStyle();

                CellStyle borderedStyle = styleCache.computeIfAbsent(baseStyle, s -> {
                    XSSFCellStyle st = wb.createCellStyle();
                    st.cloneStyleFrom(s);
                    st.setBorderTop(BorderStyle.THIN);
                    st.setBorderBottom(BorderStyle.THIN);
                    st.setBorderLeft(BorderStyle.THIN);
                    st.setBorderRight(BorderStyle.THIN);
                    st.setTopBorderColor(IndexedColors.BLACK.getIndex());
                    st.setBottomBorderColor(IndexedColors.BLACK.getIndex());
                    st.setLeftBorderColor(IndexedColors.BLACK.getIndex());
                    st.setRightBorderColor(IndexedColors.BLACK.getIndex());
                    return st;
                });

                cell.setCellStyle(borderedStyle);
            }
        }
    }

    private static XSSFFont createFont(XSSFWorkbook wb, String fontName, int size, boolean bold) {
        XSSFFont font = wb.createFont();
        font.setFontName(fontName);
        font.setFontHeightInPoints((short) size);
        font.setBold(bold);
        return font;
    }

    private static XSSFCellStyle createStyle(XSSFWorkbook wb, XSSFFont font,
                                             HorizontalAlignment hAlign) {
        XSSFCellStyle style = wb.createCellStyle();
        style.setFont(font);
        style.setAlignment(hAlign);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }
}

// === class StudentRow ===
class StudentRow {

    private static class CatData {
        double sum = 0;
        List<String> comments = new ArrayList<>();
    }

    private final EnumMap<EventCategories, CatData> data = new EnumMap<>(EventCategories.class);

    String name;                                 // П.І.Б.

    void add(EventCategories cat, double mark, String descr) {
        CatData d = data.computeIfAbsent(cat, k -> new CatData());
        d.sum += mark;
        d.comments.add(formatMark(mark) + " – " + descr);
    }

    double sum(EventCategories cat) {
        return data.getOrDefault(cat, new CatData()).sum;
    }

    String comment(EventCategories cat) {
        return String.join("\n",
                data.getOrDefault(cat, new CatData()).comments);
    }

    double total() {
        return data.values().stream()
                .mapToDouble(d -> d.sum)
                .sum();
    }

    private static String formatMark(double m)
        { return (m % 1 == 0) ? String.valueOf((int) m) : String.valueOf(m); }
}
