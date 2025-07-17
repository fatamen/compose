package tw.com.ispan.eeit.service.plan;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tw.com.ispan.eeit.model.entity.plan.SubRecordBean;
import tw.com.ispan.eeit.repository.plan.SubRecordRepository;

@Service
public class SubRecordService {

    @Autowired
    private SubRecordRepository subRecordRepository;
    // 查全部
    public List<SubRecordBean> findAll() {
        return subRecordRepository.findAll();
    }
    
    // 查單筆
    public SubRecordBean findById(Integer id) {
        Optional<SubRecordBean> optional = subRecordRepository.findById(id);
        return optional.orElse(null);
    }
    
    // 查某使用者的訂閱紀錄
    public List<SubRecordBean> findByUserId(Integer userId) {
        return subRecordRepository.findByUserId(userId);
    }
    
    // 建立訂閱紀錄（填上建立時間）
    public SubRecordBean create(SubRecordBean subRecord) {
        subRecord.setPaidTime(LocalDateTime.now());
        return subRecordRepository.save(subRecord);
    }
    
    // 刪除訂閱紀錄
    public void deleteById(Integer id) {
        subRecordRepository.deleteById(id);
    }
}

