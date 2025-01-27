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
 * Copyright (c) 2017-2020 Vincent Quatrevieux
 */

package fr.quatrevieux.araknemu.game.account;

import fr.quatrevieux.araknemu.common.account.AbstractLivingAccount;
import fr.quatrevieux.araknemu.common.account.Permission;
import fr.quatrevieux.araknemu.data.living.entity.account.Account;
import fr.quatrevieux.araknemu.game.account.event.AccountPermissionsUpdated;
import fr.quatrevieux.araknemu.network.game.GameSession;
import fr.quatrevieux.araknemu.network.out.ServerMessage;
import fr.quatrevieux.araknemu.network.realm.out.ServerList;
import org.checkerframework.checker.nullness.qual.EnsuresNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Deterministic;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

/**
 * Account for game
 */
public final class GameAccount extends AbstractLivingAccount<GameSession> {
    private final AccountService service;
    private final int serverId;

    private final EnumSet<Permission> temporaryPermissions = EnumSet.noneOf(Permission.class);

    public GameAccount(Account account, AccountService service, int serverId) {
        super(account);

        this.service  = service;
        this.serverId = serverId;
    }

    /**
     * Attach a session to the account
     *
     * @param session Session to attach
     */
    @Override
    @Deterministic
    @EnsuresNonNull("#1.account()")
    public void attach(GameSession session) {
        super.attach(session);

        service.login(this);
        session.attach(this);
    }

    /**
     * Detach the account from the session
     */
    @Override
    public void detach() {
        session().ifPresent(GameSession::detach);
        service.logout(this);

        super.detach();
    }

    /**
     * Get the current session for the account
     */
    public Optional<GameSession> session() {
        return Optional.ofNullable(session);
    }

    /**
     * Get the remaining premium time
     *
     * @todo save to account entity
     */
    public long remainingTime() {
        return ServerList.ONE_YEAR;
    }

    /**
     * Get the current game server id
     */
    public int serverId() {
        return serverId;
    }

    @Override
    public boolean isMaster() {
        return super.isMaster() || !temporaryPermissions.isEmpty();
    }

    /**
     * Check if the account has the asked permission
     */
    public boolean isGranted(Permission permission) {
        return temporaryPermissions.contains(permission) || account.permissions().contains(permission);
    }

    /**
     * Check if the account has the asked permissions
     */
    public boolean isGranted(Set<Permission> permissions) {
        return temporaryPermissions.containsAll(permissions) || account.permissions().containsAll(permissions);
    }

    /**
     * Grant list of temporary permissions
     * This methode will trigger an {@link AccountPermissionsUpdated} event on the session
     *
     * @param permissions The permissions to add
     */
    public void grant(Permission... permissions) {
        grant(permissions, null);
    }

    /**
     * Grant list of temporary permissions
     * This methode will trigger an {@link AccountPermissionsUpdated} event on the session
     *
     * @param permissions The permissions to add
     * @param performer The admin account which grants those permissions. Can be null.
     */
    public void grant(Permission[] permissions, @Nullable GameAccount performer) {
        temporaryPermissions.addAll(Arrays.asList(permissions));

        if (session != null) {
            session.dispatch(new AccountPermissionsUpdated(performer, this));
        }
    }

    /**
     * Revoke all temporary permissions
     * This methode will trigger an {@link AccountPermissionsUpdated} event on the session
     *
     * @param performer The admin account which revoke permissions. Can be null.
     */
    public void revoke(@Nullable GameAccount performer) {
        if (temporaryPermissions.isEmpty()) {
            return;
        }

        temporaryPermissions.clear();

        if (session != null) {
            session.dispatch(new AccountPermissionsUpdated(performer, this));
        }
    }

    /**
     * Check the secret answer
     */
    public boolean checkAnswer(String input) {
        return account.answer().equalsIgnoreCase(input);
    }

    /**
     * Kick the account with the given server message
     * If the account is not logged, this method will de nothing
     *
     * @param message Message to show
     */
    public void kick(ServerMessage message) {
        if (isLogged()) {
            session.send(message);
            session.close();
        }
    }
}
