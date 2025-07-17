// 這個檔案專門負責與後端 API 溝通
import axios from 'axios';

// 1. 建立一個 axios 實例 (instance)
const apiClient = axios.create({
    // 未來換成真實後端 API 時，只需要改這裡
    baseURL: '/', // 我們模擬的 API 在根目錄下的 /api/
    headers: {
        'Content-Type': 'application/json',
        // 'Authorization': 'Bearer YOUR_TOKEN' // 未來可以放 Token
    }
});

// 2. (可選但強烈建議) 設置請求攔截器
apiClient.interceptors.request.use(config => {
    // 在發送請求之前做些什麼，例如從 localStorage 獲取 token
    // const token = localStorage.getItem('user-token');
    // if (token) {
    //     config.headers.Authorization = `Bearer ${token}`;
    // }
    return config;
}, error => {
    // 對請求錯誤做些什麼
    return Promise.reject(error);
});

// 3. (可選但強烈建議) 設置回應攔截器
apiClient.interceptors.response.use(response => {
    // 對 2xx 範圍內的回應數據做點什麼
    // 因為 axios 會把結果包在 data 裡，我們可以在這裡直接返回 response.data
    // 這樣在組件中就不需要寫 .data 了，更乾淨
    return response.data;
}, error => {
    // 超出 2xx 範圍的狀態碼都會進到這裡
    // 在這裡可以做全域的錯誤處理
    // 例如：
    // if (error.response.status === 401) {
    //     // 跳轉到登入頁
    // }
    console.error('API Error:', error.response);
    return Promise.reject(error);
});


// 4. 導出我們的 API 請求函式
export const apiService = {
    // 使用新的 apiClient 來發送請求
    getCategories: () => apiClient.get('/api/categories.json'),
    getItems: () => apiClient.get('/api/items.json'),
    getSpecs: () => apiClient.get('/api/specs.json'),

    // 未來更新或新增的函式可以這樣寫：
    // updateItem: (itemId, data) => apiClient.put(`/api/items/${itemId}`, data),
    // createItem: (data) => apiClient.post('/api/items', data),
};