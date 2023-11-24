package name.yzhu.ydal.jdbc;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import name.yzhu.ydal.YdalUtil;
import name.yzhu.ydal.enums.SqlType;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public abstract class SqlMetrcis {
    private static final Pattern selectPattern = Pattern.compile("^.*?SELECT\\s+.+?FROM.+$", Pattern.CASE_INSENSITIVE| Pattern.DOTALL);
    private static final Pattern insertPattern = Pattern.compile("^.*?INSERT\\s+INTO\\s+.+$", Pattern.CASE_INSENSITIVE| Pattern.DOTALL);
    private static final Pattern updatePattern = Pattern.compile("^.*?UPDATE\\s+.+?SET.+$", Pattern.CASE_INSENSITIVE| Pattern.DOTALL);
    private static final Pattern deletePattern = Pattern.compile("^.*?DELETE\\s+FROM\\s+.+$", Pattern.CASE_INSENSITIVE| Pattern.DOTALL);

    protected final String datasourceId;
    protected final String route;

    private MeterRegistry meterRegistry;

    public SqlMetrcis(String datasourceId, String route) {
        this.datasourceId = datasourceId;
        this.route = route;
        try {
            meterRegistry = YdalUtil.getBean(MeterRegistry.class);
        }
        catch (Exception e){
            log.error(e.getMessage()+", no find bean: MetricsReportor", e);
        }
    }


    protected <T> T callSql(Callable<T> c, SqlType sqlType) throws Exception {
        if (meterRegistry == null){
            log.error("metricsReportor is null");
            return c.call();
        }

        Map<String, String> labels = new HashMap<>();
        labels.put("datasourceId", datasourceId);
        labels.put("route", route);
        labels.put("type", sqlType.getTxt());

        return elapse("sql_execute_time", labels, () -> c.call());
    }

    protected SqlType parseSqlType(String sql){
        Matcher matcher = insertPattern.matcher(sql);
        if (matcher.matches()) {
            return SqlType.INSERT;
        }

        matcher = updatePattern.matcher(sql);
        if (matcher.matches()) {
            return SqlType.UPDATE;
        }

        matcher = deletePattern.matcher(sql);
        if (matcher.matches()) {
            return SqlType.DELETE;
        }

        matcher = selectPattern.matcher(sql);
        if (matcher.matches()) {
            return SqlType.SELECT;
        }

        log.warn("unknow sql type: "+sql);
        return SqlType.UNKNOWN;
    }

    private <T> T elapse(String name, Map<String, String> labels, Callable<T> callable) {
        List<Tag> listTag = buildTags(labels);
        long cost = 0;
        T obj;
        long begin = meterRegistry.config().clock().monotonicTime();
        try {
            obj = callable.call();
            cost = meterRegistry.config().clock().monotonicTime() - begin;
        } catch (Exception e) {
            cost = meterRegistry.config().clock().monotonicTime() - begin;
            throw new RuntimeException(e);
        }
        finally {
            Timer.builder(name).description(name).tags(listTag).register(meterRegistry).record(cost, TimeUnit.NANOSECONDS);
        }

        return obj;
    }

    private List<Tag> buildTags(Map<String, String> labels) {
        List<Tag> listTag = new ArrayList<>();
        try {
            if (!CollectionUtils.isEmpty(labels)) {
                for (Map.Entry<String, String> entry : labels.entrySet()) {
                    listTag.add(Tag.of(entry.getKey(), entry.getValue()));
                }
            }
        }
        catch (Exception e){
            log.error(e.getMessage(), e);
        }
        return listTag;
    }
}
