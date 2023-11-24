# ydal-spring-boot-starter
Sharding-Jdbc(Shardingsphere)多数据库与springboot集成

## Quick Start

### springboot集成 

注入DalDatasourceFactory

```
    @Autowired
    private DalDatasourceFactory dalDatasourceFactory;
```

生成order库的Datasource

```
    @Bean(name = "orderDataSource")
    public DataSource getDataSource() throws SQLException {
        return dalDatasourceFactory.getMasterSlaveDataSource("order");
    }
```

生成orderHis的Datasource

```
    @Bean(name = "orderHisDataSource")
    public DataSource getDataSource() throws SQLException {
        return dalDatasourceFactory.getMasterSlaveDataSource("orderHis");
    }
```

### 生成prometheus监控 SQL metrics
* 例如:
```sql
# HELP sql_execute_time_seconds sql_execute_time
# TYPE sql_execute_time_seconds summary
sql_execute_time_seconds_count{route="slave",datasource="order",type="select",} 82.0
sql_execute_time_seconds_sum{route="slave",datasource="order",type="select",} 12.373654891
sql_execute_time_seconds_count{route="master",datasource="order",type="select",} 14.0
sql_execute_time_seconds_sum{route="master",datasource="order",type="select",} 167.934782891
sql_execute_time_seconds_count{route="master",datasource="order",type="update",} 1.0
sql_execute_time_seconds_sum{route="master",datasource="order",type="update",} 0.493257718

```



### properties 配置
以order和orderHis两数据库为例，数据库连接池采用HikariDataSource

```
spring.ydal.props.sql.show=true

spring.ydal.order.masterslave.name=ms
spring.ydal.order.masterslave.master-data-source-name=master
spring.ydal.order.masterslave.slave-data-source-names=slave0,slave1
spring.ydal.order.masterslave.load-balance-algorithm-type=round_robin
spring.ydal.order.datasource.names=master,slave0,slave1

spring.ydal.order.datasource.master.jdbcUrl=jdbc:mysql://master.order.db/order?useUnicode=true&characterEncoding=UTF-8
spring.ydal.order.datasource.master.username=root
spring.ydal.order.datasource.master.password=123
spring.ydal.order.datasource.master.connectionTimeout=10000
spring.ydal.order.datasource.master.maximumPoolSize=10
spring.ydal.order.datasource.master.minimumIdle=5
spring.ydal.order.datasource.master.idleTimeout=300000
spring.ydal.order.datasource.master.maxLifetime=1200000
spring.ydal.order.datasource.master.poolName=order-db-pool-master

spring.ydal.order.datasource.slave0.jdbcUrl=jdbc:mysql://slave0.order.db/order?useUnicode=true&characterEncoding=UTF-8
spring.ydal.order.datasource.slave0.username=root
spring.ydal.order.datasource.slave0.password=123
spring.ydal.order.datasource.slave0.connectionTimeout=10000
spring.ydal.order.datasource.slave0.maximumPoolSize=10
spring.ydal.order.datasource.slave0.minimumIdle=5
spring.ydal.order.datasource.slave0.idleTimeout=300000
spring.ydal.order.datasource.slave0.maxLifetime=1200000
spring.ydal.order.datasource.slave0.poolName=order-db-pool-slave0

spring.ydal.order.datasource.slave1.jdbcUrl=jdbc:mysql://slave1.order.db/order?useUnicode=true&characterEncoding=UTF-8
spring.ydal.order.datasource.slave1.username=root
spring.ydal.order.datasource.slave1.password=123
spring.ydal.order.datasource.slave1.connectionTimeout=10000
spring.ydal.order.datasource.slave1.maximumPoolSize=10
spring.ydal.order.datasource.slave1.minimumIdle=5
spring.ydal.order.datasource.slave1.idleTimeout=300000
spring.ydal.order.datasource.slave1.maxLifetime=1200000
spring.ydal.order.datasource.slave1.poolName=order-db-pool-slave1


spring.ydal.orderHis.masterslave.name=ms
spring.ydal.orderHis.masterslave.master-data-source-name=master
spring.ydal.orderHis.masterslave.slave-data-source-names=slave0,slave1
spring.ydal.orderHis.masterslave.load-balance-algorithm-type=round_robin
spring.ydal.orderHis.datasource.names=master,slave0,slave1

spring.ydal.orderHis.datasource.master.jdbcUrl=jdbc:mysql://master.order.db/orderHis?useUnicode=true&characterEncoding=UTF-8
spring.ydal.orderHis.datasource.master.username=root
spring.ydal.orderHis.datasource.master.password=123
spring.ydal.orderHis.datasource.master.connectionTimeout=10000
spring.ydal.orderHis.datasource.master.maximumPoolSize=10
spring.ydal.orderHis.datasource.master.minimumIdle=5
spring.ydal.orderHis.datasource.master.idleTimeout=300000
spring.ydal.orderHis.datasource.master.maxLifetime=1200000
spring.ydal.orderHis.datasource.master.poolName=orderHis-db-pool-master

spring.ydal.orderHis.datasource.slave0.jdbcUrl=jdbc:mysql://slave0.order.db/orderHis?useUnicode=true&characterEncoding=UTF-8
spring.ydal.orderHis.datasource.slave0.username=root
spring.ydal.orderHis.datasource.slave0.password=123
spring.ydal.orderHis.datasource.slave0.connectionTimeout=10000
spring.ydal.orderHis.datasource.slave0.maximumPoolSize=10
spring.ydal.orderHis.datasource.slave0.minimumIdle=5
spring.ydal.orderHis.datasource.slave0.idleTimeout=300000
spring.ydal.orderHis.datasource.slave0.maxLifetime=1200000
spring.ydal.orderHis.datasource.slave0.poolName=orderHis-db-pool-slave0

spring.ydal.orderHis.datasource.slave1.jdbcUrl=jdbc:mysql://slave1.order.db/orderHis?useUnicode=true&characterEncoding=UTF-8
spring.ydal.orderHis.datasource.slave1.username=root
spring.ydal.orderHis.datasource.slave1.password=123
spring.ydal.orderHis.datasource.slave1.connectionTimeout=10000
spring.ydal.orderHis.datasource.slave1.maximumPoolSize=10
spring.ydal.orderHis.datasource.slave1.minimumIdle=5
spring.ydal.orderHis.datasource.slave1.idleTimeout=300000
spring.ydal.orderHis.datasource.slave1.maxLifetime=1200000
spring.ydal.orderHis.datasource.slave1.poolName=orderHis-db-pool-slave1
```
