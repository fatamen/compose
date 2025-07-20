<template>
    <div class="container py-5">
        <div class="text-center mb-4">
            <h3 class="d-inline-block">餐廳資料</h3>
            <i class="text-secondary ms-2"></i>
        </div>

        <!-- 🔥 NEW: 載入中狀態 - 最小化加入 -->
        <div v-if="isLoading" class="text-center py-5">
            <div class="spinner-border text-warning" role="status">
                <span class="visually-hidden">載入中...</span>
            </div>
            <p class="mt-3">載入店家資料中...</p>
        </div>

        <div v-else class="mx-auto" style="max-width: 420px;">
            <!-- 餐廳名稱 -->
            <div class="mb-3 text-center">
                <label class="form-label w-100 text-start">餐廳名稱</label>
                <input v-model="localProfile.name" type="text" class="form-control rounded-pill" placeholder="請輸入餐廳名稱" />
            </div>
            <!-- 餐廳地址 -->
            <div class="mb-3 text-center">
                <label class="form-label w-100 text-start">餐廳地址</label>
                <input v-model="localProfile.address" type="text" class="form-control rounded-pill" placeholder="請輸入餐廳地址" />
            </div>
            <!-- 餐廳介紹 -->
            <div class="mb-3 text-center">
                <label class="form-label w-100 text-start">餐廳介紹</label>
                <textarea v-model="localProfile.intro" class="form-control" rows="4" placeholder="請輸入餐廳介紹" style="resize:vertical"></textarea>
            </div>
            <!-- 餐廳照片 -->
            <div class="mb-3 text-center">
                <label class="form-label">餐廳照片</label>
                <input type="file" class="form-control" multiple @change="onFileChange" accept="image/*" />
                            <div class="form-text">可選擇多張照片</div>
            </div>
            <!-- 手機號碼 -->
            <div class="mb-3 text-center">
                <label class="form-label w-100 text-start">手機號碼</label>
                <input v-model="localProfile.phone" class="form-control rounded-pill" placeholder="0912345678" />
            </div>
            <!-- 電子郵件 -->
            <div class="mb-2 text-center">
                <label class="form-label w-100 text-start">電子郵件</label>
                <input type="email" v-model="localProfile.email" class="form-control rounded-pill" placeholder="請輸入 email" />
            </div>
            <div class="d-flex align-items-center justify-content-center mb-3">
                <i class="bi me-2"
                    :class="isEmailVerified ? 'bi-check-circle-fill text-success' : 'bi-exclamation-circle-fill text-warning'"></i>
                <small class="text-secondary">
                    {{ isEmailVerified ? '已驗證' : '未驗證' }}
                </small>
            </div>
            <!-- 合併一顆儲存按鈕 -->
            <button type="button" class="btn btn-primary rounded-pill px-4 d-block mx-auto mb-2" :disabled="!isDirty || isSaving"
                @click="handleSave">
                <span v-if="isSaving" class="spinner-border spinner-border-sm me-2" role="status"></span>
                {{ isSaving ? '儲存中...' : '儲存' }}
            </button>
            <button type="button" class="btn btn-primary rounded-pill px-4 d-block mx-auto mt-2 mb-4" @click="goBack">
                返回
            </button>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive, watchEffect, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user.js'
import { useStore } from '@/composables/useStore.js' // 🔥 NEW: 僅為了店家切換
import axios from '@/plungins/axios.js'
// import { uploadImage } from '@/plungins/firebase-storage.js'
import { uploadImageGeneric } from '../plungins/imageUpload.js';
import { useImageUrl } from '../composables/useImageUrl.js';

const router = useRouter()
const userStore = useUserStore()

// 🔥 圖片上傳邏輯
const { getImageUrl, defaultPhoto } = useImageUrl();

// 🔥 加入 stores 來同步更新下拉選單
const { selectedStore, stores } = useStore()

// 🔥 新增：圖片上傳相關變數（原本缺少的）
const files = ref([]);
const previewUrls = ref([]);
const uploadedImagePaths = ref([]);
const isUploading = ref(false);
const uploadError = ref('');
const photoFile = ref(null); // 原本的單檔變數保留

