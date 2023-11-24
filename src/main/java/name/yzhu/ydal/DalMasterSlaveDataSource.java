package name.yzhu.ydal;

import org.apache.shardingsphere.core.rule.MasterSlaveRule;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.MasterSlaveDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

public class DalMasterSlaveDataSource extends MasterSlaveDataSource  {

    public DalMasterSlaveDataSource(Map<String, DataSource> dataSourceMap, MasterSlaveRule masterSlaveRule, Properties props) throws SQLException {
        super(dataSourceMap, masterSlaveRule, props);
    }

}
