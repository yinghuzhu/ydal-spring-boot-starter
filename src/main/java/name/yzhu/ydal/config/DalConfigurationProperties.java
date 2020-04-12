package name.yzhu.ydal.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Properties;

/**
 * @author yhzhu
 */
@Getter @Setter
@ConfigurationProperties(prefix = "spring.ydal")
public class DalConfigurationProperties {
    private Properties props = new Properties();
}
