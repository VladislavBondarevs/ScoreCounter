package game.scorecounter;

import game.scorecounter.storage.StorageProperties;
import game.scorecounter.storage.StorageService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class ScorecounterApplication {

	public static void main(String[] args) {
		SpringApplication.run(ScorecounterApplication.class, args);
	}


	@Bean
	CommandLineRunner init(StorageService storageService){
		return (args) ->{
			storageService.init();
		};
	}
}
