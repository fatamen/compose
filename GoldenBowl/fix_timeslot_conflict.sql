-- TimeSlot 資料庫衝突修復腳本
-- 執行此腳本來檢查和修復 time_slots 表的問題

-- 1. 檢查 time_slots 表結構
SELECT 
    COLUMN_NAME, 
    DATA_TYPE, 
    CHARACTER_MAXIMUM_LENGTH,
    IS_NULLABLE, 
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'time_slots' 
ORDER BY ORDINAL_POSITION;

-- 2. 檢查是否有無效的時間資料
SELECT 
    id,
    store_id,
    day,
    start_time,
    end_time,
    is_active
FROM time_slots
WHERE start_time IS NULL 
   OR end_time IS NULL
   OR start_time >= end_time;

-- 3. 測試 CONVERT 函數是否正常工作
SELECT 
    id,
    store_id,
    day,
    start_time,
    CONVERT(time, start_time) as start_time_converted,
    end_time,
    CONVERT(time, end_time) as end_time_converted,
    is_active
FROM time_slots
WHERE store_id = 1
  AND day = '2025-07-11'
  AND CONVERT(time, start_time) = CONVERT(time, '18:00:00');

-- 4. 檢查重複的時段
SELECT 
    store_id,
    day,
    start_time,
    COUNT(*) as duplicate_count
FROM time_slots
GROUP BY store_id, day, start_time
HAVING COUNT(*) > 1;

-- 5. 檢查時段與預約的衝突
SELECT 
    ts.id as timeslot_id,
    ts.store_id,
    ts.day,
    ts.start_time,
    ts.end_time,
    r.id as reservation_id,
    r.reserved_date,
    r.reserved_time,
    CONVERT(time, r.reserved_time) as reserved_time_converted
FROM time_slots ts
LEFT JOIN reservation r ON ts.store_id = r.store_id 
    AND ts.day = r.reserved_date
    AND CONVERT(time, ts.start_time) = CONVERT(time, r.reserved_time)
WHERE ts.store_id = 1
  AND ts.day = '2025-07-11'
  AND ts.is_active = 1;

-- 6. 修復可能的資料問題（如果需要）
-- 刪除無效的時段資料
-- DELETE FROM time_slots WHERE start_time IS NULL OR end_time IS NULL OR start_time >= end_time;

-- 7. 重新建立索引（如果需要）
-- CREATE INDEX IX_time_slots_store_day_time ON time_slots(store_id, day, start_time);

-- 8. 檢查索引是否存在
SELECT 
    i.name AS index_name,
    i.type_desc,
    i.is_unique,
    c.name AS column_name
FROM sys.indexes i
INNER JOIN sys.index_columns ic ON i.object_id = ic.object_id AND i.index_id = ic.index_id
INNER JOIN sys.columns c ON ic.object_id = c.object_id AND ic.column_id = c.column_id
WHERE i.object_id = OBJECT_ID('time_slots')
ORDER BY i.name, ic.index_column_id; 