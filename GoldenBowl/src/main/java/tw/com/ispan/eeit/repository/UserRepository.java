package tw.com.ispan.eeit.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tw.com.ispan.eeit.model.entity.UserBean;

@Repository
public interface UserRepository extends JpaRepository<UserBean, Integer> {
	
	//用Email查詢(最常用於登入/驗證)
	Optional<UserBean> findByEmail(String email);
	
	// 用 Email + Password 查詢（for 登入）
	Optional<UserBean> findByEmailAndPassword(String email, String password);

	//名字模糊查詢
	List<UserBean> findByNameContaining(String keyword);
	//查詢是否存在指定email
	boolean existsByEmail(String email);
	//依啟用狀態查詢
	List<UserBean> findByIsActive(Boolean isActive);
	//依驗證狀態查詢
	List<UserBean> findByIsVerify(Boolean isVerify);
	
}