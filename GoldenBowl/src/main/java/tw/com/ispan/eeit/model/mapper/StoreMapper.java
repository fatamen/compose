package tw.com.ispan.eeit.model.mapper;

import tw.com.ispan.eeit.model.dto.store.StoreDTO;
import tw.com.ispan.eeit.model.entity.store.StoreBean;

public class StoreMapper {

    public static StoreDTO toDto(StoreBean bean) {
        if (bean == null) return null;
        StoreDTO dto = new StoreDTO();
        dto.setId(bean.getId());
        dto.setName(bean.getName());
        dto.setAddress(bean.getAddress());
        dto.setLat(bean.getLat());
        dto.setLng(bean.getLng());
        dto.setStoreIntro(bean.getStoreIntro());
        dto.setPhoto(bean.getPhoto());
        dto.setIsOpen(bean.getIsOpen());
        dto.setScore(bean.getScore());
        dto.setCreatedTime(bean.getCreatedTime());
        dto.setUpdatedTime(bean.getUpdatedTime());
        dto.setIsActive(bean.getIsActive());
        if (bean.getOwner() != null) {
            dto.setOwnerId(bean.getOwner().getId());
        }
        return dto;
    }
}
