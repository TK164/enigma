package enigma;

import static enigma.EnigmaException.*;

/** Class that represents a rotating rotor in the enigma machine.
 *  @author Teja Kanthamneni
 */
class MovingRotor extends Rotor {

    /** A rotor named NAME whose permutation in its default setting is
     *  PERM, and whose notches are at the positions indicated in NOTCHES.
     *  The Rotor is initally in its 0 setting (first character of its
     *  alphabet).
     */
    MovingRotor(String name, Permutation perm, String notches) {
        super(name, perm);
        for (int i = 0; i < notches.length(); i++) {
            if (!alphabet().contains(notches.charAt(i))) {
                throw error("Notches must be in alphabet");
            }
        }
        _notches = notches;
        _initialNotches = notches;
    }

    @Override
    boolean rotates() {
        return true;
    }

    @Override
    void advance() {
        set(setting() + 1);
    }

    @Override
    String notches() {
        return _notches;
    }

    void changeNotches(char mod) {
        String result = "";
        for (int i = 0; i < _notches.length(); i++) {
            int currCharIndex = alphabet().toInt(_notches.charAt(i));
            int newCharIndex = alphabet().toInt(mod);
            char newNotch = alphabet().toChar(permutation()
                    .wrap(currCharIndex - newCharIndex));
            result += newNotch;
        }
        _notches = result;
    }

    @Override
    void resetNotches() {
        _notches = _initialNotches;
    }

    /** String representing the characters at which the rotor
     * has notches. */
    private String _notches;

    /** Storing the initial notches so they can be accessed
     * after they've been changed. */
    private String _initialNotches;

}