// 🔥 修正：圖片批量上傳函數
const uploadImages = async () => {
    if (!files.value || files.value.length === 0) return [];
    
    isUploading.value = true;
    uploadError.value = '';
    uploadedImagePaths.value = [];
    
    try {
        console.log('開始批量上傳', files.value.length, '張圖片...');
        
        const uploadPromises = files.value.map(async (file, index) => {
            try {
                console.log(`上傳第 ${index + 1} 張圖片:`, file.name);
                const imagePath = await uploadImageGeneric(file);
                console.log(`第 ${index + 1} 張圖片上傳成功:`, imagePath);
                return imagePath;
            } catch (error) {
                console.error(`第 ${index + 1} 張圖片上傳失敗:`, error);
                throw error;
            }
        });
        
        const results = await Promise.all(uploadPromises);
        uploadedImagePaths.value = results;
        
        console.log('✅ 所有圖片上傳完成:', results);
        return results;
        
    } catch (error) {
        uploadError.value = `圖片上傳失敗: ${error.message}`;
        console.error('❌ 圖片上傳過程出錯:', error);
        throw error;
    } finally {
        isUploading.value = false;
    }
};

//  最小化加入載入和儲存狀態
const isLoading = ref(false)
const isSaving = ref(false)


const localProfile = reactive({
    name: '',
    address: '',
    intro: '',
    phone: '',
    email: '',
    isEmailVerified: false,
    lat: null,
    lng: null,
})

// 擴展 fetchStoreProfile 來支援特定店家 ID
const fetchStoreProfile = async (storeId = null) => {
    try {
        isLoading.value = true
        console.log(`🚀 [EditStore] 載入店家資料 (storeId: ${storeId})`)
        
        if (storeId) {
            // 🔥 NEW: 載入特定店家的詳細資料
            const response = await axios.get(`/api/stores/${storeId}`)
            if (response.data) {
                userStore.setStoreProfile(response.data)
                console.log('✅ [EditStore] 載入特定店家資料成功:', response.data)
            }
        } else {
            // 🔥 保持原始邏輯 - 載入預設店家
            await userStore.fetchStoreProfile?.()
        }
    } catch (error) {
        console.error('❌ [EditStore] 載入店家資料失敗:', error)
    } finally {
        isLoading.value = false
    }
}


onMounted(async () => {
    // 🔥 修正：優先載入當前選中的店家，而不是預設店家
    const targetStoreId = selectedStore.value
    if (targetStoreId) {
        console.log(`🎯 [EditStore] onMounted - 載入當前選中的店家 ${targetStoreId}`)
        await fetchStoreProfile(targetStoreId)
    } else {
        console.log(`🎯 [EditStore] onMounted - 沒有選中店家，載入預設店家`)
        await fetchStoreProfile()
    }
    console.log('[onMounted] userStore.storeProfile:', JSON.stringify(userStore.storeProfile, null, 2))
})

//  最小化加入店家切換監聽 - 使用組員相同的邏輯模式
watch(selectedStore, async (newStoreId, oldStoreId) => {
    if (newStoreId && newStoreId !== oldStoreId) {
        console.log(`🔄 [EditStore] 店家切換: ${oldStoreId} → ${newStoreId}`)
        await fetchStoreProfile(newStoreId)
    }
}, { immediate: false })

// 🔥 原始設計不變，這個設計真的很優雅！
watchEffect(() => {
    const p = userStore.storeProfile
    if (p) {
        // debug: 每次 pinia 資料同步也 log 一下
        console.log('[watchEffect] Sync localProfile with userStore.storeProfile:', JSON.stringify(p, null, 2))
        localProfile.name = p.name || ''
        localProfile.address = p.address || ''
        localProfile.intro = p.storeIntro || ''
        localProfile.phone = p.phone || ''
        localProfile.email = p.email || ''
        localProfile.isEmailVerified = p.isEmailVerified || false
        localProfile.lat = p.lat ?? null
        localProfile.lng = p.lng ?? null
    }
})

// 🔥 原始設計 - 保持不變，但加入 null 安全檢查
const isDirty = computed(() => {
    if (!userStore.storeProfile) return false
    return Object.keys(localProfile).some(
        key => localProfile[key] !== userStore.storeProfile[key]
    ) || photoFile.value
})

// 🔥 原始設計 - 完全保持不變
const isEmailVerified = computed(() =>
    localProfile.email === userStore.storeProfile?.email && userStore.storeProfile?.isEmailVerified
)

