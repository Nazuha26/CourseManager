package com.coursemanager.ui;

import java.awt.*;
import java.beans.BeanProperty;
import java.io.Serializable;
import javax.swing.*;

/**
 * Кастомная панель с закруглёнными углами
 */
public class RoundedPanel extends JPanel implements Serializable {

    private int radius;
    private Color borderColor;
    private Color panelColor;

    // Пустой конструктор (JavaBean-требование)
    public RoundedPanel() {
        this(999, Color.BLACK, Color.LIGHT_GRAY, new BorderLayout());
    }

    public RoundedPanel(int radius, Color borderColor, Color panelColor, LayoutManager layout) {
        this.radius = radius;
        this.borderColor = borderColor;
        this.panelColor = panelColor;
        // Чтобы панель могла отрисовывать "прозрачные" углы
        setOpaque(false);
        setLayout(layout);
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Сначала даём JPanel нарисовать фоновую часть
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Делаем радиус не больше, чем половина меньшей стороны
        int diameter = Math.min(width, height);
        int arcSize = Math.min(radius, diameter);

        // Рисуем закруглённый фон
        g2.setColor(panelColor);
        g2.fillRoundRect(0, 0, width - 1, height - 1, arcSize, arcSize);

        // Рисуем рамку (обводку)
        g2.setColor(borderColor);
        g2.drawRoundRect(0, 0, width - 1, height - 1, arcSize, arcSize);

        g2.dispose();
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int diameter = Math.min(width, height);
        int arcSize = Math.min(radius, diameter);

        g2.setColor(borderColor);
        g2.drawRoundRect(0, 0, width - 1, height - 1, arcSize, arcSize);

        g2.dispose();
    }

    @BeanProperty
    @Override
    public LayoutManager getLayout() {
        return super.getLayout();
    }

    @BeanProperty
    @Override
    public void setLayout(LayoutManager mgr) {
        super.setLayout(mgr);
    }


    @BeanProperty
    public int getRadius() {
        return radius;
    }

    @BeanProperty
    public void setRadius(int radius) {
        this.radius = radius;
        repaint(); // Перерисовать панель после изменения
    }

    @BeanProperty
    public Color getBorderColor() {
        return borderColor;
    }

    @BeanProperty
    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        repaint();
    }

    @BeanProperty
    public Color getPanelColor() {
        return panelColor;
    }

    @BeanProperty
    public void setPanelColor(Color panelColor) {
        this.panelColor = panelColor;
        repaint();
    }
}
