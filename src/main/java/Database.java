import java.sql.*;
import java.util.Properties;

public class Database {
    private final String url;
    private final String user;
    private final String password;
    private final Properties props;
    private static final String createTableSQL =
            "CREATE TABLE IF NOT EXISTS receipts (" +
            "id SERIAL PRIMARY KEY," +
            " gstin VARCHAR(50) NOT NULL, " +
            " total VARCHAR(50) NOT NULL," +
            " path VARCHAR(50));";

    public Database() throws SQLException {
        this.url = "jdbc:postgresql://localhost:5433/deduplicator";
        this.user = "postgres";
        this.password = "1308249756";
        this.props = getProps();
        this.createTable();
    }

    private void createTable() throws SQLException {
        try (Connection conn = DriverManager.getConnection(this.url, this.props);
            Statement statement = conn.createStatement()) {
            System.out.println(createTableSQL);
            statement.execute(createTableSQL);
        }
    }

    public void insertReceipt(String gstin, String total, String path) throws SQLException {
        try (Connection conn = DriverManager.getConnection(this.url, this.props);
             Statement statement = conn.createStatement()) {
            String insertReceiptSQL = String.format(
                    "INSERT INTO receipts (gstin, total, path) VALUES ('%s', '%s', '%s');",
                    gstin,
                    total,
                    path);
            System.out.println(insertReceiptSQL);
            statement.execute(insertReceiptSQL);
        }
    }

    public boolean notDuplicate(String gstin, String total) throws SQLException {
        String select = "SELECT count(*) FROM receipts WHERE gstin = ? AND total = ?";
        int count;

        try (Connection conn = DriverManager.getConnection(this.url, this.props);
             PreparedStatement statement = conn.prepareStatement(select)) {
            statement.setString(1, gstin);
            statement.setString(2, total);

            System.out.println(statement);
            ResultSet rs = statement.executeQuery();
            rs.next();
            count = rs.getInt(1);
        }

        return count <= 0;
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

    public Properties getProps() {
        Properties props = new Properties();
        props.setProperty("user", this.user);
        props.setProperty("password", this.password);
        props.setProperty("encoding", "UTF8");
        return props;
    }
}
