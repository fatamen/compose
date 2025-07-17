# 日期計算器 - 測試日期設定

## 📅 資料庫資料範圍

資料庫只生成從今天開始算 7 天的資料，請使用以下日期進行測試：

## 🗓️ 測試日期建議

### 今天 (Day 1)

- **日期**: 2025-01-15
- **說明**: 今天的資料，最適合測試

### 明天 (Day 2)

- **日期**: 2025-01-16
- **說明**: 明天的資料，適合測試未來預約

### 後天 (Day 3)

- **日期**: 2025-01-17
- **說明**: 後天的資料

### 第 4 天

- **日期**: 2025-01-18

### 第 5 天

- **日期**: 2025-01-19

### 第 6 天

- **日期**: 2025-01-20

### 第 7 天

- **日期**: 2025-01-21
- **說明**: 最後一天有資料的日期

## 🔧 如何更新測試日期

### 方法 1: 手動更新環境變數

1. 在 Postman 中選擇環境 "GoldenBowl 預約系統環境"
2. 點擊右上角的齒輪圖示
3. 找到 `TEST_DATE` 變數
4. 將值改為上述任一日期
5. 點擊 "Save"

### 方法 2: 使用預設腳本

在 Postman 的 Console 中執行以下腳本來自動設定今天的日期：

```javascript
// 設定今天的日期
const today = new Date();
const year = today.getFullYear();
const month = String(today.getMonth() + 1).padStart(2, "0");
const day = String(today.getDate()).padStart(2, "0");
const todayString = `${year}-${month}-${day}`;

// 更新環境變數
pm.environment.set("TEST_DATE", todayString);
console.log("已設定測試日期為:", todayString);
```

### 方法 3: 批量更新所有日期

```javascript
// 批量更新未來7天的日期
const today = new Date();
for (let i = 0; i < 7; i++) {
  const testDate = new Date(today);
  testDate.setDate(today.getDate() + i);

  const year = testDate.getFullYear();
  const month = String(testDate.getMonth() + 1).padStart(2, "0");
  const day = String(testDate.getDate()).padStart(2, "0");
  const dateString = `${year}-${month}-${day}`;

  pm.environment.set(`TEST_DATE_${i + 1}`, dateString);
  console.log(`TEST_DATE_${i + 1}:`, dateString);
}
```

## ⚠️ 重要提醒

1. **不要使用過去的日期**: 資料庫沒有過去的資料
2. **不要使用 7 天後的日期**: 資料庫沒有未來的資料
3. **建議使用今天或明天的日期**: 確保有完整的測試資料

## 📊 測試策略

### 第一天測試

- 使用今天的日期
- 測試基本功能
- 驗證資料載入

### 第二天測試

- 使用明天的日期
- 測試預約功能
- 驗證業務邏輯

### 第三天測試

- 使用後天的日期
- 測試邊界情況
- 驗證錯誤處理

## 🎯 推薦測試順序

1. **設定日期**: 使用今天的日期
2. **測試時間段獲取**: 確認能獲取資料
3. **測試可用性檢查**: 確認邏輯正確
4. **測試預約創建**: 確認能成功預約
5. **測試預約查詢**: 確認能查詢記錄
6. **測試取消預約**: 確認能取消預約

## 🔍 驗證方法

### 檢查資料是否存在

```bash
# 在後端控制台查看日誌
# 或使用資料庫查詢
SELECT * FROM time_slots WHERE day >= CURDATE() AND day <= DATE_ADD(CURDATE(), INTERVAL 7 DAY);
```

### 檢查 API 響應

- 狀態碼應該是 200
- 響應應該包含資料
- 不應該返回空陣列
