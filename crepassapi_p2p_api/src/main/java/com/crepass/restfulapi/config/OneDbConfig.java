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
@MapperScan(value = "com.crepass.restfulapi.one.dao", sqlSessionFactoryRef = "oneSqlSessionFactory")
@EnableTransactionManagement
public class OneDbConfig {

    @Bean(name = "oneDataSource", destroyMethod = "close")  
    @ConfigurationProperties(prefix = "one.datasource")
    public DataSource oneDataSource() {
        return DataSourceBuilder.create().build();
    }
    
    @Bean(name = "oneTransactionManager") 
    public PlatformTransactionManager oneTransactionManager(@Qualifier("oneDataSource") DataSource oneDataSource) 
    { 
        return new DataSourceTransactionManager(oneDataSource); 
    }

    @Bean(name = "oneSqlSessionFactory")
    public SqlSessionFactory oneSqlSessionFactory(@Qualifier("oneDataSource") DataSource oneDataSource, ApplicationContext applicationContext) throws Exception {

        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(oneDataSource);
        sqlSessionFactoryBean.setTypeAliasesPackage("com.crepass.restfulapi.one.domain");
        sqlSessionFactoryBean.setMapperLocations(applicationContext.getResources("classpath:sql/mybatis/mapper/one/*.xml"));
        return sqlSessionFactoryBean.getObject();
    } 

    @Bean(name = "oneSqlSessionTemplate")
    public SqlSessionTemplate oneSqlSessionTemplate(SqlSessionFactory oneSqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(oneSqlSessionFactory);
    }
    
}
