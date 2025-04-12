package cn.hdj.springcircularreferences;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@Slf4j
@EnableAsync
public class SpringCircularReferencesApplication {


	public static void main(String[] args) {
		SpringApplication.run(SpringCircularReferencesApplication.class, args);

		log.info("111111111111111");
	}

}
