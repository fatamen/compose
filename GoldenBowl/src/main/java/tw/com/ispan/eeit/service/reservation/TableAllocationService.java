package tw.com.ispan.eeit.service.reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import tw.com.ispan.eeit.model.entity.reservation.ReservationBean;
import tw.com.ispan.eeit.model.entity.reservation.TableBean;
import tw.com.ispan.eeit.repository.reservation.ReservationRepository;
import tw.com.ispan.eeit.repository.reservation.TableRepository;

/**
 * 桌位分配服務
 */
@Service
public class TableAllocationService {

    @Autowired
    private TableRepository tableRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    /**
     * 檢查桌位容量是否足夠（使用快取）
     */
    @Cacheable(value = "tableCapacity", key = "#storeId + '_' + #date + '_' + #time")
    public TableCapacityResult checkTableCapacity(Integer storeId, LocalDate date, LocalTime time,
            Integer guests, Integer duration) {

        LocalTime endTime = time.plusMinutes(duration != null ? duration : 120);

        // 查詢衝突預約
        List<ReservationBean> existingReservations = reservationRepository
                .findConflictingReservationsInTimeRange(storeId, date, time, endTime);

        // 計算已被預約的座位數
        int reservedSeats = existingReservations.stream()
                .mapToInt(ReservationBean::getGuests)
                .sum();

        // 查詢餐廳總座位數（考慮 quantity * seats）
        List<TableBean> allTables = tableRepository.findByStoreId(storeId);
        int totalSeats = allTables.stream()
                .filter(TableBean::getStatus)
                .mapToInt(table -> {
                    int quantity = table.getQuantity() != null ? table.getQuantity() : 1;
                    int seats = table.getSeats() != null ? table.getSeats() : 0;
                    return quantity * seats;
                })
                .sum();

        int availableSeats = totalSeats - reservedSeats;

        // 檢查是否有合適的桌位組合
        List<TableBean> suitableTables = tableRepository
                .findAvailableTablesByStoreIdAndMinSeats(storeId, guests);

        return new TableCapacityResult(
                availableSeats >= guests,
                availableSeats,
                totalSeats,
                reservedSeats,
                !suitableTables.isEmpty(),
                suitableTables);
    }

    /**
     * 尋找最佳桌位組合（使用貪婪演算法）
     */
    public Optional<TableCombination> findBestTableCombination(Integer storeId, Integer guests) {
        List<TableBean> availableTables = tableRepository
                .findAvailableTablesByStoreIdAndMinSeats(storeId, guests);

        if (availableTables.isEmpty()) {
            return Optional.empty();
        }

        // 貪婪演算法：優先選擇最接近需求大小的桌位
        List<TableBean> sortedTables = availableTables.stream()
                .sorted((t1, t2) -> {
                    int diff1 = Math.abs(t1.getSeats() - guests);
                    int diff2 = Math.abs(t2.getSeats() - guests);
                    return Integer.compare(diff1, diff2);
                })
                .toList();

        // 尋找單一桌位解決方案
        Optional<TableBean> singleTable = sortedTables.stream()
                .filter(table -> table.getSeats() >= guests)
                .findFirst();

        if (singleTable.isPresent()) {
            return Optional.of(new TableCombination(List.of(singleTable.get()), singleTable.get().getSeats()));
        }

        // 尋找多桌組合解決方案
        TableCombination combination = findMultiTableCombination(sortedTables, guests);
        return Optional.ofNullable(combination);
    }

    /**
     * 尋找多桌組合（背包問題的簡化版本）
     */
    private TableCombination findMultiTableCombination(List<TableBean> tables, Integer targetGuests) {
        // 簡化的貪婪演算法：從大到小選擇桌位
        List<TableBean> sortedBySize = tables.stream()
                .sorted((t1, t2) -> Integer.compare(t2.getSeats(), t1.getSeats()))
                .toList();

        int currentSeats = 0;
        List<TableBean> selectedTables = new java.util.ArrayList<>();

        for (TableBean table : sortedBySize) {
            if (currentSeats >= targetGuests) {
                break;
            }
            selectedTables.add(table);
            currentSeats += table.getSeats();
        }

        if (currentSeats >= targetGuests) {
            return new TableCombination(selectedTables, currentSeats);
        }

        return null; // 無法找到合適的組合
    }

