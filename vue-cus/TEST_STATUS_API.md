# 測試預約狀態更新 API

## 新增的 API 端點

### PUT `/api/reservations/{id}/status`

**功能**: 更新預約狀態

**請求體**:

```json
{
  "status": "CONFIRMED" // 或 "CANCELLED", "PENDING"
}
```

**回應**: 更新後的預約物件

## 前端整合

### 1. 確認預約

```javascript
const response = await fetch(`/api/reservations/${record.id}/status`, {
  method: "PUT",
  headers: {
    "Content-Type": "application/json",
  },
  body: JSON.stringify({
    status: "CONFIRMED",
  }),
});
```

### 2. 取消預約

```javascript
const response = await fetch(`/api/reservations/${record.id}/status`, {
  method: "PUT",
  headers: {
    "Content-Type": "application/json",
  },
  body: JSON.stringify({
    status: "CANCELLED",
  }),
});
```

## 測試步驟

### 1. 準備測試資料

執行 SQL 腳本：

```sql
-- 執行 GoldenBowl/test_reservation_status_api.sql
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

### 4. 測試功能

1. 訪問預約紀錄頁面
2. 找到狀態為「待確認」的預約
3. 點擊「確認」按鈕
4. 檢查狀態是否變更為「已確認」

## API 測試

### 使用 Postman 測試

1. **確認預約**:

   - Method: `PUT`
   - URL: `http://localhost:8080/api/reservations/{id}/status`
   - Headers: `Content-Type: application/json`
   - Body: `{"status": "CONFIRMED"}`

2. **取消預約**:
   - Method: `PUT`
   - URL: `http://localhost:8080/api/reservations/{id}/status`
   - Headers: `Content-Type: application/json`
   - Body: `{"status": "CANCELLED"}`

## 支援的狀態

- `PENDING`: 待確認
- `CONFIRMED`: 已確認
- `CANCELLED`: 已取消

## 錯誤處理

- **400 Bad Request**: 狀態值無效
- **404 Not Found**: 預約不存在
- **500 Internal Server Error**: 伺服器錯誤

## 前端錯誤修正

### 1. JSON 解析錯誤

- 修正了 `getUserName` 函數的錯誤處理
- 當 JSON 解析失敗時返回預設值

### 2. API 端點統一

- 所有狀態更新都使用 `/api/reservations/{id}/status`
- 統一使用 JSON 請求體傳遞狀態

## 使用方式

使用者現在可以：

1. **查看預約紀錄**: 載入所有預約並顯示餐廳名稱和用戶姓名
2. **確認預約**: 將 PENDING 狀態的預約改為 CONFIRMED
3. **取消預約**: 將預約狀態改為 CANCELLED
4. **修改預約**: 修改人數和備註（僅限 CONFIRMED 狀態）
5. **篩選預約**: 根據狀態篩選預約紀錄

## 除錯技巧

1. **檢查瀏覽器開發者工具**

   - Network 標籤查看 API 請求
   - Console 標籤查看錯誤訊息

2. **檢查後端日誌**

   - 查看 Spring Boot 啟動日誌
   - 檢查是否有異常堆疊追蹤

3. **測試 API 端點**
   - 使用 Postman 測試狀態更新
   - 確認請求體格式正確
