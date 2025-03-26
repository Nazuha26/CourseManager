package com.coursemanager.other;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GlobalCompAnims {

    /**
     * Анимированное изменение видимости компонента.
     * @param comp      Компонент (должен быть JComponent с переопределённым paintComponent для учета альфа-прозрачности).
     * @param speedAnim Скорость анимации (например, 0.05 – медленно, 0.1 – быстрее).
     * @param visible   true для появления (fade in), false для исчезания (fade out).
     */
    public static void setAnimVisible(final JComponent comp, final float speedAnim, final boolean visible) {
        // Устанавливаем, что компонент не закрашивается полностью фоном,
        // чтобы можно было видеть эффект прозрачности.
        comp.setOpaque(false);
        // Начальное значение альфа:
        // Если хотим появление, начинаем с 0 (полностью прозрачно),
        // если исчезание – с 1 (полностью непрозрачно).
        final float[] alpha = { visible ? 0f : 1f };

        // Для fade in компонент нужно сделать видимым сразу,
        // чтобы анимация была заметна.
        if (visible) {
            comp.setVisible(true);
        }
        // Сохраняем начальное значение альфа как клиентское свойство.
        comp.putClientProperty("globalCompAlpha", alpha[0]);

        // Таймер, который будет обновлять альфа значение каждые 40 мс (~25 кадров/сек).
        Timer timer = new Timer(40, null);
        timer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (visible) {
                    alpha[0] += speedAnim;
                    if (alpha[0] >= 1f) {
                        alpha[0] = 1f;
                        timer.stop();
                    }
                } else {
                    alpha[0] -= speedAnim;
                    if (alpha[0] <= 0f) {
                        alpha[0] = 0f;
                        timer.stop();
                        comp.setVisible(false);
                    }
                }
                comp.putClientProperty("globalCompAlpha", alpha[0]);
                comp.repaint();
            }
        });
        timer.start();
    }
}
