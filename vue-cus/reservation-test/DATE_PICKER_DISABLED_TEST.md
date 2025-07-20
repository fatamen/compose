# 日期選擇器禁用日期測試

## 🎯 測試目標

驗證日期選擇器中不營業的日期按鈕被正確禁用：

1. 一般公休日（`openhour` 中 `opentime` 與 `closetime` 皆是 `null`）
2. 特殊休假日（`specialhour` 中 `isclose=1`）
3. 特殊營業時間中的公休日（`specialhour` 中 `opentime` 與 `closetime` 皆是 `null`）

## 📅 測試案例

### 案例 1: 一般公休日

**設定：**

```sql
-- 設定週一為公休日
UPDATE open_hour SET open_time = NULL, close_time = NULL WHERE store_id = 1 AND day = 1;
```

**測試日期：** 2025-01-20 (週一)

**預期結果：**

- 日期選擇器中 2025-01-20 應被禁用
- 按鈕顯示為灰色，不可點選
- 滑鼠懸停時顯示 `cursor: not-allowed`

**API 檢查：**

```bash
GET /api/stores/1/open-hours
```

預期回應：

```json
[
  {
    "dayOfWeek": "MONDAY",
    "openTime": null,
    "closeTime": null
  }
]
```

### 案例 2: 特殊休假日 (isclose=1)

**設定：**

```bash
POST /api/stores/1/special/holiday?date=2025-01-21&reason=測試特殊休假日
```

**測試日期：** 2025-01-21

**預期結果：**

- 日期選擇器中 2025-01-21 應被禁用
- 按鈕顯示為灰色，不可點選

**API 檢查：**

```bash
GET /api/stores/1/special/check/2025-01-21
```

預期回應：

```json
{
  "exists": true,
  "isClose": true,
  "openTime": null,
  "closeTime": null,
  "date": "2025-01-21"
}
```

### 案例 3: 特殊營業時間中的公休日

**設定：**

```bash
POST /api/stores/1/special/business?date=2025-01-22&openTime=null&closeTime=null&reason=特殊公休日
```

**測試日期：** 2025-01-22

**預期結果：**

- 日期選擇器中 2025-01-22 應被禁用
- 按鈕顯示為灰色，不可點選

**API 檢查：**

```bash
GET /api/stores/1/special/check/2025-01-22
```

預期回應：

```json
{
  "exists": true,
  "isClose": false,
  "openTime": null,
  "closeTime": null,
  "date": "2025-01-22"
}
```

## 🔧 前端測試步驟

### 1. 檢查禁用日期計算

在瀏覽器控制台執行：

```javascript
// 檢查動態禁用日期
console.log("動態禁用日期:", dynamicDisabledDates.value);

// 檢查靜態禁用日期
console.log("靜態禁用日期:", staticDisabledDates.value);

// 檢查合併後的禁用日期
console.log("合併禁用日期:", disabledDates.value);
```

### 2. 檢查日期選擇器狀態

```javascript
// 檢查特定日期是否被禁用
const testDate = new Date("2025-01-21");
const isDisabled = disabledDates.value.some(
  (date) =>
    date.toISOString().split("T")[0] === testDate.toISOString().split("T")[0]
);
console.log("2025-01-21 是否被禁用:", isDisabled);
```

### 3. 檢查 API 調用

在 Network 標籤中檢查以下 API 調用：

```bash
# 檢查特殊營業時間
GET /api/stores/1/special/check/2025-01-21

# 檢查營業時間設定
GET /api/stores/1/open-hours
```

## 🎨 CSS 樣式驗證

### 禁用日期樣式

```css
/* 禁用日期的樣式 */
.reservation-form-container
  .p-datepicker
  .p-datepicker-day.p-datepicker-day-disabled {
  background-color: var(--restaurant-bg-secondary) !important;
  color: var(--restaurant-text-light) !important;
  border: 1px solid var(--restaurant-border-light) !important;
  cursor: not-allowed !important;
  opacity: 0.5 !important;
}
```

### 驗證步驟

1. **視覺檢查：**

   - 禁用日期應顯示為灰色
   - 透明度應為 0.5
   - 邊框應為淺色

2. **互動檢查：**

   - 滑鼠懸停時游標應為 `not-allowed`
   - 點擊時不應有任何反應

3. **可訪問性檢查：**
   - 禁用日期應有 `aria-disabled="true"` 屬性
   - 螢幕閱讀器應能識別禁用狀態

## 🐛 故障排除

### 問題 1: 禁用日期未正確計算

**症狀：** 應該被禁用的日期仍然可以選擇

**解決方案：**

```javascript
// 檢查 isAnyClosedDay 函數
const testDate = new Date("2025-01-21");
const isClosed = await isAnyClosedDay(testDate);
console.log("isAnyClosedDay 結果:", isClosed);

// 檢查 API 響應
const response = await fetch(`/api/stores/1/special/check/2025-01-21`);
const data = await response.json();
console.log("API 響應:", data);
```

### 問題 2: 日期選擇器未更新

**症狀：** 禁用日期計算正確，但選擇器未反映

**解決方案：**

```javascript
// 強制重新計算
await calculateDisabledDates();

// 檢查 computed 屬性
console.log("disabledDates computed:", disabledDates.value);
```

### 問題 3: API 調用失敗

**症狀：** 控制台顯示 API 錯誤

**解決方案：**

```bash
# 檢查後端是否運行
curl "http://localhost:8080/actuator/health"

# 檢查 API 端點
curl "http://localhost:8080/api/stores/1/special/check/2025-01-21"
```

## 📊 預期結果

### 成功情況

```javascript
// 禁用日期列表
disabledDates: [
  Date(2025 - 01 - 20), // 一般公休日
  Date(2025 - 01 - 21), // 特殊休假日
  Date(2025 - 01 - 22), // 特殊公休日
];

// 日期選擇器狀態
// 2025-01-20: disabled
// 2025-01-21: disabled
// 2025-01-22: disabled
// 2025-01-23: enabled
```

### 失敗情況

```javascript
// 禁用日期為空
disabledDates: [];

// 或 API 錯誤
error: "Failed to fetch";
```

## 🎯 驗證清單

- [ ] 一般公休日被正確禁用
- [ ] 特殊休假日被正確禁用
- [ ] 特殊公休日被正確禁用
- [ ] 正常營業日可以選擇
- [ ] 禁用日期的樣式正確
- [ ] 滑鼠懸停時游標正確
- [ ] 點擊禁用日期無反應
- [ ] 螢幕閱讀器支援正確
- [ ] API 調用成功
- [ ] 錯誤處理正確
