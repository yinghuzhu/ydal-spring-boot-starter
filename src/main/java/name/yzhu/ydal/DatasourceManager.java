package name.yzhu.ydal;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.api.hint.HintManager;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DatasourceManager {
    private static DatasourceManager ourInstance = new DatasourceManager();

    public static DatasourceManager getInstance() {
        return ourInstance;
    }

    public void setMasterRouteOnly(){
        HintManager.getInstance().setMasterRouteOnly();
    }

    public void unSetMasterRouteOnly(){
        HintManager.getInstance().close();
    }
}
