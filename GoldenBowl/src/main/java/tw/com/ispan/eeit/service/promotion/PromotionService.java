package tw.com.ispan.eeit.service.promotion;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import tw.com.ispan.eeit.model.dto.promotion.PromotionCreateDTO;
import tw.com.ispan.eeit.model.dto.promotion.PromotionDTO;
import tw.com.ispan.eeit.model.dto.promotion.PromotionUpdateDTO;
import tw.com.ispan.eeit.model.entity.UserBean;
import tw.com.ispan.eeit.model.entity.food.TagBean;
import tw.com.ispan.eeit.model.entity.plan.PlanBean;
import tw.com.ispan.eeit.model.entity.promotion.NotificationBean;
import tw.com.ispan.eeit.model.entity.promotion.PromotionBean;
import tw.com.ispan.eeit.model.entity.store.StoreBean;
import tw.com.ispan.eeit.repository.UserRepository;
import tw.com.ispan.eeit.repository.food.TagRepository;
import tw.com.ispan.eeit.repository.plan.PlanRepository;
import tw.com.ispan.eeit.repository.promotion.PromotionRepository;
import tw.com.ispan.eeit.repository.store.StoreRepository;
import tw.com.ispan.eeit.util.DatetimeConvert;

@Service
public class PromotionService {

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    // 查詢全部優惠券（後台管理）
    public List<PromotionBean> findAll() {
        return promotionRepository.findAll();
    }

    // 根據方案 ID 查優惠券
    public List<PromotionBean> findByPlanId(Integer planId) {
        return promotionRepository.findByPlanId(planId);
    }

    // 根據商店 ID 查優惠券
    public List<PromotionBean> findByStoreId(Integer storeId) {
        return promotionRepository.findByStoreId(storeId);
    }

    // 查詢單筆優惠券（轉 DTO）
    public PromotionDTO findById(Integer id) {
        Optional<PromotionBean> optional = promotionRepository.findById(id);
        // return optional.orElse(null);
        return toDTO(optional.get());
    }

    // 新增優惠券
    public PromotionBean createFromDTO(PromotionCreateDTO dto) {
        PromotionBean bean = new PromotionBean();
        bean.setTitle(dto.getTitle());
        bean.setDescription(dto.getDescription());
        bean.setDiscountType(dto.getDiscountType());
        bean.setDiscountValue(dto.getDiscountValue());
        bean.setMinSpend(dto.getMinSpend());
        bean.setCode(dto.getCode());
        bean.setMaxUsage(dto.getMaxUsage());
        bean.setUserUsageLimit(dto.getUserUsageLimit());
        bean.setStatus("open");
        bean.setCreatedTime(LocalDateTime.now());

        // ✅ 預設時間區間
        bean.setStartTime(dto.getStartTime() != null ? dto.getStartTime() : LocalDateTime.now().minusHours(1));
        bean.setEndTime(dto.getEndTime() != null ? dto.getEndTime() : LocalDateTime.now().plusDays(7));

        // 關聯條件綁定
        if (dto.getStoreId() != null) {
            StoreBean store = storeRepository.findById(dto.getStoreId()).orElse(null);
            bean.setStore(store);
        }

        if (dto.getTagId() != null) {
            TagBean tag = tagRepository.findById(dto.getTagId()).orElse(null);
            bean.setTag(tag);
        }

        if (dto.getPlanId() != null) {
            PlanBean plan = planRepository.findById(dto.getPlanId()).orElse(null);
            bean.setPlan(plan);
        }
        // ✅ 儲存優惠券
        PromotionBean saved = promotionRepository.save(bean);

        // ✅ 發送通知給所有使用者
        List<UserBean> users = userRepository.findAll();
        for (UserBean user : users) {
            NotificationBean noti = new NotificationBean();
            noti.setUser(user);
            noti.setPromotion(saved);
            notificationService.create(noti);
        }

        return saved;
    }

