package tw.com.ispan.eeit.repository.emailVerify;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tw.com.ispan.eeit.model.entity.emailVerify.UserTokenBean;

@Repository
public interface UserTokenRepository extends JpaRepository<UserTokenBean, Integer> {


    Optional<UserTokenBean> findByVerifyCodeAndUsedFalse(String verifyCode);
    Optional<UserTokenBean> findByEmail(String email);
    List<UserTokenBean> findByEmailAndUsedFalse(String email);
    Optional<UserTokenBean> findTopByEmailAndUsedTrueOrderByIdDesc(String email);
}
