package com.coursemanager;

import com.coursemanager.other.Group;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.fonts.inter.FlatInterFont;
import com.formdev.flatlaf.ui.FlatLineBorder;
import raven.toast.ToastClientProperties;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class CM_HELPER {
    public static final String THEME_ACCENT_COLOR_BLUE = "#007aff"; // Blue
    public static final String THEME_ACCENT_COLOR_RED = "#ff003c"; // Red
    public static final String THEME_ACCENT_COLOR_GREEN = "#00ff59"; // Red

    public static final int BG_SUCCESS_COLOR = 0xb4e2bd;
    public static final int FG_SUCCESS_COLOR = 0x1ab403;

    public static final int BG_ERROR_COLOR = 0xe2b4b4;
    public static final int FG_ERROR_COLOR = 0xb40202;

    public static final File CONFIG_DIR = new File(System.getProperty("user.home"), "AppData/Local/CourseManager");
    public static final File COURSES_DIR = new File(CONFIG_DIR.toString(), "Courses/");
    public static final File SECRET_KEY_DIR = new File(System.getProperty("user.home"), ".coursemanager");
    public static final File FIRST_RUN_FILE = new File(CONFIG_DIR, "FirstRun");
    public static final File CONFIG_FILE = new File(CM_HELPER.CONFIG_DIR, "config.json");

    private static String courseName;
    private static List<Group> courseGroupsList;
    private static String FileCoursePath;

    public static ResourceBundle getBundle() {
        return ResourceBundle.getBundle("strings", Locale.getDefault());
    }

    public static String getCourseName() {
        return courseName;
    }

    public static void setCourseName(String courseName) {
        CM_HELPER.courseName = courseName;
    }

    // ------------------------------------------------------------------

    public static List<Group> getCourseGroupsList() {
        return courseGroupsList;
    }

    public static void setCourseGroupsList(List<Group> courseGroupsList) {
        CM_HELPER.courseGroupsList = courseGroupsList;
    }

    // ------------------------------------------------------------------

    public static String getFileCoursePath() {
        return FileCoursePath;
    }

    public static void setFileCoursePath(String fileCoursePath) {
        FileCoursePath = fileCoursePath;
    }

    // ------------------------------------------------------------------

    public static void initTheme() {
        try {
            FlatLaf.setGlobalExtraDefaults(Collections.singletonMap("@accentColor", CM_HELPER.THEME_ACCENT_COLOR_BLUE));

            FlatLightLaf.setup();

            //UIManager.put("Toast.closeIconColor", new Color(0xd30000));
            UIManager.put(ToastClientProperties.TOAST_SUCCESS_ICON, new FlatSVGIcon("Icons/notification/success_icon_32x32.svg"));
            UIManager.put(ToastClientProperties.TOAST_INFO_ICON, new FlatSVGIcon("Icons/notification/info_icon_32x32.svg"));
            UIManager.put(ToastClientProperties.TOAST_WARNING_ICON, new FlatSVGIcon("Icons/notification/warning_icon_32x32.svg"));
            UIManager.put(ToastClientProperties.TOAST_ERROR_ICON, new FlatSVGIcon("Icons/notification/error_icon_32x32.svg"));

            //FlatRobotoFont.install();
            UIManager.put("defaultFont", new Font(FlatInterFont.FAMILY, Font.PLAIN, 13));

            // Включаем кастомные window decorations
            UIManager.put("RootPane.useWindowDecorations", true);
            UIManager.put("RootPane.menuBarEmbedded", true);
            UIManager.put("TitlePane.buttonSize", new Dimension(50, 35));
            UIManager.put("TitlePane.centerTitle", true);

            UIManager.put("ScrollBar.thumbArc", 100);
            //UIManager.put("ScrollBar.thumb", 100);

            // Устанавливаем стиль кнопок (по умолчанию)
            UIManager.put("Button.arc", 10);
            //UIManager.put("Button.borderWidth", 0);
            //UIManager.put("Button.focusWidth", 0);
            //UIManager.put("Button.innerFocusWidth", 0);

            //UIManager.put("Table.selectionBackground", new Color(48, 134, 89, 153));
            //UIManager.put("Table.cellFocusColor", new Color(0, 186, 89, 255));
            //UIManager.put("Table.focusCellBackground", new Color(0, 186, 89, 255));
            UIManager.put("Table.alternateRowColor", new Color(0, 0, 0, 10));
            UIManager.put("TableHeader.background", new Color(0x7BBBFF));
            UIManager.put("TableHeader.separatorColor", new Color(0, 0, 0));
            UIManager.put("TableHeader.bottomSeparatorColor", new Color(0, 0, 0));
            UIManager.put("Table.sortIconColor", new Color(0, 0, 0));
            UIManager.put("TableHeader.cellMargins", new Insets(5,5,5,5));
            UIManager.put("Table.rowHeight", 25);

            UIManager.put("TabbedPane.tabAreaAlignment", "fill");
            UIManager.put("TabbedPane.tabType", "card");
            UIManager.put("TabbedPane.cardTabArc", 20);

            //UIManager.put("Table.gridColor", new Color(73, 73, 73, 255));

            /*UIManager.put("TextField.border", new FlatLineBorder(
                    new Insets(0,0,0,0),
                    new Color(147, 147, 147),
                    2.0f, 15));*/

            UIManager.put("Component.borderWidth", 1.5f);
            //UIManager.put("Component.borderColor", new Color(197, 197, 197));

            UIManager.put("ToolTip.border", new FlatLineBorder(
                    new Insets(0, 0, 0, 0),
                    new Color(197, 197, 197)));

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    null,
                    "Failed to load FlatLaf theme: " + e.getMessage() +
                            "\n\n If you have any questions, please write\n to this email address: coolfr490@gmail.com",
                    "FATAL THEME ERROR",
                    JOptionPane.ERROR_MESSAGE
            );
            System.err.println("FATAL FlatLaf THEME ERROR: " + e.getMessage());
        }
    }
}
