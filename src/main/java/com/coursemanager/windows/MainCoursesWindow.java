/*
 * Created by JFormDesigner on Thu Mar 20 11:16:35 EET 2025
 */

package com.coursemanager.windows;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.Timer;

import com.coursemanager.CM_HELPER;
import com.coursemanager.other.CmanParser;
import com.coursemanager.other.CourseFileSaver;
import com.coursemanager.other.Group;
import com.coursemanager.panels.*;
import com.formdev.flatlaf.extras.*;
import jnafilechooser.api.JnaFileChooser;
import raven.toast.Notifications;

/**
 * @author Nazar
 */
public class MainCoursesWindow extends JFrame {
    public MainCoursesWindow() {
        initComponents();
        initRadioButtons();
        Notifications.getInstance().setJFrame(this);
    }

    private void initRadioButtons() {
        Locale currentLocale = Locale.getDefault();
        String lang = currentLocale.getLanguage();
        String country = currentLocale.getCountry();

        if (lang.equals("uk") && country.equals("UA")) {
            rdBtnMiUkrLang.setSelected(true);
        } else {
            rdBtnMiEngLang.setSelected(true); // по умолчанию English
        }
    }

    public void populateTabs(List<Group> groups) {
        tabbedPane.removeAll();
        for (int i = 0; i < groups.size(); i++) {
            MainGroupPanel groupPanel = new MainGroupPanel();
            groupPanel.setGroupData(i + 1, groups.get(i));
            tabbedPane.addTab(MessageFormat.format(CM_HELPER.getBundle().getString("MainCoursesWindow.tabTitle.text"), (i + 1)), groupPanel);
        }
        tabbedPane.revalidate();
        tabbedPane.repaint();
    }

