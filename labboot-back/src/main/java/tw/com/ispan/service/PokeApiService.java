package tw.com.ispan.service;

import java.net.URI;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PokeApiService {
    private static final String POKE_API_URL = "https://pokeapi.co/api/v2/pokemon/";
    private RestTemplate template = new RestTemplate();
    public void getPokemonInfo(String pokemonName) {
        URI uri = URI.create(POKE_API_URL+pokemonName);
        RequestEntity<Void> request = RequestEntity.get(uri).build();
        try {
            ResponseEntity<String> response = template.exchange(request, String.class);
            HttpStatusCode code = response.getStatusCode();
            System.out.println("status code : "+ code);

            String body = response.getBody();
            System.out.println("body : "+ body);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
