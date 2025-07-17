# Postman 測試集合

## 概述

此文件提供使用 Postman 測試預約系統 API 的說明。

## 測試集合結構

### 1. 環境變數設置

在 Postman 中創建環境變數：

```
BASE_URL: http://localhost:8080
STORE_ID: 1
USER_ID: 1
```

### 2. API 測試端點

#### 獲取餐廳時間段

- **Method**: GET
- **URL**: `{{BASE_URL}}/api/booking/all-slots/{{STORE_ID}}`
- **Params**:
  - `date`: 2025-01-15

#### 獲取已預訂時間段

- **Method**: GET
- **URL**: `{{BASE_URL}}/api/booking/slots/{{STORE_ID}}`
- **Params**:
  - `date`: 2025-01-15
  - `guests`: 4

#### 檢查時間可用性

- **Method**: GET
- **URL**: `{{BASE_URL}}/api/booking/check`
- **Params**:
  - `storeId`: {{STORE_ID}}
  - `date`: 2025-01-15
  - `time`: 18:30
  - `guests`: 4
  - `duration`: 120

#### 創建預約

- **Method**: POST
- **URL**: `{{BASE_URL}}/api/reservations`
- **Headers**:
  - `Content-Type`: application/json
- **Body** (raw JSON):

```json
{
  "userId": {{USER_ID}},
  "storeId": {{STORE_ID}},
  "reservedDate": "2025-01-15",
  "reservedTime": "18:30",
  "guests": 4,
  "duration": 120,
  "content": "測試預約 - 張先生, 電話: 0912345678"
}
```

## 測試步驟

### 1. 環境設置

1. 啟動後端服務
2. 在 Postman 中設置環境變數
3. 導入測試集合

### 2. 執行測試

1. **測試時間段獲取**: 確認能獲取餐廳時間段
2. **測試可用性檢查**: 確認能檢查時間可用性
3. **測試預約創建**: 確認能成功創建預約
4. **測試預約查詢**: 確認能查詢預約記錄

### 3. 驗證結果

- 檢查響應狀態碼 (200 OK)
- 檢查響應數據格式
- 檢查錯誤處理

## 常見問題

### CORS 錯誤

- 確認後端 SecurityConfig 已配置 CORS
- 檢查請求頭設置

### 404 錯誤

- 確認後端服務正在運行
- 檢查 API 路徑是否正確

### 500 錯誤

- 檢查數據庫連接
- 檢查請求參數格式

## 測試數據

### 測試餐廳

- **餐廳 ID**: 1
- **餐廳名稱**: 測試餐廳

### 測試用戶

- **用戶 ID**: 1
- **用戶名稱**: 測試用戶

### 測試預約

- **日期**: 2025-01-15
- **時間**: 18:30
- **人數**: 4
- **時長**: 120 分鐘
