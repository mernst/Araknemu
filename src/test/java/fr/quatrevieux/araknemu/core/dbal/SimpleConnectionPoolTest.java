package fr.quatrevieux.araknemu.core.dbal;

import fr.quatrevieux.araknemu.core.config.DefaultConfiguration;
import fr.quatrevieux.araknemu.core.config.IniDriver;
import org.ini4j.Ini;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class SimpleConnectionPoolTest {
    private Driver driver;

    @BeforeEach
    void setUp() throws IOException {
        driver = new SQLiteDriver(
            new DefaultConfiguration(new IniDriver(new Ini(new File("src/test/test_config.ini"))))
                .module(DatabaseConfiguration.class)
                .connection("realm")
        );
    }

    @Test
    void acquireWillCreateConnections() throws SQLException {
        SimpleConnectionPool pool = new SimpleConnectionPool(driver, 2);

        Connection connection1 = pool.acquire();
        Connection connection2 = pool.acquire();

        assertNotSame(connection1, connection2);

        connection1.close();
        connection2.close();
    }

    @Test
    void acquireAndRelease() throws SQLException {
        SimpleConnectionPool pool = new SimpleConnectionPool(driver, 1);

        try (Connection connection = pool.acquire()) {
            pool.release(connection);
            assertSame(connection, pool.acquire());
        }
    }

    @Test
    void releaseClosedConnection() throws SQLException {
        SimpleConnectionPool pool = new SimpleConnectionPool(driver, 1);

        Connection connection = pool.acquire();
        connection.close();

        pool.release(connection);

        assertNotSame(connection, pool.acquire());
    }

    @Test
    void execute() throws SQLException {
        SimpleConnectionPool pool = new SimpleConnectionPool(driver, 1);

        assertTrue(pool.execute((ConnectionPool.Task<Boolean>) connection -> {
            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate("create table test_table (`value` text)");
                stmt.executeUpdate("insert into test_table values ('FOO')");
            }

            return true;
        }));

        assertEquals("FOO", pool.execute(connection -> {
            try (Statement stmt = connection.createStatement()) {
                ResultSet rs = stmt.executeQuery("select * from test_table");

                rs.next();

                return rs.getString("value");
            }
        }));

        pool.execute(connection -> {
            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate("drop table test_table");
            }

            return true;
        });
    }
}
