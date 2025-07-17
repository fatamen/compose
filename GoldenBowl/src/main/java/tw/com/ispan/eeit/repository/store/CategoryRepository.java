package tw.com.ispan.eeit.repository.store;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tw.com.ispan.eeit.model.entity.store.CategoryBean;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryBean, Integer> {
	List<CategoryBean> findByName(String name);
}