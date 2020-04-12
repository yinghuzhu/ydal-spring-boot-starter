package name.yzhu.ydal.config;

import org.springframework.boot.autoconfigure.AutoConfigurationImportFilter;
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class DalAutoConfigurationImportFilter implements AutoConfigurationImportFilter {
    private static final Set<String> SHOULD_SKIP = new HashSet<>(
            Arrays.asList("org.apache.shardingsphere.shardingjdbc.spring.boot.SpringBootConfiguration"));
    @Override
    public boolean[] match(String[] autoConfigurationClasses, AutoConfigurationMetadata autoConfigurationMetadata) {
        boolean[] matches = new boolean[autoConfigurationClasses.length];

        for(int i = 0; i< autoConfigurationClasses.length; i++) {
            matches[i] = !SHOULD_SKIP.contains(autoConfigurationClasses[i]);
        }
        return matches;
    }
}
