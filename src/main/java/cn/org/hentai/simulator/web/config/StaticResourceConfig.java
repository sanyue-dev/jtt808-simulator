package cn.org.hentai.simulator.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer
{
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry)
    {
        registry.addResourceHandler("/static/vendor/**")
                .addResourceLocations("classpath:/static/vendor/")
                .setCachePeriod((int) TimeUnit.DAYS.toSeconds(7));

        registry.addResourceHandler("/static/img/**")
                .addResourceLocations("classpath:/static/img/")
                .setCachePeriod((int) TimeUnit.DAYS.toSeconds(1));
    }
}
