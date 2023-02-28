package enigma;

import java.util.HashMap;
import java.util.Collection;

import static enigma.EnigmaException.*;

/** Class that represents a complete enigma machine.
 *  @author Teja Kanthamneni
 */
class Machine {

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        if (numRotors <= 1) {
            throw error("Machine must have 2 or more rotors");
        }
        if (pawls <= 0 || pawls >= numRotors) {
            throw error("Machine must have a number of pawls greater than 0 "
                    + "and less than the number of rotors.");
        }
        _alphabet = alpha;
        _numPawls = pawls;
        _numRotors = numRotors;
        _plugboard = new Permutation("", _alphabet);
        _allRotors = new HashMap<String, Rotor>();
        for (Rotor r : allRotors) {
            _allRotors.put(r.name(), r);
        }
        _usedRotors = new Rotor[numRotors];
    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _numRotors;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _numPawls;
    }

    /** Return Rotor #K, where Rotor #0 is the reflector, and Rotor
     *  #(numRotors()-1) is the fast Rotor.  Modifying this Rotor has
     *  undefined results. */
    Rotor getRotor(int k) {
        return _usedRotors[k];
    }

    Alphabet alphabet() {
        return _alphabet;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        for (int i = 0; i < rotors.length; i++) {
            if (!_allRotors.containsKey(rotors[i])) {
                throw error("Rotor '" + rotors[i] + "' does not exist");
            }
            Rotor currRotor = _allRotors.get(rotors[i]);
            if (i == 0 && !currRotor.reflecting()) {
                throw error("ROTORS[0] must be a reflector");
            } else if (i != 0 && currRotor.reflecting()) {
                throw error("The reflector must be in position 0");
            } else if (i <= _numRotors - _numPawls - 1 && currRotor.rotates()) {
                throw error("Rotor at position " + i + " must be fixed");
            } else if (i > _numRotors - _numPawls - 1 && !currRotor.rotates()) {
                throw error("Rotor at position " + i + " must rotate");
            }
            _usedRotors[i] = currRotor;
        }
    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        if (setting.length() != _usedRotors.length - 1) {
            throw error("Setting line must have "
                    + (_usedRotors.length - 1) + "characters");
        }
        for (int i = 0; i < setting.length(); i++) {
            char currChar = setting.charAt(i);
            if (!alphabet().contains(currChar)) {
                throw error("" + currChar + " is not contained in alphabet");
            }
            Rotor currRotor = _usedRotors[i + 1];
            currRotor.set(currChar);
        }
    }

    /** Return the current plugboard's permutation. */
    Permutation plugboard() {
        return _plugboard;
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        for (int i = 0; i < plugboard.alphabet().size(); i++) {
            if (plugboard.permute(i) != plugboard.invert(i)) {
                throw error("Invalid permutation for plugboard.");
            }
        }
        _plugboard = plugboard;
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {
        advanceRotors();
        if (Main.verbose()) {
            System.err.printf("[");
            for (int r = 1; r < numRotors(); r += 1) {
                System.err.printf("%c",
                        alphabet().toChar(getRotor(r).setting()));
            }
            System.err.printf("] %c -> ", alphabet().toChar(c));
        }
        c = plugboard().permute(c);
        if (Main.verbose()) {
            System.err.printf("%c -> ", alphabet().toChar(c));
        }
        c = applyRotors(c);
        c = plugboard().permute(c);
        if (Main.verbose()) {
            System.err.printf("%c%n", alphabet().toChar(c));
        }
        return c;
    }

    /** Advance all rotors to their next position. */
    private void advanceRotors() {
        boolean[] willAdvance = new boolean[_numRotors];
        for (int i = 1; i < willAdvance.length - 1; i++) {
            if (_usedRotors[i + 1].atNotch() && _usedRotors[i].rotates()) {
                willAdvance[i] = true;
            } else if (_usedRotors[i].atNotch()
                    && _usedRotors[i - 1].rotates()) {
                willAdvance[i] = true;
            }
        }
        willAdvance[willAdvance.length - 1] = true;

        for (int i = 0; i < willAdvance.length; i++) {
            if (willAdvance[i]) {
                _usedRotors[i].advance();
            }
        }
    }

    /** Return the result of applying the rotors to the character C (as an
     *  index in the range 0..alphabet size - 1). */
    private int applyRotors(int c) {
        for (int i = _usedRotors.length - 1; i >= 0; i--) {
            c = _usedRotors[i].convertForward(c);
        }
        for (int i = 1; i < _usedRotors.length; i++) {
            c = _usedRotors[i].convertBackward(c);
        }
        return c;
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        msg = msg.replaceAll("\\s+", "");
        String result = "";
        for (int i = 0; i < msg.length(); i++) {
            int charInt = alphabet().toInt(msg.charAt(i));
            result += _alphabet.toChar(convert(charInt));
        }
        return result;
    }

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;

    /** Number of rotors that the machine holds. */
    private int _numRotors;

    /** Number of pawls in the machine. */
    private int _numPawls;

    /** Permutation representing the plugboard. */
    private Permutation _plugboard;

    /** HashMap containing all available rotors. */
    private HashMap<String, Rotor> _allRotors;

    /** Array of rotors currently in the machine. */
    private Rotor[] _usedRotors;
}
