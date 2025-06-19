package tw.com.ispan.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class PokiApiServiceTests {
    
    @Autowired
    private PokeApiService pokiApiService;
    
    @Test
    public void testGetPokemon() {
        
        pokiApiService.getPokemonInfo("Charizard");


    }

}
