package com.coursemanagerfx.logic.config_api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AppConfigTest {

    @Test
    void obsoleteFieldsAreNotPartOfCurrentSchema() {
        String json = new Gson().toJson(new AppConfig());

        assertFalse(json.contains("auto_update"));
        assertFalse(json.contains("default_password"));
    }

    @Test
    void unknownFieldsAreIgnoredAndDroppedWhenRewritten() {
        Gson gson = new Gson();
        AppConfig config = gson.fromJson("""
                {
                  "language": "ua",
                  "future_setting": true
                }
                """, AppConfig.class);

        JsonObject rewritten = JsonParser.parseString(gson.toJson(config))
                .getAsJsonObject();

        assertEquals("ua", config.language);
        assertEquals(Set.of(
                "open_course",
                "language",
                "auto_save",
                "auto_save_sec_interval",
                "export_path"), rewritten.keySet());
    }
}
