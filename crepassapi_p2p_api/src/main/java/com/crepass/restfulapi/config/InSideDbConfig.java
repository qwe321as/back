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
@MapperScan(value = "com.crepass.restfulapi.inside.dao", sqlSessionFactoryRef = "insideSqlSessionFactory")
@EnableTransactionManagement
public class InSideDbConfig {

    @Bean(name = "insideDataSource", destroyMethod = "close")  
    @ConfigurationProperties(prefix = "inside.datasource")
    public DataSource insideDataSource() {
        return DataSourceBuilder.create().build();
    }
    
    @Bean(name = "insideTransactionManager") 
    public PlatformTransactionManager insideTransactionManager(@Qualifier("insideDataSource") DataSource insideDataSource) 
    { 
        return new DataSourceTransactionManager(insideDataSource); 
    }

    @Bean(name = "insideSqlSessionFactory")
    public SqlSessionFactory insideSqlSessionFactory(@Qualifier("insideDataSource") DataSource insideDataSource, ApplicationContext applicationContext) throws Exception {

        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(insideDataSource);
        sqlSessionFactoryBean.setTypeAliasesPackage("com.crepass.restfulapi.inside.domain");
        sqlSessionFactoryBean.setMapperLocations(applicationContext.getResources("classpath:sql/mybatis/mapper/inside/*.xml"));
        return sqlSessionFactoryBean.getObject();
    } 

    @Bean(name = "insideSqlSessionTemplate")
    public SqlSessionTemplate insideSqlSessionTemplate(SqlSessionFactory insideSqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(insideSqlSessionFactory);
    }
    
}
