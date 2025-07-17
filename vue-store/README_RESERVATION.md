# 商家端訂位管理系統

## 功能概述

商家端訂位管理系統提供完整的訂位管理功能，讓餐廳能夠有效管理顧客的訂位預約。

## 主要功能

### 1. 訂位儀表板 (Dashboard)

- **統計概覽**: 顯示總訂位數、今日訂位、未來訂位、平均人數等關鍵指標
- **狀態統計**: 按狀態分類顯示訂位數量（待確認、已確認、已取消、已完成）
- **快速操作**: 提供快速查看特定狀態訂位的按鈕

### 2. 訂位列表管理

- **搜尋篩選**: 支援按顧客姓名、電話、備註進行關鍵字搜尋
- **狀態篩選**: 可篩選特定狀態的訂位
- **日期篩選**: 可篩選特定日期的訂位
- **CRUD 操作**:
  - 查看訂位詳情
  - 確認訂位
  - 取消訂位
  - 標記完成
  - 刪除訂位

### 3. 時段管理

- **時段設定**: 管理餐廳的營業時段
- **新增時段**: 可新增特定日期的時段
- **編輯時段**: 修改時段的開始時間、結束時間
- **啟用/停用**: 可快速切換時段的啟用狀態
- **批量生成**: 可一次性生成未來多天的時段資料

## 技術架構

### 前端技術

- **Vue 3**: 使用 Composition API
- **Bootstrap 5**: UI 框架
- **Font Awesome**: 圖示庫
- **Axios**: HTTP 客戶端

### 後端 API 整合

- **訂位管理 API**: `/api/reservations/*`
- **時段管理 API**: `/api/stores/{storeId}/timeslots/*`
- **統計資料 API**: `/api/reservations/store/{storeId}/stats`

## 檔案結構

```
vue-store/src/
├── pages/
│   └── ReservationManagement.vue          # 主頁面
├── components/
│   └── reservation/
│       ├── ReservationDashboard.vue       # 儀表板組件
│       └── TimeSlotManager.vue           # 時段管理組件
├── services/
│   └── reservationService.js             # API服務
└── router/
    └── index.js                         # 路由配置
```

## 使用方式

### 1. 進入訂位管理頁面

點擊側邊欄的「訂位管理」連結，或直接訪問 `/reservations`

### 2. 查看儀表板

- 預設顯示儀表板，提供訂位統計概覽
- 點擊快速操作按鈕可快速篩選訂位列表

### 3. 管理訂位列表

- 切換到「訂位列表」標籤
- 使用搜尋和篩選功能找到特定訂位
- 點擊操作按鈕進行狀態管理

### 4. 管理時段

- 切換到「時段管理」標籤
- 選擇日期查看該日期的時段
- 新增、編輯或刪除時段

## API 端點說明

### 訂位管理

- `GET /api/reservations/store/{storeId}` - 取得餐廳訂位列表
- `GET /api/reservations/{id}` - 取得特定訂位詳情
- `PUT /api/reservations/{id}/status` - 更新訂位狀態
- `DELETE /api/reservations/{id}` - 刪除訂位

### 時段管理

- `GET /api/stores/{storeId}/timeslots` - 取得時段列表
- `POST /api/stores/{storeId}/timeslots` - 新增時段
- `PUT /api/stores/{storeId}/timeslots/{id}` - 更新時段
- `DELETE /api/stores/{storeId}/timeslots/{id}` - 刪除時段

## 狀態管理

### 訂位狀態

- **PENDING**: 待確認
- **CONFIRMED**: 已確認
- **CANCELLED**: 已取消
- **COMPLETED**: 已完成

### 時段狀態

- **isActive: true**: 啟用
- **isActive: false**: 停用

## 錯誤處理

系統具備完善的錯誤處理機制：

- API 呼叫失敗時會自動降級使用模擬資料
- 提供友善的錯誤提示訊息
- 載入狀態指示器

## 未來擴展

1. **通知系統**: 新增訂位時自動發送通知
2. **日曆視圖**: 提供日曆形式的訂位檢視
3. **報表功能**: 更詳細的統計報表
4. **批量操作**: 支援批量處理訂位
5. **匯入功能**: 支援從外部系統匯入訂位資料

## 開發注意事項

1. **API 整合**: 確保後端 API 端點正確配置
2. **權限控制**: 需要實作商家身份驗證
3. **資料驗證**: 前端和後端都需要進行資料驗證
4. **效能優化**: 大量資料時考慮分頁和虛擬滾動
5. **響應式設計**: 確保在不同裝置上都能正常使用
