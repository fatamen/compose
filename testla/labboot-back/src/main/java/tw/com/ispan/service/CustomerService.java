package tw.com.ispan.service;

import java.util.Arrays;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tw.com.ispan.domain.CustomerBean;
import tw.com.ispan.repository.CustomerRepository;

@Service
@Transactional
public class CustomerService {
    @Autowired
	private CustomerRepository customerRepository;

	public boolean exists(String custid) {
		if(custid!=null) {
			return customerRepository.existsById(custid);
		}
		return false;
	}

	public boolean changePassword(String username , String oldPass, String newPass) {
		if(newPass!=null && newPass.length()!=0) {
			CustomerBean bean = this.login(username, oldPass);
			if(bean!=null) {
				bean.setPassword(newPass.getBytes());
                CustomerBean update = customerRepository.save(bean);
				if(update!=null) {
					return true;
				}
			}
		}
		return false;
	}

	public CustomerBean login(String username, String password) {
		if(username!=null && username.length()!=0 && password!=null && password.length()!=0) {

			Optional<CustomerBean> optional = customerRepository.findById(username);
			if (optional.isPresent()) {
                CustomerBean bean = optional.get();
				byte[] temp = bean.getPassword();   //資料庫抓出
                byte[] pass = password.getBytes();  //使用者輸入
				if(Arrays.equals(pass, temp)) {
					return bean;
				}
			}		
		}
		return null;
	}
}
