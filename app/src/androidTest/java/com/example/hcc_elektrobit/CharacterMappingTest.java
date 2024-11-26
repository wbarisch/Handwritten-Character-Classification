package com.example.hcc_elektrobit;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.example.hcc_elektrobit.utils.CharacterMapping;

public class CharacterMappingTest {

    private CharacterMapping characterMapping;

    @Before
    public void setUp() {
        characterMapping = new CharacterMapping();
    }

    @Test
    public void testNumberMapping() {
        for (int i = 0; i < 10; i++) {
            String expected = String.valueOf(i);
            assertEquals("Mapping for number " + i + " failed", expected, characterMapping.getCharacterForId(i));
        }
    }

    @Test
    public void testLetterMapping() {
        char letter = 'a';
        for (int i = 10; i <= 35; i++) {
            String expected = String.valueOf(letter);
            assertEquals("Mapping for letter ID " + i + " failed", expected, characterMapping.getCharacterForId(i));
            letter++;
        }
    }

    @Test
    public void testInvalidIdMapping() {
        //outside the range
        assertEquals("Invalid ID should return an empty string", "", characterMapping.getCharacterForId(36));
    }

    @Test
    public void testPadding() {
        assertEquals("Padding for ID 5 failed", "005", characterMapping.getPaddedId(5));
        assertEquals("Padding for ID 50 failed", "050", characterMapping.getPaddedId(50));
        assertEquals("Padding for ID 500 failed", "500", characterMapping.getPaddedId(500));
    }
}
