package com.coursemanagerfx.logic.utilities.security;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SeedPhraseGeneratorTest {

    @Test
    void loadsCompleteEffDictionaryAndGeneratesFiveWords() {
        assertEquals(7_776, SeedPhraseGenerator.dictionarySize());

        char[] phrase = SeedPhraseGenerator.generate();
        try {
            assertTrue(SeedPhraseGenerator.isValid(phrase));
            assertEquals(
                    SeedPhraseGenerator.WORD_COUNT,
                    new String(phrase).split(" ").length);
        } finally {
            Arrays.fill(phrase, '\0');
        }
    }

    @Test
    void normalizesCaseAndWhitespace() {
        char[] raw = "  ABACUS\tAbdomen  abdominal\nABIDE abiding  ".toCharArray();
        char[] normalized = SeedPhraseGenerator.normalize(raw);
        try {
            assertTrue(SeedPhraseGenerator.isValid(normalized));
            assertEquals(
                    "abacus abdomen abdominal abide abiding",
                    new String(normalized));
        } finally {
            Arrays.fill(raw, '\0');
            Arrays.fill(normalized, '\0');
        }
    }
}
