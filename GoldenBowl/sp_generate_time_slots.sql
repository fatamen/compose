-- ===================================================================
-- SQL Server 儲存程序：大宗生成時段 (sp_generate_time_slots) - 改進版
-- ===================================================================

USE back;
GO

IF OBJECT_ID('sp_generate_time_slots', 'P') IS NOT NULL
    DROP PROCEDURE sp_generate_time_slots;
GO

CREATE PROCEDURE sp_generate_time_slots
    @store_id INT,
    @days_ahead INT = 30
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @start_date DATE = CAST(GETDATE() AS DATE);
    DECLARE @current_date DATE;
    DECLARE @day_of_week INT;
    DECLARE @interval_minutes INT;

    DECLARE @open_time TIME(0);
    DECLARE @close_time TIME(0);
    DECLARE @slots_generated INT = 0;

    DECLARE @date_str VARCHAR(20);
    DECLARE @time_start_str VARCHAR(10);
    DECLARE @time_end_str VARCHAR(10);

    -- 檢查餐廳是否存在
    IF NOT EXISTS (SELECT 1 FROM store WHERE id = @store_id)
    BEGIN
        RAISERROR('❌ 餐廳不存在: %d', 16, 1, @store_id);
        RETURN;
    END

    -- 取得時間間隔
    SELECT @interval_minutes = interval
    FROM time_setting
    WHERE store_id = @store_id;

    IF @interval_minutes IS NULL
        SET @interval_minutes = 30;

    RAISERROR('🔧 開始生成餐廳 %d 的時段（共 %d 天）', 0, 1, @store_id, @days_ahead) WITH NOWAIT;

    DECLARE @day_counter INT = 0;

    WHILE @day_counter < @days_ahead
    BEGIN
        SET @current_date = DATEADD(DAY, @day_counter, @start_date);
        SET @day_of_week = DATEPART(WEEKDAY, @current_date); -- 1=週日 ~ 7=週六
        SET @day_of_week = CASE WHEN @day_of_week = 1 THEN 0 ELSE @day_of_week - 1 END;

        SET @date_str = CONVERT(VARCHAR, @current_date, 23);
        RAISERROR('📆 處理日期：%s（週%d）', 0, 1, @date_str, @day_of_week) WITH NOWAIT;

        -- 暫存所有營業時段
        DECLARE @open_hours TABLE (row_num INT IDENTITY(1,1), open_time TIME, close_time TIME);

        INSERT INTO @open_hours(open_time, close_time)
        SELECT open_time, close_time
        FROM open_hour
        WHERE store_id = @store_id AND day = @day_of_week
              AND open_time IS NOT NULL AND close_time IS NOT NULL;

        DECLARE @row INT = 1;
        DECLARE @max_row INT = (SELECT COUNT(*) FROM @open_hours);

        WHILE @row <= @max_row
        BEGIN
            SELECT @open_time = open_time, @close_time = close_time
            FROM @open_hours WHERE row_num = @row;

            -- 判斷是否跨日
            DECLARE @is_overnight BIT = CASE WHEN @close_time < @open_time THEN 1 ELSE 0 END;

            -- 建立完整的起始與結束時間（DATETIME）
            DECLARE @open_dt DATETIME = CAST(@current_date AS DATETIME) + CAST(@open_time AS DATETIME);
            DECLARE @close_dt DATETIME = 
                CASE WHEN @is_overnight = 1 
                     THEN DATEADD(DAY, 1, CAST(@current_date AS DATETIME)) + CAST(@close_time AS DATETIME)
                     ELSE CAST(@current_date AS DATETIME) + CAST(@close_time AS DATETIME)
                END;

            DECLARE @current_dt DATETIME = @open_dt;
            DECLARE @slot_start TIME(0);
            DECLARE @slot_end TIME(0);
            DECLARE @slot_date DATE;

            SET @time_start_str = CONVERT(VARCHAR(8), @open_time, 108);
            SET @time_end_str = CONVERT(VARCHAR(8), @close_time, 108);
            RAISERROR('🕒 營業時間：%s ~ %s', 0, 1, @time_start_str, @time_end_str) WITH NOWAIT;

            WHILE @current_dt < @close_dt
            BEGIN
                SET @slot_start = CONVERT(TIME(0), @current_dt);
                SET @slot_end = CONVERT(TIME(0), DATEADD(MINUTE, @interval_minutes, @current_dt));
                SET @slot_date = CAST(@current_dt AS DATE);

                IF DATEADD(MINUTE, @interval_minutes, @current_dt) > @close_dt BREAK;
                IF @slot_end <= @slot_start BREAK;

                IF NOT EXISTS (
                    SELECT 1 FROM time_slots
                    WHERE store_id = @store_id
                      AND day = @slot_date
                      AND start_time = @slot_start
                )
                BEGIN
                    INSERT INTO time_slots(store_id, day, start_time, end_time, is_active)
                    VALUES (@store_id, @slot_date, @slot_start, @slot_end, 1);

                    SET @slots_generated += 1;

                    SET @time_start_str = CONVERT(VARCHAR(8), @slot_start, 108);
                    SET @time_end_str = CONVERT(VARCHAR(8), @slot_end, 108);
                    RAISERROR('    ➕ 新增時段：%s ~ %s', 0, 1, @time_start_str, @time_end_str) WITH NOWAIT;
                END

                SET @current_dt = DATEADD(MINUTE, @interval_minutes, @current_dt);
            END

            SET @row += 1;
        END

        SET @day_counter += 1;
    END

    RAISERROR('✅ 完成！共產生 %d 筆時段。', 0, 1, @slots_generated) WITH NOWAIT;
    SELECT @slots_generated AS slots_generated;
END
GO

-- 測試儲存程序
-- EXEC sp_generate_time_slots @store_id = 1, @days_ahead = 7; 