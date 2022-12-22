/*
 * This file is part of Araknemu.
 *
 * Araknemu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Araknemu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Araknemu.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (c) 2017-2019 Vincent Quatrevieux
 */

package fr.quatrevieux.araknemu.network.game.in.exchange;

import fr.quatrevieux.araknemu.game.exploration.exchange.ExchangeType;
import fr.quatrevieux.araknemu.core.network.parser.ParsePacketException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExchangeRequestTest {
    private ExchangeRequest.Parser parser;

    void setUp() {
        parser = new ExchangeRequest.Parser();
    }

    void parseBadPartsNumber() {
        assertThrows(ParsePacketException.class, () -> parser.parse("2"));
    }

    void parseBadType() {
        assertThrows(ParsePacketException.class, () -> parser.parse("33|123"));
    }

    void parseWithId() {
        ExchangeRequest request = parser.parse("2|123");

        assertEquals(ExchangeType.NPC_EXCHANGE, request.type());
        assertEquals(123, request.id().get().intValue());
        assertFalse(request.cell().isPresent());
    }

    void parseWithIdAndCell() {
        ExchangeRequest request = parser.parse("2|123|145");

        assertEquals(ExchangeType.NPC_EXCHANGE, request.type());
        assertEquals(123, request.id().get().intValue());
        assertEquals(145, request.cell().get().intValue());
    }

    void parseWithCell() {
        ExchangeRequest request = parser.parse("2||145");

        assertEquals(ExchangeType.NPC_EXCHANGE, request.type());
        assertFalse(request.id().isPresent());
        assertEquals(145, request.cell().get().intValue());
    }
}
