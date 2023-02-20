package cn.hdj.oom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
@ServletComponentScan(basePackageClasses={OomAnalysisApplication.class})
public class OomAnalysisApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(OomAnalysisApplication.class, args);
    }


    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(OomAnalysisApplication.class);
    }
}
