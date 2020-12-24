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
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@MapperScan(value = "com.crepass.restfulapi.cre.dao", sqlSessionFactoryRef = "creSqlSessionFactory")
@EnableTransactionManagement
public class CreDbConfig {

    @Bean(name = "creDataSource", destroyMethod = "close")
    @Primary
    @ConfigurationProperties(prefix = "cre.datasource")
    public DataSource creDataSource() {
        return DataSourceBuilder.create().build();
    }
    
    @Bean(name = "creSqlSessionFactory")
    @Primary
    public SqlSessionFactory creSqlSessionFactory(@Qualifier("creDataSource") DataSource creDataSource, ApplicationContext applicationContext) throws Exception {

        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(creDataSource);
        sqlSessionFactoryBean.setTypeAliasesPackage("com.crepass.restfulapi.cre.domain");
        sqlSessionFactoryBean.setMapperLocations(applicationContext.getResources("classpath:sql/mybatis/mapper/cre/*.xml"));
        return sqlSessionFactoryBean.getObject();
    } 

    @Bean(name = "creSqlSessionTemplate")
    @Primary
    public SqlSessionTemplate creSqlSessionTemplate(SqlSessionFactory creSqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(creSqlSessionFactory);
    }
    
}