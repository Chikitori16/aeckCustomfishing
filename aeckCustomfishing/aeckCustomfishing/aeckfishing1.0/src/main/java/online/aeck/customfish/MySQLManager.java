
package online.aeck.customfish;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MySQLManager {

    private final String host;
    private final String database;
    private final String username;
    private final String password;
    private final String table;
    private final int port;

    private Connection connection;

    private final ExecutorService pool = Executors.newFixedThreadPool(2);

    public MySQLManager(CustomFish plugin) {

        this.host = plugin.getConfig().getString("mysql.host");
        this.port = plugin.getConfig().getInt("mysql.port");
        this.database = plugin.getConfig().getString("mysql.database");
        this.username = plugin.getConfig().getString("mysql.username");
        this.password = plugin.getConfig().getString("mysql.password");
        this.table = plugin.getConfig().getString("mysql.table-name");

        setupTable();
    }

    private synchronized void connect() throws SQLException {

        if (connection != null && !connection.isClosed()) return;

        connection = DriverManager.getConnection(
                "jdbc:mysql://" + host + ":" + port + "/" + database +
                        "?useSSL=false&autoReconnect=true",
                username,
                password
        );
    }

    private void setupTable() {

        try {

            connect();

            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS " + table + " (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY," +
                            "player_name VARCHAR(32)," +
                            "fish_type VARCHAR(32)," +
                            "quality VARCHAR(16)," +
                            "length DOUBLE," +
                            "weight DOUBLE," +
                            "catch_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
            ).executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void logCatchAsync(String player, String fish, String quality, double length, double weight) {

        pool.execute(() -> {

            try {

                connect();

                PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO " + table +
                                " (player_name, fish_type, quality, length, weight) VALUES (?, ?, ?, ?, ?)"
                );

                ps.setString(1, player);
                ps.setString(2, fish);
                ps.setString(3, quality);
                ps.setDouble(4, length);
                ps.setDouble(5, weight);

                ps.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            }

        });
    }

    public static class FishRecord {
        public String playerName;
        public String fishType;
        public String quality;
        public double weight;
    }

    public List<FishRecord> getTopFish(int limit) {

        List<FishRecord> list = new ArrayList<>();

        try {

            connect();

            ResultSet rs = connection.prepareStatement(
                    "SELECT player_name, fish_type, quality, weight FROM "
                            + table +
                            " ORDER BY weight DESC LIMIT " + limit
            ).executeQuery();

            while (rs.next()) {

                FishRecord r = new FishRecord();

                r.playerName = rs.getString("player_name");
                r.fishType = rs.getString("fish_type");
                r.quality = rs.getString("quality");
                r.weight = rs.getDouble("weight");

                list.add(r);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public void shutdown(){

        pool.shutdown();

        try{
            if(connection!=null) connection.close();
        }catch(Exception ignored){}

    }
}
