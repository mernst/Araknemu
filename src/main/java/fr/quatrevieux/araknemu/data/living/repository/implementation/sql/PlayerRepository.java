package fr.quatrevieux.araknemu.data.living.repository.implementation.sql;

import fr.quatrevieux.araknemu.core.dbal.ConnectionPool;
import fr.quatrevieux.araknemu.core.dbal.repository.EntityNotFoundException;
import fr.quatrevieux.araknemu.core.dbal.repository.RepositoryException;
import fr.quatrevieux.araknemu.core.dbal.repository.RepositoryUtils;
import fr.quatrevieux.araknemu.core.dbal.util.ConnectionPoolUtils;
import fr.quatrevieux.araknemu.data.constant.Race;
import fr.quatrevieux.araknemu.data.constant.Sex;
import fr.quatrevieux.araknemu.data.living.entity.player.Player;
import fr.quatrevieux.araknemu.data.transformer.Transformer;
import fr.quatrevieux.araknemu.data.value.Colors;
import fr.quatrevieux.araknemu.data.value.Position;
import fr.quatrevieux.araknemu.data.value.ServerCharacters;
import fr.quatrevieux.araknemu.game.chat.ChannelType;
import fr.quatrevieux.araknemu.game.world.creature.characteristics.MutableCharacteristics;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

final class PlayerRepository implements fr.quatrevieux.araknemu.data.living.repository.player.PlayerRepository {
    private class Loader implements RepositoryUtils.Loader<Player> {
        @Override
        public Player create(ResultSet rs) throws SQLException {
            return new Player(
                rs.getInt("PLAYER_ID"),
                rs.getInt("ACCOUNT_ID"),
                rs.getInt("SERVER_ID"),
                rs.getString("PLAYER_NAME"),
                Race.byId(rs.getInt("RACE")),
                Sex.values()[rs.getInt("SEX")],
                new Colors(
                    rs.getInt("COLOR1"),
                    rs.getInt("COLOR2"),
                    rs.getInt("COLOR3")
                ),
                rs.getInt("PLAYER_LEVEL"),
                characteristicsTransformer.unserialize(
                    rs.getString("PLAYER_STATS")
                ),
                new Position(
                    rs.getInt("MAP_ID"),
                    rs.getInt("CELL_ID")
                ),
                channelsTransformer.unserialize(rs.getString("CHANNELS"))
            );
        }

        @Override
        public Player fillKeys(Player entity, ResultSet keys) throws SQLException {
            return entity.withId(
                keys.getInt(1)
            );
        }
    }

    final private ConnectionPoolUtils pool;
    final private Transformer<MutableCharacteristics> characteristicsTransformer;
    final private Transformer<Set<ChannelType>> channelsTransformer;

    final private RepositoryUtils<Player> utils;

    public PlayerRepository(ConnectionPool pool, Transformer<MutableCharacteristics> characteristicsTransformer, Transformer<Set<ChannelType>> channelsTransformer) {
        this.pool = new ConnectionPoolUtils(pool);
        this.characteristicsTransformer = characteristicsTransformer;
        this.channelsTransformer = channelsTransformer;
        this.utils = new RepositoryUtils<>(this.pool, new PlayerRepository.Loader());
    }

