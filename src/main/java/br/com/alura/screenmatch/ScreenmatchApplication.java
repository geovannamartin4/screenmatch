package br.com.alura.screenmatch;

import br.com.alura.screenmatch.service.ConsumoApi;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ScreenmatchApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(ScreenmatchApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		ConsumoApi consumoApi = new ConsumoApi();
		String json = consumoApi.obterDados("https://www.omdbapi.com/?apikey=5cd120e1&t=Friends&");
		System.out.println(json);
	}
}
