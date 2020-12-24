package com.crepass.restfulapi.config;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@MapperScan(value = "com.crepass.restfulapi.ks.dao", sqlSessionFactoryRef = "ksSqlSessionFactory")
@EnableTransactionManagement
public class KSDbConfig {

    @Bean(name = "ksDataSource", destroyMethod = "close")  
    @ConfigurationProperties(prefix = "ks.datasource")
    public DataSource ksDataSource() {
        return DataSourceBuilder.create().build();
    }
    
    @Bean(name = "ksTransactionManager") 
    public PlatformTransactionManager ksTransactionManager(@Qualifier("ksDataSource") DataSource ksDataSource) 
    { 
        return new DataSourceTransactionManager(ksDataSource); 
    }

    @Bean(name = "ksSqlSessionFactory")
    public SqlSessionFactory ksSqlSessionFactory(@Qualifier("ksDataSource") DataSource ksDataSource, ApplicationContext applicationContext) throws Exception {

        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(ksDataSource);
        sqlSessionFactoryBean.setTypeAliasesPackage("com.crepass.restfulapi.ks.domain");
        sqlSessionFactoryBean.setMapperLocations(applicationContext.getResources("classpath:sql/mybatis/mapper/ks/*.xml"));
        return sqlSessionFactoryBean.getObject();
    } 

    @Bean(name = "ksSqlSessionTemplate")
    public SqlSessionTemplate ksSqlSessionTemplate(SqlSessionFactory ksSqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(ksSqlSessionFactory);
    }
    
}
