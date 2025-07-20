<template>
    <div class="modal-bg" v-if="show">
        <div class="modal-dialog modal-dialog-centered modal-lg">
            <div class="modal-content">
                <div class="modal-header border-0 pb-0 justify-content-between">
                    <button class="btn nav-btn" @click="emit('back')">
                        <svg width="28" height="28" fill="none" viewBox="0 0 24 24">
                            <path d="M15 6l-6 6 6 6" stroke="#222" stroke-width="2" stroke-linecap="round"
                                stroke-linejoin="round" />
                        </svg>
                    </button>
                    <button type="button" class="btn-close custom-close" @click="emit('close')"></button>
                </div>
                
                <div class="modal-body">
                    <div class="d-flex justify-content-between align-items-center mb-3">
                        <h3 class="fw-bold mb-0">告訴我們您的商家資訊</h3>
                        <button class="btn btn-outline-secondary btn-sm" @click="onSaveDraft">儲存並離開</button>
                    </div>
                    <div class="mb-3 text-secondary" style="font-size:15px;">
                        此訊息將顯示在 金碗 平台上，以便消費者有任何問題時可以搜尋並與您聯繫。
                    </div>
                    
                    <form @submit.prevent="onSubmit">
                        <!-- 餐廳名稱 -->
                        <div class="mb-3 position-relative">
                            <input type="text" class="form-control" v-model="storeName" placeholder="餐廳或商店名稱 *" required />
                            <span class="qmark" tabindex="0" title="請填寫完整店名">
                                <svg width="19" height="19" fill="none" viewBox="0 0 24 24">
                                    <circle cx="12" cy="12" r="10" stroke="#ffba20" stroke-width="2" />
                                    <text x="7" y="16" font-size="13" fill="#ffba20">?</text>
                                </svg>
                            </span>
                        </div>
                        
                        <!-- 選擇商家類型 -->
                        <div class="mb-3">
                            <select class="form-select" v-model="storeCategory" required>
                                <option value="" disabled>餐廳類型 *</option>
                                <option v-for="c in categories" :key="c.id" :value="c.name">{{ c.name }}</option>
                            </select>
                        </div>
                        
                        <!-- 電話 -->
                        <div class="mb-3 d-flex align-items-center">
                            <div class="flag-box d-flex align-items-center px-2 me-2">
                                <img src="https://flagcdn.com/h20/tw.png" alt="台灣" style="width:22px; height:16px" />
                                <span class="ms-1">+886</span>
                            </div>
                            <input type="tel" class="form-control" v-model="phone" pattern="[0-9]{9,10}" 
                                placeholder="行動電話 *" style="flex:1" required />
                        </div>
                        
                        <!-- 餐廳介紹 -->
                        <div class="mb-3">
                            <textarea v-model="storeIntro" class="form-control" placeholder="餐廳介紹" rows="4" 
                                style="resize: vertical; min-height: 90px;"></textarea>
                        </div>
                        
                        <!-- 餐廳照片 -->
                        <div class="mb-3">
                            <label class="form-label">餐廳照片</label>
                            <input type="file" class="form-control" multiple @change="onFileChange" accept="image/*" />
                            <div class="form-text">可選擇多張照片</div>
                        </div>
                        
                        <!-- 顯示選中的檔案 -->
                        <div v-if="files && files.length > 0" class="mb-3">
                            <small class="text-muted">已選擇 {{ files.length }} 張照片</small>
                            <div class="d-flex flex-wrap gap-2 mt-2">
                                <span v-for="(file, index) in files" :key="index" class="badge bg-light text-dark">
                                    {{ file.name }}
                                </span>
                            </div>
                        </div>
                        
                        <div v-if="error" class="text-danger mb-3">{{ error }}</div>
                        
                        <!-- 提交按鈕 -->
                        <div class="d-flex justify-content-end">
                            <button class="btn btn-main px-4" style="font-size:18px;" type="submit" :disabled="loading">
                                <span v-if="loading">處理中...</span>
                                <span v-else>繼續</span>
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useUserStore } from '@/stores/user.js'
import axios from '@/plungins/axios.js'
// import { uploadFilesToFirebase } from '@/utils/uploadToFirebase.js'
import { uploadImageGeneric } from '../../plungins/imageUpload.js';
import { useImageUrl } from '../../composables/useImageUrl.js';

const props = defineProps({ 
    show: Boolean,
    registerData: Object  // 從 RegisterBusinessModal 傳來的資料
})

const emit = defineEmits(['close', 'submit', 'back'])

const { getImageUrl, defaultPhoto } = useImageUrl();

const userStore = useUserStore()
const storeName = ref('')
const storeCategory = ref('')
const phone = ref('')
const storeIntro = ref('')
const files = ref(null)
const previewUrls = ref([]);
const uploadedImagePaths = ref([]); // 儲存上傳成功的圖片路徑
const isUploading = ref(false);
const uploadError = ref('');
const categories = ref([])
const error = ref('')
const loading = ref(false)

const uploadImages = async () => {
    if (!files.value || files.value.length === 0) return [];
    
    isUploading.value = true;
    uploadError.value = '';
    uploadedImagePaths.value = [];
    
    try {
        //console.log('開始批量上傳', files.value.length, '張圖片...');
        
        const uploadPromises = files.value.map(async (file, index) => {
            try {
                //console.log(`上傳第 ${index + 1} 張圖片:`, file.name);
                const imagePath = await uploadImageGeneric(file);
                //console.log(`第 ${index + 1} 張圖片上傳成功:`, imagePath);
                return imagePath;
            } catch (error) {
                //console.error(`第 ${index + 1} 張圖片上傳失敗:`, error);
                throw error;
            }
        });
        
        const results = await Promise.all(uploadPromises);
        uploadedImagePaths.value = results;
        
        //console.log('✅ 所有圖片上傳完成:', results);
        return results;
        
    } catch (error) {
        uploadError.value = `圖片上傳失敗: ${error.message}`;
        //console.error('❌ 圖片上傳過程出錯:', error);
        throw error;
    } finally {
        isUploading.value = false;
    }
};

