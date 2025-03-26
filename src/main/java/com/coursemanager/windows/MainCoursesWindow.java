/*
 * Created by JFormDesigner on Thu Mar 20 11:16:35 EET 2025
 */

package com.coursemanager.windows;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;

import com.coursemanager.CM_HELPER;
import com.coursemanager.other.CourseFileSaver;
import com.coursemanager.other.Group;
import com.coursemanager.panels.*;
import com.formdev.flatlaf.extras.*;
import raven.toast.Notifications;

/**
 * @author Nazar
 */
public class MainCoursesWindow extends JFrame {
    public MainCoursesWindow() {
        initComponents();
        Notifications.getInstance().setJFrame(this);
    }

    public void populateTabs(List<Group> groups) {
        tabbedPane.removeAll();
        for (int i = 0; i < groups.size(); i++) {
            MainGroupPanel groupPanel = new MainGroupPanel();
            groupPanel.setGroupData(i + 1, groups.get(i));
            tabbedPane.addTab("Group " + (i + 1), groupPanel);
        }
        tabbedPane.revalidate();
        tabbedPane.repaint();
    }

    private void miSave(ActionEvent e) {
        String filePath = CM_HELPER.getFileCoursePath();
        List<Group> groups = CM_HELPER.getCourseGroupsList();

        try {
            CourseFileSaver.save(filePath, groups);     // СОХРАНЯЕМ

            Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER, "Successfully saved");

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

    private void chbMiSStdsByAlphabetStateChanged(ChangeEvent e) {
        // TODO add your code here
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        menuBar = new JMenuBar();
        menuFile = new JMenu();
        miOpen = new JMenuItem();
        miSave = new JMenuItem();
        miExport = new JMenuItem();
        menuSettings = new JMenu();
        menuSort = new JMenu();
        lblCaption = new JLabel();
        radBtnSbyCreatDate = new JRadioButtonMenuItem();
        radBtnSbyAlphabet = new JRadioButtonMenuItem();
        radBtnSbyMarks = new JRadioButtonMenuItem();
        radBtnSbyType = new JRadioButtonMenuItem();
        chbMiSStdsByAlphabet = new JCheckBoxMenuItem();
        chbMiAutoSave = new JCheckBoxMenuItem();
        menuAbout = new JMenu();
        miInfo = new JMenuItem();
        lblSaveInfo = new JLabel();
        tabbedPane = new JTabbedPane();

        //======== this ========
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("CourseName - Something");
        setMinimumSize(new Dimension(400, 250));
        setPreferredSize(null);
        var contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout(5, 5));

        //======== menuBar ========
        {
            menuBar.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

            //======== menuFile ========
            {
                menuFile.setText("File");

                //---- miOpen ----
                miOpen.setText("Open");
                miOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
                miOpen.setIcon(new FlatSVGIcon("Icons/folder_icon_16x16.svg"));
                menuFile.add(miOpen);

                //---- miSave ----
                miSave.setText("Save");
                miSave.setToolTipText("Saves all changes");
                miSave.setIcon(new FlatSVGIcon("Icons/save_icon_16x16.svg"));
                miSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
                miSave.addActionListener(e -> miSave(e));
                menuFile.add(miSave);
                menuFile.addSeparator();

                //---- miExport ----
                miExport.setText("Export");
                miExport.setIcon(new FlatSVGIcon("Icons/export_icon_16x16.svg"));
                miExport.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
                menuFile.add(miExport);
            }
            menuBar.add(menuFile);

            //======== menuSettings ========
            {
                menuSettings.setText("Settings");

                //======== menuSort ========
                {
                    menuSort.setText("Sort");
                    menuSort.addSeparator();

                    //---- lblCaption ----
                    lblCaption.setText("Sort Events");
                    lblCaption.setHorizontalAlignment(SwingConstants.CENTER);
                    lblCaption.setAlignmentX(0.5F);
                    lblCaption.setEnabled(false);
                    menuSort.add(lblCaption);

                    //---- radBtnSbyCreatDate ----
                    radBtnSbyCreatDate.setText("By creation date");
                    menuSort.add(radBtnSbyCreatDate);

                    //---- radBtnSbyAlphabet ----
                    radBtnSbyAlphabet.setText("By alphabet");
                    menuSort.add(radBtnSbyAlphabet);

                    //---- radBtnSbyMarks ----
                    radBtnSbyMarks.setText("By marks");
                    menuSort.add(radBtnSbyMarks);

                    //---- radBtnSbyType ----
                    radBtnSbyType.setText("By Type");
                    radBtnSbyType.setSelected(true);
                    menuSort.add(radBtnSbyType);
                }
                menuSettings.add(menuSort);

                //---- chbMiSStdsByAlphabet ----
                chbMiSStdsByAlphabet.setText("Sort students alphabetically");
                chbMiSStdsByAlphabet.addChangeListener(e -> chbMiSStdsByAlphabetStateChanged(e));
                menuSettings.add(chbMiSStdsByAlphabet);

                //---- chbMiAutoSave ----
                chbMiAutoSave.setText("Auto save");
                menuSettings.add(chbMiAutoSave);
            }
            menuBar.add(menuSettings);

            //======== menuAbout ========
            {
                menuAbout.setText("About");

                //---- miInfo ----
                miInfo.setText("Info");
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

        //---- sortBtnsGroup ----
        var sortBtnsGroup = new ButtonGroup();
        sortBtnsGroup.add(radBtnSbyCreatDate);
        sortBtnsGroup.add(radBtnSbyAlphabet);
        sortBtnsGroup.add(radBtnSbyMarks);
        sortBtnsGroup.add(radBtnSbyType);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    private JMenuBar menuBar;
    private JMenu menuFile;
    private JMenuItem miOpen;
    private JMenuItem miSave;
    private JMenuItem miExport;
    private JMenu menuSettings;
    private JMenu menuSort;
    private JLabel lblCaption;
    private JRadioButtonMenuItem radBtnSbyCreatDate;
    private JRadioButtonMenuItem radBtnSbyAlphabet;
    private JRadioButtonMenuItem radBtnSbyMarks;
    private JRadioButtonMenuItem radBtnSbyType;
    private JCheckBoxMenuItem chbMiSStdsByAlphabet;
    private JCheckBoxMenuItem chbMiAutoSave;
    private JMenu menuAbout;
    private JMenuItem miInfo;
    private JLabel lblSaveInfo;
    private JTabbedPane tabbedPane;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
