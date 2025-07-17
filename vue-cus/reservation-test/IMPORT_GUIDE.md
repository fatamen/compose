# Postman 導入指南

## 📁 檔案說明

### 1. GoldenBowl_Reservation_API.postman_collection.json

- **用途**: 預約系統 API 測試集合
- **包含**: 所有預約相關的 API 端點測試

### 2. GoldenBowl_Reservation_Environment.postman_environment.json

- **用途**: Postman 環境變數設定
- **包含**: 測試所需的變數和預設值

### 3. DATE_CALCULATOR.md

- **用途**: 日期設定指南
- **重要**: 資料庫只有 7 天的資料，請務必查看此文件

## 🚀 快速導入步驟

### 步驟 1: 導入環境變數

1. 打開 Postman
2. 點擊右上角的齒輪圖示 (Settings)
3. 選擇 "Import" 標籤
4. 拖拽或選擇 `GoldenBowl_Reservation_Environment.postman_environment.json`
5. 點擊 "Import"
6. 在右上角選擇剛導入的環境 "GoldenBowl 預約系統環境"

### 步驟 2: 導入測試集合

1. 在 Postman 中點擊 "Import" 按鈕
2. 拖拽或選擇 `GoldenBowl_Reservation_API.postman_collection.json`
3. 點擊 "Import"

### 步驟 3: 設定測試日期 ⚠️ 重要

**資料庫只有從今天開始算 7 天的資料！**

1. 在 Postman 中選擇環境 "GoldenBowl 預約系統環境"
2. 點擊右上角的齒輪圖示
3. 找到 `TEST_DATE` 變數
4. 將值改為今天的日期 (格式: YYYY-MM-DD)
5. 點擊 "Save"

**或者使用自動腳本**:

```javascript
// 在 Postman Console 中執行
const today = new Date();
const year = today.getFullYear();
const month = String(today.getMonth() + 1).padStart(2, "0");
const day = String(today.getDate()).padStart(2, "0");
const todayString = `${year}-${month}-${day}`;
pm.environment.set("TEST_DATE", todayString);
```

### 步驟 4: 啟動後端服務

```bash
cd GoldenBowl
mvn spring-boot:run
```

### 步驟 5: 開始測試

1. 在 Postman 中找到 "GoldenBowl 預約系統 API 測試" 集合
2. 按照以下順序執行測試：

## 📋 測試順序建議

### 1. 時間段管理

- ✅ 獲取餐廳所有時間段
- ✅ 獲取已預訂時間段

### 2. 可用性檢查

- ✅ 檢查時間可用性

### 3. 預約管理

- ✅ 創建預約
- ✅ 獲取用戶預約記錄
- ✅ 獲取餐廳預約記錄
- ✅ 取消預約

### 4. 桌位管理

- ✅ 獲取可用桌位

## 🔧 環境變數說明

| 變數名稱           | 預設值                | 說明                      |
| ------------------ | --------------------- | ------------------------- |
| BASE_URL           | http://localhost:8080 | 後端服務地址              |
| STORE_ID           | 1                     | 測試餐廳 ID               |
| USER_ID            | 1                     | 測試用戶 ID               |
| TEST_DATE          | 2025-01-15            | **測試日期 (請改為今天)** |
| TEST_TIME          | 18:30                 | 測試時間                  |
| TEST_GUESTS        | 4                     | 測試用餐人數              |
| TEST_DURATION      | 120                   | 測試用餐時長              |
| TEST_CUSTOMER_NAME | 張先生                | 測試客戶姓名              |
| TEST_PHONE         | 0912345678            | 測試電話號碼              |

## ⚠️ 重要注意事項

### 日期設定 ⚠️ 非常重要

1. **資料庫範圍**: 只有從今天開始算 7 天的資料
2. **不要使用過去的日期**: 會返回空資料
3. **不要使用 7 天後的日期**: 會返回空資料
4. **建議使用今天或明天的日期**: 確保有完整資料

### 測試前檢查

1. **後端服務**: 確保後端服務正在運行
2. **環境選擇**: 確保選擇了正確的環境
3. **日期設定**: 確保 TEST_DATE 是今天的日期
4. **CORS**: 確保後端已配置 CORS

## 🐛 故障排除

### 常見錯誤

1. **404 錯誤**: 檢查後端服務是否運行
2. **CORS 錯誤**: 檢查 SecurityConfig 配置
3. **500 錯誤**: 檢查數據庫連接
4. **空資料**: 檢查 TEST_DATE 是否正確設定
5. **變數錯誤**: 檢查環境變數設置

### 調試技巧

1. 檢查 Postman Console 日誌
2. 查看後端應用日誌
3. 使用瀏覽器開發者工具
4. 檢查數據庫連接狀態
5. 驗證日期設定是否正確

## 📊 測試結果驗證

### 成功響應範例

```json
{
  "success": true,
  "message": "操作成功",
  "data": [...]
}
```

### 錯誤響應範例

```json
{
  "success": false,
  "message": "錯誤訊息"
}
```

### 空資料響應 (日期錯誤)

```json
[]
```

## 🎯 測試目標

- ✅ 驗證所有 API 端點正常工作
- ✅ 確認數據格式正確
- ✅ 測試錯誤處理機制
- ✅ 驗證業務邏輯正確性
- ✅ 確認日期設定正確

## 📅 日期設定參考

請參考 `DATE_CALCULATOR.md` 文件了解：

- 可用的測試日期範圍
- 自動設定日期的腳本
- 測試策略建議
