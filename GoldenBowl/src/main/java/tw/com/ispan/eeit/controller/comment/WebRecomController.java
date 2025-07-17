package tw.com.ispan.eeit.controller.comment;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tw.com.ispan.eeit.model.entity.comment.WebRecomBean;
import tw.com.ispan.eeit.service.comment.WebRecomService;

@RestController
@RequestMapping("/api/web-recom")
public class WebRecomController {
    @Autowired
    private WebRecomService webRecomService;

    // 創建TAG
    @PostMapping
    public ResponseEntity<WebRecomBean> createOrUpdate(@RequestBody WebRecomBean webRecomBean) {
        try {
            WebRecomBean savedBean = webRecomService.save(webRecomBean);
            return ResponseEntity.ok(savedBean);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // 更新TAG
    @PutMapping("/{id}")
    public ResponseEntity<WebRecomBean> update(@PathVariable Integer id, @RequestBody WebRecomBean webRecom) {
        try {
            WebRecomBean updated = webRecomService.update(id, webRecom);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    // 根據 ID 查詢單筆記錄
    @GetMapping("/{id}")
    public ResponseEntity<WebRecomBean> getById(@PathVariable Integer id) {
        Optional<WebRecomBean> result = webRecomService.findById(id);
        return result.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 查詢所有記錄
    @GetMapping
    public ResponseEntity<List<WebRecomBean>> getAll() {
        List<WebRecomBean> results = webRecomService.findAll();
        return ResponseEntity.ok(results);
    }

    // 根據 tag 查詢記錄
    @GetMapping("/tag/{tag}")
    public ResponseEntity<List<WebRecomBean>> getByTag(@PathVariable String tag) {
        List<WebRecomBean> results = webRecomService.findByTag(tag);
        return ResponseEntity.ok(results);
    }

    // 刪除記錄
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Integer id) {
        try {
            webRecomService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 批量更新
    @PostMapping("/batch-update")
    public ResponseEntity<List<WebRecomBean>> batchUpdate(@RequestBody List<WebRecomBean> webRecomBeans) {
        try {
            List<WebRecomBean> updatedBeans = webRecomBeans.stream()
                    .map(bean -> webRecomService.update(bean.getId(), bean))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(updatedBeans);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}
