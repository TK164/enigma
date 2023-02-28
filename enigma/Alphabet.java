package enigma;
import java.util.HashMap;

import static enigma.EnigmaException.*;

/** An alphabet of encodable characters.  Provides a mapping from characters
 *  to and from indices into the alphabet.
 *  @author Teja Kanthamneni
 */
class Alphabet {

    /** A new alphabet containing CHARS. The K-th character has index
     *  K (numbering from 0). No character may be duplicated. */
    Alphabet(String chars) {
        if (chars.length() == 0) {
            throw error("Cannot have an empty alphabet");
        }
        String wstest = chars.replaceAll("\\s+", "");
        if (wstest.length() != chars.length()) {
            throw error("Alphabet cannot include whitespace");
        }
        _chars = new HashMap<>();
        _indicies = new HashMap<>();
        int i = 0;
        while (i < chars.length()) {
            char currChar = chars.charAt(i);
            if (_chars.containsValue(currChar)) {
                throw error("May not have duplicate characters in alphabet");
            } else {
                _chars.put(i, currChar);
                _indicies.put(currChar, i);
                i++;
            }
        }
    }

    /** A default alphabet of all upper-case characters. */
    Alphabet() {
        this("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    /** Returns the size of the alphabet. */
    int size() {
        return _chars.size();
    }

    /** Returns true if CH is in this alphabet. */
    boolean contains(char ch) {
        return _chars.containsValue(ch);
    }

    /** Returns character number INDEX in the alphabet, where
     *  0 <= INDEX < size(). */
    char toChar(int index) {
        if (index < 0 || index >= size()) {
            throw error("Index out of range");
        }
        return _chars.get(index);
    }

    /** Returns the index of character CH which must be in
     *  the alphabet. This is the inverse of toChar(). */
    int toInt(char ch) {
        if (!contains(ch)) {
            throw error("Character not in alphabet");
        }
        return _indicies.get(ch);

    }
    /** HashMap with key = index in alphabet and value = character. */
    private HashMap<Integer, Character> _chars;

    /** HashMap with key = character and value = index in alphabet. */
    private HashMap<Character, Integer> _indicies;


}
