# 預約系統 API 整合測試

## 概述

此文件說明如何測試前端 `ReservationForm.vue` 與後端預約 API 的整合。

## 後端 API 端點

### 1. 獲取餐廳時間段

- **端點**: `GET /api/booking/all-slots/{storeId}`
- **參數**:
  - `storeId`: 餐廳 ID
  - `date`: 日期 (YYYY-MM-DD 格式)
- **用途**: 獲取餐廳在指定日期的所有可用時間段

### 2. 獲取已預訂時間段

- **端點**: `GET /api/booking/slots/{storeId}`
- **參數**:
  - `storeId`: 餐廳 ID
  - `date`: 日期 (YYYY-MM-DD 格式)
  - `guests`: 用餐人數
- **用途**: 獲取餐廳在指定日期已預訂的時間段

### 3. 檢查時間可用性

- **端點**: `GET /api/booking/check`
- **參數**:
  - `storeId`: 餐廳 ID
  - `date`: 日期 (YYYY-MM-DD 格式)
  - `time`: 時間 (HH:MM 格式)
  - `guests`: 用餐人數
  - `duration`: 用餐時長 (預設 120 分鐘)
- **用途**: 檢查特定時間是否可預約

### 4. 創建預約

- **端點**: `POST /api/reservations`
- **請求體**:
  ```json
  {
    "userId": 1,
    "storeId": 1,
    "reservedDate": "2025-01-15",
    "reservedTime": "18:30",
    "guests": 4,
    "duration": 120,
    "content": "預約人: 張先生, 電話: 0912345678"
  }
  ```
- **用途**: 創建新的預約記錄

## 前端整合

### 服務層 (`timeSlotService.js`)

- `fetchRestaurantTimeSlots()`: 獲取餐廳時間段
- `fetchBookedTimeSlots()`: 獲取已預訂時間段
- `checkTimeAvailability()`: 檢查時間可用性
- `createReservation()`: 創建預約

### 組件 (`ReservationForm.vue`)

- 自動載入餐廳時間段數據
- 日期變化時重新載入數據
- 提交前檢查時間可用性
- 成功預約後重新載入數據

## 測試步驟

### 1. 啟動後端服務

```bash
cd GoldenBowl
mvn spring-boot:run
```

### 2. 啟動前端服務

```bash
cd vue-cus
npm run dev
```

### 3. 測試流程

1. 訪問餐廳頁面
2. 選擇用餐人數（大人和小孩）
3. 選擇用餐日期
4. 選擇用餐時間
5. 填寫姓名、電話和備註
6. 點擊「送出預約」

### 4. 檢查控制台日誌

前端會在瀏覽器控制台輸出詳細的 API 調用日誌：

- 時間段數據獲取
- 已預訂時間段獲取
- 可用性檢查
- 預約提交結果

## 預期結果

### 成功情況

- 時間段數據正確載入
- 日期選擇器顯示可用日期
- 時間選擇器顯示可用時間段
- 預約成功後顯示成功訊息
- 表單重置並重新載入數據

### 錯誤處理

- API 錯誤時顯示錯誤訊息
- 時間不可用時提示選擇其他時間
- 表單驗證失敗時提示填寫完整資訊

## 注意事項

1. **用戶 ID**: 目前使用固定用戶 ID (1)，實際應該從登入狀態獲取
2. **CORS**: 後端已配置 CORS 允許前端訪問
3. **日期格式**: 使用 YYYY-MM-DD 格式
4. **時間格式**: 使用 HH:MM 格式
5. **錯誤處理**: 前端會捕獲並顯示後端錯誤訊息

## 故障排除

### 常見問題

1. **CORS 錯誤**: 檢查後端 SecurityConfig 配置
2. **API 404**: 檢查後端服務是否正常運行
3. **數據格式錯誤**: 檢查前端數據轉換邏輯
4. **預約失敗**: 檢查後端數據庫連接和業務邏輯

### 調試技巧

1. 使用瀏覽器開發者工具查看網路請求
2. 檢查後端日誌輸出
3. 使用 Postman 測試 API 端點
4. 檢查前端控制台錯誤訊息
