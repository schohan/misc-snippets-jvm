package ssc

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext


/**
 * Created by schohan on 7/15/2015.
 */
@SpringBootApplication
class Application {

    public static void main(String[] args) {
        ConfigurableApplicationContext appContext = SpringApplication.run(Application.class, args)

    }
}
