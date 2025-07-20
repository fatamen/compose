# 特殊休息時段測試

## 🎯 測試目標

驗證當特殊營業時間中 `isclose=0` 時，`opentime` 到 `closetime` 之間的時間段被正確禁用：

- **opentime 到 closetime 之間**：**不可選取**（被禁用）
- **opentime 到 closetime 以外**：**可以選取**（正常營業時間）

## 📅 測試案例

### 案例 1: 特殊休息時段設定

**設定：**

```bash
POST /api/stores/1/special/business?date=2025-01-21&openTime=14:00&closeTime=16:00&reason=下午休息時段
```

**測試日期：** 2025-01-21

**特殊設定：**

- `isclose`: 0
- `openTime`: 14:00
- `closeTime`: 16:00

**預期結果：**

- 14:00-16:00 之間的時間段應被禁用
- 14:00, 14:30, 15:00, 15:30 這些時間段不可選取
- 11:00, 12:00, 13:00, 16:00, 17:00 等時間段可以正常選取

**API 檢查：**

```bash
GET /api/stores/1/special/check/2025-01-21
```

預期回應：

```json
{
  "exists": true,
  "isClose": false,
  "openTime": "14:00",
  "closeTime": "16:00",
  "date": "2025-01-21"
}
```

### 案例 2: 跨小時休息時段

**設定：**

```bash
POST /api/stores/1/special/business?date=2025-01-22&openTime=13:30&closeTime=15:30&reason=跨小時休息時段
```

**測試日期：** 2025-01-22

**特殊設定：**

- `isclose`: 0
- `openTime`: 13:30
- `closeTime`: 15:30

**預期結果：**

- 13:30-15:30 之間的時間段應被禁用
- 13:30, 14:00, 14:30, 15:00 這些時間段不可選取
- 11:00, 12:00, 13:00, 15:30, 16:00 等時間段可以正常選取

### 案例 3: 全天休息時段

**設定：**

```bash
POST /api/stores/1/special/business?date=2025-01-23&openTime=00:00&closeTime=23:59&reason=全天休息
```

**測試日期：** 2025-01-23

**特殊設定：**

- `isclose`: 0
- `openTime`: 00:00
- `closeTime`: 23:59

**預期結果：**

- 所有時間段應被禁用
- 顯示「當日為公休日，暫不提供預約服務」

## 🔧 前端測試步驟

### 1. 檢查特殊休息時段數據

在瀏覽器控制台執行：

```javascript
// 檢查特殊休息時段
console.log("特殊休息時段:", specialRestTimeSlots.value);

// 檢查禁用時間段
console.log("禁用時間段:", disabledTimeSlots.value);

// 檢查時間段顯示
console.log("時間段顯示:", timeSections.value);
```

### 2. 檢查特定時間是否被禁用

```javascript
// 檢查特定時間是否被禁用
const testTime = "14:30";
const isDisabled = disabledTimeSlots.value.includes(testTime);
console.log(`${testTime} 是否被禁用:`, isDisabled);

// 檢查休息時段內的所有時間
const restStart = "14:00";
const restEnd = "16:00";
const restTimes = ["14:00", "14:30", "15:00", "15:30"];
restTimes.forEach((time) => {
  const disabled = disabledTimeSlots.value.includes(time);
  console.log(`${time} 是否被禁用:`, disabled);
});
```

### 3. 檢查 API 調用

在 Network 標籤中檢查以下 API 調用：

```bash
# 檢查特殊營業時間
GET /api/stores/1/special/check/2025-01-21

# 檢查時間段數據
GET /api/booking/all-slots/1?date=2025-01-21
```

## 🎨 視覺驗證

### 禁用時間段樣式

```css
/* 禁用時間段的樣式 */
.time-slot.disabled {
  background-color: var(--restaurant-bg-secondary) !important;
  color: var(--restaurant-text-light) !important;
  border: 1px solid var(--restaurant-border-light) !important;
  cursor: not-allowed !important;
  opacity: 0.5 !important;
}
```

