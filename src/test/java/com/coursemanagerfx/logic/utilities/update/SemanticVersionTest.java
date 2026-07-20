package com.coursemanagerfx.logic.utilities.update;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SemanticVersionTest {

    @Test
    void parsesTagAndPlainVersion() {
        assertEquals(new SemanticVersion(1, 2, 3), SemanticVersion.parse("v1.2.3"));
        assertEquals(new SemanticVersion(1, 2, 3), SemanticVersion.parse("1.2.3"));
    }

    @Test
    void comparesNumericComponents() {
        assertTrue(SemanticVersion.parse("1.10.0")
                .compareTo(SemanticVersion.parse("1.9.9")) > 0);
        assertEquals(0, SemanticVersion.parse("2.0.0")
                .compareTo(SemanticVersion.parse("v2.0.0")));
    }

    @Test
    void rejectsNonStableOrAmbiguousVersions() {
        assertThrows(IllegalArgumentException.class, () -> SemanticVersion.parse("1.2"));
        assertThrows(IllegalArgumentException.class, () -> SemanticVersion.parse("1.2.3-fx"));
        assertThrows(IllegalArgumentException.class, () -> SemanticVersion.parse("1.02.3"));
    }
}
