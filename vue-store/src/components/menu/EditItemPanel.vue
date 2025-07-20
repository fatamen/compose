<script setup>
import { ref, watchEffect, computed } from 'vue';
import { useImageUrl } from '../../composables/useImageUrl.js';

const props = defineProps({
    item: {
        type: Object,
        default: null, // 如果是新增，prop 會是 null
    },
    categories: { 
        type: Array, 
        required: true } // 在 props 中接收 categories
});

const emit = defineEmits(['close', 'save', 'delete']);

const { getImageUrl } = useImageUrl(); // 使用 useImageUrl 建立圖片 URL

// 建立一個本地的響應式物件來處理表單資料
// 這樣才不會直接修改到 prop，這是 Vue 的一個重要實踐
const form = ref({});

// 🔥 新增：圖片相關的響應式變數
const selectedFile = ref(null);
const previewUrl = ref('');
const fileInput = ref(null);
const imageError = ref('');

// 使用 watchEffect 來監聽 prop 的變化，並更新本地 form
watchEffect(() => {
    if (props.item) {
        // 編輯模式：複製 prop item 的資料到 form
        form.value = { ...props.item };

        // 🔥 修正：處理圖片顯示 - 使用後端的 imgResource 欄位
        if (props.item.imgResource) {
            previewUrl.value = getImageUrl(props.item.imgResource);
            form.value.imageUrl = props.item.imgResource; // 統一使用 imageUrl 在前端
        } else {
            previewUrl.value = '';
        }
        
        // 🔥 修正：確保供應狀態正確對應
        // 檢查後端傳來的狀態欄位名稱，可能是 status 或其他名稱
        if (props.item.isActive !== undefined) {
            form.value.status = props.item.isActive ? '供應中' : '暫停供應';
        } else {
            form.value.status = '供應中'; // 預設值
        }
        
        // 🔥 debug：印出接收到的資料，確認欄位名稱
        console.log('編輯模式 - 接收到的 item 資料:', props.item);

    } else {
        // 新增模式：設定預設空值
        form.value = {
            name: '',
            price: 0,
            status: '供應中',
            stock: 0,
            categoryId: props.categories.length > 0 ? props.categories[0].id : null, // 設定預設類別為第一個類別
            description: '',
            imageUrl: ''
        };
        // 清空圖片相關資料
        previewUrl.value = '';
        selectedFile.value = null;
    }
});

// 🔥 新增：檔案驗證函數
const validateImage = (file) => {
    const errors = [];
    
    // 檢查檔案類型
    if (!file.type.startsWith('image/jpeg') && !file.type.startsWith('image/jpg')) {
        errors.push('只允許 JPG 格式的圖片');
    }
    
    // 檢查檔案大小 (200KB 到 20MB)
    const sizeInMB = file.size / (1024 * 1024);
    if (sizeInMB < 0.2) {
        errors.push('檔案大小不能小於 200KB');
    }
    if (sizeInMB > 20) {
        errors.push('檔案大小不能超過 20MB');
    }
    
    return errors;
};

// 🔥 新增：檢查圖片尺寸
const checkImageDimensions = (file) => {
    return new Promise((resolve) => {
        const img = new Image();
        img.onload = () => {
            const errors = [];
            if (img.width < 1000 || img.height < 731) {
                errors.push('圖片尺寸至少需要 1000x731 像素');
            }
            resolve(errors);
        };
        img.src = URL.createObjectURL(file);
    });
};

// 🔥 新增：處理檔案選擇
const handleFileSelect = async (event) => {
    const file = event.target.files[0];
    if (!file) return;
    
    imageError.value = '';
    
    // 基本驗證
    const basicErrors = validateImage(file);
    if (basicErrors.length > 0) {
        imageError.value = basicErrors.join('、');
        return;
    }
    
    // 尺寸驗證
    const dimensionErrors = await checkImageDimensions(file);
    if (dimensionErrors.length > 0) {
        imageError.value = dimensionErrors.join('、');
        return;
    }
    
    // 驗證通過，設定檔案和預覽
    selectedFile.value = file;
    previewUrl.value = URL.createObjectURL(file);
    
    // 將檔案加入表單資料
    form.value.imageFile = file;
};

// 🔥 新增：移除圖片
const removeImage = () => {
    selectedFile.value = null;
    previewUrl.value = '';
    imageError.value = '';
    
    // 清空檔案輸入
    if (fileInput.value) {
        fileInput.value.value = '';
    }
    
    // 從表單資料中移除
    delete form.value.imageFile;
    
    // 如果是編輯模式且原本有圖片，標記為刪除
    if (props.item?.imgResource) {  // ← 修正：使用 imgResource
        form.value.deleteExistingImage = true;
        form.value.imageUrl = ''; // 清空前端的 imageUrl
    }
};

// 🔥 新增：觸發檔案選擇
const triggerFileSelect = () => {
    fileInput.value?.click();
};

// 🔥 新增：計算是否顯示圖片
const hasImage = computed(() => {
    return previewUrl.value && previewUrl.value !== '';
});

// 🔥 新增：預設圖片 URL（你可以放在 public/images/ 下）
const defaultImageUrl = '/images/default-food.jpg';


const handleSave = () => {
    // 把表單資料透過 event 傳回給父組件
    emit('save', form.value);
};

