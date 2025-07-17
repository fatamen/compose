package tw.com.ispan.eeit.repository.plan;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tw.com.ispan.eeit.model.entity.plan.SubRecordBean;

@Repository
public interface SubRecordRepository extends JpaRepository<SubRecordBean, Integer>{
	List<SubRecordBean> findByUserId(Integer userId);
}
