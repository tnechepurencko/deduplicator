import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class Database {
    private final String url;
    private final String user;
    private final String password;

    public Database() throws SQLException {
        this.url = "jdbc:postgresql://localhost/deduplicator";
        this.user = "postgres";
        this.password = "1308249756";
        this.createTable();
    }

    private static final String createTableSQL =
            "CREATE TABLE IF NOT EXISTS receipts (" +
            "id INT PRIMARY KEY," +
            " gstin VARCHAR(50) UNIQUE NOT NULL, " +
            " path VARCHAR(50) UNIQUE NOT NULL)";

    private void createTable() throws SQLException {
        System.out.println(createTableSQL);
        Properties props = new Properties();
        props.setProperty("user", "postgres");
        props.setProperty("password", "1308249756");
        props.setProperty("encoding", "UTF8");

        try (Connection conn = DriverManager.getConnection(url, props);
            Statement statement = conn.createStatement()) {
            statement.execute(createTableSQL);
        }

//        try (Connection conn = DriverManager.getConnection(url, user, password);
//            Statement statement = conn.createStatement()) {
//            statement.execute(createTableSQL);
//        }
    }

    public static void printSQLException(SQLException ex) {
        for (Throwable e: ex) {
            if (e instanceof SQLException) {
                e.printStackTrace(System.err);
                System.err.println("SQLState: " + ((SQLException) e).getSQLState());
                System.err.println("Error Code: " + ((SQLException) e).getErrorCode());
                System.err.println("Message: " + e.getMessage());
                Throwable t = ex.getCause();
                while (t != null) {
                    System.out.println("Cause: " + t);
                    t = t.getCause();
                }
            }
        }
    }
}
