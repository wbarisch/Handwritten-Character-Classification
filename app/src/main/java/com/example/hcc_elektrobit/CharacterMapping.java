package com.example.hcc_elektrobit;

import java.util.HashMap;
import java.util.Map;

public class CharacterMapping {
    private final Map<Integer, String> idToCharacterMap;

    public CharacterMapping() {
        idToCharacterMap = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            idToCharacterMap.put(i, String.valueOf(i)); // For digits 0-9
        }

        char letter = 'a';
        for (int i = 10; i <= 35; i++) {
            idToCharacterMap.put(i, String.valueOf(letter));
            letter++;
        }
    }

    public String getCharacterForId(int id) {
        return idToCharacterMap.getOrDefault(id, "");
    }
}