package fr.quatrevieux.araknemu.core.dbal;

import fr.quatrevieux.araknemu.core.config.ConfigurationModule;
import fr.quatrevieux.araknemu.core.config.Pool;
import fr.quatrevieux.araknemu.core.config.PoolUtils;

/**
 * Configuration module for database system
 */
final public class DatabaseConfiguration implements ConfigurationModule {
    final static public class Connection {
        final private String name;
        final private PoolUtils pool;

        public Connection(String name, PoolUtils pool) {
            this.name = name;
            this.pool = pool;
        }

        public String name() {
            return name;
        }

        /**
         * Get the connection host (useful for MySQL)
         */
        public String host() {
            return pool.string(name + ".host", "127.0.0.1");
        }

        /**
         * Get the username
         */
        public String user() {
            return pool.string(name + ".user", "root");
        }

        /**
         * Get the user password
         */
        public String password() {
            return pool.string(name + ".password");
        }

        /**
         * Get the database name (by default, same as connection name)
         */
        public String dbname() {
            return pool.string(name + ".dbname", name);
        }

        /**
         * Get the database type name
         * Can be "mysql" or "sqlite"
         */
        public String type() {
            return pool.string(name + ".type", "mysql");
        }

        /**
         * Is a memory connection (sqlite)
         */
        public boolean memory() {
            return pool.bool(name + ".memory");
        }

        /**
         * Shared cache (sqlite)
         * Useful for SQLite in-memory, for sharing data between connections
         */
        public boolean shared() {
            return pool.bool(name + ".shared", true);
        }

        /**
         * Get the maximum pool size
         */
        public int maxPoolSize() {
            return pool.integer(name + ".poolSize", 16);
        }

        /**
         * Get the database file path (sqlite)
         * By default same as {@link Connection#dbname()} with .db extension
         */
        public String path() {
            return pool.string(name + ".path", dbname() + ".db");
        }
    }

    private PoolUtils pool;

    @Override
    public void setPool(Pool pool) {
        this.pool = new PoolUtils(pool);
    }

    @Override
    public String name() {
        return "database";
    }

    /**
     * Get a connection configuration
     * @param name The connection name
     * @return
     */
    public Connection connection(String name) {
        return new Connection(name, pool);
    }
}
