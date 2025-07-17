-- 測試預約狀態更新 API
-- 1. 創建測試預約
INSERT INTO reservation (
    user_id,
    store_id,
    reserved_date,
    reserved_time,
    guests,
    duration,
    status,
    content,
    created_at,
    updated_at
) VALUES (
    1,  -- 用戶 ID
    1,  -- 餐廳 ID
    DATEADD(day, 7, GETDATE()),  -- 7天後的日期
    '18:30:00',  -- 時間
    4,  -- 人數
    120,  -- 用餐時長（分鐘）
    'PENDING',  -- 狀態
    '測試預約 - 待確認',  -- 備註
    GETDATE(),  -- 創建時間
    GETDATE()   -- 更新時間
);

-- 2. 檢查創建的預約
SELECT 
    id,
    user_id,
    store_id,
    reserved_date,
    reserved_time,
    guests,
    duration,
    status,
    content,
    created_at,
    updated_at
FROM reservation
WHERE user_id = 1
ORDER BY id DESC;

-- 3. 測試 API 端點（需要在 Postman 或瀏覽器中測試）
-- PUT /api/reservations/{id}/status
-- Body: {"status": "CONFIRMED"}

-- 4. 檢查更新後的狀態
SELECT 
    id,
    user_id,
    store_id,
    reserved_date,
    reserved_time,
    status,
    updated_at
FROM reservation
WHERE user_id = 1
ORDER BY id DESC; 