const axios = require('axios');

const BASE_URL = 'http://localhost:8080';

async function testBookedSlotsAPI() {
    console.log('🧪 測試已預訂時間段 API');

    try {
        // 測試獲取已預訂時間段
        const response = await axios.get(`${BASE_URL}/api/booking/booked-slots/1`, {
            params: {
                date: '2025-01-15'
            }
        });

        console.log('✅ 已預訂時間段 API 測試成功');
        console.log('📊 響應數據:', response.data);
        console.log('📈 已預訂數量:', response.data.length);

        if (response.data.length > 0) {
            console.log('📋 已預訂時間段樣本:');
            response.data.slice(0, 3).forEach((slot, index) => {
                console.log(`  ${index + 1}. 時間: ${slot.startTime}, 人數: ${slot.guests}, 狀態: ${slot.status}`);
            });
        } else {
            console.log('📝 該日期暫無已預訂時間段');
        }

    } catch (error) {
        console.error('❌ 已預訂時間段 API 測試失敗:', error.response?.data || error.message);
    }
}

async function testAvailableSlotsAPI() {
    console.log('\n🧪 測試可用時間段 API');

    try {
        // 測試獲取可用時間段
        const response = await axios.get(`${BASE_URL}/api/booking/all-slots/1`, {
            params: {
                date: '2025-01-15'
            }
        });

        console.log('✅ 可用時間段 API 測試成功');
        console.log('📊 響應數據:', response.data);
        console.log('📈 可用數量:', response.data.length);

        if (response.data.length > 0) {
            console.log('📋 可用時間段樣本:');
            response.data.slice(0, 3).forEach((slot, index) => {
                console.log(`  ${index + 1}. 時間: ${slot.startTime}, 狀態: ${slot.isActive}`);
            });
        } else {
            console.log('📝 該日期暫無可用時間段');
        }

    } catch (error) {
        console.error('❌ 可用時間段 API 測試失敗:', error.response?.data || error.message);
    }
}

async function runTests() {
    console.log('🚀 開始測試預約系統 API\n');

    await testBookedSlotsAPI();
    await testAvailableSlotsAPI();

    console.log('\n✨ 測試完成');
}

runTests().catch(console.error);