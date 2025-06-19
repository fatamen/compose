package tw.com.ispan.service;

import java.util.List;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import tw.com.ispan.domain.ProductBean;

@SpringBootTest
public class ProductServiceTests {
    
    @Autowired
    private ProductService productService;

    @Test
    public void testFind() {
		JSONObject obj = new JSONObject()
				.put("name", "a")
				.put("start", 0)
				.put("rows", 2)
				.put("order", "price")
				.put("dir", true);
        
        List<ProductBean> find = productService.find(obj.toString());
		System.out.println("find="+find);
    }

    @Test
    public void testCount() {
		JSONObject obj = new JSONObject()
				.put("name", "a")
				.put("start", 0)
				.put("rows", 2)
				.put("order", "price")
				.put("dir", true);
        
                long count = productService.count(obj.toString());
                System.out.println("count="+count);
    }
}
