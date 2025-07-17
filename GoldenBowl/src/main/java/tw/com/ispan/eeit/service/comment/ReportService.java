package tw.com.ispan.eeit.service.comment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import tw.com.ispan.eeit.model.dto.comment.ReportRequestDTO;
import tw.com.ispan.eeit.model.dto.comment.ReportResponseDTO;
import tw.com.ispan.eeit.model.entity.comment.ReportBean;
import tw.com.ispan.eeit.repository.comment.CommentRepository;
import tw.com.ispan.eeit.repository.comment.ReportRepository;

@Service
@Transactional // 確保所有 Service 方法都在事務中運行
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private CommentRepository commentRepository; // 注入 Comment 的 Repository

    // 創建新的舉報
    public ReportResponseDTO createReport(ReportRequestDTO requestDTO) {
        ReportBean reportBean = new ReportBean();
        // 直接從 DTO 複製基本屬性
        BeanUtils.copyProperties(requestDTO, reportBean);

        reportBean.setReportDate(LocalDateTime.now()); // 設置舉報日期為當前時間
        reportBean.setStatus("pending"); // 預設狀態為 PENDING

        // 由於 ReportBean 中的 reportTypeId 和 commentId 是直接映射的，
        // 且 @ManyToOne 設置了 insertable = false, updatable = false，
        // Spring Data JPA 會自動處理這些 ID 的保存。

        ReportBean savedReport = reportRepository.save(reportBean);
        return convertToResponseDTO(savedReport);
    }

    // 根據 ID 查詢舉報 (返回 DTO)

    public Optional<ReportResponseDTO> getReportById(Integer id) {
        return reportRepository.findById(id)
                .map(this::convertToResponseDTO);
    }

    // 查詢所有舉報 (返回 DTO 列表)

    public List<ReportResponseDTO> getAllReports() {
        return reportRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    // 更新舉報
    public Optional<ReportResponseDTO> updateReport(Integer id, ReportRequestDTO requestDTO) {
        return reportRepository.findById(id).map(existingReport -> {
            // 更新基本屬性
            BeanUtils.copyProperties(requestDTO, existingReport);
            // 舉報日期和狀態可能不通過 DTO 更新，或者 DTO 中包含這些字段
            // 這裡假設 status 可以被更新，reportDate 不變或由後端邏輯更新

            ReportBean updatedReport = reportRepository.save(existingReport);
            return convertToResponseDTO(updatedReport);
        });
    }

    // 刪除舉報
    public boolean deleteReport(Integer id) {
        if (reportRepository.existsById(id)) {
            reportRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // 0716 JIMMY 整個FUNCTION覆蓋**輔助方法：將 ReportBean 轉換為 ReportResponseDTO**
    private ReportResponseDTO convertToResponseDTO(ReportBean reportBean) {
        ReportResponseDTO dto = new ReportResponseDTO();
        BeanUtils.copyProperties(reportBean, dto); // 複製相同名稱的屬性

        // 確保 reportType 和 comment 關聯已經加載
        if (reportBean.getReportType() != null) {
            dto.setReportTypeName(reportBean.getReportType().getType());
        } else {
            // 這部分代碼在使用了 JOIN FETCH 後理論上不會被觸發，除非資料庫存在不一致情況
            System.err.println("Warning: reportType is null for Report ID: " + reportBean.getId()
                    + ". This might indicate a data consistency issue or a missing JOIN FETCH.");
            dto.setReportTypeName(null);
        }

        if (reportBean.getComment() != null) {
            dto.setCommentContent(reportBean.getComment().getContent());
            dto.setCommentScore(reportBean.getComment().getScore());
            dto.setCommentIsHidden(reportBean.getComment().getIsHidden()); // **新增：設置 isHidden**
        } else {
            // 這部分代碼在使用了 JOIN FETCH 後理論上不會被觸發，除非資料庫存在不一致情況
            // 如果確實需要手動查詢，請確保此處的 commentRepository.findById() 不會導致 N+1
            System.err.println("Warning: comment is null for Report ID: " + reportBean.getId()
                    + ". This might indicate a data consistency issue or a missing JOIN FETCH.");
            // 備用方案：如果 comment 為空，嘗試透過 commentId 查詢（但這會導致額外查詢）
            if (reportBean.getCommentId() != null) {
                commentRepository.findById(reportBean.getCommentId()).ifPresent(comment -> {
                    dto.setCommentContent(comment.getContent());
                    dto.setCommentScore(comment.getScore());
                    dto.setCommentIsHidden(comment.getIsHidden()); // **設置 isHidden**
                });
            }
        }
        return dto;
    }
}