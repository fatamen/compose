package tw.com.ispan.controller;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tw.com.ispan.domain.ProductBean;
import tw.com.ispan.service.ProductService;

@RestController
@RequestMapping("/rest/pages/products")
public class ProductRestController {
    @Autowired
    private ProductService productService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ProductBean bean) {
        if(bean!=null) {
            Integer id = bean.getId();
            if(id!=null && !productService.exists(id)) {
                ProductBean product = productService.insert(bean);
                if(product!=null) {
                    // 成功：201 (Created)、message body包含新增成功的resource資料、Location header是新增成功resource的URL
                    URI uri = URI.create("http://localhost:8080/rest/pages/products/"+product.getId());
                    ResponseEntity<ProductBean> response = ResponseEntity.created(uri).body(product);
                    return response;
                }
            }
        }

        // 失敗：204 (No Content)
        ResponseEntity<Void> response = ResponseEntity.noContent().build();
        return response;
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> modify(@PathVariable Integer id, @RequestBody String body) {
        if(id!=null && productService.exists(id)) {
            ProductBean product = productService.modify(body);
            if(product!=null) {
                // 成功(resource存在、替換成功)：200 (OK)、message body包含修改(整體替換)過的resource資料
                return ResponseEntity.ok(product);
            }
        }

        // 失敗(resource不存在、不新增resource)：404 (Not Found)
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(@PathVariable Integer id) {
        if(id!=null && productService.exists(id)) {
            if(productService.remove(id)) {
                // 成功：204 (No Content)、message body空白(不包含已經刪除的resource資料)
                return ResponseEntity.noContent().build();
            }
        }

        // 失敗(resource不存在)：404 (Not Found)
        return ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<?> findAll() {
        List<ProductBean> products = productService.findAll();
        if(products!=null && !products.isEmpty() ) {
            // 成功：200 (OK)、message body包含所有resource資料
            return ResponseEntity.ok(products);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{pk}")
    public ResponseEntity<?> findById(@PathVariable("pk") Integer id) {
        if(id!=null && productService.exists(id)) {
            // 成功：200 (OK)、message body包含1個resource的資料
            ProductBean product = productService.findById(id);
            return ResponseEntity.ok(product);
        }

        // 失敗(resource不存在)：404 (Not Found)
        return ResponseEntity.notFound().build();
    }
}
