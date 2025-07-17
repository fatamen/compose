package tw.com.ispan.eeit.controller.plan;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import tw.com.ispan.eeit.model.entity.plan.PlanBean;
import tw.com.ispan.eeit.service.plan.PlanService;

public class PlanController {

	@Autowired
	private PlanService planService;
	
	//查全部訂閱方案
	@GetMapping
	public List<PlanBean> findAll(){
		return planService.findAll();
	}
	
	// 查單筆
	@GetMapping("/{id}")
	public PlanBean findById(@PathVariable Integer id) {
		return planService.findById(id);
	}
	
	// 新增訂閱方案
    @PostMapping
    public PlanBean create(@RequestBody PlanBean plan) {
        return planService.create(plan);
    }
    
    // 修改訂閱方案
    @PutMapping("/{id}")
    public PlanBean update(@PathVariable Integer id, @RequestBody PlanBean plan) {
        return planService.update(id, plan);
    }
    
    // 刪除訂閱方案
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        planService.deleteById(id);
    }
}
