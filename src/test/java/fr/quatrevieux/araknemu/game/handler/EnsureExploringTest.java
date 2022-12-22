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

package fr.quatrevieux.araknemu.game.handler;

import fr.quatrevieux.araknemu.core.di.ContainerException;
import fr.quatrevieux.araknemu.game.GameBaseCase;
import fr.quatrevieux.araknemu.game.account.CharactersService;
import fr.quatrevieux.araknemu.game.handler.account.ListCharacters;
import fr.quatrevieux.araknemu.core.network.exception.CloseImmediately;
import fr.quatrevieux.araknemu.network.game.in.account.AskCharacterList;
import fr.quatrevieux.araknemu.core.network.parser.Packet;
import fr.quatrevieux.araknemu.core.network.parser.PacketHandler;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EnsureExploringTest extends GameBaseCase{

    void handleNoPlayer() {
        PacketHandler inner = Mockito.mock(PacketHandler.class);
        EnsureExploring handler = new EnsureExploring<>(inner);

        assertThrows(CloseImmediately.class, () -> handler.handle(session, Mockito.mock(Packet.class)));
    }

    void handleSuccess() throws Exception {
        PacketHandler inner = Mockito.mock(PacketHandler.class);
        EnsureExploring handler = new EnsureExploring<>(inner);
        explorationPlayer();

        Packet packet = new AskCharacterList(false);

        handler.handle(session, packet);

        Mockito.verify(inner).handle(session, packet);
    }

    void packet() throws ContainerException {
        assertEquals(
            AskCharacterList.class,
            new EnsureExploring<>(new ListCharacters(
                container.get(CharactersService.class)
            )).packet()
        );
    }
}
