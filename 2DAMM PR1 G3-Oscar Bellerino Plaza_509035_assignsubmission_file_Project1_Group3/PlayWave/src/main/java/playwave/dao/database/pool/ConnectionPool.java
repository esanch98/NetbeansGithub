/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package playwave.dao.database.pool;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionPool {
    /* Singleton pattern */
    private static final HikariConfig config = new HikariConfig();
    private static HikariDataSource dataSource;

    static {
        /* Pool configuration, see other properties: https://github.com/brettwooldridge/HikariCP#rocket-initialization*/
        config.setJdbcUrl("jdbc:mysql://localhost/playwave?useUnicode=true&serverTimezone=Europe/Madrid");
        config.setUsername("user");
        config.setPassword("123456");
        config.addDataSourceProperty("maximumPoolSize", 1); //Only 1 connection
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        dataSource = new HikariDataSource(config); //Pool connection
    }
    
    private ConnectionPool() {}
    
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