    @Override
    public void initialize() throws RepositoryException {
        try {
            pool.query(
                "CREATE TABLE PLAYER (" +
                    "PLAYER_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "ACCOUNT_ID INTEGER," +
                    "SERVER_ID INTEGER," +
                    "PLAYER_NAME VARCHAR(32)," +
                    "RACE INTEGER(2)," +
                    "SEX INTEGER(1)," +
                    "COLOR1 INTEGER," +
                    "COLOR2 INTEGER," +
                    "COLOR3 INTEGER," +
                    "PLAYER_LEVEL INTEGER," +
                    "PLAYER_STATS TEXT," +
                    "MAP_ID INTEGER," +
                    "CELL_ID INTEGER," +
                    "CHANNELS VARCHAR(16)," +
                    "UNIQUE (PLAYER_NAME, SERVER_ID)" +
                ")"
            );

            pool.query("CREATE INDEX IDX_ACC_SRV ON PLAYER (ACCOUNT_ID, SERVER_ID)");
        } catch (SQLException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public void destroy() throws RepositoryException {
        try {
            pool.query("DROP TABLE PLAYER");
        } catch (SQLException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public Player add(Player entity) throws RepositoryException {
        return utils.update(
            "INSERT INTO PLAYER " +
                "(ACCOUNT_ID, SERVER_ID, PLAYER_NAME, RACE, SEX, COLOR1, COLOR2, COLOR3, PLAYER_LEVEL, PLAYER_STATS, MAP_ID, CELL_ID, CHANNELS) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            stmt -> {
                stmt.setInt(1,     entity.accountId());
                stmt.setInt(2,     entity.serverId());
                stmt.setString(3,  entity.name());
                stmt.setInt(4,     entity.race().ordinal());
                stmt.setInt(5,     entity.sex().ordinal());
                stmt.setInt(6,     entity.colors().color1());
                stmt.setInt(7,     entity.colors().color2());
                stmt.setInt(8,     entity.colors().color3());
                stmt.setInt(9,     entity.level());
                stmt.setString(10, characteristicsTransformer.serialize(entity.stats()));
                stmt.setInt(11,    entity.position().map());
                stmt.setInt(12,    entity.position().cell());
                stmt.setString(13, channelsTransformer.serialize(entity.channels()));
            },
            entity
        );
    }

    @Override
    public void delete(Player entity) throws RepositoryException {
        if (utils.update("DELETE FROM PLAYER WHERE PLAYER_ID = ?", rs -> rs.setInt(1, entity.id())) < 1) {
            throw new EntityNotFoundException();
        }
    }

    @Override
    public Player get(Player entity) throws RepositoryException {
        return utils.findOne(
            "SELECT * FROM PLAYER WHERE PLAYER_ID = ?",
            stmt -> stmt.setInt(1, entity.id())
        );
    }

    @Override
    public boolean has(Player entity) throws RepositoryException {
        return utils.aggregate(
            "SELECT COUNT(*) FROM PLAYER WHERE PLAYER_ID = ?",
            stmt -> stmt.setInt(1, entity.id())
        ) > 0;
    }

    @Override
    public Collection<Player> findByAccount(int accountId, int serverId) {
        return utils.findAll(
            "SELECT * FROM PLAYER WHERE ACCOUNT_ID = ? AND SERVER_ID = ?",
            stmt -> {
                stmt.setInt(1, accountId);
                stmt.setInt(2, serverId);
            }
        );
    }

    @Override
    public boolean nameExists(Player player) {
        return utils.aggregate(
            "SELECT COUNT(*) FROM PLAYER WHERE PLAYER_NAME = ? AND SERVER_ID = ?",
            stmt -> {
                stmt.setString(1, player.name());
                stmt.setInt(2,    player.serverId());
            }
        ) > 0;
    }

    @Override
    public int accountCharactersCount(Player player) {
        return utils.aggregate(
            "SELECT COUNT(*) FROM PLAYER WHERE ACCOUNT_ID = ? AND SERVER_ID = ?",
            stmt -> {
                stmt.setInt(1, player.accountId());
                stmt.setInt(2, player.serverId());
            }
        );
    }

    @Override
    public Collection<ServerCharacters> accountCharactersCount(int accountId) {
        try {
            return pool.prepare(
                "SELECT SERVER_ID, COUNT(*) FROM PLAYER WHERE ACCOUNT_ID = ? GROUP BY SERVER_ID",
                stmt -> {
                    stmt.setInt(1, accountId);

                    try (ResultSet rs = stmt.executeQuery()) {
                        Collection<ServerCharacters> list = new ArrayList<>();

                        while (rs.next()) {
                            list.add(
                                new ServerCharacters(
                                    rs.getInt("SERVER_ID"),
                                    rs.getInt("COUNT(*)")
                                )
                            );
                        }

                        return list;
                    }
                }
            );
        } catch (SQLException e) {
            throw new RepositoryException("Cannot load characters count", e);
        }
    }

    @Override
    public Player getForGame(Player player) {
        return utils.findOne(
            "SELECT * FROM PLAYER WHERE PLAYER_ID = ? AND ACCOUNT_ID = ? AND SERVER_ID = ?",
            stmt -> {
                stmt.setInt(1, player.id());
                stmt.setInt(2, player.accountId());
                stmt.setInt(3, player.serverId());
            }
        );
    }

    @Override
    public void save(Player player) {
        int rows = utils.update(
            "UPDATE PLAYER SET " +
                "PLAYER_LEVEL = ?, PLAYER_STATS = ?, MAP_ID = ?, CELL_ID = ?, CHANNELS = ? " +
                "WHERE PLAYER_ID = ?",
            stmt -> {
                stmt.setInt(1,    player.level());
                stmt.setString(2, characteristicsTransformer.serialize(player.stats()));
                stmt.setInt(3,    player.position().map());
                stmt.setInt(4,    player.position().cell());
                stmt.setString(5, channelsTransformer.serialize(player.channels()));
                stmt.setInt(6,    player.id());
            }
        );

        if (rows != 1) {
            throw new EntityNotFoundException();
        }
    }
}