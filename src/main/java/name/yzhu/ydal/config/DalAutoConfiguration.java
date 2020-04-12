package name.yzhu.ydal.config;

import name.yzhu.ydal.DalDatasourceFactory;
import name.yzhu.ydal.aop.MasterRouteAspect;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * @author yhzhu
 */
@ConditionalOnProperty(
        prefix = "spring.ydal",
        name = {"enabled"},
        havingValue = "true",
        matchIfMissing = true
)
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
public class DalAutoConfiguration  {

    @Bean
    public DalConfigurationProperties dalConfigurationProperties(){
        return new DalConfigurationProperties();
    }

    @Bean
    public DalDatasourceFactory dalDatasourceFactory(DalConfigurationProperties properties){
        return new DalDatasourceFactory(properties);
    }

    @Bean
    public MasterRouteAspect masterRouteAspect(){
        return new MasterRouteAspect();
    }
}
