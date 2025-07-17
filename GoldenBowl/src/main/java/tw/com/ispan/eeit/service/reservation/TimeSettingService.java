package tw.com.ispan.eeit.service.reservation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tw.com.ispan.eeit.model.entity.reservation.TimeSettingBean;
import tw.com.ispan.eeit.repository.reservation.TimeSettingRepository;

@Service
public class TimeSettingService {

    @Autowired
    private TimeSettingRepository repository;

    public TimeSettingBean getByStoreId(Integer storeId) {
        return repository.findByStoreId(storeId)
                .orElseGet(() -> {
                    // 如果找不到時段設定，返回預設值
                    TimeSettingBean defaultSetting = new TimeSettingBean();
                    defaultSetting.setStoreId(storeId);
                    defaultSetting.setInterval(30);
                    defaultSetting.setMealTime(90);
                    return defaultSetting;
                });
    }

    @Transactional
    public TimeSettingBean update(Integer storeId, Integer interval, Integer mealTime) {
        TimeSettingBean setting = repository.findByStoreId(storeId)
                .orElse(new TimeSettingBean());

        setting.setStoreId(storeId);
        setting.setInterval(interval);
        setting.setMealTime(mealTime);

        return repository.save(setting);
    }
}