package tw.com.ispan.eeit.service.comment;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tw.com.ispan.eeit.model.entity.comment.WebRecomBean;
import tw.com.ispan.eeit.repository.comment.WebRecomRepository;

@Service
public class WebRecomService {
    @Autowired
    private WebRecomRepository webRecomRepository;

    // 創建TAG
    public WebRecomBean save(WebRecomBean webRecomBean) {
        if (webRecomBean.getTag() == null || webRecomBean.getTag().trim().isEmpty()) {
            throw new IllegalArgumentException("Tag cannot be null or empty");
        }
        return webRecomRepository.save(webRecomBean);
    }

    // 更新TAG
    public WebRecomBean update(Integer id, WebRecomBean webRecom) {
        if (!webRecomRepository.existsById(id)) {
            throw new RuntimeException("標籤不存在");
        }
        webRecom.setId(id);
        return webRecomRepository.save(webRecom);
    }

    // 根據 ID 查詢單筆記錄
    public Optional<WebRecomBean> findById(Integer id) {
        return webRecomRepository.findById(id);
    }

    // 查詢所有記錄
    public List<WebRecomBean> findAll() {
        return webRecomRepository.findAll();
    }

    // 根據 tag 查詢記錄
    public List<WebRecomBean> findByTag(String tag) {
        return webRecomRepository.findByTag(tag);
    }

    // 刪除記錄
    public void deleteById(Integer id) {
        if (!webRecomRepository.existsById(id)) {
            throw new IllegalArgumentException("Record with ID " + id + " does not exist");
        }
        webRecomRepository.deleteById(id);
    }
}
