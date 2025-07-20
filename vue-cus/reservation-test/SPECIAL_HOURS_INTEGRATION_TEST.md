# 特殊營業時間整合測試

## 測試目標

驗證預約表單中特殊營業時間的處理邏輯：

1. 當 `isclose=1` 時，當日應被標示為休假日
2. 當 `isclose=0` 且有 `opentime` 和 `endtime` 時，應顯示特殊營業時間的時段
3. 當 `openhour` 中的 `opentime` 與 `closetime` 皆是 `null` 時，當日應被標示為公休日

## 測試案例

### 案例 1: 特殊休假日 (isclose=1)

**設定：**

```bash
POST /api/stores/1/special/holiday?date=2025-01-15&reason=測試特殊休假日
```

**預期結果：**

- 預約表單中 2025-01-15 應顯示「當日為公休日，暫不提供預約服務」
- 不應顯示任何時間段

**API 檢查：**

```bash
GET /api/stores/1/special/check/2025-01-15
```

預期回應：

```json
{
  "exists": true,
  "isClose": true,
  "openTime": null,
  "closeTime": null,
  "date": "2025-01-15"
}
```

### 案例 2: 特殊營業時間 (isclose=0, 有營業時間)

**設定：**

```bash
POST /api/stores/1/special/business?date=2025-01-16&openTime=14:00&closeTime=20:00&reason=測試特殊營業時間
```

**預期結果：**

- 預約表單中 2025-01-16 應顯示 14:00-20:00 的時間段
- 其他時間段應被禁用

**API 檢查：**

```bash
GET /api/stores/1/special/check/2025-01-16
```

預期回應：

```json
{
  "exists": true,
  "isClose": false,
  "openTime": "14:00",
  "closeTime": "20:00",
  "date": "2025-01-16"
}
```

### 案例 3: 一般公休日 (opentime=null, closetime=null)

**設定：**

```bash
PUT /api/stores/1/hours/1?openTime=null&closeTime=null&isOpen=false
```

**預期結果：**

- 預約表單中該星期的對應日期應顯示「當日為公休日，暫不提供預約服務」
- 不應顯示任何時間段

**API 檢查：**

```bash
GET /api/stores/1/hours/closed/date/2025-01-20
```

預期回應：

```json
true
```

## 前端邏輯測試

### 1. 檢查 `isClosedDay` 函數

```javascript
// 測試一般公休日檢查
const testDate = new Date("2025-01-20"); // 假設週一為公休日
const isClosed = isClosedDay(testDate);
console.log("一般公休日檢查:", isClosed); // 應為 true
```

### 2. 檢查 `isSpecialClosedDay` 函數

```javascript
// 測試特殊休假日檢查
const testDate = new Date("2025-01-15");
const isSpecialClosed = await isSpecialClosedDay(testDate);
console.log("特殊休假日檢查:", isSpecialClosed); // 應為 true
```

### 3. 檢查 `isAnyClosedDay` 函數

```javascript
// 測試綜合休假日檢查
const testDate = new Date("2025-01-15");
const isAnyClosed = await isAnyClosedDay(testDate);
console.log("綜合休假日檢查:", isAnyClosed); // 應為 true
```

## 後端邏輯測試

### 1. 檢查特殊營業時間處理

```java
// 測試 ReservationService.handleSpecialBusinessHours
@Test
public void testHandleSpecialBusinessHours() {
    // 設定特殊休假日
    SpecialHoursBean special = new SpecialHoursBean();
    special.setIsClose(true);
    special.setDate(LocalDate.of(2025, 1, 15));

    // 驗證時段被停用
    List<TimeSlot> timeSlots = timeSlotRepository.findByStoreAndDay(store, date);
    assertTrue(timeSlots.stream().allMatch(slot -> !slot.getIsActive()));
}
```

### 2. 檢查時段生成邏輯

```java
// 測試特殊營業時間時段生成
@Test
public void testGenerateSpecialTimeSlots() {
    LocalTime openTime = LocalTime.of(14, 0);
    LocalTime closeTime = LocalTime.of(20, 0);

    // 驗證生成特殊時段
    List<TimeSlot> timeSlots = timeSlotRepository.findByStoreAndDay(store, date);
    assertTrue(timeSlots.stream().allMatch(slot ->
        slot.getStartTime().isAfter(openTime.minusMinutes(1)) &&
        slot.getEndTime().isBefore(closeTime.plusMinutes(1))
    ));
}
```

## 整合測試步驟

### 步驟 1: 設定測試環境

1. 確保後端服務正常運行
2. 確保前端應用正常運行
3. 準備測試數據

### 步驟 2: 執行測試案例

1. 執行案例 1 (特殊休假日)
2. 執行案例 2 (特殊營業時間)
3. 執行案例 3 (一般公休日)

### 步驟 3: 驗證結果

1. 檢查 API 回應是否正確
2. 檢查前端顯示是否正確
3. 檢查時段生成是否正確

### 步驟 4: 清理測試數據

1. 刪除測試的特殊營業時間設定
2. 恢復正常的營業時間設定

## 錯誤處理測試

### 1. API 錯誤處理

```javascript
// 測試 API 錯誤情況
try {
  const response = await fetch("/api/stores/999/special/check/2025-01-15");
  if (!response.ok) {
    console.log("API 錯誤處理正常");
  }
} catch (error) {
  console.log("網路錯誤處理正常");
}
```

### 2. 前端錯誤處理

```javascript
// 測試前端錯誤情況
const isSpecialClosed = await isSpecialClosedDay(null);
console.log("null 日期處理:", isSpecialClosed); // 應為 false
```

## 性能測試

### 1. API 響應時間

```bash
# 測試 API 響應時間
time curl -X GET "http://localhost:8080/api/stores/1/special/check/2025-01-15"
```

### 2. 前端渲染時間

```javascript
// 測試前端渲染時間
console.time("updateTimeSections");
await updateTimeSections();
console.timeEnd("updateTimeSections");
```

## 總結

這個測試文件涵蓋了特殊營業時間整合功能的所有重要方面：

1. **功能測試**：驗證各種特殊營業時間情況的正確處理
2. **API 測試**：驗證後端 API 的正確性
3. **前端測試**：驗證前端邏輯的正確性
4. **整合測試**：驗證前後端整合的正確性
5. **錯誤處理測試**：驗證錯誤情況的處理
6. **性能測試**：驗證系統性能

通過這些測試，可以確保特殊營業時間功能在各種情況下都能正確工作。