    private void miSave(ActionEvent e) {
        String filePath = CM_HELPER.getFileCoursePath();
        List<Group> groups = CM_HELPER.getCourseGroupsList();

        try {
            CourseFileSaver.save(filePath, groups);     // СОХРАНЯЕМ

            Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER,
                    CM_HELPER.getBundle().getString("MainCoursesWindow.saveAction.text"));

            /*Notification saveNotification = new Notification(this, Notification.Type.SUCCESS,
                    Notification.Location.TOP_CENTER, "Successfully saved");

            saveNotification.showNotification();*/

            /*this.getLblSaveInfo().setBackground(new Color(CM_CONSTANTS.BG_SUCCESS));
            this.getLblSaveInfo().setForeground(new Color(CM_CONSTANTS.FG_SUCCESS));
            this.getLblSaveInfo().setText("SUCCESSFULLY SAVED");

            this.getLblSaveInfo().setVisible(true);
            Timer timer = new Timer(5000, evt -> {
                this.getLblSaveInfo().setVisible(false);
            });
            timer.setRepeats(false); // срабатывает только один раз
            timer.start();*/
        } catch (IOException ex) {
            this.getLblSaveInfo().setBackground(new Color(CM_HELPER.BG_ERROR_COLOR));
            this.getLblSaveInfo().setForeground(new Color(CM_HELPER.FG_ERROR_COLOR));
            this.getLblSaveInfo().setText("SAVE ERROR");

            this.getLblSaveInfo().setVisible(true);
            Timer timer = new Timer(5000, evt -> {
                this.getLblSaveInfo().setVisible(false);
            });
            timer.setRepeats(false); // срабатывает только один раз
            JOptionPane.showMessageDialog(this, "Ошибка при сохранении: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public JLabel getLblSaveInfo() {
        return lblSaveInfo;
    }

    private void rdBtnMiUkrLang(ActionEvent e) {
        changeLocale("uk", "UA");
    }

    private void rdBtnMiEngLang(ActionEvent e) {
        changeLocale("en", "US");
    }

    private void changeLocale(String lang, String country) {
        try {
            // Запись в config.json
            org.json.JSONObject config = new org.json.JSONObject();
            config.put("language", lang);
            config.put("country", country);

            try (FileWriter writer = new FileWriter(CM_HELPER.CONFIG_FILE)) {
                writer.write(config.toString(2)); // красиво с отступами
            }
            String languageName = switch (lang) {
                case "uk" -> "Ukrainian";
                case "en" -> "Англійську";
                default -> "Unknown";
            };

            // Сообщение пользователю
            JOptionPane.showMessageDialog(this,
                    MessageFormat.format(CM_HELPER.getBundle().getString("MainCoursesWindow.changeLang.text"), languageName),
                    CM_HELPER.getBundle().getString("MainCoursesWindow.changeLang.title"),
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error changing language settings: " + ex.getMessage(),
                    "ERROR",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void miInfo(ActionEvent e) {
        new InfoWindow().setVisible(true);
    }

    private void miOpen(ActionEvent e) {
        JnaFileChooser chooser = new JnaFileChooser();

        chooser.setMode(JnaFileChooser.Mode.Files);
        chooser.setCurrentDirectory(CM_HELPER.COURSES_DIR.getAbsolutePath()); // Стартовая директория
        chooser.addFilter("Course Manager files (*.cman)", "cman"); // Фильтр по расширению

        boolean action = chooser.showOpenDialog(this);
        if (action) {
            File file = chooser.getSelectedFile();

            try {
                // Сохраняем путь и имя курса
                CM_HELPER.setFileCoursePath(file.getAbsolutePath());
                CM_HELPER.setCourseName(file.getName().replace(".cman", ""));

                // Сохраняем в файл для автозагрузки при следующем запуске
                try (FileWriter writer = new FileWriter(CM_HELPER.FIRST_RUN_FILE)) {
                    writer.write(CM_HELPER.getCourseName().trim());
                }

                // Парсим файл и обновляем UI
                List<Group> groups = CmanParser.parseFile(file.getAbsolutePath());
                CM_HELPER.setCourseGroupsList(groups);
                populateTabs(groups);

                this.setTitle("CourseManager - " + CM_HELPER.getCourseName());

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Ошибка при открытии файла: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        ResourceBundle bundle = ResourceBundle.getBundle("strings");
        menuBar = new JMenuBar();
        menuFile = new JMenu();
        miOpen = new JMenuItem();
        miSave = new JMenuItem();
        miExport = new JMenuItem();
        menuSettings = new JMenu();
        menuLangs = new JMenu();
        rdBtnMiEngLang = new JRadioButtonMenuItem();
        rdBtnMiUkrLang = new JRadioButtonMenuItem();
        chbMiAutoSave = new JCheckBoxMenuItem();
        menuAbout = new JMenu();
        miInfo = new JMenuItem();
        lblSaveInfo = new JLabel();
        tabbedPane = new JTabbedPane();

        //======== this ========
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle(bundle.getString("MainCoursesWindow.this.title"));
        setMinimumSize(new Dimension(400, 250));
        setPreferredSize(null);
        setIconImage(new ImageIcon(getClass().getResource("/Icons/AppIcon.png")).getImage());
        var contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout(5, 5));

        //======== menuBar ========
        {
            menuBar.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

            //======== menuFile ========
            {
                menuFile.setText(bundle.getString("MainCoursesWindow.menuFile.text"));

                //---- miOpen ----
                miOpen.setText(bundle.getString("MainCoursesWindow.miOpen.text"));
                miOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
                miOpen.setIcon(new FlatSVGIcon("Icons/folder_icon_16x16.svg"));
                miOpen.addActionListener(e -> miOpen(e));
                menuFile.add(miOpen);

                //---- miSave ----
                miSave.setText(bundle.getString("MainCoursesWindow.miSave.text"));
                miSave.setToolTipText(bundle.getString("MainCoursesWindow.miSave.toolTipText"));
                miSave.setIcon(new FlatSVGIcon("Icons/save_icon_16x16.svg"));
                miSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
                miSave.addActionListener(e -> miSave(e));
                menuFile.add(miSave);
                menuFile.addSeparator();

                //---- miExport ----
                miExport.setText(bundle.getString("MainCoursesWindow.miExport.text"));
                miExport.setIcon(new FlatSVGIcon("Icons/export_icon_16x16.svg"));
                miExport.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
                menuFile.add(miExport);
            }
            menuBar.add(menuFile);

            //======== menuSettings ========
            {
                menuSettings.setText(bundle.getString("MainCoursesWindow.menuSettings.text"));

                //======== menuLangs ========
                {
                    menuLangs.setText(bundle.getString("MainCoursesWindow.menuLangs.text"));

                    //---- rdBtnMiEngLang ----
                    rdBtnMiEngLang.setText(bundle.getString("MainCoursesWindow.rdBtnMiEngLang.text"));
                    rdBtnMiEngLang.setSelected(true);
                    rdBtnMiEngLang.addActionListener(e -> rdBtnMiEngLang(e));
                    menuLangs.add(rdBtnMiEngLang);

                    //---- rdBtnMiUkrLang ----
                    rdBtnMiUkrLang.setText(bundle.getString("MainCoursesWindow.rdBtnMiUkrLang.text"));
                    rdBtnMiUkrLang.addActionListener(e -> rdBtnMiUkrLang(e));
                    menuLangs.add(rdBtnMiUkrLang);
                }
                menuSettings.add(menuLangs);

                //---- chbMiAutoSave ----
                chbMiAutoSave.setText(bundle.getString("MainCoursesWindow.chbMiAutoSave.text"));
                menuSettings.add(chbMiAutoSave);
            }
            menuBar.add(menuSettings);

            //======== menuAbout ========
            {
                menuAbout.setText(bundle.getString("MainCoursesWindow.menuAbout.text"));

                //---- miInfo ----
                miInfo.setText(bundle.getString("MainCoursesWindow.miInfo.text"));
                miInfo.addActionListener(e -> miInfo(e));
                menuAbout.add(miInfo);
            }
            menuBar.add(menuAbout);
        }
        setJMenuBar(menuBar);

        //---- lblSaveInfo ----
        lblSaveInfo.setText("SUCCESSFULLY SAVED");
        lblSaveInfo.setFont(lblSaveInfo.getFont().deriveFont(lblSaveInfo.getFont().getStyle() | Font.BOLD, lblSaveInfo.getFont().getSize() + 3f));
        lblSaveInfo.setForeground(new Color(0x1ab403));
        lblSaveInfo.setBackground(new Color(0xb4e2bd));
        lblSaveInfo.setHorizontalAlignment(SwingConstants.CENTER);
        lblSaveInfo.setOpaque(true);
        lblSaveInfo.setVisible(false);
        contentPane.add(lblSaveInfo, BorderLayout.NORTH);

        //======== tabbedPane ========
        {
            tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
            tabbedPane.setFont(tabbedPane.getFont().deriveFont(tabbedPane.getFont().getSize() + 2f));
        }
        contentPane.add(tabbedPane, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(getOwner());

        //---- LangsBtnGroup ----
        var LangsBtnGroup = new ButtonGroup();
        LangsBtnGroup.add(rdBtnMiEngLang);
        LangsBtnGroup.add(rdBtnMiUkrLang);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    private JMenuBar menuBar;
    private JMenu menuFile;
    private JMenuItem miOpen;
    private JMenuItem miSave;
    private JMenuItem miExport;
    private JMenu menuSettings;
    private JMenu menuLangs;
    private JRadioButtonMenuItem rdBtnMiEngLang;
    private JRadioButtonMenuItem rdBtnMiUkrLang;
    private JCheckBoxMenuItem chbMiAutoSave;
    private JMenu menuAbout;
    private JMenuItem miInfo;
    private JLabel lblSaveInfo;
    private JTabbedPane tabbedPane;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
