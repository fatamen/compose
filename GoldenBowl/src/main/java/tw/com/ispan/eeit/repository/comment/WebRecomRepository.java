package tw.com.ispan.eeit.repository.comment;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tw.com.ispan.eeit.model.entity.comment.WebRecomBean;

@Repository
public interface WebRecomRepository extends JpaRepository<WebRecomBean, Integer> {
    // 根據 tag 查詢記錄
    List<WebRecomBean> findByTag(String tag);

}
