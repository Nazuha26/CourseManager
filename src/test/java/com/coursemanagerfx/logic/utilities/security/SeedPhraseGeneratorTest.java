package com.coursemanagerfx.logic.utilities.security;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SeedPhraseGeneratorTest {

    @Test
    void loadsBasicEnglishDictionaryAndGeneratesFiveWords() {
        assertEquals(3_000, SeedPhraseGenerator.dictionarySize());

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
        char[] raw = "  APPLE\tBeach  chair\nDANCE earth  ".toCharArray();
        char[] normalized = SeedPhraseGenerator.normalize(raw);
        try {
            assertTrue(SeedPhraseGenerator.isValid(normalized));
            assertEquals(
                    "apple beach chair dance earth",
                    new String(normalized));
        } finally {
            Arrays.fill(raw, '\0');
            Arrays.fill(normalized, '\0');
        }
    }

    @Test
    void acceptsTheStructureOfPhrasesGeneratedByAnOlderDictionary() {
        char[] oldPhrase = "abacus abdomen abdominal abide abiding".toCharArray();
        try {
            assertTrue(SeedPhraseGenerator.hasValidStructure(oldPhrase));
        } finally {
            Arrays.fill(oldPhrase, '\0');
        }
    }
}
