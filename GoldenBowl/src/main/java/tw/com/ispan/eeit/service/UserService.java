package tw.com.ispan.eeit.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tw.com.ispan.eeit.model.dto.UserDTO;
import tw.com.ispan.eeit.model.entity.UserBean;
import tw.com.ispan.eeit.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    
    //查全部
    public List<UserBean> getAllUsers() {
        return userRepository.findAll();
    }

    //查單一
    public Optional<UserBean> getUserById(Integer id) {
        return userRepository.findById(id);
    }

    // 新增會員
    public UserBean createUser(UserBean user) {
        // email 必填
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email 不可為空");
        }
        // 先查 email 是否存在
        if (userRepository.existsByEmail(user.getEmail().trim())) {
            throw new IllegalStateException("Email 已註冊");
        }
        // 設定創建日期和最後登入日期
        user.setSignupDate(LocalDateTime.now());
        user.setLastLogin(LocalDateTime.now());
        if (user.getIsActive() == null) user.setIsActive(true);
        if (user.getIsVerify() == null) user.setIsVerify(false);
        return userRepository.save(user);
    }

    public UserDTO updateUser(Integer id, UserDTO dto) {
        Optional<UserBean> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            UserBean existingUser = optionalUser.get();
            // 只允許下列欄位更新
            if (dto.getName() != null) existingUser.setName(dto.getName());
            if (dto.getPhone() != null) existingUser.setPhone(dto.getPhone());
            if (dto.getIsActive() != null) existingUser.setIsActive(dto.getIsActive());
            // Email 只能走驗證信 API 流程，這裡不允許直接變更
            // isVerify 也只由驗證流程設定
            // lastLogin 只由登入時自動更新
            UserBean updated = userRepository.save(existingUser);
            return toDTO(updated);
        }
        return null;
    }
    
    public boolean deleteUser(Integer id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    //查email是否存在
    public boolean checkEmailExists(String email) {
    	//System.out.println("查詢用 email: [" + email + "]");
        return userRepository.existsByEmail(email.trim());
    }
    
    // 登入
    public UserBean findByEmailAndPassword(String email, String password) {
        Optional<UserBean> optional = userRepository.findByEmail(email);
        if (optional.isPresent()) {
            UserBean user = optional.get();
            if (user.getPassword().equals(password)) {
            	user.setLastLogin(LocalDateTime.now());
            	userRepository.save(user);
                return user;
            }
        }
        return null;
    }
    
    public Optional<UserBean> getUserByEmail(String email){
    	if(email == null) return Optional.empty();
    	return userRepository.findByEmail(email.trim());
    }
    
    @Transactional
    public void verifyEmail(String email) {
        if(email == null) return;
        Optional<UserBean> opt = userRepository.findByEmail(email.trim());
        if(opt.isPresent()){
            UserBean user = opt.get();
            user.setIsVerify(true);
            userRepository.save(user);
        }
    }
    
 // 補一個 Entity <-> DTO 的 Mapper 方法（通常建議放 Utility class）
    public static UserDTO toDTO(UserBean user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());
        dto.setPhone(user.getPhone());
        dto.setIsActive(user.getIsActive());
        dto.setIsVerify(user.getIsVerify());
        dto.setSignupDate(user.getSignupDate());
        dto.setLastLogin(user.getLastLogin());
        dto.setHideUntil(user.getHideUntil());
        return dto;
    }
    
    public static UserBean toEntity(UserDTO dto) {
        UserBean user = new UserBean();
        user.setId(dto.getId());
        user.setEmail(dto.getEmail());
        user.setName(dto.getName());
        user.setPhone(dto.getPhone());
        user.setIsActive(dto.getIsActive());
        user.setIsVerify(dto.getIsVerify());
        user.setSignupDate(dto.getSignupDate());
        user.setLastLogin(dto.getLastLogin());
        user.setHideUntil(dto.getHideUntil());
        user.setPassword(dto.getPassword());
        return user;
    }
}