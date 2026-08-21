package uz.cib.mobile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import uz.cib.mobile.config.AppProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class CibMobileApplication {
    public static void main(String[] args) {
        SpringApplication.run(CibMobileApplication.class, args);
    }
}
