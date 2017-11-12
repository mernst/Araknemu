package fr.quatrevieux.araknemu.util;

import java.util.Random;

/**
 * Random generation for strings
 */
final public class RandomStringUtil {
    final private Random random;
    final private CharSequence charset;

    /**
     * Create instance
     * @param random The random generator instance
     * @param charset  The charset to use
     */
    public RandomStringUtil(Random random, CharSequence charset) {
        this.random = random;
        this.charset = charset;
    }

    /**
     * Generate the random string
     * @param length The required string length
     * @return
     */
    public String generate(int length) {
        char[] buffer = new char[length];

        for (int i = 0; i < length; ++i) {
            buffer[i] = charset.charAt(random.nextInt(charset.length()));
        }

        return new String(buffer);
    }
}
