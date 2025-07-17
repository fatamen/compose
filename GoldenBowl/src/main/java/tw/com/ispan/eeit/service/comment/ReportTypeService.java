package tw.com.ispan.eeit.service.comment;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import tw.com.ispan.eeit.model.entity.comment.ReportTypeBean;
import tw.com.ispan.eeit.repository.comment.ReportTypeRepository;

@Service
public class ReportTypeService {

    @Autowired
    private ReportTypeRepository reportTypeRepository;

    // 取得檢舉類型BY ID
    public Optional<ReportTypeBean> findById(Integer id) {
        return reportTypeRepository.findById(id);
    }

    // 取得全部
    public List<ReportTypeBean> findAll() {
        return reportTypeRepository.findAll(Sort.by(Sort.Direction.ASC, "prime"));// 0716 JIMMY 修改排序
    }

    // 更新
    public ReportTypeBean update(Integer id, ReportTypeBean reportTypeBean) {
        if (!reportTypeRepository.existsById(id)) {
            throw new RuntimeException("標籤不存在");
        }
        reportTypeBean.setId(id);
        return reportTypeRepository.save(reportTypeBean);
    }

    /// 新增檢舉類型
    public ReportTypeBean save(ReportTypeBean reportTypeBean) {
        if (reportTypeBean.getType() == null || reportTypeBean.getType().trim().isEmpty()) {
            throw new IllegalArgumentException("type cannot be null or empty");
        }
        if (reportTypeBean.getDescription() == null || reportTypeBean.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be null or empty");
        }
        return reportTypeRepository.save(reportTypeBean);
    }

    // 刪除檢舉類型
    public void deleteById(Integer id) {
        if (!reportTypeRepository.existsById(id)) {
            throw new IllegalArgumentException("Record with ID " + id + " does not exist");
        }
        reportTypeRepository.deleteById(id);

    }

}
