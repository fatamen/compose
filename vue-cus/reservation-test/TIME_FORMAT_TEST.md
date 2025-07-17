# 時間格式轉換測試

## 🔧 問題描述

後端返回的時間格式是 `LocalTime` 對象，但前端需要 `HH:mm` 格式的字符串。

## 📋 修改內容

### 1. 新增時間格式轉換函數

在 `vue-cus/src/utils/timeSlotUtils.js` 中新增：

```javascript
export function formatTimeToString(time) {
  if (!time) return "";

  // 如果已經是字符串格式，直接返回
  if (typeof time === "string") {
    return time;
  }

  // 如果是 LocalTime 對象（包含 hour, minute 屬性）
  if (time && typeof time === "object") {
    try {
      const hour = String(time.hour || 0).padStart(2, "0");
      const minute = String(time.minute || 0).padStart(2, "0");
      return `${hour}:${minute}`;
    } catch (error) {
      console.error("格式化時間時發生錯誤:", error, time);
      return "";
    }
  }

  console.error("無效的時間格式:", time);
  return "";
}
```

### 2. 修改時間段分組函數

在 `groupTimeSlotsByPeriod` 函數中使用新的格式轉換：

```javascript
const slots = timeSlots
  .map((slot) => formatTimeToString(slot.startTime))
  .sort();
```

### 3. 修改已預訂時間段處理

在 `disabledTimeSlots` 計算屬性中使用新的格式轉換：

```javascript
.map(slot => formatTimeToString(slot.startTime))
```

### 4. 修改時間段檢查函數

在 `isTimeSlotBooked` 函數中使用新的格式轉換：

```javascript
formatTimeToString(booking.startTime) === timeSlot;
```

## 🧪 測試案例

### 測試案例 1: LocalTime 對象轉換

```javascript
// 輸入
const localTime = { hour: 14, minute: 30 };
// 期望輸出
("14:30");
```

### 測試案例 2: 字符串格式保持不變

```javascript
// 輸入
const timeString = "14:30";
// 期望輸出
("14:30");
```

### 測試案例 3: 無效輸入處理

```javascript
// 輸入
const invalidTime = null;
// 期望輸出
("");
```

## 📊 預期結果

### 修復前

- 後端返回：`{ hour: 14, minute: 30 }`
- 前端顯示：`[object Object]` 或錯誤

### 修復後

- 後端返回：`{ hour: 14, minute: 30 }`
- 前端顯示：`14:30`

## 🔍 驗證步驟

1. **檢查時間段顯示**

   - 打開預約表單
   - 選擇一個日期
   - 確認時間段顯示為 `HH:mm` 格式

2. **檢查已預訂時間段**

   - 創建一個預約
   - 再次打開預約表單
   - 確認已預訂的時間段被正確禁用

3. **檢查時間段分組**
   - 確認午餐、下午、晚餐時段正確分組
   - 確認時間格式一致

## ⚠️ 注意事項

1. **向後兼容性**: 函數同時支持字符串和對象格式
2. **錯誤處理**: 無效輸入會返回空字符串並記錄錯誤
3. **性能考慮**: 轉換函數輕量級，不會影響性能

## 🐛 故障排除

### 常見問題

1. **時間顯示為 [object Object]**: 確認使用了 `formatTimeToString` 函數
2. **時間格式不正確**: 檢查後端返回的時間對象結構
3. **已預訂時間段不正確**: 確認 `disabledTimeSlots` 使用了格式轉換

### 調試技巧

1. 在瀏覽器控制台檢查時間對象結構
2. 使用 `console.log` 檢查轉換結果
3. 確認所有相關函數都使用了新的格式轉換
