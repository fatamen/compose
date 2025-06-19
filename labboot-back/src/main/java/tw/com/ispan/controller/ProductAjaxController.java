package tw.com.ispan.controller;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tw.com.ispan.domain.ProductBean;
import tw.com.ispan.dto.ProductResponse;
import tw.com.ispan.service.ProductService;
import tw.com.ispan.util.DatetimeConverter;


@RestController
@RequestMapping("/ajax/pages/products")
public class ProductAjaxController {
    @Autowired
    private ProductService productService;

    @PostMapping
    public ProductResponse create(@RequestBody ProductBean bean) {
        ProductResponse response = new ProductResponse();
        if(bean==null) {
            response.setSuccess(false);
            response.setMessage("資料錯誤");
        } else {
            Integer id = bean.getId();
            if(id==null) {
                response.setSuccess(false);
                response.setMessage("Id是必要欄位");
    
            } else if(productService.exists(id)) {
                response.setSuccess(false);
                response.setMessage("Id已存在");
    
            } else {
                ProductBean product = productService.insert(bean);
                if(product==null) {
                    response.setSuccess(false);
                    response.setMessage("新增失敗");
                } else {
                    response.setSuccess(true);
                    response.setMessage("新增成功");
                }
            }
        }
        return response;
    }

    @PutMapping("/{id}")
    public ProductResponse modify(@PathVariable Integer id, @RequestBody String body) {
        ProductResponse response = new ProductResponse();

        if(id==null) {
            response.setSuccess(false);
            response.setMessage("Id是必要欄位");

        } else if(!productService.exists(id)) {
            response.setSuccess(false);
            response.setMessage("Id不存在");

        } else {
            ProductBean product = productService.modify(body);
            if(product==null) {
                response.setSuccess(false);
                response.setMessage("修改失敗");
            } else {
                response.setSuccess(true);
                response.setMessage("修改成功");
            }
        }
        return response;
    }

    @DeleteMapping("/{id}")
    public ProductResponse remove(@PathVariable Integer id) {
        ProductResponse response = new ProductResponse();

        if(id==null) {
            response.setSuccess(false);
            response.setMessage("Id是必要欄位");

        } else if(!productService.exists(id)) {
            response.setSuccess(false);
            response.setMessage("Id不存在");

        } else {
            if(!productService.remove(id)) {
                response.setSuccess(false);
                response.setMessage("刪除失敗");
            } else {
                response.setSuccess(true);
                response.setMessage("刪除成功");
            }
        }
        return response;
    }

    @PostMapping("/find")
    public String find(@RequestBody String json) {
        JSONObject responseJson = new JSONObject();

        long count = productService.count(json);
        responseJson.put("count", count);

        JSONArray array = new JSONArray();
        List<ProductBean> products = productService.find(json);
        if(products!=null && !products.isEmpty()) {
            for(ProductBean product : products) {
                String make = DatetimeConverter.toString(product.getMake(), "yyyy-MM-dd");

                JSONObject item = new JSONObject()
                    .put("id", product.getId())
                    .put("name", product.getName())
                    .put("price", product.getPrice())
                    .put("make", make)
                    .put("expire", product.getExpire());
                array.put(item);
            }
        }

        responseJson.put("list", array);
        return responseJson.toString();
    }

    @GetMapping("/{pk}")
    public ProductResponse findById(@PathVariable("pk") Integer id) {
        System.out.println("findById: ");
        ProductResponse response = new ProductResponse();
        response.setList(new ArrayList<>());
        if(id!=null) {
            ProductBean product = productService.findById(id);
            if(product!=null) {
                List<ProductBean> array = new ArrayList<>();
                array.add(product);

                response.setList(array);
            }
        }
        return response;
    }
}
