-- 檢查預約狀態
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
ORDER BY id DESC;

-- 檢查是否有 PENDING 狀態的預約
SELECT 
    id,
    user_id,
    store_id,
    reserved_date,
    reserved_time,
    status
FROM reservation
WHERE status = 'PENDING'
ORDER BY id DESC;

-- 檢查用戶 1 的所有預約
SELECT 
    id,
    user_id,
    store_id,
    reserved_date,
    reserved_time,
    status
FROM reservation
WHERE user_id = 1
ORDER BY reserved_date DESC, reserved_time DESC;

-- 更新一個預約為 PENDING 狀態（如果沒有的話）
UPDATE reservation 
SET status = 'PENDING' 
WHERE id = (SELECT TOP 1 id FROM reservation WHERE user_id = 1 ORDER BY id DESC);

-- 檢查更新後的狀態
SELECT 
    id,
    user_id,
    store_id,
    reserved_date,
    reserved_time,
    status
FROM reservation
WHERE user_id = 1
ORDER BY id DESC; 