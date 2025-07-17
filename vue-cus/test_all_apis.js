const axios = require('axios');
const fs = require('fs');

// 設定基本配置
const BASE_URL = 'http://localhost:8080';
const TEST_RESULTS = [];

// 顏色輸出
const colors = {
    green: '\x1b[32m',
    red: '\x1b[31m',
    yellow: '\x1b[33m',
    blue: '\x1b[34m',
    reset: '\x1b[0m'
};

// 測試用資料
const testData = {
    storeId: 1,
    userId: 1,
    date: '2024-01-15',
    time: '18:30',
    guests: 4,
    duration: 120,
    tableId: 1,
    timeslotId: 1,
    reservationId: 1
};

// 記錄測試結果
function logResult(endpoint, method, status, success, error = null) {
    const result = {
        endpoint,
        method,
        status,
        success,
        error: error?.message || null,
        timestamp: new Date().toISOString()
    };

    TEST_RESULTS.push(result);

    const color = success ? colors.green : colors.red;
    console.log(`${color}${method} ${endpoint} - ${status} ${success ? '✓' : '✗'}${colors.reset}`);
    if (error) {
        console.log(`  ${colors.yellow}Error: ${error.message}${colors.reset}`);
    }
}

// 執行 API 測試
async function testAPI(method, endpoint, data = null, params = {}) {
    try {
        const config = {
            method: method.toLowerCase(),
            url: `${BASE_URL}${endpoint}`,
            timeout: 10000,
            validateStatus: () => true // 接受所有狀態碼
        };

        if (data) {
            config.data = data;
            config.headers = {
                'Content-Type': 'application/json'
            };
        }

        if (Object.keys(params).length > 0) {
            config.params = params;
        }

        const response = await axios(config);
        const success = response.status >= 200 && response.status < 400;

        logResult(endpoint, method, response.status, success);
        return {
            success,
            status: response.status,
            data: response.data
        };

    } catch (error) {
        logResult(endpoint, method, 'ERROR', false, error);
        return {
            success: false,
            status: 'ERROR',
            error: error.message
        };
    }
}

