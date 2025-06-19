package tw.com.ispan;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import tw.com.ispan.jwt.JsonWebTokenUtility;

@SpringBootTest
class LabbootJpaApplicationTests {

	@Test
	void contextLoads() {
	}

	@Autowired
	private JsonWebTokenUtility jsonWebTokenUtility;
	
	@Test
	public void testJwt() {
		String token = jsonWebTokenUtility.createToken("測試");
		System.out.println("token="+token);
		
		String result = jsonWebTokenUtility.validateToken(token);
		System.out.println("result="+result);
	}
}
