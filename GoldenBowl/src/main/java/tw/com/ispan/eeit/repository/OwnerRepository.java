package tw.com.ispan.eeit.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tw.com.ispan.eeit.model.entity.OwnerBean;

@Repository
public interface OwnerRepository extends JpaRepository<OwnerBean, Integer> {
	boolean existsByEmail(String email);

	OwnerBean findByEmailAndPassword(String email, String password);

	// 檢查電話是否存在
	boolean existsByPhone(String phone);

	// 用名字模糊查詢
	List<OwnerBean> findByNameContaining(String keyword);

	// 分頁查詢（JPA Pageable 支援）
	Page<OwnerBean> findAll(Pageable pageable);
}
