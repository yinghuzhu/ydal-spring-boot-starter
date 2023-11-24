package name.yzhu.ydal;

import com.zaxxer.hikari.HikariDataSource;

public class DalConnectPoolDataSource extends HikariDataSource {
    private String datasourceId = "";
    private String route = "";

    @Override
    public String toString()
    {
        return DalConnectPoolDataSource.class.getSimpleName()+" (" + datasourceId + "-"+route+")";
    }

    public void setDatasourceId(String datasourceId) {
        this.datasourceId = datasourceId;
    }

    public void setRoute(String route) {
        this.route = route;
    }
}
