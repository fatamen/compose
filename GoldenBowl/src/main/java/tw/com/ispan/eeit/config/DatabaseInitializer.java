package tw.com.ispan.eeit.config;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import tw.com.ispan.eeit.model.entity.UserBean;
import tw.com.ispan.eeit.model.entity.reservation.TableBean;
import tw.com.ispan.eeit.model.entity.reservation.TimeSettingBean;
import tw.com.ispan.eeit.model.entity.store.OpenHourBean;
import tw.com.ispan.eeit.model.entity.store.StoreBean;
import tw.com.ispan.eeit.repository.UserRepository;
import tw.com.ispan.eeit.repository.reservation.TableRepository;
import tw.com.ispan.eeit.repository.reservation.TimeSettingRepository;
import tw.com.ispan.eeit.repository.store.OpenHourRepository;
import tw.com.ispan.eeit.repository.store.StoreRepository;
import tw.com.ispan.eeit.service.reservation.ReservationService;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private TableRepository tableRepository;

    @Autowired
    private OpenHourRepository openHourRepository;

    @Autowired
    private TimeSettingRepository timeSettingRepository;

    @Autowired
    private ReservationService reservationService;

    @Override
    public void run(String... args) throws Exception {
        // 初始化測試資料
        initializeTestData();
    }

    private void initializeTestData() {
        // 檢查是否已有資料，避免重複初始化
        if (userRepository.count() > 0 && storeRepository.count() > 0) {
            System.out.println("資料庫已有資料，跳過初始化");
            return;
        }

        System.out.println("開始初始化測試資料...");

        // 1. 建立測試用戶
        UserBean testUser = createTestUser();
        userRepository.save(testUser);

        // 2. 建立測試餐廳
        StoreBean testStore = createTestStore();
        storeRepository.save(testStore);

        // 3. 建立桌位
        createTestTables(testStore);

        // 4. 設定營業時間
        createOpenHours(testStore);

        // 5. 建立時段設定
        createTimeSetting(testStore);

        // 6. 生成時段資料
        reservationService.generateTimeSlotsForRestaurant(testStore.getId(), 30);

        System.out.println("測試資料初始化完成！");
        System.out.println("測試用戶 ID: " + testUser.getId());
        System.out.println("測試餐廳 ID: " + testStore.getId());
    }

    private UserBean createTestUser() {
        UserBean user = new UserBean();
        user.setName("測試用戶");
        user.setEmail("test@example.com");
        user.setPassword("password"); // 設定密碼
        user.setPhone("0912345678");
        user.setIsActive(true);
        user.setIsVerify(true); // 設定為已驗證
        return user;
    }

    private StoreBean createTestStore() {
        StoreBean store = new StoreBean();
        store.setName("黃金碗餐廳");
        store.setAddress("台北市信義區信義路五段7號");
        store.setStoreIntro("美味的黃金碗料理");
        store.setIsOpen(true);
        store.setIsActive(true);
        return store;
    }

    private void createTestTables(StoreBean store) {
        // 建立不同大小的桌位
        List<TableBean> tables = Arrays.asList(
                createTable(store, 2, 5, true), // 2人桌 x 5張
                createTable(store, 4, 8, true), // 4人桌 x 8張
                createTable(store, 6, 4, true), // 6人桌 x 4張
                createTable(store, 8, 2, true), // 8人桌 x 2張
                createTable(store, 10, 1, true) // 10人桌 x 1張
        );

        tableRepository.saveAll(tables);
    }

    private TableBean createTable(StoreBean store, Integer seats, Integer quantity, Boolean status) {
        TableBean table = new TableBean();
        table.setStore(store);
        table.setSeats(seats);
        table.setQuantity(quantity);
        table.setStatus(status);
        return table;
    }

    private void createOpenHours(StoreBean store) {
        // 設定週一到週日的營業時間
        DayOfWeek[] weekdays = {
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
        };

        for (DayOfWeek day : weekdays) {
            OpenHourBean openHour = new OpenHourBean();
            openHour.setStore(store);
            openHour.setDayOfWeek(day);
            // openHour.setIsOpen(true);

            // 設定營業時間
            if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
                // 週末營業時間
                openHour.setOpenTime(LocalTime.of(10, 0));
                openHour.setCloseTime(LocalTime.of(23, 0));
            } else {
                // 平日營業時間
                openHour.setOpenTime(LocalTime.of(11, 0));
                openHour.setCloseTime(LocalTime.of(22, 0));
            }

            openHourRepository.save(openHour);
        }
    }

    private void createTimeSetting(StoreBean store) {
        TimeSettingBean timeSetting = new TimeSettingBean();
        timeSetting.setStoreId(store.getId());
        timeSetting.setInterval(30); // 30分鐘間隔
        timeSetting.setMealTime(120); // 120分鐘用餐時間

        timeSettingRepository.save(timeSetting);
    }
}