    /**
     * 桌位容量結果
     */
    public static class TableCapacityResult {
        private final boolean sufficient;
        private final int availableSeats;
        private final int totalSeats;
        private final int reservedSeats;
        private final boolean hasSuitableTables;
        private final List<TableBean> suitableTables;

        public TableCapacityResult(boolean sufficient, int availableSeats, int totalSeats,
                int reservedSeats, boolean hasSuitableTables, List<TableBean> suitableTables) {
            this.sufficient = sufficient;
            this.availableSeats = availableSeats;
            this.totalSeats = totalSeats;
            this.reservedSeats = reservedSeats;
            this.hasSuitableTables = hasSuitableTables;
            this.suitableTables = suitableTables;
        }

        // Getters
        public boolean isSufficient() {
            return sufficient;
        }

        public int getAvailableSeats() {
            return availableSeats;
        }

        public int getTotalSeats() {
            return totalSeats;
        }

        public int getReservedSeats() {
            return reservedSeats;
        }

        public boolean hasSuitableTables() {
            return hasSuitableTables;
        }

        public List<TableBean> getSuitableTables() {
            return suitableTables;
        }
    }

    /**
     * 桌位組合
     */
    public static class TableCombination {
        private final List<TableBean> tables;
        private final int totalSeats;

        public TableCombination(List<TableBean> tables, int totalSeats) {
            this.tables = tables;
            this.totalSeats = totalSeats;
        }

        // Getters
        public List<TableBean> getTables() {
            return tables;
        }

        public int getTotalSeats() {
            return totalSeats;
        }
    }

    /**
     * 桌位分配結果
     */
    public static record TableAllocationResult(
            boolean success,
            List<TableBean> tables,
            String strategyUsed,
            String message) {
        public static TableAllocationResult success(List<TableBean> tables, String strategyUsed) {
            return new TableAllocationResult(true, tables, strategyUsed, "桌位分配成功");
        }

        public static TableAllocationResult failure(String message) {
            return new TableAllocationResult(false, List.of(), "NONE", message);
        }
    }

