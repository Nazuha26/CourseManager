package com.coursemanager.ui;

import java.awt.*;
import java.beans.BeanProperty;
import javax.swing.border.Border;

/**
 * МОЙ КАСТОМНЫЙ БОРДЕР ДЛЯ NamePart
 */
public class RoundedBorder implements Border {
    private int radius;
    private Color borderColor;

    public RoundedBorder() {
        this(999, Color.BLACK); // Значения по умолчанию
    }

    public RoundedBorder(int radius, Color borderColor) {
        this.radius = radius;
        this.borderColor = borderColor;
    }

    @Override
    public void paintBorder(Component c, Graphics g,
                            int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // Вычисляем реальный «размер скругления»
        // Берём вдвое меньшее из (width, height),
        // чтобы при больших значениях (например, 999) получался овал
        int diameter = Math.min(width, height);
        int arcSize = Math.min(radius, diameter);

        g2.setColor(borderColor);
        // Используем arcSize и для ширины, и для высоты скругления
        g2.drawRoundRect(x, y, width - 1, height - 1, arcSize, arcSize);

        g2.dispose();
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(4, 4, 4, 4);
    }

    @Override
    public boolean isBorderOpaque() {
        return false;
    }

    @BeanProperty
    public int getRadius() {
        return radius;
    }

    @BeanProperty
    public void setRadius(int radius) {
        this.radius = radius;
    }

    @BeanProperty
    public Color getBorderColor() {
        return borderColor;
    }

    @BeanProperty
    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }
}
