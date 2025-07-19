package tw.com.ispan.eeit.controller.owner;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tw.com.ispan.eeit.model.dto.OwnerDTO;
import tw.com.ispan.eeit.model.entity.OwnerBean;
import tw.com.ispan.eeit.service.OwnerService;

@RestController
@RequestMapping("/api/owner")
public class OwnerController {
	@Autowired
	private OwnerService ownerService;
	

// 查詢全部（Read All）
    @GetMapping
    public List<OwnerBean> findAll() {
        return ownerService.findAll();
    }

    // 查詢單筆（Read One）
    @GetMapping("/{id}")
    public Map<String, Object> findById(@PathVariable Integer id) {
        OwnerBean bean = ownerService.findById(id);
        if (bean == null) return Map.of("success", false, "message", "找不到此帳號");
        OwnerDTO dto = ownerService.toDTO(bean);
        return Map.of("success", true, "owner", dto);
    }

    @PostMapping("/check-email")
    public Map<String, Object> checkEmail(@RequestBody Map<String, String> map) {
        String email = map.get("email");
        boolean exists = ownerService.checkEmailExists(email);
        return Map.of("exists", exists);
    }

    // 註冊（Create）
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> map) {
        String email = map.get("email");
        String pwd = map.get("password");
        String name = map.get("name");
        String phone = map.get("phone");
        OwnerBean owner = ownerService.register(email, pwd, name, phone);
        if (owner == null) {
            return Map.of("success", false, "message", "Email 已註冊");
        }
        return Map.of("success", true, "ownerId", owner.getId());
    }

    // 登入
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> map) {
        String email = map.get("email");
        String pwd = map.get("password");
        System.out.println("login email: " + email + ", pwd: " + pwd);
        OwnerBean owner = ownerService.findByEmailAndPassword(email, pwd);
        if (owner == null) {
            System.out.println("login fail");
            return Map.of("success", false, "message", "帳號或密碼錯誤");
        }
        // 把所需欄位全部回傳
        return Map.of(
            "success", true,
            "ownerId", owner.getId(),
            "name", owner.getName(),
            "email", owner.getEmail(),
            "phone", owner.getPhone(),
            "lastLogin", owner.getLastLogin()
        );
    }
    // 修改（Update）
    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Integer id, @RequestBody Map<String, String> map) {
        String name = map.get("name");
        String phone = map.get("phone");
        String email = map.get("email");
        OwnerBean updated = ownerService.updateOwner(id, name, phone, email);
        if (updated == null) {
            return Map.of("success", false, "message", "找不到此帳號");
        }
        // 直接呼叫 service 的 DTO 方法
        OwnerDTO dto = ownerService.toDTO(updated);
        return Map.of("success", true, "owner", dto);
    }

    // 刪除（Delete）
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Integer id) {
        boolean deleted = ownerService.deleteOwner(id);
        if (!deleted) {
            return Map.of("success", false, "message", "找不到此帳號");
        }
        return Map.of("success", true);
    }
}
