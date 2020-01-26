package config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


@Configuration
@ComponentScan(basePackages = {"filterandsorting","querybuilder"})
public class FASAutoConfiguration {

}
