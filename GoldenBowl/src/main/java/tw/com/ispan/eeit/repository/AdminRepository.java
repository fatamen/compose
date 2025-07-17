package tw.com.ispan.eeit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tw.com.ispan.eeit.model.entity.AdminBean;


@Repository
public interface AdminRepository extends JpaRepository<AdminBean, Integer> {
    
}
