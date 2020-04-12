package name.yzhu.ydal;

import name.yzhu.ydal.config.DalConfigurationProperties;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.config.masterslave.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.core.config.inline.InlineExpressionParser;
import org.apache.shardingsphere.core.exception.ShardingException;
import org.apache.shardingsphere.core.yaml.config.masterslave.YamlMasterSlaveRuleConfiguration;
import org.apache.shardingsphere.core.yaml.swapper.impl.MasterSlaveRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.shardingjdbc.api.MasterSlaveDataSourceFactory;
import org.apache.shardingsphere.spring.boot.datasource.DataSourcePropertiesSetter;
import org.apache.shardingsphere.spring.boot.datasource.DataSourcePropertiesSetterHolder;
import org.apache.shardingsphere.spring.boot.util.DataSourceUtil;
import org.apache.shardingsphere.spring.boot.util.PropertyUtil;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @author yhzhu
 */
@Slf4j
public class DalDatasourceFactory implements EnvironmentAware {
    private final Map<String, Map<String, DataSource>> dataSourceMap = new LinkedHashMap<>();
    private final Map<String, YamlMasterSlaveRuleConfiguration> masterSlaveMap = new LinkedHashMap<>();
    private final Map<String, Properties> propertiesMap = new LinkedHashMap<>();
    private final DalConfigurationProperties dalConfigurationProperties;

    public DalDatasourceFactory(DalConfigurationProperties dalConfigurationProperties) {
        this.dalConfigurationProperties = dalConfigurationProperties;
    }

    public DataSource getMasterSlaveDataSource(String name) throws SQLException {
        Map<String, DataSource> ds = dataSourceMap.get(name);
        Properties props = propertiesMap.get(name);
        YamlMasterSlaveRuleConfiguration masterSlaveRule = masterSlaveMap.get(name);
        MasterSlaveRuleConfiguration msConfig = new MasterSlaveRuleConfigurationYamlSwapper().swap(masterSlaveRule);
        return MasterSlaveDataSourceFactory.createDataSource(ds, msConfig, props);
    }

    @Override
    public void setEnvironment(Environment environment) {
        String prefix = "spring.ydal";

        for (String each : getDatasourceList(environment, prefix)) {
            if ("props".equals(each)){
                continue;
            }
            String dsPrefix = prefix+"."+each+".datasource.";
            Map<String, DataSource> map = new LinkedHashMap<>();
            dataSourceMap.put(each, map);
            for (String dsName: getDataSourceNames(environment, dsPrefix)){
                try {
                    DataSource datasource = getDataSource(environment, dsPrefix, dsName);
                    map.put(dsName, datasource);
                } catch (ReflectiveOperationException e) {
                    throw new ShardingException("Can't find datasource type!", e);
                }
            }

            String msPrefix = prefix+"."+each+".masterslave.";
            YamlMasterSlaveRuleConfiguration masterSlaveRuleConfiguration = PropertyUtil.handle(environment, msPrefix, YamlMasterSlaveRuleConfiguration.class);
            masterSlaveMap.put(each, masterSlaveRuleConfiguration);

            String propsPrefix = prefix+"."+each+".props.";
            Properties props = dalConfigurationProperties.getProps();
            try{
                Properties dsProps = PropertyUtil.handle(environment, propsPrefix, Properties.class);
                props.putAll(dsProps);
            }
            catch(Exception e){
                if (e instanceof ReflectiveOperationException){
                    // no find the props for datasource
                }
                else{
                    log.error(e.getMessage(), e);
                }

            }

            propertiesMap.put(each, props);
        }

    }

    private DataSource getDataSource(final Environment environment, final String prefix, final String dataSourceName) throws ReflectiveOperationException {
        Map<String, Object> dataSourceProps = PropertyUtil.handle(environment, prefix + dataSourceName.trim(), Map.class);
        Preconditions.checkState(!dataSourceProps.isEmpty(), "Wrong datasource properties!");

        DataSource result = DataSourceUtil.getDataSource(dataSourceProps.get("type").toString(), dataSourceProps);
        Optional<DataSourcePropertiesSetter> dataSourcePropertiesSetter = DataSourcePropertiesSetterHolder.getDataSourcePropertiesSetterByType(dataSourceProps.get("type").toString());
        if (dataSourcePropertiesSetter.isPresent()) {
            dataSourcePropertiesSetter.get().propertiesSet(environment, prefix, dataSourceName, result);
        }
        return result;
    }

    private Set<String> getDatasourceList(final Environment environment, final String prefix){
        Map<String, Object> map = PropertyUtil.handle(environment, prefix , Map.class);

        return map.keySet();
    }
    private List<String> getDataSourceNames(final Environment environment, final String prefix) {
        StandardEnvironment standardEnv = (StandardEnvironment) environment;
        standardEnv.setIgnoreUnresolvableNestedPlaceholders(true);
        return null == standardEnv.getProperty(prefix + "name")
                ? new InlineExpressionParser(standardEnv.getProperty(prefix + "names")).splitAndEvaluate() : Collections.singletonList(standardEnv.getProperty(prefix + "name"));
    }
}