    /**
     * 🔐 核心方法：分配桌位
     */
    public TableAllocationResult allocateTables(Integer storeId, Integer guests,
            LocalDate date, LocalTime time, Integer duration) {

        try {
            System.out.println("=== 開始桌位分配 ===");
            System.out.println("餐廳ID: " + storeId + ", 客人數: " + guests + ", 日期: " + date + ", 時間: " + time
                    + ", 用餐時長: " + duration);

            // 取得該時段所有可用桌位
            List<TableBean> availableTables = getAvailableTablesInTimeRange(storeId, date, time, duration);
            System.out.println("查詢到可用桌位數量: " + (availableTables != null ? availableTables.size() : "null"));

            if (availableTables.isEmpty()) {
                System.out.println("沒有可用桌位");
                return TableAllocationResult.failure("該時段沒有可用桌位");
            }

            // 打印可用桌位詳情
            for (int i = 0; i < availableTables.size(); i++) {
                TableBean table = availableTables.get(i);
                System.out.println("桌位" + (i + 1) + ": ID=" + table.getId() + ", 座位數=" + table.getSeats() + ", 狀態="
                        + table.getStatus());
            }

            // 策略 1: 尋找恰好合適的桌位（座位數 = 客人數 或 客人數+1）
            System.out.println("嘗試策略1: 尋找恰好合適的桌位");
            Optional<TableBean> exactMatch = availableTables.stream()
                    .filter(table -> {
                        boolean matches = table.getSeats() != null && table.getSeats() >= guests
                                && table.getSeats() <= guests + 1;
                        System.out.println("桌位ID" + table.getId() + "座位數" + table.getSeats() + " 是否匹配: " + matches);
                        return matches;
                    })
                    .min(java.util.Comparator.comparing(TableBean::getSeats));

            if (exactMatch.isPresent()) {
                System.out.println("策略1成功: 找到恰好合適的桌位ID " + exactMatch.get().getId());
                return TableAllocationResult.success(List.of(exactMatch.get()), "EXACT_MATCH");
            }

            // 策略 2: 尋找最小的能容納所有客人的桌位
            System.out.println("嘗試策略2: 尋找最小容納桌位");
            Optional<TableBean> minSufficientTable = availableTables.stream()
                    .filter(table -> {
                        boolean matches = table.getSeats() != null && table.getSeats() >= guests;
                        System.out.println("桌位ID" + table.getId() + "座位數" + table.getSeats() + " 能否容納: " + matches);
                        return matches;
                    })
                    .min(java.util.Comparator.comparing(TableBean::getSeats));

            if (minSufficientTable.isPresent()) {
                System.out.println("策略2成功: 找到最小容納桌位ID " + minSufficientTable.get().getId());
                return TableAllocationResult.success(List.of(minSufficientTable.get()), "MIN_SUITABLE");
            }

            // 策略 3: 組合多個小桌位（簡化版本 - 最多組合2個桌位）
            System.out.println("嘗試策略3: 組合多個桌位");
            for (TableBean table1 : availableTables) {
                if (table1.getSeats() == null) {
                    System.out.println("桌位ID" + table1.getId() + " 座位數為null，跳過");
                    continue;
                }
                for (TableBean table2 : availableTables) {
                    if (table2.getSeats() == null || table1.getId().equals(table2.getId()))
                        continue;

                    int combinedSeats = table1.getSeats() + table2.getSeats();
                    System.out.println("嘗試組合桌位ID" + table1.getId() + "(" + table1.getSeats() + "位) + ID"
                            + table2.getId() + "(" + table2.getSeats() + "位) = " + combinedSeats + "位");

                    if (combinedSeats >= guests) {
                        System.out.println("策略3成功: 組合桌位ID" + table1.getId() + " + ID" + table2.getId());
                        return TableAllocationResult.success(List.of(table1, table2), "COMBINED");
                    }
                }
            }

            System.out.println("所有策略都失敗");
            return TableAllocationResult.failure("無法找到合適的桌位組合");

        } catch (Exception e) {
            System.err.println("桌位分配異常: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            return TableAllocationResult.failure("桌位分配過程發生錯誤: " + e.getMessage());
        }
    }

    /**
     * 檢查指定時間範圍內的可用桌位
     */
    private List<TableBean> getAvailableTablesInTimeRange(Integer storeId, LocalDate date,
            LocalTime startTime, Integer duration) {

        try {
            System.out.println("--- 檢查可用桌位 ---");
            if (duration == null || duration <= 0) {
                duration = 120; // 預設用餐時間2小時
                System.out.println("使用預設用餐時間: " + duration + "分鐘");
            }

            LocalTime endTime = startTime.plusMinutes(duration);
            System.out.println("用餐時間範圍: " + startTime + " ~ " + endTime);

            // 取得餐廳所有啟用的桌位
            List<TableBean> allTables = tableRepository.findByStoreId(storeId);
            System.out.println("餐廳總桌位數: " + (allTables != null ? allTables.size() : "null"));

            if (allTables.isEmpty()) {
                System.out.println("餐廳沒有任何桌位");
                return List.of();
            }

            List<TableBean> activeTables = filterActiveTables(allTables);
            System.out.println("啟用的桌位數: " + activeTables.size());

            // 預先查出所有衝突預約（優化：減少多次 DB call）
            List<ReservationBean> conflictingReservations = reservationRepository
                    .findConflictingReservationsInTimeRange(storeId, date, startTime, endTime);

            // 檢查每個桌位在該時段是否被預約
            List<TableBean> availableTables = activeTables.stream()
                    .filter(table -> {
                        boolean isAvailable = isTableNotUsed(table.getId(), conflictingReservations);
                        System.out.println("桌位ID" + table.getId() + " 在時段內是否可用: " + isAvailable);
                        return isAvailable;
                    })
                    .toList();

            System.out.println("最終可用桌位數: " + availableTables.size());
            return availableTables;
        } catch (Exception e) {
            System.err.println("取得可用桌位時發生錯誤: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * 抽出啟用桌位過濾邏輯
     */
    private List<TableBean> filterActiveTables(List<TableBean> tables) {
        return tables.stream()
                .filter(table -> Boolean.TRUE.equals(table.getStatus()))
                .toList();
    }

    /**
     * 檢查桌位是否未被使用
     */
    private boolean isTableNotUsed(Integer tableId, List<ReservationBean> reservations) {
        // 暫時跳過桌位關聯檢查，因為 ReservationBean 和 TableBean 的關聯被暫時移除了
        // 改為檢查時間衝突：如果該時段有預約，就認為桌位不可用
        return reservations.isEmpty();

        // 原本的邏輯（當關聯恢復時使用）：
        // return reservations.stream()
        // .filter(r -> r.getTables() != null)
        // .noneMatch(r -> r.getTables().stream()
        // .anyMatch(t -> tableId.equals(t.getId())));
    }
}