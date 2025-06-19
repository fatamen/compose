package tw.com.ispan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tw.com.ispan.domain.ProductBean;

@Repository
public interface ProductRepository extends JpaRepository<ProductBean, Integer>, ProductDAO {

}