### 驗證步驟

1. **視覺檢查：**

   - 休息時段內的時間段應顯示為灰色
   - 透明度應為 0.5
   - 邊框應為淺色

2. **互動檢查：**

   - 滑鼠懸停時游標應為 `not-allowed`
   - 點擊時不應有任何反應

3. **功能檢查：**
   - 休息時段外的時間段可以正常選取
   - 選取後可以正常提交預約

## 🐛 故障排除

### 問題 1: 特殊休息時段未正確禁用

**症狀：** 休息時段內的時間段仍然可以選取

**解決方案：**

```javascript
// 檢查特殊休息時段數據
console.log("特殊休息時段數據:", specialRestTimeSlots.value);

// 檢查生成的禁用時間段
const { startTime, endTime } = specialRestTimeSlots.value;
console.log("休息時段範圍:", startTime, "到", endTime);

// 檢查 API 響應
const response = await fetch(`/api/stores/1/special/check/2025-01-21`);
const data = await response.json();
console.log("API 響應:", data);
```

### 問題 2: 時間段生成錯誤

**症狀：** 休息時段內的時間段生成不正確

**解決方案：**

```javascript
// 手動測試時間段生成
const startTime = "14:00";
const endTime = "16:00";

const startHour = parseInt(startTime.split(":")[0]);
const startMinute = parseInt(startTime.split(":")[1]);
const endHour = parseInt(endTime.split(":")[0]);
const endMinute = parseInt(endTime.split(":")[1]);

console.log("解析的時間:", { startHour, startMinute, endHour, endMinute });

// 生成時間段
let currentHour = startHour;
let currentMinute = startMinute;
const generatedSlots = [];

while (
  currentHour < endHour ||
  (currentHour === endHour && currentMinute < endMinute)
) {
  const timeString = `${String(currentHour).padStart(2, "0")}:${String(
    currentMinute
  ).padStart(2, "0")}`;
  generatedSlots.push(timeString);

  currentMinute += 30;
  if (currentMinute >= 60) {
    currentHour += 1;
    currentMinute = 0;
  }
}

console.log("生成的時間段:", generatedSlots);
```

### 問題 3: 日期變化時未更新

**症狀：** 切換日期後特殊休息時段未更新

**解決方案：**

```javascript
// 強制更新特殊休息時段
await updateSpecialRestTimeSlots();

// 檢查更新後的數據
console.log("更新後的特殊休息時段:", specialRestTimeSlots.value);
console.log("更新後的禁用時間段:", disabledTimeSlots.value);
```

## 📊 預期結果

### 成功情況

```javascript
// 特殊休息時段數據
specialRestTimeSlots: {
  startTime: "14:00",
  endTime: "16:00",
  reason: "特殊休息時段"
}

// 禁用時間段
disabledTimeSlots: [
  "14:00",  // 特殊休息時段
  "14:30",  // 特殊休息時段
  "15:00",  // 特殊休息時段
  "15:30",  // 特殊休息時段
  "16:00"   // 已預訂時段
]

// 可選取時間段
availableSlots: [
  "11:00",  // 正常營業時間
  "11:30",  // 正常營業時間
  "12:00",  // 正常營業時間
  "12:30",  // 正常營業時間
  "13:00",  // 正常營業時間
  "13:30",  // 正常營業時間
  "16:30",  // 正常營業時間
  "17:00"   // 正常營業時間
]
```

### 失敗情況

```javascript
// 特殊休息時段為空
specialRestTimeSlots: null;

// 或 API 錯誤
error: "Failed to fetch";
```

## 🎯 驗證清單

- [ ] 特殊休息時段正確獲取
- [ ] 休息時段內的時間段被禁用
- [ ] 休息時段外的時間段可以選取
- [ ] 禁用時間段的樣式正確
- [ ] 滑鼠懸停時游標正確
- [ ] 點擊禁用時間段無反應
- [ ] 日期變化時正確更新
- [ ] API 調用成功
- [ ] 錯誤處理正確
- [ ] 時間段生成邏輯正確