// 🔥 原始設計 - 完全保持不變
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
    
    console.log('選擇了', selectedFiles.length, '張圖片');
    
    // 🔥 保持向下相容：如果只有一張圖片，也設定到 photoFile
    photoFile.value = selectedFiles.length > 0 ? selectedFiles[0] : null;
}

// 🔥 新增：移除圖片函數
const removeImage = (index) => {
    URL.revokeObjectURL(previewUrls.value[index].url);
    files.value.splice(index, 1);
    previewUrls.value.splice(index, 1);
    
    // 更新 photoFile
    photoFile.value = files.value.length > 0 ? files.value[0] : null;
};

// 🔥 修正：儲存函數
async function handleSave() {
    if (!localProfile.name || !localProfile.address) {
        alert("餐廳名稱/地址必填")
        return
    }

    try {
        isSaving.value = true // 加入儲存狀態

        let photoUrl = userStore.storeProfile.photo || ""; // 預設維持舊照

        // 🔥 修正：處理多圖片上傳
        if (files.value && files.value.length > 0) {
            try {
                const photoUrls = await uploadImages();
                photoUrl = photoUrls.join(';'); // 多張圖片用分號分隔
                console.log('[handleSave] 多圖片上傳結果:', photoUrl);
            } catch (uploadError) {
                console.error('圖片上傳失敗:', uploadError);
                alert('圖片上傳失敗，請重試');
                return;
            }
        }

        const storeId = userStore.storeProfile.id
        // debug: 儲存前 log 關鍵資料
        console.log('[handleSave] storeId:', storeId)
        console.log('[handleSave] localProfile:', JSON.stringify(localProfile, null, 2))
        console.log('[handleSave] storeProfile:', JSON.stringify(userStore.storeProfile, null, 2))

        const payload = {
            name: localProfile.name,
            address: localProfile.address,
            storeIntro: localProfile.intro,
            phone: localProfile.phone,
            email: localProfile.email,
            photo: photoUrl, // 存網址（可能是多張用分號分隔）
            lat: localProfile.lat,
            lng: localProfile.lng,
        }
        console.log('[handleSave] axios.put payload:', payload)

        const resp = await axios.put(`/api/stores/${storeId}`, payload)
        console.log('[handleSave] axios response:', resp)

        if (resp.data) {
            alert('儲存成功！')
            
            // 正確對應欄位名稱，避免 intro/storeIntro 混亂
            const updatedStoreProfile = {
                ...userStore.storeProfile,
                name: localProfile.name,
                address: localProfile.address,
                storeIntro: localProfile.intro, // 🔥 關鍵：intro → storeIntro
                phone: localProfile.phone,
                email: localProfile.email,
                photo: photoUrl,
                lat: localProfile.lat,
                lng: localProfile.lng,
            }
            userStore.setStoreProfile(updatedStoreProfile)
            
            //  同步更新下拉選單中的店家名稱
            const currentStore = stores.value.find(store => store.id === storeId)
            if (currentStore && currentStore.name !== localProfile.name) {
                currentStore.name = localProfile.name
                console.log('✅ [EditStore] 已同步更新下拉選單中的店家名稱')
            }
            
            // 清理
            photoFile.value = null
            files.value = []
            previewUrls.value.forEach(preview => URL.revokeObjectURL(preview.url))
            previewUrls.value = []
        } else {
            alert('儲存失敗：' + (resp.data.message || ''))
        }
    } catch (err) {
        console.error('[handleSave] error:', err)
        alert('發生錯誤：' + (err?.message || err))
    } finally {
        isSaving.value = false //  重設儲存狀態
    }
}

// 🔥 原始設計 - 完全保持不變
function goBack() {
    router.push('/store')
}
</script>

<style scoped>
.form-control.rounded-pill {
    border-radius: 50px;
}

.btn.rounded-pill {
    border-radius: 50px;
}

.btn.btn-primary {
    background-color: #ffba20;
    border-color: #ffba20;
    color: #fff;
}

.btn.btn-primary:disabled {
    background-color: #d5d5d5;
    border-color: #d5d5d5;
    color: #fff;
}

.btn.btn-primary:hover:not(:disabled) {
    background-color: #f1cd78;
    border-color: #f1cd78;
}

/* 🔥 NEW: 最小化加入載入狀態樣式 */
.spinner-border {
    border-width: 0.2em;
}

.spinner-border-sm {
    width: 1rem;
    height: 1rem;
    border-width: 0.1em;
}
</style>