    // 修改優惠券
    public PromotionBean updateFromDTO(Integer id, PromotionUpdateDTO dto) {
        PromotionBean p = promotionRepository.findById(id).orElseThrow();

        p.setTitle(dto.getTitle());
        p.setDescription(dto.getDescription());
        p.setDiscountType(dto.getDiscountType());
        p.setMinSpend(dto.getMinSpend());
        p.setStartTime(dto.getStartTime());
        p.setEndTime(dto.getEndTime());
        p.setCode(dto.getCode());
        p.setMaxUsage(dto.getMaxUsage());
        p.setUserUsageLimit(dto.getUserUsageLimit());

        // ✅ 更新 store 綁定
        if (dto.getStoreId() != null) {
            StoreBean store = storeRepository.findById(dto.getStoreId()).orElse(null);
            p.setStore(store);
        } else {
            p.setStore(null); // 沒填就解除綁定
        }

        // ✅ 更新 tag 綁定
        if (dto.getTagId() != null) {
            TagBean tag = tagRepository.findById(dto.getTagId()).orElse(null);
            p.setTag(tag);
        } else {
            p.setTag(null);
        }

        // ✅ 更新 plan 綁定
        if (dto.getPlanId() != null) {
            PlanBean plan = planRepository.findById(dto.getPlanId()).orElse(null);
            p.setPlan(plan);
        } else {
            p.setPlan(null);
        }

        // ✅ 將 BigDecimal 轉為 String 儲存
        if (dto.getDiscountValue() != null) {
            p.setDiscountValue(dto.getDiscountValue().toPlainString());
        }
        return promotionRepository.save(p);
    }

    // 刪除優惠券
    public void deleteById(Integer id) {
        promotionRepository.deleteById(id);
    }

    // ✅ 優惠券清單：查詢目前有效的優惠券
    public List<PromotionDTO> findAllAvailable() {
        List<PromotionBean> all = promotionRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        System.out.println("🔍 現在時間：" + now);
        System.out.println("📦 資料庫優惠券筆數：" + all.size());

        List<PromotionDTO> result = all.stream()
                .map(this::toDTO) // ⬅️ 先轉成 DTO，再根據 available 判斷
                .peek(p -> {
                    System.out.println("➡️ 優惠券：" + p.getTitle());
                    System.out.println("   狀態：" + p.getStatus());
                    System.out.println("   時間區間：" + p.getStartTime() + " ~ " + p.getEndTime());
                    System.out.println("   可用狀態：" + p.isAvailable());
                })
                .filter(PromotionDTO::isAvailable) // ⬅️ 只保留 available = true 的
                .toList();

        System.out.println("✅ 篩選後筆數：" + result.size());

        return result;
    }

    // ✅ 結帳時使用：查詢符合條件的優惠券（userId、storeId、金額、tagId）
    public List<PromotionDTO> getAvailablePromotions(
            Integer userId,
            Integer storeId,
            Integer amount,
            List<Integer> tagIds,
            String tagSpendMapJson // 前端傳入的 JSON 字串：Map<tagId, 金額>
    ) {
        // 1️⃣ 解析 JSON → Map<Integer, Integer>
        final Map<Integer, Integer> tagSpendMap = new HashMap<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<Integer, Integer> parsed = mapper.readValue(tagSpendMapJson, new TypeReference<>() {
            });
            tagSpendMap.putAll(parsed); // ✅ 用 putAll，不重新指定變數
            System.out.println("📨 後端收到 tagSpendMap：" + tagSpendMap);
        } catch (Exception e) {
            System.out.println("❌ 無法解析 tagSpendMap");
            e.printStackTrace();
        }

        // 2️⃣ 初步條件查詢（時間、狀態、user 使用次數等）
        List<PromotionBean> rawList = promotionRepository.findAvailablePromotions(userId, storeId, amount, tagIds);
        System.out.println("🧩 初步符合條件的優惠券數：" + rawList.size());

