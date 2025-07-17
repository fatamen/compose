# 測試確認預約功能

## 問題描述

前端確認預約時出現錯誤：`確認預約失敗: Error: 確認失敗`

## 已修正的問題

### 1. API URL 問題

- **問題**：前端使用相對路徑 `/api/reservations/{id}/confirm`
- **修正**：保持相對路徑，利用 Vite 代理設定

### 2. 所有 API 呼叫都使用相對路徑

- `loadRecords()`: `/api/reservations/user/1`
- `getStoreName()`: `/api/stores/{storeId}`
- `getUserName()`: `/api/users/{userId}`
- `saveEdit()`: `/api/reservations/{id}`
- `confirmCancel()`: `/api/reservations/{id}/cancel`
- `confirmRecord()`: `/api/reservations/{id}/confirm`

### 3. Vite 代理設定

- 在 `vite.config.js` 中設定代理：`'/api': 'http://localhost:8080'`
- 這樣前端可以使用相對路徑，Vite 會自動代理到後端

### 4. 新增錯誤處理

- 在 `confirmRecord()` 中新增詳細的錯誤日誌
- 顯示 HTTP 狀態碼和錯誤訊息

## 測試步驟

### 1. 準備測試資料

執行 SQL 腳本創建測試預約：

```sql
-- 執行 GoldenBowl/create_test_pending_reservation.sql
```

### 2. 啟動後端服務

```bash
cd GoldenBowl
mvn spring-boot:run
```

### 3. 啟動前端服務

```bash
cd vue-cus
npm run dev
```

### 4. 測試確認功能

1. 訪問預約紀錄頁面
2. 找到狀態為「待確認」的預約
3. 點擊「確認」按鈕
4. 檢查是否成功確認

## 後端 API 端點

### 確認預約

- **URL**: `PUT /api/reservations/{id}/confirm`
- **功能**: 將 PENDING 狀態的預約改為 CONFIRMED
- **權限檢查**: 只有 PENDING 狀態的預約可以確認

### 取消預約

- **URL**: `PUT /api/reservations/{id}/cancel`
- **功能**: 將預約狀態改為 CANCELLED
- **權限檢查**: 只有未過期且狀態為 PENDING 或 CONFIRMED 的預約可以取消

### 修改預約

- **URL**: `PUT /api/reservations/{id}`
- **功能**: 修改預約的人數和備註
- **權限檢查**: 只有未過期且狀態為 CONFIRMED 的預約可以修改

## 預約狀態說明

- **PENDING**: 待確認 - 可以確認或取消
- **CONFIRMED**: 已確認 - 可以取消或修改
- **CANCELLED**: 已取消 - 無法再操作

## 錯誤處理

如果仍然出現錯誤，請檢查：

1. **後端服務是否正常運行**

   - 檢查 `http://localhost:8080/api/reservations/user/1` 是否返回資料

2. **資料庫中是否有 PENDING 狀態的預約**

   - 執行 `GoldenBowl/test_reservation_status.sql` 檢查

3. **CORS 設定**

   - 後端已設定 `@CrossOrigin(origins = "*")`

4. **Vite 代理設定**
   - 確認 `vite.config.js` 中有正確的代理設定
   - 確認前端開發服務器正在運行

## 除錯技巧

1. **檢查瀏覽器開發者工具**

   - Network 標籤查看 API 請求
   - Console 標籤查看錯誤訊息

2. **檢查後端日誌**

   - 查看 Spring Boot 啟動日誌
   - 檢查是否有異常堆疊追蹤

3. **測試 API 端點**

   - 使用 Postman 測試 `PUT /api/reservations/{id}/confirm`

4. **檢查 Vite 代理**
   - 確認前端請求是否正確代理到後端
   - 檢查 Network 標籤中的請求 URL
