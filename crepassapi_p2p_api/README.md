# crePASS API 

## Sample URL

### mybatis Version
- http://localhost:8900/api/creone/7
- http://localhost:8900/api/cities
- http://localhost:8900/api/cities/7

- curl --noproxy localhost -u user:user -v http://localhost:8900/api/cities

### API Documents 
http://localhost:8900/swagger-ui.html


## Prerequisites
- JDK 1.8
- Maven 3+
- MySQL 5.6+
- Spring Boot 1.5.2.RELEASE
- mybatis 1.3.1
- swagger2 2.7.0 
 

## Stack
- Spring Boot
- Spring Data REST
- MySQL
- Lombok
- mybatis
- swagger2


## Run
`mvn clean spring-boot:run`


## Create deploy files 
`mvn clean package`
- location : project folder > target > *.jar

