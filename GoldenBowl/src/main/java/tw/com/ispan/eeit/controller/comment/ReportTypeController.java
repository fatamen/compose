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

import tw.com.ispan.eeit.model.entity.comment.ReportTypeBean;
import tw.com.ispan.eeit.service.comment.ReportTypeService;

@RestController
@RequestMapping("/api/report-type")
public class ReportTypeController {

    @Autowired
    private ReportTypeService reportTypeService;

    // 搜索單筆BY ID
    @GetMapping("/{id}")
    public ResponseEntity<ReportTypeBean> getById(@PathVariable Integer id) {
        Optional<ReportTypeBean> result = reportTypeService.findById(id);
        return result.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 新增
    @PostMapping
    public ResponseEntity<ReportTypeBean> createOrUpdate(@RequestBody ReportTypeBean reportTypeBean) {
        try {
            ReportTypeBean savedBean = reportTypeService.save(reportTypeBean);
            return ResponseEntity.ok(savedBean);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // 修改
    @PutMapping("/{id}")
    public ResponseEntity<ReportTypeBean> update(@PathVariable Integer id, @RequestBody ReportTypeBean reportTypeBean) {
        try {
            reportTypeBean.setId(id);
            ReportTypeBean savedBean = reportTypeService.save(reportTypeBean);
            return ResponseEntity.ok(savedBean);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // 取得全部
    @GetMapping
    public ResponseEntity<List<ReportTypeBean>> getAll() {
        return ResponseEntity.ok(reportTypeService.findAll());
    }

    // 刪除
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Integer id) {
        try {
            reportTypeService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 批量更新
    @PostMapping("/batch-update")
    public ResponseEntity<List<ReportTypeBean>> batchUpdate(@RequestBody List<ReportTypeBean> reportTypeBean) {
        try {
            List<ReportTypeBean> updatedBeans = reportTypeBean.stream()
                    .map(bean -> reportTypeService.update(bean.getId(), bean))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(updatedBeans);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

}
