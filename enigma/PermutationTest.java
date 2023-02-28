package enigma;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;

import static org.junit.Assert.*;

import static enigma.TestUtils.*;

/** The suite of all JUnit tests for the Permutation class.
 *  @author Teja Kanthamneni
 */
public class PermutationTest {

    /** Testing time limit. */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    /* ***** TESTING UTILITIES ***** */

    private Permutation perm;
    private String alpha = UPPER_STRING;

    /** Check that perm has an alphabet whose size is that of
     *  FROMALPHA and TOALPHA and that maps each character of
     *  FROMALPHA to the corresponding character of FROMALPHA, and
     *  vice-versa. TESTID is used in error messages. */
    private void checkPerm(String testId,
                           String fromAlpha, String toAlpha) {
        int N = fromAlpha.length();
        assertEquals(testId + " (wrong length)", N, perm.size());
        for (int i = 0; i < N; i += 1) {
            char c = fromAlpha.charAt(i), e = toAlpha.charAt(i);
            assertEquals(msg(testId, "wrong translation of '%c'", c),
                         e, perm.permute(c));
            assertEquals(msg(testId, "wrong inverse of '%c'", e),
                         c, perm.invert(e));
            int ci = alpha.indexOf(c), ei = alpha.indexOf(e);
            assertEquals(msg(testId, "wrong translation of %d", ci),
                         ei, perm.permute(ci));
            assertEquals(msg(testId, "wrong inverse of %d", ei),
                         ci, perm.invert(ei));
        }
    }

    private void checkPerm2(String testId,
                           String fromAlpha, String toAlpha) {
        int N = fromAlpha.length();
        assertEquals(testId + " (wrong length)", N, perm.size());
        for (int i = 0; i < N; i += 1) {
            char c = fromAlpha.charAt(i), e = toAlpha.charAt(i);
            assertEquals(msg(testId, "wrong translation of '%c'", c),
                    e, perm.permute(c));
            assertEquals(msg(testId, "wrong inverse of '%c'", e),
                    c, perm.invert(e));

            String beta = "123456abc()";

            int ci = beta.indexOf(c), ei = beta.indexOf(e);
            assertEquals(msg(testId, "wrong translation of %d", ci),
                    ei, perm.permute(ci));
            assertEquals(msg(testId, "wrong inverse of %d", ei),
                    ci, perm.invert(ei));
        }
    }

    /* ***** TESTS ***** */

    @Test
    public void checkIdTransform() {
        perm = new Permutation("", UPPER);
        checkPerm("identity", UPPER_STRING, UPPER_STRING);
    }

    @Test
    public void checkTransform() {
        Alphabet alpha1 = new Alphabet("123456abc()");
        perm = new Permutation("(123)) ((456) (abc)", alpha1);
        perm.print();
        checkPerm2("test0", "123456abc()", "23)56(bca41");

    }

    @Test
    public void alphabetTest() {
        int errorCount = 0;

        String alphabet = "AbCdEfhIjKlMnpQrStUvWxY";
        Alphabet alpha1 = new Alphabet(alphabet);
        assertEquals(4, alpha1.toInt('E'));
        assertTrue(alpha1.toChar(18) == 'U');
        assertEquals(alphabet.length(), alpha1.size());
        assertTrue(alpha1.contains('Y'));
        assertFalse(alpha1.contains('5'));
        try {
            Alphabet beta = new Alphabet("aabcebcdd");
        } catch (EnigmaException e) {
            System.out.println("Error 1");
            errorCount++;
        }
        try {
            alpha1.toChar(26);
        } catch (EnigmaException e) {
            System.out.println("Error 2");
            errorCount++;
        }
        try {
            alpha1.toInt('4');
        } catch (EnigmaException e) {
            System.out.println("Error 3");
            errorCount++;
        }
        try {
            Alphabet gamma = new Alphabet("dkas\r\njie");
        } catch (EnigmaException e) {
            System.out.println("Error 4");
            errorCount++;
        }
        assertEquals(4, errorCount);
    }
}