        // 3️⃣ 進一步過濾是否符合 storeId、tag、minSpend 條件
        List<PromotionDTO> filtered = rawList.stream()
                .filter(p -> {
                    int minSpend = p.getMinSpend();

                    // ⛔ 若有綁定 store 限定，檢查是否符合
                    if (p.getStore() != null && !p.getStore().getId().equals(storeId)) {
                        return false;
                    }

                    // ⛔ 若有綁定 tag 限定，則該 tag 的消費金額需達到門檻
                    if (p.getTag() != null) {
                        Integer tagSpend = tagSpendMap.getOrDefault(p.getTag().getId(), 0);
                        return tagSpend >= minSpend;
                    }

                    // ✅ 若無綁定 tag，則總金額需達門檻
                    return amount >= minSpend;
                })
                .map(this::toDTO)
                .toList();

        System.out.println("✅ 符合條件的最終優惠券數：" + filtered.size());

        return filtered;
    }

    // ✅✨ 新增：分類用（由後端過濾分類 type）
    public List<PromotionDTO> findByType(String type) {
        List<PromotionBean> all = promotionRepository.findAll();
        return all.stream()
                .filter(p -> getType(p).equals(type)) // 後端分類條件
                .map(this::toDTO)
                .toList();
    }

    // ✅✨ 新增：取得分類（配合上方 findByType 使用）
    private String getType(PromotionBean p) {
        if (p.getStore() != null)
            return "restaurant";
        if (p.getTag() != null)
            return "food";
        if (p.getPlan() != null)
            return "member";
        return "global";
    }

    // 判斷優惠券狀態 open / close
    public void updateStatus(Integer id, String status) {
        PromotionBean promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("找不到優惠券 ID：" + id));

        if (!status.equalsIgnoreCase("open") && !status.equalsIgnoreCase("close")) {
            throw new IllegalArgumentException("狀態只能是 open 或 close");
        }

        promotion.setStatus(status.toLowerCase());
        promotion.setUpdatedTime(LocalDateTime.now());
        promotionRepository.save(promotion);
    }

    // 查某使用者用過的優惠券（根據訂單紀錄）
    public List<PromotionDTO> findUsedPromotions(Integer userId) {
        List<PromotionBean> usedPromos = promotionRepository.findUsedByUserId(userId);
        return usedPromos.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Entity 轉 DTO（包含分類 type 判斷）
    public PromotionDTO toDTO(PromotionBean p) {
        String type = getType(p);
        LocalDateTime now = LocalDateTime.now();

        boolean available = "open".equalsIgnoreCase(p.getStatus()) &&
                (p.getStartTime() == null || !now.isBefore(p.getStartTime())) &&
                (p.getEndTime() == null || !now.isAfter(p.getEndTime()));

        PromotionDTO dto = new PromotionDTO(
                p.getId(), p.getTitle(), p.getDescription(), p.getDiscountType(),
                p.getDiscountValue(), p.getMinSpend(), p.getStartTime(), p.getEndTime(),
                p.getStatus(), available, type,
                p.getTag() != null ? p.getTag().getId() : null,
                p.getTag() != null ? p.getTag().getName() : null,
                p.getStore() != null ? p.getStore().getId() : null,
                p.getStore() != null ? p.getStore().getName() : null,
                p.getPlan() != null ? p.getPlan().getId() : null,
                p.getPlan() != null ? p.getPlan().getName() : null,
                p.getPlan() != null ? p.getPlan().getPrice() : null,
                p.getPlan() != null ? p.getPlan().getValidMonths() : null,
                p.getCode(), p.getMaxUsage(), p.getUserUsageLimit(),
                null, // startTimeStr 預設 null，待會補上
                null // endTimeStr 預設 null，待會補上
        );
        // ✨ 新增格式化欄位
        String startTimeStr = DatetimeConvert.toString(p.getStartTime(), "yyyy/MM/dd（E） HH:mm", Locale.TAIWAN);
        String endTimeStr = DatetimeConvert.toString(p.getEndTime(), "yyyy/MM/dd（E） HH:mm", Locale.TAIWAN);
        dto.setStartTimeStr(startTimeStr);
        dto.setEndTimeStr(endTimeStr);

        return dto;
    }
}