package tw.com.ispan.eeit.repository.plan;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tw.com.ispan.eeit.model.entity.plan.PlanBean;

@Repository
public interface PlanRepository extends JpaRepository<PlanBean, Integer> {
}
