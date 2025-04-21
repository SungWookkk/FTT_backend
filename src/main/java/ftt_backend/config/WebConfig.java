package ftt_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/ws/**")
                        .allowedOrigins("http://localhost:3000")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .exposedHeaders("Authorization")
                        .allowCredentials(true);
            }

            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                // React build 결과물
                registry
                        .addResourceHandler("/static/**")
                        .addResourceLocations("classpath:/static/static/");

                // 업로드 파일
                registry
                        .addResourceHandler("/uploads/**")
                        .addResourceLocations("file:uploads/",
                                "file:" + System.getProperty("user.dir") + "/uploads/");
                // **루트 요청**(/, /index.html)을 index.html 로 서빙하도록 명시
                registry
                        .addResourceHandler("/", "/index.html")
                        .addResourceLocations("classpath:/static/index.html")
                        .setCachePeriod(0);
            }
        };
    }
}