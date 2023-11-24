package name.yzhu.ydal.jdbc;

import java.util.HashMap;
import java.util.Map;

public interface DalDatasource {
    String getName();

    default Map<String, String> metricsLabels(){return new HashMap<>();}

}
