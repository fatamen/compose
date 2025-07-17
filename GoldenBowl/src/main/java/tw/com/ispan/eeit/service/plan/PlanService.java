package tw.com.ispan.eeit.service.plan;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tw.com.ispan.eeit.model.entity.plan.PlanBean;
import tw.com.ispan.eeit.repository.plan.PlanRepository;

@Service
public class PlanService {

	
	@Autowired
	private PlanRepository planRepository;
	
	//查詢全部訂閱方案
	public List<PlanBean>findAll(){
		return planRepository.findAll();
	}
	
	//查詢單筆
    public PlanBean findById(Integer id) {
        Optional<PlanBean> optional = planRepository.findById(id);
        return optional.orElse(null);
    }
    
    //新增訂閱方案
    public PlanBean create(PlanBean plan) {
        plan.setCreatedTime(LocalDateTime.now());
        plan.setUpdateTime(LocalDateTime.now());
        return planRepository.save(plan);
    }
    
    //修改訂閱方案
    public PlanBean update(Integer id, PlanBean newData) {
        Optional<PlanBean> optional = planRepository.findById(id);
        if (optional.isPresent()) {
            PlanBean plan = optional.get();
            plan.setName(newData.getName());
            plan.setPrice(newData.getPrice());
            plan.setValidMonths(newData.getValidMonths());
            plan.setUpdateTime(LocalDateTime.now());
            return planRepository.save(plan);
        }
        return null;
    }
    
    //刪除訂閱方案
    public void deleteById(Integer id) {
        planRepository.deleteById(id);
    }
}
