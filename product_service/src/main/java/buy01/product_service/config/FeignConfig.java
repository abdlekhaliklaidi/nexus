package buy01.product_service.config;

import feign.codec.Encoder;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import feign.form.spring.SpringFormEncoder;

@Configuration
public class FeignConfig {

    @Bean
    public Encoder feignFormEncoder() {
        return new SpringFormEncoder(
                new SpringEncoder(HttpMessageConverters::new)
        );
    }
}