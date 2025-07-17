package tw.com.ispan.eeit.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tw.com.ispan.eeit.model.dto.OwnerDTO;
import tw.com.ispan.eeit.model.entity.OwnerBean;
import tw.com.ispan.eeit.repository.OwnerRepository;

@Service
public class OwnerService {
    @Autowired
    private OwnerRepository ownerRepository;

    // 查詢全部
    public List<OwnerBean> findAll() { return ownerRepository.findAll(); }

    // 查詢單筆
    public OwnerBean findById(Integer id) { return ownerRepository.findById(id).orElse(null); }

    // 更新
    public OwnerBean updateOwner(Integer id, String name, String phone, String email) {
        Optional<OwnerBean> optional = ownerRepository.findById(id);
        if (optional.isPresent()) {
            OwnerBean owner = optional.get();
            if (name != null && !name.isEmpty()) owner.setName(name);
            if (phone != null && !phone.isEmpty()) owner.setPhone(phone);
            if (email != null && !email.isEmpty()) owner.setEmail(email);
            return ownerRepository.save(owner);
        }
        return null;
    }

    // 刪除
    public boolean deleteOwner(Integer id) {
        if (ownerRepository.existsById(id)) {
            ownerRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // 查詢 email 是否存在
    public boolean checkEmailExists(String email) {
        return ownerRepository.existsByEmail(email);
    }

    // 登入（建議 hash 密碼比對）
    public OwnerBean findByEmailAndPassword(String email, String password) {
        OwnerBean owner = ownerRepository.findByEmailAndPassword(email, password);
        if (owner != null) {
            owner.setLastLogin(LocalDateTime.now());
            ownerRepository.save(owner);
        }
        return owner;
    }

    // 註冊
    public OwnerBean register(String email, String password, String name, String phone) {
        if (ownerRepository.existsByEmail(email)) return null;
        OwnerBean owner = new OwnerBean();
        owner.setEmail(email);
        owner.setPassword(password); // hash!
        owner.setName(name);
        owner.setPhone(phone);
        owner.setSignupDate(LocalDateTime.now());
        owner.setIsActive(true);
        owner.setIsVerify(false);
        return ownerRepository.save(owner);
    }
	
	public OwnerDTO toDTO(OwnerBean bean) {
	    if (bean == null) return null;
	    OwnerDTO dto = new OwnerDTO();
	    dto.setId(bean.getId());
	    dto.setEmail(bean.getEmail());
	    dto.setName(bean.getName());
	    dto.setPhone(bean.getPhone());
	    dto.setSignupDate(bean.getSignupDate());
	    dto.setLastLogin(bean.getLastLogin());
	    dto.setIsActive(bean.getIsActive());
	    dto.setIsVerify(bean.getIsVerify());
	    // 只傳簡單的 store info
	    if(bean.getStores() != null) {
	        List<OwnerDTO.StoreSimpleDTO> stores = bean.getStores().stream().map(store -> {
	            OwnerDTO.StoreSimpleDTO s = new OwnerDTO.StoreSimpleDTO();
	            s.setId(store.getId());
	            s.setName(store.getName());
	            return s;
	        }).toList();
	        dto.setStores(stores);
	    }
	    return dto;
	}
}
