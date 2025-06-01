package com.coursemanagerfx.logic.utilities;

import com.coursemanagerfx.logic.basic.Group;
import com.coursemanagerfx.logic.basic.Student;
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

    public static boolean exportToExcel(Group[] course, String courseName, File path) {
        DateTimeFormatter monthFormatter =
                DateTimeFormatter.ofPattern("LLLL", Locale.forLanguageTag("uk"));

        LocalDate now = LocalDate.now();
        String caption  = courseName + " за " +
                now.format(monthFormatter) + " " +
                now.getYear() + " року";

        String fileName = caption + ".xlsx";

        File outFile = new File(path, fileName);

        try (XSSFWorkbook wb = new XSSFWorkbook();
             FileOutputStream fos = new FileOutputStream(outFile)) {

            // Fonts
            XSSFFont font14 = createFont(wb, 14, false);
            XSSFFont font16 = createFont(wb, 16, false);
            XSSFFont font16Bold = createFont(wb, 16, true);
            XSSFFont font18Bold = createFont(wb, 18, true);
            XSSFFont font20Bold = createFont(wb, 20, true);

            // Styles
            XSSFCellStyle centerStyle = createStyle(wb, font16, HorizontalAlignment.CENTER);
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
            XSSFRow row = sheet.createRow(0);
            row.setHeightInPoints(30);
            XSSFCell cell = row.createCell(0);
            cell.setCellValue(caption);
            cell.setCellStyle(headerStyle);     // set center style



            /* === HEADING CELLS === */

            /* create HEADING cells */
            sheet.setColumnWidth(1, 19000);                                          // set wight for column "ПІБ"

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

                for (StudentEvent ev : student.getEvents()) {
                    studentRow.description = ev.getDescription();
                    switch (ev.getCategory()) {
                        case MOD_1   ->   studentRow.sessiya                += ev.getMark();
                        case MOD_2   ->   studentRow.sfp                    += ev.getMark();
                        case MOD_3   ->   studentRow.skKgKv                 += ev.getMark();
                        case MOD_4   ->   studentRow.zaohochennya           += ev.getMark();
                        case MOD_5   ->   studentRow.styagnennya            += ev.getMark();
                        case MOD_6   ->   studentRow.sekretniki             += ev.getMark();
                        case MOD_7   ->   studentRow.redkolegiya            += ev.getMark();
                        case MOD_8   ->   studentRow.jurnalisty             += ev.getMark();
                        case MOD_9   ->   studentRow.sportorgi              += ev.getMark();
                        case MOD_10  ->   studentRow.naukova_diyalnist      += ev.getMark();
                        case MOD_11  ->   studentRow.sertifikaty            += ev.getMark();
                        case MOD_12  ->   studentRow.prizery_zmagan         += ev.getMark();
                        case MOD_13  ->   studentRow.volonterska_diyalnist  += ev.getMark();
                        case MOD_14  ->   studentRow.gromadske_jittya       += ev.getMark();
                        case CUSTOM  ->   studentRow.dodatkovi_baly         += ev.getMark();
                    }
                }

                studentRows.add(studentRow);
            }

            /* =============================== */



            /* === SET STUDENTS INFO === */

            studentRows.sort(Comparator
                    .comparingInt(StudentRow::total).reversed()     // sort by total mark
                    .thenComparing(sr -> sr.name)                   // sort by name if total mark equals
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

                /* if 0 skip */
                if (sr.sessiya != 0)                { XSSFCell c = studentRow.createCell(2);  c.setCellValue(sr.sessiya);                c.setCellStyle(centerStyle);   addHiddenCommentToCell(wb, sheet, c, sr.description); }
                if (sr.sfp != 0)                    { XSSFCell c = studentRow.createCell(3);  c.setCellValue(sr.sfp);                    c.setCellStyle(centerStyle);   addHiddenCommentToCell(wb, sheet, c, sr.description); }
                if (sr.skKgKv != 0)                 { XSSFCell c = studentRow.createCell(4);  c.setCellValue(sr.skKgKv);                 c.setCellStyle(centerStyle);   addHiddenCommentToCell(wb, sheet, c, sr.description); }
                if (sr.zaohochennya != 0)           { XSSFCell c = studentRow.createCell(5);  c.setCellValue(sr.zaohochennya);           c.setCellStyle(centerStyle);   addHiddenCommentToCell(wb, sheet, c, sr.description); }
                if (sr.styagnennya != 0)            { XSSFCell c = studentRow.createCell(6);  c.setCellValue(sr.styagnennya);            c.setCellStyle(centerStyle);   addHiddenCommentToCell(wb, sheet, c, sr.description); }
                if (sr.sekretniki != 0)             { XSSFCell c = studentRow.createCell(7);  c.setCellValue(sr.sekretniki);             c.setCellStyle(centerStyle);   addHiddenCommentToCell(wb, sheet, c, sr.description); }
                if (sr.redkolegiya != 0)            { XSSFCell c = studentRow.createCell(8);  c.setCellValue(sr.redkolegiya);            c.setCellStyle(centerStyle);   addHiddenCommentToCell(wb, sheet, c, sr.description); }
                if (sr.jurnalisty != 0)             { XSSFCell c = studentRow.createCell(9);  c.setCellValue(sr.jurnalisty);             c.setCellStyle(centerStyle);   addHiddenCommentToCell(wb, sheet, c, sr.description); }
                if (sr.sportorgi != 0)              { XSSFCell c = studentRow.createCell(10); c.setCellValue(sr.sportorgi);              c.setCellStyle(centerStyle);   addHiddenCommentToCell(wb, sheet, c, sr.description); }
                if (sr.naukova_diyalnist != 0)      { XSSFCell c = studentRow.createCell(11); c.setCellValue(sr.naukova_diyalnist);      c.setCellStyle(centerStyle);   addHiddenCommentToCell(wb, sheet, c, sr.description); }
                if (sr.sertifikaty != 0)            { XSSFCell c = studentRow.createCell(12); c.setCellValue(sr.sertifikaty);            c.setCellStyle(centerStyle);   addHiddenCommentToCell(wb, sheet, c, sr.description); }
                if (sr.prizery_zmagan != 0)         { XSSFCell c = studentRow.createCell(13); c.setCellValue(sr.prizery_zmagan);         c.setCellStyle(centerStyle);   addHiddenCommentToCell(wb, sheet, c, sr.description); }
                if (sr.volonterska_diyalnist != 0)  { XSSFCell c = studentRow.createCell(14); c.setCellValue(sr.volonterska_diyalnist);  c.setCellStyle(centerStyle);   addHiddenCommentToCell(wb, sheet, c, sr.description); }
                if (sr.gromadske_jittya != 0)       { XSSFCell c = studentRow.createCell(15); c.setCellValue(sr.gromadske_jittya);       c.setCellStyle(centerStyle);   addHiddenCommentToCell(wb, sheet, c, sr.description); }
                if (sr.dodatkovi_baly != 0)         { XSSFCell c = studentRow.createCell(16); c.setCellValue(sr.dodatkovi_baly);         c.setCellStyle(centerStyle);   addHiddenCommentToCell(wb, sheet, c, sr.description); }

                /* total number of marks */
                XSSFCell c17 = studentRow.createCell(17);
                c17.setCellValue(sr.total());
                c17.setCellStyle(centerStyle);
            }

            /* ========================= */



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

            sheet.addMergedRegion(new CellRangeAddress(baseRow + 1, baseRow + 1, 9, 17));   // merge cols J-R
            XSSFCell nameCell = r2.createCell(9); // J
            nameCell.setCellValue("Віктор ФЕСЕНКО");
            nameCell.setCellStyle(headOfCourseStyle);
            /* ================================ */



            /* apply all borders for cells */
            Cell topLeft = sheet.getRow(1).getCell(0);
            Cell bottomRight = sheet.getRow(startRow + studentRows.size() - 1).getCell(17);
            applyAllBorders(wb, sheet, topLeft, bottomRight);
            /* --------------------------- */



            wb.write(fos);

            if (Desktop.isDesktopSupported())
                Desktop.getDesktop().open(outFile);     // open up exported file

            return true;
        } catch (IOException e) {
            throw new RuntimeException("=== FATAL ERROR EXPORTING TO EXCEL ===", e);
        }
    }

    /* ===== CORE ===== */
    private static void addHiddenCommentToCell(XSSFWorkbook workbook, XSSFSheet sheet, XSSFCell cell, String text) {
        CreationHelper factory = workbook.getCreationHelper();
        Drawing<?> drawing = sheet.createDrawingPatriarch();

        ClientAnchor anchor = factory.createClientAnchor();
        anchor.setCol1(cell.getColumnIndex());
        anchor.setCol2(cell.getColumnIndex() + 2); // width
        anchor.setRow1(cell.getRowIndex());
        anchor.setRow2(cell.getRowIndex() + 3);    // height

        Comment comment = drawing.createCellComment(anchor);
        comment.setString(factory.createRichTextString(text));
        comment.setVisible(false); // hide by default

        cell.setCellComment(comment);
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

    private static XSSFFont createFont(XSSFWorkbook wb, int size, boolean bold) {
        XSSFFont font = wb.createFont();
        font.setFontName("Times New Roman");
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

class StudentRow {
    String name;
    String description;

    int sessiya;
    int sfp;
    int skKgKv;
    int zaohochennya;
    int styagnennya;
    int sekretniki;
    int redkolegiya;
    int jurnalisty;
    int sportorgi;
    int naukova_diyalnist;
    int sertifikaty;
    int prizery_zmagan;
    int volonterska_diyalnist;
    int gromadske_jittya;
    int dodatkovi_baly;

    int total() {
        return  sessiya               + sfp               + skKgKv         + zaohochennya   +
                styagnennya           + sekretniki        + redkolegiya    + jurnalisty     +
                sportorgi             + naukova_diyalnist + sertifikaty    + prizery_zmagan +
                volonterska_diyalnist + gromadske_jittya  + dodatkovi_baly;
    }
}