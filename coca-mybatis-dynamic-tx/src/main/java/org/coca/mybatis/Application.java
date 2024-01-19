package org.coca.mybatis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.concurrent.CountDownLatch;

@Slf4j
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableTransactionManagement
public class Application {
    private static final CountDownLatch closeLatch = new CountDownLatch(1);
    public static void main(String[] args) throws InterruptedException{
        SpringApplication.run(Application.class, args);
        closeLatch.await();
    }
}