const handleDelete = () => {
    // 把要刪除的 id 傳回去
    if (props.item?.id) { // 使用可選鏈 ?. 更安全
        emit('delete', props.item.id);
    }
};

// 這個 handleClose 主要是為了讓父組件能接收到關閉訊號，例如在儲存成功後觸發
const handleClose = () => {
    emit('close');
};

</script>


<template>
    <div>
        <!-- 表單的主體內容 -->
        <form @submit.prevent="handleSave">
            <!-- 品項內容 -->
            <h6 class="mb-3">品項內容</h6>

            <!-- 🔥 修改：圖片上傳區域 -->
            <div class="text-center mb-3">
                <div class="position-relative d-inline-block">
                    <!-- 圖片預覽區域 -->
                    <div 
                        class="border rounded d-flex justify-content-center align-items-center bg-light position-relative overflow-hidden" 
                        style="width: 150px; height: 150px; cursor: pointer;"
                        @click="!hasImage ? triggerFileSelect() : null"
                    >
                        <!-- 沒有圖片時顯示 + 號 -->
                        <span v-if="!hasImage" class="fs-1 text-muted">+</span>
                        
                        <!-- 有圖片時顯示預覽 -->
                        <img 
                            v-else 
                            :src="previewUrl" 
                            class="img-fluid w-100 h-100"
                            style="object-fit: cover;"
                            alt="圖片預覽"
                        >
                    </div>
                    
                    <!-- 🔥 新增：移除圖片按鈕 -->
                    <button 
                        v-if="hasImage"
                        type="button"
                        class="btn btn-danger btn-sm rounded-circle position-absolute"
                        style="top: -8px; right: -8px; width: 24px; height: 24px; padding: 0; line-height: 1;"
                        @click="removeImage"
                        title="移除圖片"
                    >
                        ×
                    </button>
                </div>
                
                <!-- 🔥 新增：隱藏的檔案輸入 -->
                <input 
                    ref="fileInput"
                    type="file" 
                    accept="image/jpeg,image/jpg" 
                    style="display: none"
                    @change="handleFileSelect"
                >
                
                <!-- 🔥 修改：提示文字和錯誤訊息 -->
                <div class="mt-2">
                    <!-- 顯示檔案名稱或提示文字 -->
                    <div v-if="hasImage && selectedFile">
                        <small class="form-text text-success d-block">
                            已選擇：{{ selectedFile.name }}
                        </small>
                    </div>
                    
                    <div v-else-if="hasImage && !selectedFile">
                        <small class="form-text text-info d-block">
                            目前使用圖片
                        </small>
                    </div>
                    
                    <div v-else>
                        <small class="form-text text-muted d-block">
                            請上傳 JPG 格式圖片<br>
                            尺寸：最小 1000x731 像素<br>
                            大小：200KB - 20MB
                        </small>
                    </div>
                    
                    <!-- 🔥 新增：錯誤訊息 -->
                    <small v-if="imageError" class="text-danger d-block mt-1">
                        {{ imageError }}
                    </small>
                    
                    <!-- 🔥 新增：重新選擇按鈕 -->
                    <button 
                        v-if="hasImage"
                        type="button"
                        class="btn btn-outline-secondary btn-sm mt-2"
                        @click="triggerFileSelect"
                    >
                        重新選擇
                    </button>
                </div>
            </div>

            <div class="mb-3">
                <label class="form-label">品項名稱*</label>
                <input type="text" class="form-control" v-model="form.name" required>
            </div>
            
            <div class="mb-3">
                <label class="form-label">品項類別*</label>
                <select class="form-select" v-model="form.categoryId" required>
                <option v-for="category in categories" :key="category.id" :value="category.id">
                    {{ category.name }}
                </option>
                </select>
            </div>
            
            <div class="row mb-3">
                <div class="col">
                <label class="form-label">價格*</label>
                <input type="number" class="form-control" v-model.number="form.price" min="0" required>
                </div>
                <div class="col">
                    <label class="form-label">供應狀態*</label>
                    <select class="form-select" v-model="form.status">
                        <option>供應中</option>
                        <option>暫停供應</option>
                    </select>
                </div>
            </div>

            <div class="mb-3">
                <label class="form-label">庫存</label>
                <input type="number" class="form-control" v-model.number="form.stock" min="0">
            </div>

            <div class="mb-3">
                <label class="form-label">品項敘述*</label>
                <textarea class="form-control" rows="3" v-model="form.description"></textarea>
            </div>

            <hr class="my-4">

            <!-- 按鈕現在放在表單的末尾 -->
            <div class="d-flex justify-content-between">
                <button v-if="props.item" type="button" class="btn btn-outline-danger" @click="handleDelete">刪除品項</button>
                <div v-else></div><!-- 占位符，讓按鈕保持在右邊 -->
                <div class="d-flex gap-2">
                    <button type="button" class="btn btn-secondary" @click="handleClose">取消</button>
                    <button type="submit" class="btn btn-primary">確定提交</button>
                </div>
            </div>
        </form>
    </div>
</template>

<style scoped>
/* 🔥 新增：確保圖片預覽區域的樣式 */
.overflow-hidden {
    overflow: hidden;
}

/* 移除圖片按鈕的 hover 效果 */
.btn-danger.btn-sm:hover {
    transform: scale(1.1);
    transition: transform 0.2s;
}
</style>