package tw.com.ispan.eeit.controller.plan;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tw.com.ispan.eeit.model.entity.plan.SubRecordBean;
import tw.com.ispan.eeit.service.plan.SubRecordService;

@RestController
@RequestMapping("/sub-records")
@CrossOrigin // 如果前端跨網域，記得加這行
public class SubRecordController {

	@Autowired
	private SubRecordService subRecordService;
	
	//查全部訂閱紀錄
	@GetMapping
	public List<SubRecordBean> findAll(){
		return subRecordService.findAll();
	}
	
	// 查單筆
    @GetMapping("/{id}")
    public SubRecordBean findById(@PathVariable Integer id) {
        return subRecordService.findById(id);
    }
    
    // 查某使用者的訂閱紀錄
    @GetMapping("/user/{userId}")
    public List<SubRecordBean> findByUserId(@PathVariable Integer userId) {
        return subRecordService.findByUserId(userId);
    }
    
    // 新增
    @PostMapping
    public SubRecordBean create(@RequestBody SubRecordBean subRecord) {
        return subRecordService.create(subRecord);
    }
    
    // 刪除
    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable Integer id) {
        subRecordService.deleteById(id);
    }
}