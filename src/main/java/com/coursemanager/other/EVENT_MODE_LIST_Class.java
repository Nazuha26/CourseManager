package com.coursemanager.other;

import java.util.LinkedHashMap;
import java.util.Map;

public class EVENT_MODE_LIST_Class {

    // Класс для хранения данных о режиме события
    public static class EventMode {
        private String name;       // название события
        private int rating;        // оценка
        private String expiredTime; // время истечения

        public EventMode(String name, int rating, String expiredTime) {
            this.name = name;
            this.rating = rating;
            this.expiredTime = expiredTime;
        }

        public String getName() {
            return name;
        }

        public int getRating() {
            return rating;
        }

        public String getExpiredTime() {
            return expiredTime;
        }

        @Override
        public String toString() {
            return name;    // Возвращается только название, чтобы корректно отображалось в comboModeBox
        }
    }

    // Используем Map для хранения списка режимов событий.
    private static final Map<String, EventMode> eventModeList = new LinkedHashMap<>();

    // Инициализируем список режимов (можно добавлять новые режимы аналогичным образом)
    static {
        eventModeList.put("first_mode", new EventMode("Первый мод ивента Expired time 1 month", 5, "1 %m"));
        eventModeList.put("second_mode", new EventMode("Второй мод ивента Expired time 3 weeks", 2, "3 %w"));
        eventModeList.put("third_mode", new EventMode("Третий мод ивента Expired time 2 days", 3, "2 %d"));
        eventModeList.put("other", new EventMode("Інший івент", 0, "1 %d"));
        // eventModeList.put("some_key", new EventMode("Название события", оценка, "Время истечения"));
    }

    // Метод для получения списка режимов событий
    public static Map<String, EventMode> getEventModeList() {
        return eventModeList;
    }

    // Метод для добавления нового режима события в список
    public static void addEventMode(String key, EventMode mode) {
        eventModeList.put(key, mode);
    }
}
