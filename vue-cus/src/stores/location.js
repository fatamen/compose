import { ref, watch } from 'vue';
import { defineStore } from 'pinia';
import { DateTime } from 'luxon'; // 引入 luxon 來處理時區和時間

export const useLocationStore = defineStore('location', () => {
    const address = ref(localStorage.getItem('userAddress') || '');
    const coordinates = ref(JSON.parse(localStorage.getItem('userCoordinates')) || null);
    const temperature = ref(null);
    const localTime = ref(null); // 新增：儲存當地時間
    const timeZone = ref(localStorage.getItem('userTimeZone') || null); // **重點：從 localStorage 讀取時區**
    const loading = ref(false);
    const error = ref('');

    // 監聽 address 或 coordinates 變化，並存入 localStorage
    watch(address, (newAddress) => {
        localStorage.setItem('userAddress', newAddress);
    });

    watch(coordinates, async (newCoordinates) => {
        localStorage.setItem('userCoordinates', JSON.stringify(newCoordinates));
        if (newCoordinates && newCoordinates.lat && newCoordinates.lon) {
            // 這裡不再立即呼叫 getTemperature 和 getTimeZone，因為它們會在 onMounted 或 store 初始化時處理
        } else {
            temperature.value = null;
            localTime.value = null;
            timeZone.value = null;
            localStorage.removeItem('userTemperature'); // 清除
            localStorage.removeItem('userTimeZone'); // 清除
        }
    }, { deep: true });

    // 新增：監聽 temperature 和 timeZone 變化並存入 localStorage
    watch(temperature, (newTemp) => {
        if (newTemp !== null) {
            localStorage.setItem('userTemperature', JSON.stringify(newTemp));
        } else {
            localStorage.removeItem('userTemperature');
        }
    });

    // 新增：獲取時區
    const getTimeZone = async (lat, lon) => {
        if (!lat || !lon) {
            timeZone.value = null;
            localTime.value = null;
            return;
        }
        try {
            const url = `http://api.geonames.org/timezoneJSON?lat=${lat}&lng=${lon}&username=YOUR_GEONAMES_USERNAME`;
            const response = await fetch(url);
            const data = await response.json();
            if (data && data.timezoneId) {
                timeZone.value = data.timezoneId; // 設置時區時會觸發上面的 watch
            } else {
                console.warn('無法從 GeoNames 取得時區資料', data);
                timeZone.value = null;
            }
        } catch (err) {
            console.error('取得時區失敗:', err);
            timeZone.value = null;
        }
    };

    // 新增：更新當地時間
    const updateLocalTime = () => {
        if (!timeZone.value) {
            localTime.value = null;
            return;
        }
        const now = DateTime.now().setZone(timeZone.value);
        localTime.value = now.toFormat('yyyy-MM-dd HH:mm:ss');
    };

    let timeInterval = null;
    // 確保這個 setInterval 總是在 timeZone 有值時運行
    // 並在 timeZone 變為 null 時清除
    watch(timeZone, (newTimeZone) => {
        if (timeInterval) {
            clearInterval(timeInterval);
        }
        if (newTimeZone) {
            updateLocalTime(); // 立即更新一次
            timeInterval = setInterval(updateLocalTime, 60000); // 每 1 分鐘更新一次
        }
    });

    watch(timeZone, (newTimeZone) => {
        if (newTimeZone) {
            localStorage.setItem('userTimeZone', newTimeZone);
            updateLocalTime(); // 時區變化時立即更新當地時間
        } else {
            localStorage.removeItem('userTimeZone');
            localTime.value = null;
        }
    }, { immediate: true }); // **重要：讓 timeZone 的 watch 在初始化時立即執行**

    // 格式化地址（保持不變）
    const formatAddress = (input) => {
        if (!input.trim()) return input;
        const fullAddressRegex = /^(\S+?)([縣市])(.+?)(區|鄉|鎮|市)(.*?)(\d+號)/;
        const simpleAddressRegex = /^(.+?)(\d+號)$/;
        let match = input.match(fullAddressRegex);
        if (match) {
            return `${match[1]}${match[2]}${match[3]}${match[4]}${match[5].trim()}${match[6]}`;
        }
        match = input.match(simpleAddressRegex);
        if (match) {
            return `${match[1].trim()}${match[2]}`;
        }
        return input;
    };

    // 格式化台灣地址（保持不變）
    const formatTaiwanAddress = (addressData) => {
        if (!addressData) return '';
        const country = addressData.country || '臺灣';
        const city = addressData.city || addressData.county || '';
        const district = addressData.suburb || addressData.town || addressData.city_district || '';
        const village = addressData.neighbourhood || addressData.village || '';
        const road = addressData.road || '';
        let houseNumber = addressData.house_number || '';
        if (houseNumber.includes('之')) {
            houseNumber = houseNumber.split('之')[1] || houseNumber;
        }
        return [country, city, district, village, road, houseNumber].filter(part => part).join('');
    };

    // 取得溫度（保持不變）
    const getTemperature = async (lat, lon) => {
        if (!lat || !lon) {
            temperature.value = null;
            return;
        }
        try {
            const url = `https://api.open-meteo.com/v1/forecast?latitude=${lat}&longitude=${lon}&current_weather=true`;
            const response = await fetch(url);
            const data = await response.json();
            if (data && data.current_weather && typeof data.current_weather.temperature === 'number') {
                temperature.value = data.current_weather.temperature; // 設置溫度時會觸發上面的 watch
            } else {
                console.warn('無法從 Open-Meteo 取得溫度資料', data);
                temperature.value = null;
            }
        } catch (err) {
            console.error('取得溫度失敗:', err);
            temperature.value = null;
        }
    };

    // 新增一個初始化函數，在 Pinia store 被使用時立即執行
    // 或者在 getCoordinates/getCurrentLocation 成功後呼叫
    const initializeLocationData = async () => {
        if (coordinates.value && coordinates.value.lat && coordinates.value.lon) {
            if (temperature.value === null) { // 只有當溫度為空時才去獲取
                await getTemperature(coordinates.value.lat, coordinates.value.lon);
            }
            if (timeZone.value === null) { // 只有當時區為空時才去獲取
                await getTimeZone(coordinates.value.lat, coordinates.value.lon);
            }
        }
    };

    // 在 store 首次被載入時執行初始化
    // 或者您可以在 App.vue 的 onMounted 中呼叫 locationStore.initializeLocationData()
    if (coordinates.value && (temperature.value === null || timeZone.value === null)) {
        initializeLocationData();
    }

    // 查詢座標（保持不變）
    const getCoordinates = async (inputAddress) => {
        const addrToSearch = inputAddress ? formatAddress(inputAddress) : formatAddress(address.value);
        if (!addrToSearch.trim()) {
            error.value = '請輸入地址';
            coordinates.value = null;
            temperature.value = null;
            localTime.value = null; // 清空當地時間
            timeZone.value = null; // 清空時區
            return false;
        }
        loading.value = true;
        error.value = '';
        coordinates.value = null;
        try {
            const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(addrToSearch)}`;
            const response = await fetch(url, {
                headers: { 'User-Agent': 'Jimmy/tokin81@yahoo.com.tw' }
            });
            const data = await response.json();
            if (data.length > 0) {
                coordinates.value = { lat: data[0].lat, lon: data[0].lon };
                address.value = addrToSearch;
                return true;
            } else {
                error.value = '無法找到該地址的座標';
                coordinates.value = null;
                temperature.value = null;
                localTime.value = null; // 清空當地時間
                timeZone.value = null; // 清空時區
                return false;
            }
        } catch (err) {
            error.value = '查詢失敗，請稍後再試';
            console.error('API 錯誤:', err);
            coordinates.value = null;
            temperature.value = null;
            localTime.value = null; // 清空當地時間
            timeZone.value = null; // 清空時區
            return false;
        } finally {
            loading.value = false;
        }
    };

    // 獲取當前位置（保持不變）
    const getCurrentLocation = async () => {
        if (!navigator.geolocation) {
            alert('您的瀏覽器不支援定位功能');
            return false;
        }
        loading.value = true;
        error.value = '';
        try {
            const position = await new Promise((resolve, reject) => {
                navigator.geolocation.getCurrentPosition(resolve, reject);
            });
            const { latitude, longitude } = position.coords;
            const response = await fetch(
                `https://nominatim.openstreetmap.org/reverse?lat=${latitude}&lon=${longitude}&format=json&addressdetails=1`
            );
            const data = await response.json();
            if (data && data.display_name) {
                address.value = formatTaiwanAddress(data.address);
                coordinates.value = { lat: latitude, lon: longitude };
                return true;
            } else {
                alert('無法解析地址，請稍後再試');
                return false;
            }
        } catch (err) {
            console.error('定位失敗:', err);
            alert('無法獲取位置，請檢查權限或稍後再試');
            return false;
        } finally {
            loading.value = false;
        }
    };

    // 設定地址（保持不變）
    const setAddress = (newAddress) => {
        address.value = newAddress;
    };

    return {
        address,
        coordinates,
        temperature,
        localTime, // 暴露當地時間
        timeZone, // 暴露時區
        loading,
        error,
        setAddress,
        getCoordinates,
        getCurrentLocation,
        getTemperature,
        getTimeZone, // 暴露時區方法（可選）
        updateLocalTime, // 暴露時間更新方法（可選）
        initializeLocationData // 暴露這個初始化方法
    };
});