// 處理多檔案選擇
function onFileChange(event) {
const selectedFiles = Array.from(event.target.files);
    files.value = selectedFiles;
    uploadError.value = '';
    
    // 生成預覽 URL
    previewUrls.value = selectedFiles.map(file => ({
        file: file,
        url: URL.createObjectURL(file),
        name: file.name
    }));
    
    //console.log('選擇了', selectedFiles.length, '張圖片');
};

// 當 Modal 顯示且有 registerData 時，初始化資料
watch(() => [props.show, props.registerData], ([show, registerData]) => {
    if (show && registerData) {
        // 使用從上一步傳來的資料作為預設值
        storeName.value = registerData.storeName || ''
        phone.value = registerData.phone || ''
        
        // 清除之前的錯誤和檔案
        error.value = ''
        files.value = null
        
        // 載入分類資料
        loadCategories()
    }
}, { immediate: true })

// 載入分類資料
async function loadCategories() {
    try {
        const res = await axios.get('/api/categories')
        categories.value = res.data
    } catch (e) {
        categories.value = []
        console.error('載入分類失敗', e)
        error.value = '載入分類資料失敗，請重試'
    }
}

// 表單送出
async function onSubmit() {
    if (!props.registerData?.ownerId) {
        error.value = '缺少 Owner ID，請重新開始註冊流程'
        return
    }
    
    if (!storeName.value || !storeCategory.value || !phone.value) {
        error.value = '請完整填寫必要欄位'
        return
    }

    loading.value = true
    error.value = ''

    try {
        // 🔥 修改：使用新的本地上傳邏輯
        let photoUrls = []
        if (files.value && files.value.length > 0) {
            try {
                photoUrls = await uploadImages();  // ← 改用新的上傳函數
                //console.log('圖片上傳成功，路徑:', photoUrls)
            } catch (uploadError) {
                //console.error('圖片上傳失敗:', uploadError)
                error.value = '圖片上傳失敗，請重試'
                return
            }
        }

        // 2. 準備 payload
        const payload = {
            ownerId: props.registerData.ownerId,
            name: storeName.value,
            storeCategory: storeCategory.value,
            storeIntro: storeIntro.value,
            phone: phone.value,
            photo: photoUrls.join(';') // 多張用分號隔開
        }

        // 3. 發送 API 請求
        const res = await axios.post('/api/stores/registerInfo', payload)
        
        if (res.data.success) {
            // 成功後將資料傳給父組件，進入下一步
            const storeData = {
                ...payload,
                storeId: res.data.storeId,
                files: files.value
            }
            
            emit('submit', storeData)
        } else {
            error.value = res.data.message || '商家資訊送出失敗'
        }
    } catch (e) {
        if (e.response) {
            error.value = `伺服器錯誤 (HTTP ${e.response.status})：${e.response.data?.message || ''}`
        } else {
            error.value = '伺服器錯誤，請稍後再試'
        }
    } finally {
        loading.value = false
    }
}

// 儲存草稿
function onSaveDraft() {
    localStorage.setItem('registerStoreName', storeName.value)
    localStorage.setItem('registerStoreCategory', storeCategory.value)
    localStorage.setItem('registerPhone', phone.value)
    localStorage.setItem('registerStoreIntro', storeIntro.value)
    
    emit('close')
    alert('已儲存草稿')
}
</script>

<style scoped>
.modal-bg {
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.08);
    z-index: 9999;
    display: flex;
    align-items: center;
    justify-content: center;
}

.modal-dialog {
    max-width: 600px;
    width: 95vw;
    margin: 0 auto;
    max-height: 90vh;
    overflow-y: auto;
}

.modal-content {
    background: #fff !important;
    border-radius: 20px;
    box-shadow: 0 2px 24px 4px rgba(0, 0, 0, 0.10);
    border: none;
    padding: 1.5rem;

    width: 100%;
    max-width: 600px;
    max-height: 90vh;
    overflow-y: auto;
}

.nav-btn {
    background: none;
    border: none;
    padding: 0;
    margin-left: -5px;
    margin-top: -5px;
    box-shadow: none;
    outline: none;
}

.form-control, .form-select {
    border-radius: 8px;
    border: 2px solid #ddd;
    font-size: 16px;
    padding: 12px;
}

.form-control:focus, .form-select:focus {
    border-color: #ffba20;
    box-shadow: 0 0 0 1px #ffba2021;
}

.qmark {
    position: absolute;
    right: 10px;
    top: 13px;
    cursor: pointer;
    display: flex;
    align-items: center;
}

.flag-box {
    background: #f7f7f7;
    border-radius: 7px;
    border: 1px solid #eee;
    height: 46px;
}

.btn-main {
    background: #ffba20;
    color: #fff;
    font-weight: bold;
    font-size: 18px;
    border-radius: 10px;
    border: none;
    transition: filter 0.15s;
    box-shadow: 0 2px 8px 1px #ffba200f;
    padding: 12px 24px;
}

.btn-main:hover {
    filter: brightness(1.08);
    background: #ffba20;
}

.btn-main:disabled {
    opacity: 0.6;
    cursor: not-allowed;
}

.text-danger {
    color: #dc3545;
    font-size: 14px;
}

.badge {
    font-size: 12px;
    padding: 4px 8px;
}
</style>