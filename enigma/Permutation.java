package enigma;
import java.util.HashMap;

import static enigma.EnigmaException.*;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author Teja Kanthamneni
 */
class Permutation {

    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters in the
     *  alphabet that are not included in any cycle map to themselves.
     *  Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        _cycles = new HashMap<>();
        _rcycles = new HashMap<>();

        cycles = cycles.replaceAll("\\s+", "");
        if (!cycles.equals("")
                && (cycles.lastIndexOf(")") != cycles.length() - 1
                || !cycles.contains("("))) {
            throw error("Cycles must be contained within parentheses");
        }
        while (cycles.length() > 0) {
            if (!cycles.startsWith("(")) {
                throw error("Incorrect format of input");
            }
            cycles = cycles.substring(1);
            int endIndex = cycles.indexOf(")(");
            try {
                String currCycle = cycles.substring(0, endIndex);
                addCycle(currCycle);
                cycles = cycles.substring(endIndex + 1);
            } catch (IndexOutOfBoundsException e) {
                String currCycle = cycles.substring(0, cycles.length() - 1);
                addCycle(currCycle);
                cycles = "";
            }
        }
        for (int i = 0; i < _alphabet.size(); i++) {
            char currChar = _alphabet.toChar(i);
            if (!_cycles.containsKey(currChar)) {
                _cycles.put(currChar, currChar);
                _rcycles.put(currChar, currChar);
            }
        }
    }

    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    private void addCycle(String cycle) {

        for (int i = 0; i < cycle.length(); i++) {
            char curr = cycle.charAt(i);
            if (_cycles.containsKey(curr)) {
                throw error("Cannot have duplicate character in cycle");
            }
            if (!_alphabet.contains(curr)) {
                throw error("Input contains character not present in alphabet");
            }
            char next = cycle.charAt((i + 1) % cycle.length());
            char prev = cycle.charAt((i - 1 + cycle.length()) % cycle.length());
            _cycles.put(curr, next);
            _rcycles.put(curr, prev);
        }
    }

    /** Return the value of P modulo the size of this permutation. */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /** Returns the size of the alphabet I permute. */
    int size() {
        return _alphabet.size();
    }

    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {
        char mappedChar = _cycles.get(_alphabet.toChar(wrap(p)));
        return _alphabet.toInt(mappedChar);
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        char mappedChar = _rcycles.get(_alphabet.toChar((wrap(c))));
        return _alphabet.toInt(mappedChar);
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        return _cycles.get(p);
    }

    /** Return the result of applying the inverse of this permutation to C. */
    char invert(char c) {
        return _rcycles.get(c);
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {
        for (int i = 0; i < _alphabet.size(); i++) {
            char currChar = _alphabet.toChar(i);
            if (_cycles.get(currChar).equals(currChar)) {
                return false;
            }
        }
        return true;
    }

    void print() {
        System.out.println(_cycles);
        System.out.println(_rcycles);
    }

    void changeAlphabet(String chars) {
        _alphabet = new Alphabet(chars);
    }

    /** Alphabet of this permutation. */
    private Alphabet _alphabet;

    /** HashMap of the forward conversions of each character. */
    private HashMap<Character, Character> _cycles;

    /** HashMap of the backward conversions of each character. */
    private HashMap<Character, Character> _rcycles;

}