// 測試所有端點
async function runAllTests() {
    console.log(`${colors.blue}開始測試所有 API 端點...${colors.reset}\n`);

    // 1. Reservations 預約管理
    console.log(`${colors.blue}=== 測試 Reservations 預約管理 ===${colors.reset}`);

    await testAPI('POST', '/api/reservations', {
        userId: testData.userId,
        storeId: testData.storeId,
        reservedDate: testData.date,
        reservedTime: testData.time,
        guests: testData.guests,
        duration: testData.duration,
        content: '測試預約'
    });

    await testAPI('GET', `/api/reservations/user/${testData.userId}`);
    await testAPI('GET', `/api/reservations/store/${testData.storeId}`);
    await testAPI('GET', '/api/reservations/availability', null, {
        storeId: testData.storeId,
        date: testData.date,
        time: testData.time,
        guests: testData.guests
    });
    await testAPI('GET', '/api/reservations/tables', null, {
        storeId: testData.storeId,
        minSeats: testData.guests
    });

    await testAPI('PUT', `/api/reservations/${testData.reservationId}/status`, {
        status: 'CONFIRMED'
    });

    await testAPI('DELETE', `/api/reservations/${testData.reservationId}`, null, {
        userId: testData.userId
    });

    // 2. Booking Availability 預約可用性檢查
    console.log(`\n${colors.blue}=== 測試 Booking Availability 預約可用性檢查 ===${colors.reset}`);

    await testAPI('GET', '/api/booking/check', null, {
        storeId: testData.storeId,
        date: testData.date,
        time: testData.time,
        guests: testData.guests,
        duration: testData.duration
    });

    await testAPI('GET', '/api/booking/batch-check', null, {
        storeId: testData.storeId,
        date: testData.date,
        times: '18:00,18:30,19:00',
        guests: testData.guests,
        duration: testData.duration
    });

    await testAPI('GET', `/api/booking/slots/${testData.storeId}`, null, {
        date: testData.date,
        guests: testData.guests
    });

    await testAPI('GET', `/api/booking/all-slots/${testData.storeId}`, null, {
        date: testData.date
    });

    // 3. Store Management 店家管理
    console.log(`\n${colors.blue}=== 測試 Store Management 店家管理 ===${colors.reset}`);

    await testAPI('GET', '/api/stores/search', null, {
        keyword: '火鍋',
        category: '中式',
        isOpen: true
    });

    await testAPI('GET', `/api/stores/${testData.storeId}`);

    await testAPI('GET', `/api/stores/${testData.storeId}/availability`, null, {
        date: testData.date,
        time: testData.time,
        guests: testData.guests
    });

    await testAPI('POST', `/api/stores/${testData.storeId}/reservations`, {
        userId: testData.userId,
        reservedDate: testData.date,
        reservedTime: testData.time,
        guests: testData.guests,
        duration: testData.duration,
        content: '店家預約測試'
    });

    await testAPI('GET', `/api/stores/${testData.storeId}/stats`);

    // 4. Time Slots Management 時段管理
    console.log(`\n${colors.blue}=== 測試 Time Slots Management 時段管理 ===${colors.reset}`);

    await testAPI('GET', `/api/stores/${testData.storeId}/timeslots`);
    await testAPI('GET', `/api/stores/${testData.storeId}/timeslots/date/${testData.date}`);
    await testAPI('GET', `/api/stores/${testData.storeId}/timeslots/range`, null, {
        startDate: testData.date,
        endDate: '2024-01-20'
    });

    await testAPI('POST', `/api/stores/${testData.storeId}/timeslots`, null, {
        day: testData.date,
        startTime: '18:00',
        endTime: '18:30',
        isActive: true
    });

    await testAPI('GET', `/api/stores/${testData.storeId}/timeslots/${testData.timeslotId}`);

    await testAPI('PUT', `/api/stores/${testData.storeId}/timeslots/${testData.timeslotId}`, null, {
        startTime: '18:00',
        endTime: '18:30',
        isActive: true
    });

    await testAPI('POST', `/api/stores/${testData.storeId}/timeslots/generate`, null, {
        daysAhead: 30
    });

    await testAPI('PUT', `/api/stores/${testData.storeId}/timeslots/disable/${testData.date}`);
    await testAPI('PUT', `/api/stores/${testData.storeId}/timeslots/enable/${testData.date}`);

    // CONVERT 函數測試
    await testAPI('GET', `/api/stores/${testData.storeId}/timeslots/test-convert`, null, {
        date: testData.date
    });

    await testAPI('DELETE', `/api/stores/${testData.storeId}/timeslots/${testData.timeslotId}`);

    // 5. Table Management 桌位管理
    console.log(`\n${colors.blue}=== 測試 Table Management 桌位管理 ===${colors.reset}`);

    await testAPI('GET', `/api/stores/${testData.storeId}/tables`);
    await testAPI('GET', `/api/stores/${testData.storeId}/tables/available`, null, {
        startTime: `${testData.date}T${testData.time}:00`,
        duration: testData.duration,
        minSeats: testData.guests
    });

    await testAPI('POST', `/api/stores/${testData.storeId}/tables`, null, {
        seats: testData.guests,
        quantity: 1,
        status: true
    });

    await testAPI('GET', `/api/stores/${testData.storeId}/tables/${testData.tableId}`);

    await testAPI('PUT', `/api/stores/${testData.storeId}/tables/${testData.tableId}`, null, {
        seats: 6,
        quantity: 1,
        status: true
    });

    await testAPI('GET', `/api/stores/${testData.storeId}/tables/${testData.tableId}/availability`, null, {
        startTime: `${testData.date}T${testData.time}:00`,
        duration: testData.duration
    });

    await testAPI('DELETE', `/api/stores/${testData.storeId}/tables/${testData.tableId}`);

    // 6. Store Hours Management 營業時間管理
    console.log(`\n${colors.blue}=== 測試 Store Hours Management 營業時間管理 ===${colors.reset}`);

    await testAPI('GET', `/api/stores/${testData.storeId}/hours`);
    await testAPI('GET', `/api/stores/${testData.storeId}/hours/MONDAY`);

    await testAPI('POST', `/api/stores/${testData.storeId}/hours`, null, {
        day: 'MONDAY',
        openTime: '09:00',
        closeTime: '22:00',
        isOpen: true
    });

    await testAPI('PUT', `/api/stores/${testData.storeId}/hours/1`, null, {
        openTime: '10:00',
        closeTime: '23:00',
        isOpen: true
    });

    await testAPI('POST', `/api/stores/${testData.storeId}/hours/default`);

    await testAPI('GET', `/api/stores/${testData.storeId}/hours/check`, null, {
        day: 'MONDAY',
        time: testData.time
    });

    await testAPI('GET', `/api/stores/${testData.storeId}/hours/check/${testData.date}`, null, {
        time: testData.time
    });

    await testAPI('DELETE', `/api/stores/${testData.storeId}/hours/1`);

    // 7. Special Hours Management 特殊營業時間管理
    console.log(`\n${colors.blue}=== 測試 Special Hours Management 特殊營業時間管理 ===${colors.reset}`);

    await testAPI('GET', `/api/stores/${testData.storeId}/special`);

    await testAPI('POST', `/api/stores/${testData.storeId}/special/holiday`, null, {
        date: testData.date,
        reason: '測試假日'
    });

    await testAPI('POST', `/api/stores/${testData.storeId}/special/business`, null, {
        date: testData.date,
        openTime: '10:00',
        closeTime: '22:00',
        reason: '測試營業時間'
    });

    await testAPI('POST', `/api/stores/${testData.storeId}/special/holidays`, null, {
        dates: '2024-01-15,2024-01-16,2024-01-17',
        reason: '測試連續假期'
    });

    await testAPI('GET', `/api/stores/${testData.storeId}/special/range`, null, {
        startDate: '2024-01-01',
        endDate: '2024-01-31'
    });

    await testAPI('GET', `/api/stores/${testData.storeId}/special/${testData.date}`);
    await testAPI('GET', `/api/stores/${testData.storeId}/special/holiday/${testData.date}`);
    await testAPI('GET', `/api/stores/${testData.storeId}/special/business/${testData.date}`);

    await testAPI('GET', `/api/stores/${testData.storeId}/special/summary`, null, {
        startDate: '2024-01-01',
        endDate: '2024-01-31'
    });

    await testAPI('DELETE', `/api/stores/${testData.storeId}/special/${testData.date}`);

    // 生成測試報告
    generateTestReport();
}

