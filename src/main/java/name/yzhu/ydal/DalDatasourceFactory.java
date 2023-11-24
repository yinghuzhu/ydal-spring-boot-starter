package name.yzhu.ydal;

import name.yzhu.ydal.config.DalConfigurationProperties;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import name.yzhu.ydal.enums.DatasourceRoute;
import name.yzhu.ydal.jdbc.DalMySqlDriver;
import org.apache.shardingsphere.api.config.masterslave.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.core.config.inline.InlineExpressionParser;
import org.apache.shardingsphere.core.exception.ShardingException;
import org.apache.shardingsphere.core.rule.MasterSlaveRule;
import org.apache.shardingsphere.core.yaml.config.masterslave.YamlMasterSlaveRuleConfiguration;
import org.apache.shardingsphere.core.yaml.swapper.impl.MasterSlaveRuleConfigurationYamlSwapper;
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
    private static final String DATASOURCE_TYPE = "type";
    private static final String DEFAULT_DATASOURCE_TYPE = DalConnectPoolDataSource.class.getCanonicalName(); //"com.zaxxer.hikari.HikariDataSource";
    private static final String DRIVER_CLASS_NAME = "driver-class-name";
    private static final String DEFAULT_DRIVER_CLASS_NAME = DalMySqlDriver.class.getCanonicalName(); // "com.mysql.cj.jdbc.Driver";
    private static final String DEFAULT_LOAD_BALACE_TYPE = "round_robin";
    private static final String MINI_POOL_SIZE = "minimumIdle";
    private static final Integer DEFAULT_MINI_POOL_SIZE = 5;
    private static final String MAX_POOL_SIZE = "maximumPoolSize";
    private static final Integer DEFAULT_MAX_POOL_SIZE = 10;

    private final Map<String, Map<String, DataSource>> dataSourceMap = new LinkedHashMap<>();
    private final Map<String, YamlMasterSlaveRuleConfiguration> masterSlaveMap = new LinkedHashMap<>();
    private final Map<String, Properties> propertiesMap = new LinkedHashMap<>();
    private final DalConfigurationProperties dalConfigurationProperties;

    public DalDatasourceFactory(DalConfigurationProperties dalConfigurationProperties) {
        this.dalConfigurationProperties = dalConfigurationProperties;
    }

    public DataSource getMasterSlaveDataSource(String name) throws SQLException {
        try {
            Map<String, DataSource> ds = dataSourceMap.get(name);
            Properties props = propertiesMap.get(name);
            YamlMasterSlaveRuleConfiguration masterSlaveRule = masterSlaveMap.get(name);
            if (masterSlaveRule.getName() == null){
                masterSlaveRule.setName(name+"MS");
            }
            if(masterSlaveRule.getLoadBalanceAlgorithmType() == null){
                masterSlaveRule.setLoadBalanceAlgorithmType(DEFAULT_LOAD_BALACE_TYPE);
            }
            MasterSlaveRuleConfiguration msConfig = new MasterSlaveRuleConfigurationYamlSwapper().swap(masterSlaveRule);
            return new DalMasterSlaveDataSource(ds, new MasterSlaveRule(msConfig), props);
        }
        catch (Exception e){
            throw new SQLException(name+" datasource error, "+e.getMessage(), e);
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        String prefix = getPropertiesPrefix();

        for (String each : getDatasourceList(environment, prefix)) {
            if ("props".equals(each)){
                continue;
            }
            if (!enableDataSource(environment, prefix+"."+each)){
                log.info("{} is disable", each);
                continue;
            }

            String msPrefix = prefix+"."+each+".masterslave.";
            YamlMasterSlaveRuleConfiguration masterSlaveRuleConfiguration = PropertyUtil.handle(environment, msPrefix, YamlMasterSlaveRuleConfiguration.class);
            masterSlaveMap.put(each, masterSlaveRuleConfiguration);

            String dsPrefix = prefix+"."+each+".datasource.";
            Map<String, DataSource> map = new LinkedHashMap<>();
            dataSourceMap.put(each, map);
            for (String dsName: getDataSourceNames(environment, dsPrefix)){
                try {
                    DataSource datasource = getDataSource(environment, dsPrefix, dsName, masterSlaveRuleConfiguration, each);
                    map.put(dsName, datasource);
                } catch (ReflectiveOperationException e) {
                    throw new ShardingException("Can't find datasource type!", e);
                }
            }


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

    protected String getPropertiesPrefix() {
        return "spring.eldal";
    }

    private DataSource getDataSource(final Environment environment, final String prefix, final String dataSourceName,
                                     YamlMasterSlaveRuleConfiguration masterSlaveRuleConfiguration, String datasourceId) throws ReflectiveOperationException {
        Map<String, Object> dataSourceProps = PropertyUtil.handle(environment, prefix + dataSourceName.trim(), Map.class);
        Preconditions.checkState(!dataSourceProps.isEmpty(), "Wrong datasource config!");

        dataSourceProps.put(DATASOURCE_TYPE, DEFAULT_DATASOURCE_TYPE);
        dataSourceProps.put(DRIVER_CLASS_NAME, DEFAULT_DRIVER_CLASS_NAME);
        if (!dataSourceProps.containsKey(MINI_POOL_SIZE)){
            dataSourceProps.put(MINI_POOL_SIZE, DEFAULT_MINI_POOL_SIZE);
        }

        if (!dataSourceProps.containsKey(MAX_POOL_SIZE)){
            dataSourceProps.put(MAX_POOL_SIZE, DEFAULT_MAX_POOL_SIZE);
        }

        DatasourceRoute route = DatasourceRoute.SLAVE;
        if (dataSourceName.equals(masterSlaveRuleConfiguration.getMasterDataSourceName())){
            route = DatasourceRoute.MASTER;
        }

        Properties dataSourceProperties = new Properties();
        dataSourceProperties.put("datasourceId", datasourceId);
        dataSourceProperties.put("route", route.getTxt());

        dataSourceProps.put("dataSourceProperties", dataSourceProperties);
        dataSourceProps.put("datasourceId", datasourceId);
        dataSourceProps.put("route", route.getTxt());
        DataSource result = DataSourceUtil.getDataSource(dataSourceProps.get(DATASOURCE_TYPE).toString(), dataSourceProps);
        if (result instanceof DalConnectPoolDataSource){
            DalConnectPoolDataSource ds = (DalConnectPoolDataSource) result;
            ds.setDatasourceId(datasourceId);
            ds.setRoute(route.getTxt());
            ds.setDataSourceProperties(dataSourceProperties);
        }
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

    private Boolean enableDataSource(Environment environment, String prefix) {
        String key = prefix+"."+"enable";
        try{
            return PropertyUtil.handle(environment, key, Boolean.class);
        }
        catch (Exception e){
            return Boolean.TRUE;
        }
    }
}
