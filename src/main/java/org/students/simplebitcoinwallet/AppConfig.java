package org.students.simplebitcoinwallet;

import jakarta.validation.Validator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
public class AppConfig {
    @Bean
    public Validator defaultValidator() {
        return new LocalValidatorFactoryBean();
    }
}