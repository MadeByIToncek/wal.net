package space.itoncek.wal.net;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordToolsTest {

    public static Connection conn;
    @BeforeAll
    static void setUp() throws SQLException {
            // db parameters
            String url = "jdbc:sqlite::memory:";
            // create a connection to the database
            conn = DriverManager.getConnection(url);
            System.out.println("Connection to SQLite has been established.");
            conn.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS `walnet_cache` (`key` varchar(64) NOT NULL DEFAULT '0',`value` longtext NOT NULL, PRIMARY KEY (`key`));");
            conn.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS `walnet_users` (`user` varchar(50) NOT NULL DEFAULT '', `password` varchar(50) DEFAULT NULL , PRIMARY KEY (`user`));");
    }

    @org.junit.jupiter.api.Test
    void test() throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException, AuthenticationException {
        String hash = PasswordTools.hash(conn, "test123");
        System.out.println("hash() --> " + hash);
        conn.createStatement().executeUpdate("INSERT INTO walnet_users VALUES ('%s', '%s');".formatted("test", hash));

        assertTrue(PasswordTools.validate("test",hash, conn));
    }

    @AfterAll
    static void afterAll() throws SQLException {
        conn.close();
    }
}