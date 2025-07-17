# 營業時間 API 測試文件

## 測試環境

- 後端服務：http://localhost:8080
- 測試店家：store_id = 6, 7, 8

## API 端點測試

### 1. 取得完整營業時間設定

```bash
# 取得店家6的完整營業時間（包括公休日）
GET /api/stores/6/hours/complete

# 取得店家7的完整營業時間（週一公休）
GET /api/stores/7/hours/complete

# 取得店家8的完整營業時間（週二公休）
GET /api/stores/8/hours/complete
```

### 2. 檢查公休日

```bash
# 檢查店家7週一是否公休
GET /api/stores/7/hours/closed/MONDAY

# 檢查店家8週二是否公休
GET /api/stores/8/hours/closed/TUESDAY

# 檢查店家6週一是否公休（應該返回false）
GET /api/stores/6/hours/closed/MONDAY
```

### 3. 檢查指定日期是否公休

```bash
# 檢查店家7在2024-01-01（週一）是否公休
GET /api/stores/7/hours/closed/date/2024-01-01

# 檢查店家8在2024-01-02（週二）是否公休
GET /api/stores/8/hours/closed/date/2024-01-02
```

### 4. 設定營業時間

```bash
# 設定店家9週一營業時間
POST /api/stores/9/hours?day=MONDAY&openTime=11:00&closeTime=22:00&isOpen=true

# 設定店家9週二公休
POST /api/stores/9/hours?day=TUESDAY&isOpen=false
```

### 5. 更新營業時間

```bash
# 更新店家6週一的營業時間
PUT /api/stores/6/hours/{openHourId}?openTime=10:00&closeTime=23:00

# 設定店家6週一為公休
PUT /api/stores/6/hours/{openHourId}?isOpen=false
```

## 預期結果

### 店家 6（香辣火鍋，天天營業）

- 所有天的 `isOpen` 應該為 `true`
- 所有天都有 `openTime` 和 `closeTime`

### 店家 7（義大利小館，週一公休）

- 週一的 `isOpen` 應該為 `false`
- 週一的 `openTime` 和 `closeTime` 應該為 `null`
- 其他天的 `isOpen` 應該為 `true`

### 店家 8（日式壽司吧，週二公休）

- 週二的 `isOpen` 應該為 `false`
- 週二的 `openTime` 和 `closeTime` 應該為 `null`
- 其他天的 `isOpen` 應該為 `true`

## 測試步驟

1. 執行 `test_openhour_logic.sql` 插入測試資料
2. 啟動後端服務
3. 使用 Postman 或 curl 測試上述 API 端點
4. 驗證返回結果符合預期
5. 檢查資料庫中的資料是否正確

## 注意事項

- 當 `isOpen=false` 時，`openTime` 和 `closeTime` 會被設為 `null`
- 當 `isOpen=true` 時，必須提供有效的 `openTime` 和 `closeTime`
- 公休日的判定邏輯：`openTime IS NULL OR closeTime IS NULL`
