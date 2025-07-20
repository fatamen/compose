// vue-store/src/plungins/imageUpload.js (替代 firebase.js)
import apiClient from './axios.js';

/**
 * 上傳圖片到本地伺服器
 * @param {File} file - 圖片檔案
 * @param {string} type - 圖片類型 ('food' | 'store')
 * @param {number} relatedId - 相關的 ID (foodId 或 storeId)
 * @returns {Promise<string>} 回傳圖片的相對路徑
 */
export const uploadImage = async (file, type = 'food', relatedId = null) => {
    try {
        console.log('開始上傳圖片到本地伺服器...');
        
        // 建立 FormData
        const formData = new FormData();
        formData.append('file', file);
        
        // 根據類型決定 API 路徑
        let apiPath;
        if (type === 'food' && relatedId) {
            apiPath = `/api/food/${relatedId}/upload-photo`;
        } else if (type === 'store' && relatedId) {
            apiPath = `/api/store/${relatedId}/upload-photo`;
        } else {
            // 通用上傳路徑（如果後端有提供）
            apiPath = '/api/upload-image';
        }
        
        // 發送請求
        const response = await apiClient.post(apiPath, formData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        });
        
        // 取得相對路徑
        const relativePath = response.data;
        console.log('✅ 圖片上傳成功，路徑:', relativePath);
        
        return relativePath;
        
    } catch (error) {
        console.error('❌ 圖片上傳失敗:', error);
        throw new Error(`圖片上傳失敗: ${error.response?.data || error.message}`);
    }
};

/**
 * 上傳 food 圖片的專用函數
 * @param {File} file - 圖片檔案
 * @param {number} foodId - 食物 ID
 * @returns {Promise<string>} 回傳圖片的相對路徑
 */
export const uploadFoodImage = async (file, foodId) => {
    return await uploadImage(file, 'food', foodId);
};

/**
 * 上傳 store 圖片的專用函數
 * @param {File} file - 圖片檔案
 * @param {number} storeId - 店家 ID
 * @returns {Promise<string>} 回傳圖片的相對路徑
 */
export const uploadStoreImage = async (file, storeId) => {
    return await uploadImage(file, 'store', storeId);
};

/**
 * 通用圖片上傳函數（用於新增 food 時）
 * @param {File} file - 圖片檔案
 * @returns {Promise<string>} 回傳圖片的相對路徑
 */
export const uploadImageGeneric = async (file) => {
    try {
        console.log('開始上傳圖片（通用方式）...');
        
        const formData = new FormData();
        formData.append('file', file);
        
        // 使用通用上傳 API
        const response = await apiClient.post('/api/foods/upload-image', formData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        });
        
        const relativePath = response.data;
        console.log('✅ 圖片上傳成功，路徑:', relativePath);
        
        return relativePath;
        
    } catch (error) {
        console.error('❌ 圖片上傳失敗:', error);
        throw new Error(`圖片上傳失敗: ${error.response?.data || error.message}`);
    }
};

/**
 * 取得完整的圖片 URL
 * @param {string} relativePath - 相對路徑 (例如: "images/food_123.jpg")
 * @returns {string} 完整的圖片 URL
 */
export const getImageUrl = (relativePath) => {
    if (!relativePath) return null;
    
    // 如果已經是完整 URL，直接回傳
    if (relativePath.startsWith('http')) {
        return relativePath;
    }
    
    // 建構完整 URL
    const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
    return `${baseUrl}/${relativePath}`;
};

/**
 * 檢查圖片是否存在
 * @param {string} relativePath - 相對路徑
 * @returns {Promise<boolean>} 圖片是否存在
 */
export const checkImageExists = async (relativePath) => {
    try {
        const fullUrl = getImageUrl(relativePath);
        const response = await fetch(fullUrl, { method: 'HEAD' });
        return response.ok;
    } catch (error) {
        console.warn('檢查圖片存在性失敗:', error);
        return false;
    }
};