package com.coursemanagerfx.logic.utilities.security;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Generates five-word passphrases from the local EFF Diceware word list. */
public final class SeedPhraseGenerator {

    public static final int WORD_COUNT = 5;
    private static final int EXPECTED_WORD_COUNT = 7_776;
    private static final String WORD_LIST_RESOURCE =
            "/com/coursemanagerfx/security/eff-large-wordlist.txt";

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final List<String> WORDS = loadWords();
    private static final Set<String> WORD_SET = Set.copyOf(WORDS);

    private SeedPhraseGenerator() {
    }

    public static char[] generate() {
        String[] selected = new String[WORD_COUNT];
        int length = WORD_COUNT - 1;
        for (int i = 0; i < WORD_COUNT; i++) {
            selected[i] = WORDS.get(SECURE_RANDOM.nextInt(WORDS.size()));
            length += selected[i].length();
        }

        char[] result = new char[length];
        int offset = 0;
        for (int i = 0; i < selected.length; i++) {
            if (i > 0) result[offset++] = ' ';
            selected[i].getChars(0, selected[i].length(), result, offset);
            offset += selected[i].length();
        }
        return result;
    }

    public static char[] normalize(char[] phrase) {
        if (phrase == null || phrase.length == 0) return new char[0];

        char[] normalized = new char[phrase.length];
        int length = 0;
        boolean pendingSpace = false;

        for (char value : phrase) {
            if (Character.isWhitespace(value)) {
                pendingSpace = length > 0;
                continue;
            }
            if (pendingSpace) normalized[length++] = ' ';
            normalized[length++] = Character.toLowerCase(value);
            pendingSpace = false;
        }
        return Arrays.copyOf(normalized, length);
    }

    public static boolean isValid(char[] phrase) {
        if (phrase == null || phrase.length == 0) return false;
        String[] words = new String(phrase).split(" ", -1);
        if (words.length != WORD_COUNT) return false;
        return Arrays.stream(words).allMatch(WORD_SET::contains);
    }

    static int dictionarySize() {
        return WORDS.size();
    }

    private static List<String> loadWords() {
        InputStream stream = SeedPhraseGenerator.class.getResourceAsStream(
                WORD_LIST_RESOURCE);
        if (stream == null) {
            throw new ExceptionInInitializerError(
                    "Missing EFF Diceware word list: " + WORD_LIST_RESOURCE);
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            List<String> words = reader.lines()
                    .map(String::trim)
                    .filter(word -> !word.isEmpty())
                    .toList();
            Set<String> unique = new HashSet<>(words);
            if (words.size() != EXPECTED_WORD_COUNT
                    || unique.size() != EXPECTED_WORD_COUNT) {
                throw new IOException("Invalid EFF Diceware word list");
            }
            return List.copyOf(words);
        } catch (IOException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }
}
