package pl.eg.enginegame;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Collections;

@SpringBootApplication
@EnableSwagger2
public class EngineGameApp {
	private static final Logger LOGGER=LoggerFactory.getLogger(EngineGameApp.class);
	@Autowired

	public static void main(String[] args) {
		SpringApplication.run(EngineGameApp.class, args);
		LOGGER.info("\n\t:+> Engine game server started :-)");
	}
	@Bean
	public Docket get() {
		return new Docket(DocumentationType.SWAGGER_2)
				.select()
				.paths(PathSelectors.any())
				.apis(RequestHandlerSelectors.basePackage("pl.eg.enginegame"))
				.build()
				.apiInfo(createApiInfo());
	}

	private ApiInfo createApiInfo() {
		return new ApiInfo("GameServer API",
				"GameServer",
				"1.00",
				"http://gameserver.pl",
				new Contact("GameServer", "http://gameserver.pl", "game@server.pl"),
				"Licence",
				"http://gameserver.pl",
				Collections.emptyList()
		);
	}

}
