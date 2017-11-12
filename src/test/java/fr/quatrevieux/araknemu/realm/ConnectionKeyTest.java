package fr.quatrevieux.araknemu.realm;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConnectionKeyTest {
    @Test
    void constructor() {
        assertEquals("azerty", new ConnectionKey("azerty").key());
    }

    @Test
    void generate() {
        ConnectionKey key = new ConnectionKey();

        assertSame(key.key(), key.key());
        assertEquals(32, key.key().length());

        assertNotEquals(key.key(), new ConnectionKey().key());
    }

    @Test
    void decodeRandomKey() {
        for (int i = 0; i < 100; ++i) {
            String data = "my_secret_data";

            ConnectionKey key = new ConnectionKey();
            String encoded = cryptPassword(data, key.key());

            assertEquals(data, key.decode(encoded), "For key : " + key.key() + " encoded : " + encoded);
        }
    }

    @Test
    void decodeWithSpecialChars() {
        String data = "é#ç@à²";

        ConnectionKey key = new ConnectionKey("azertyuiop");
        String encoded = cryptPassword(data, key.key());

        assertEquals(data, key.decode(encoded));
    }

    @Test
    void decodeWithComplexKey() {
        String data = "my_secret_data";

        ConnectionKey key = new ConnectionKey("é#ç@à²{ùø*µ°~§a/_.");
        String encoded = cryptPassword(data, key.key());

        assertEquals(data, key.decode(encoded));
    }

    /**
     * Adapted from :
     * https://github.com/Emudofus/Dofus/blob/1.29/ank/utils/Crypt.as#L20
     */
    static public String cryptPassword(String pwd, String key) {
        char[] HASH = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's',
            't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U',
            'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_'};

        String _loc4 = "";

        for (int _loc5 = 0;_loc5<pwd.length(); ++_loc5)
        {
            int _loc6 = pwd.charAt(_loc5);
            int _loc7 = key.charAt(_loc5);
            int _loc8 = _loc6 / 16;
            int _loc9 = _loc6 % 16;
            _loc4 += HASH[(_loc8 + _loc7 % HASH.length) % HASH.length];
            _loc4 += HASH[(_loc9 + _loc7 % HASH.length) % HASH.length];
        } // end while

        return (_loc4);
    }
}
