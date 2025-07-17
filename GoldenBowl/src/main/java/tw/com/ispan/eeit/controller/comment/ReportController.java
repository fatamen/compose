package tw.com.ispan.eeit.controller.comment;

import java.util.List;

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

import tw.com.ispan.eeit.model.dto.comment.ReportRequestDTO;
import tw.com.ispan.eeit.model.dto.comment.ReportResponseDTO;
import tw.com.ispan.eeit.service.comment.ReportService;

@RestController
@RequestMapping("/api/reports") // 建議使用 /api/reports 作為 RESTful 端點
public class ReportController {

    @Autowired
    private ReportService reportService;

    // 創建新的舉報
    @PostMapping
    public ResponseEntity<ReportResponseDTO> createReport(@RequestBody ReportRequestDTO requestDTO) {
        ReportResponseDTO createdReport = reportService.createReport(requestDTO);
        return new ResponseEntity<>(createdReport, HttpStatus.CREATED);
    }

    // 根據 ID 查詢舉報
    @GetMapping("/{id}")
    public ResponseEntity<ReportResponseDTO> getReportById(@PathVariable Integer id) {
        return reportService.getReportById(id)
                .map(reportDTO -> new ResponseEntity<>(reportDTO, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // 查詢所有舉報
    @GetMapping
    public ResponseEntity<List<ReportResponseDTO>> getAllReports() {
        List<ReportResponseDTO> reports = reportService.getAllReports();
        if (reports.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 返回 204 No Content 如果沒有數據
        }
        return new ResponseEntity<>(reports, HttpStatus.OK);
    }

    // 更新舉報
    @PutMapping("/{id}")
    public ResponseEntity<ReportResponseDTO> updateReport(@PathVariable Integer id,
            @RequestBody ReportRequestDTO requestDTO) {
        return reportService.updateReport(id, requestDTO)
                .map(updatedReport -> new ResponseEntity<>(updatedReport, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // 刪除舉報
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReport(@PathVariable Integer id) {
        if (reportService.deleteReport(id)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content 表示成功刪除
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
