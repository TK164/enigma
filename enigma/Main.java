package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import ucb.util.CommandArgs;

import static enigma.EnigmaException.*;

/** Enigma simulator.
 *  @author Teja Kanthamneni
 */
public final class Main {

    /** Process a sequence of encryptions and decryptions, as
     *  specified by ARGS, where 1 <= ARGS.length <= 3.
     *  ARGS[0] is the name of a configuration file.
     *  ARGS[1] is optional; when present, it names an input file
     *  containing messages.  Otherwise, input comes from the standard
     *  input.  ARGS[2] is optional; when present, it names an output
     *  file for processed messages.  Otherwise, output goes to the
     *  standard output. Exits normally if there are no errors in the input;
     *  otherwise with code 1. */
    public static void main(String... args) {
        try {
            CommandArgs options =
                new CommandArgs("--verbose --=(.*){1,3}", args);
            if (!options.ok()) {
                throw error("Usage: java enigma.Main [--verbose] "
                            + "[INPUT [OUTPUT]]");
            }

            _verbose = options.contains("--verbose");
            new Main(options.get("--")).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /** Open the necessary files for non-option arguments ARGS (see comment
      *  on main). */
    Main(List<String> args) {
        _config = getInput(args.get(0));

        if (args.size() > 1) {
            _input = getInput(args.get(1));
        } else {
            _input = new Scanner(System.in);
        }

        if (args.size() > 2) {
            _output = getOutput(args.get(2));
        } else {
            _output = System.out;
        }
    }

    /** Return a Scanner reading from the file named NAME. */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Return a PrintStream writing to the file named NAME. */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Configure an Enigma machine from the contents of configuration
     *  file _config and apply it to the messages in _input, sending the
     *  results to _output. */
    private void process() {
        Machine mach1 = readConfig();
        boolean rotorsPresent = false;
        String nextLine = "";
        while (_input.hasNext()) {
            nextLine = _input.nextLine();
            if (nextLine.equals("")) {
                _output.println();
            } else if (nextLine.charAt(0) == '*') {
                setUp(mach1, nextLine);
                rotorsPresent = true;
            } else {
                if (!rotorsPresent) {
                    throw error("Settings line must start with '*'");
                }
                String result = mach1.convert(nextLine);
                printMessageLine(result);
                if (_input.hasNext()) {
                    _output.println();
                }

            }
        }
    }

    /** Return an Enigma machine configured from the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {
            _alphaString = _config.next();
            _alphabet = new Alphabet(_alphaString);
            int numRotors = _config.nextInt();
            int numPawls = _config.nextInt();
            ArrayList<Rotor> allRotors = new ArrayList<>();
            while (_config.hasNext()) {
                allRotors.add(readRotor());
            }
            return new Machine(_alphabet, numRotors, numPawls, allRotors);
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /** Return a rotor, reading its description from _config. */
    private Rotor readRotor() {
        try {
            String name = _config.next();
            String rotorSetting = _config.next();
            String notches = "";
            String cycles = "";
            if (rotorSetting.length() > 1) {
                notches = rotorSetting.substring(1);
            }
            while (true) {
                try {
                    cycles += _config.next("\\(\\S+\\)\\S*");
                } catch (Exception e) {
                    break;
                }
            }
            Permutation perm = new Permutation(cycles, _alphabet);
            switch (rotorSetting.charAt(0)) {
            case 'M':
                return new MovingRotor(name, perm, notches);
            case 'N':
                if (!notches.equals("")) {
                    throw error("Fixed rotor cannot have notches");
                }
                return new FixedRotor(name, perm);
            case 'R':
                if (!notches.equals("")) {
                    throw error("Reflector cannot have notches");
                }
                return new Reflector(name, perm);
            default:
                throw error("Rotor must be moving, fixed, or a reflector");
            }
        } catch (NoSuchElementException excp) {
            throw error("bad rotor description");
        }
    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment. */
    private void setUp(Machine M, String settings) {
        settings = settings.substring(1).replaceFirst("\\s+", "");
        String[] machSettings = settings.split("\\s+");
        String[] rotors = new String[M.numRotors()];
        for (int i = 0; i < rotors.length; i++) {
            rotors[i] = machSettings[i];
        }
        M.insertRotors(rotors);
        for (int i = M.numRotors() - M.numPawls(); i < M.numRotors(); i++) {
            (M.getRotor(i)).resetNotches();
        }
        int plugIndex = rotors.length + 1;
        String settingLine = machSettings[rotors.length];
        M.setRotors(settingLine);
        if (machSettings.length > rotors.length + 1) {
            String next = machSettings[rotors.length + 1];
            if (next.matches("\\w+")
                    && next.length() == M.numRotors() - 1) {
                String ringSetting = next;
                for (int i = 0; i < ringSetting.length(); i++) {
                    int ogSet = _alphabet.toInt(settingLine.charAt(i));
                    int ringSet = _alphabet.toInt(ringSetting.charAt(i));
                    M.getRotor(i + 1).set((ogSet - ringSet) % _alphabet.size());
                }
                for (int i = M.numRotors() - M.numPawls();
                    i < M.numRotors(); i++) {
                    char newNotch = ringSetting.charAt(i - 1);
                    ((MovingRotor) M.getRotor(i)).changeNotches(newNotch);
                }
                plugIndex++;
            }
        }

        String plugboardSettings = "";
        for (int i = plugIndex; i < machSettings.length; i++) {
            plugboardSettings += machSettings[i];
        }
        M.setPlugboard(new Permutation(plugboardSettings, _alphabet));
    }

    /** Return true iff verbose option specified. */
    static boolean verbose() {
        return _verbose;
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {
        while (msg.length() > 0) {
            if (msg.length() > 5) {
                _output.print(msg.substring(0, 5) + " ");
                msg = msg.substring(5);
            } else {
                _output.print(msg);
                msg = "";
            }
        }
    }

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** String representation of the alphabet. */
    private String _alphaString;

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;

    /** True if --verbose specified. */
    private static boolean _verbose;
}
