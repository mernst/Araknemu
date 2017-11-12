package fr.quatrevieux.araknemu.network.in;

import java.util.Collection;

/**
 * Loader for packet parsers
 */
public interface ParserLoader {
    /**
     * Load parsers
     */
    public Collection<SinglePacketParser> load();
}
