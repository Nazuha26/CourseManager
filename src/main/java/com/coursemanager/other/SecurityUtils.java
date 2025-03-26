package com.coursemanager.other;

import java.security.SecureRandom;

public class SecurityUtils {
    // Метод для генерации случайного ключа из 16 символов
    public static String generateRandomKey(int length) {
        String fixedPart = "R4tC0d3";
        int fixedLength = fixedPart.length();

        if (length < fixedLength) {
            throw new IllegalArgumentException("Длина ключа должна быть не меньше 8 символов");
        }

        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();

        // Генерируем оставшуюся часть случайного ключа
        int remainingLength = length - fixedLength;
        for (int i = 0; i < remainingLength; i++) {
            int index = random.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }

        // Случайная позиция для вставки фиксированной части
        int insertPosition = random.nextInt(remainingLength + 1);
        sb.insert(insertPosition, fixedPart);

        return sb.toString();
    }
}
