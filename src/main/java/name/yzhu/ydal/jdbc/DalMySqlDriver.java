package name.yzhu.ydal.jdbc;

import com.mysql.cj.jdbc.Driver;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DalMySqlDriver extends Driver {
    static {
        try {
            java.sql.DriverManager.registerDriver(new DalMySqlDriver());
        } catch (SQLException E) {
            throw new RuntimeException("Can't register driver!");
        }
    }
    /**
     * Construct a new driver and register it with DriverManager
     *
     * @throws SQLException if a database error occurs.
     */
    public DalMySqlDriver() throws SQLException {
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        Connection conn = super.connect(url, info);
        return new DalConnection(conn, info.getProperty("datasourceId"), info.getProperty("route"));
    }
}