// 生成測試報告
function generateTestReport() {
    console.log(`\n${colors.blue}=== 測試報告 ===${colors.reset}`);

    const totalTests = TEST_RESULTS.length;
    const successTests = TEST_RESULTS.filter(r => r.success).length;
    const failedTests = totalTests - successTests;

    console.log(`總測試數: ${totalTests}`);
    console.log(`${colors.green}成功: ${successTests}${colors.reset}`);
    console.log(`${colors.red}失敗: ${failedTests}${colors.reset}`);
    console.log(`成功率: ${((successTests / totalTests) * 100).toFixed(2)}%`);

    // 失敗的測試詳情
    if (failedTests > 0) {
        console.log(`\n${colors.red}失敗的測試:${colors.reset}`);
        TEST_RESULTS.filter(r => !r.success).forEach(result => {
            console.log(`  ${colors.red}✗${colors.reset} ${result.method} ${result.endpoint} - ${result.status}`);
            if (result.error) {
                console.log(`    錯誤: ${result.error}`);
            }
        });
    }

    // 儲存詳細報告
    const reportData = {
        summary: {
            totalTests,
            successTests,
            failedTests,
            successRate: ((successTests / totalTests) * 100).toFixed(2) + '%',
            timestamp: new Date().toISOString()
        },
        results: TEST_RESULTS
    };

    fs.writeFileSync('api_test_report.json', JSON.stringify(reportData, null, 2));
    console.log(`\n詳細報告已儲存至: api_test_report.json`);
}

// 執行測試
if (require.main === module) {
    runAllTests().catch(console.error);
}

module.exports = {
    runAllTests,
    testAPI
};