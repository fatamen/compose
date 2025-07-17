# 時間段按鈕禁用問題調試

## 🔍 問題分析

時間段按鈕被錯誤禁用可能的原因：

1. **時間格式不匹配**
2. **已預訂數據加載問題**
3. **日期格式不匹配**
4. **API 響應格式問題**

## 🛠️ 調試步驟

### 1. 檢查瀏覽器控制台

在瀏覽器開發者工具的控制台中執行以下代碼：

```javascript
// 檢查時間段數據
console.log("時間段數據:", timeSlots.value);

// 檢查已預訂數據
console.log("已預訂數據:", bookedSlots.value);

// 檢查禁用時間段
console.log("禁用時間段:", disabledTimeSlots.value);

// 檢查選中日期
console.log("選中日期:", date.value);
```

### 2. 檢查時間格式

```javascript
// 測試時間格式轉換
console.log('formatTimeToString("14:30:00"):', formatTimeToString("14:30:00"));
console.log('formatTimeToString("14:30"):', formatTimeToString("14:30"));

// 檢查時間段顯示格式
console.log("時間段顯示:", timeSections.value);
```

### 3. 檢查 API 響應

在 Network 標籤中檢查以下 API 響應：

```bash
# 獲取時間段數據
GET /api/booking/all-slots/1?date=2024-01-15

# 獲取已預訂時間段
GET /api/booking/slots/1?date=2024-01-15&guests=1
```

### 4. 手動測試禁用邏輯

```javascript
// 測試特定時間是否被禁用
const testTime = "14:30";
const isDisabled = disabledTimeSlots.value.includes(testTime);
console.log(`${testTime} 是否被禁用:`, isDisabled);
```

## 🔧 常見問題解決方案

### 問題 1: 時間格式不匹配

**症狀**: 時間段顯示為 `"14:30"` 但禁用列表中是 `"14:30:00"`

**解決方案**: 確保 `formatTimeToString` 函數正確處理 ISO 格式

```javascript
// 修改 formatTimeToString 函數
export function formatTimeToString(time) {
  if (!time) return "";

  if (typeof time === "string") {
    // 處理 ISO 格式 (14:30:00)
    if (time.includes(":") && time.length >= 5) {
      return time.substring(0, 5); // 返回 14:30
    }
    return time;
  }

  // 處理 LocalTime 對象
  if (time && typeof time === "object") {
    const hour = String(time.hour || 0).padStart(2, "0");
    const minute = String(time.minute || 0).padStart(2, "0");
    return `${hour}:${minute}`;
  }

  return "";
}
```

### 問題 2: 已預訂數據為空

**症狀**: `bookedSlots.value` 為空數組

**解決方案**: 檢查 API 調用和錯誤處理

```javascript
// 在 fetchBookedSlots 函數中添加調試
const fetchBookedSlots = async (selectedDate = null) => {
  try {
    const dateParam = selectedDate ? formatDateToString(selectedDate) : null;
    console.log(
      "正在獲取已預訂時間段，餐廳ID:",
      props.restaurantId,
      "日期:",
      dateParam
    );

    const result = await fetchBookedTimeSlots(props.restaurantId, dateParam);
    console.log("API 響應:", result);

    bookedSlots.value = Array.isArray(result) ? result : [];
    console.log("已預訂時間段:", bookedSlots.value);
  } catch (error) {
    console.error("抓取已預訂時間段失敗:", error);
    bookedSlots.value = [];
  }
};
```

### 問題 3: 日期格式不匹配

**症狀**: 日期比較失敗

**解決方案**: 確保日期格式一致

```javascript
// 在 disabledTimeSlots 中添加調試
const disabledTimeSlots = computed(() => {
  if (!date.value || !bookedSlots.value) {
    console.log("日期或已預訂數據為空");
    return [];
  }

  try {
    const dateString = formatDateToString(date.value);
    console.log("格式化日期:", dateString);

    const filtered = bookedSlots.value
      .filter((slot) => {
        const slotDate = slot.date || slot.day;
        console.log("比較日期:", slotDate, "vs", dateString);
        return slotDate === dateString;
      })
      .map((slot) => {
        const formattedTime = formatTimeToString(slot.startTime);
        console.log("格式化時間:", slot.startTime, "->", formattedTime);
        return formattedTime;
      })
      .filter((time) => time);

    console.log("最終禁用時間段:", filtered);
    return filtered;
  } catch (error) {
    console.error("處理已預訂時間段時發生錯誤:", error);
    return [];
  }
});
```

## 📊 預期結果

### 正常情況

```javascript
// 時間段數據
timeSlots: [
  { day: "2024-01-15", startTime: "11:00:00", endTime: "12:00:00" },
  { day: "2024-01-15", startTime: "14:30:00", endTime: "15:30:00" },
];

// 已預訂數據
bookedSlots: [{ date: "2024-01-15", startTime: "14:30:00" }];

// 禁用時間段
disabledTimeSlots: ["14:30"];

// 時間段顯示
timeSections: [
  { label: "午餐時段", slots: ["11:00"] },
  { label: "下午時段", slots: ["14:30"] },
];
```

### 問題情況

```javascript
// 時間格式不匹配
disabledTimeSlots: ["14:30:00"]; // 應該是 ["14:30"]

// 日期不匹配
bookedSlots: [
  { date: "2024-01-15T00:00:00", startTime: "14:30:00" }, // 日期格式不一致
];

// 空數據
bookedSlots: []; // API 調用失敗
```

## 🐛 故障排除

### 1. 檢查 API 狀態

```bash
# 檢查後端是否運行
curl "http://localhost:8080/actuator/health"

# 檢查時間段 API
curl "http://localhost:8080/api/booking/all-slots/1?date=2024-01-15"

# 檢查已預訂 API
curl "http://localhost:8080/api/booking/slots/1?date=2024-01-15&guests=1"
```

### 2. 檢查網路請求

- 打開瀏覽器開發者工具
- 切換到 Network 標籤
- 重新載入頁面
- 檢查 API 請求的狀態碼和響應

### 3. 檢查控制台錯誤

- 查看是否有 JavaScript 錯誤
- 檢查 API 調用是否失敗
- 確認數據格式是否正確

## ✅ 驗證修復

修復後應該看到：

1. **時間段正常顯示**：所有可用時間段都可以點擊
2. **已預訂時間段禁用**：只有真正被預訂的時間段被禁用
3. **控制台無錯誤**：沒有 JavaScript 錯誤
4. **API 響應正常**：所有 API 都返回正確的數據
