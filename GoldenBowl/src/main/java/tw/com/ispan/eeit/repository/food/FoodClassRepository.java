package tw.com.ispan.eeit.repository.food;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tw.com.ispan.eeit.model.entity.food.FoodClassBean;

@Repository
public interface FoodClassRepository extends JpaRepository<FoodClassBean, Integer>{
	List<FoodClassBean> findByStoreId(Integer storeId);
}
