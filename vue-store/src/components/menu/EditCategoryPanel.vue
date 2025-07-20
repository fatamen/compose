<script setup>
import { ref, watchEffect } from 'vue';

// 1. 定義 Props：接收要編輯的 category 物件
const props = defineProps({
    category: {
        type: Object,
        default: null, // 新增時為 null
    },
    maxSort: {  // 用接收允許的最大值
        type: Number,
        required: true,
    }
});

// 2. 定義 Emits：聲明會發出的事件
const emit = defineEmits(['close', 'save', 'delete']);

// 3. 本地表單狀態
const form = ref({});

// 4. 使用 watchEffect 初始化表單
watchEffect(() => {
    if (props.category) {
        // 編輯模式：複製傳入的資料
        form.value = { ...props.category };
    } else {
        // 新增模式：設定預設空值
        form.value = {   //因為在 MenuManagement.vue 的 openCategoryPanel 已處理物件賦值，這邊就不用再給值
            // name: '',
            // description: '',
            // sort: 0 // 排序預設為 0
        };
    }
});

// 5. 事件處理函式
const errors = ref({});

const validateForm = () => {
    errors.value = {}; // 每次驗證前先清空錯誤

    if (!form.value.name || form.value.name.trim() === '') {
        errors.value.name = '類別名稱為必填項目。';
    }

    if (typeof form.value.sort !== 'number') {
        errors.value.sort = '排序必須是一個數字。';
    }else if (form.value.sort > props.maxSort) {
        // 新增驗證規則：sort 值不能大於允許的最大值
        errors.value.sort = `排序值不能大於 ${props.maxSort}。`;
    }
    // Object.keys(errors.value).length === 0 的意思是，錯誤物件中沒有任何鍵，代表沒有錯誤
    return Object.keys(errors.value).length === 0;
};

const handleSave = () => {
    // 在提交前先進行驗證
    if (!validateForm()) {
        // 如果驗證失敗，就不執行任何操作
        return;
    }
    // 驗證通過，才發出 save 事件
    emit('save', form.value);
};

const handleDelete = () => {
    if (props.category?.id) {
        emit('delete', props.category.id);
    }
};

const handleClose = () => {
    emit('close');
};
</script>

<template>
    <div>
        <!-- 使用 form 標籤，並監聽 submit 事件 -->
        <form @submit.prevent="handleSave">
        <!-- 類別名稱 -->
        <div class="mb-3">
            <label for="categoryName" class="form-label">類別名稱*</label>
            <input 
                type="text" 
                id="categoryName" 
                class="form-control"
                :class="{ 'is-invalid': errors.name }"
                v-model.trim="form.name" 
            ><!-- :class 動態加上錯誤樣式 -->

            <!-- 顯示錯誤訊息 -->
            <div v-if="errors.name" class="invalid-feedback">
                {{ errors.name }}
            </div>
        </div>

        <!-- 描述 -->
        <div class="mb-3">
            <label for="categoryDescription" class="form-label">描述</label>
            <textarea 
            id="categoryDescription" 
            class="form-control" 
            rows="3" 
            v-model="form.description"
            ></textarea>
        </div>
        
        <!-- 排序 -->
        <div class="mb-3">
            <label for="categorySort" class="form-label">排序</label>
            <input 
                type="number" 
                id="categorySort" 
                class="form-control"
                :class="{ 'is-invalid': errors.sort }" 
                v-model.number="form.sort" 
                min="1"
                :max="maxSort" 
            >
            <div class="form-text">數字越小，排序越前面。</div>
            <!-- 顯示錯誤訊息 -->
            <div v-if="errors.sort" class="invalid-feedback">
                {{ errors.sort }}
            </div>
        </div>

        <!-- 分隔線 -->
        <hr class="my-4">

        <!-- 底部按鈕區 -->
        <div class="d-flex justify-content-between">
            <!-- 刪除按鈕 (只有編輯模式下顯示) -->
            <button 
            v-if="props.category" 
            type="button" 
            class="btn btn-outline-danger" 
            @click="handleDelete"
            >
            刪除類別
            </button>
            <div v-else></div> <!-- 佔位符 -->

            <!-- 取消和儲存按鈕 -->
            <div class="d-flex gap-2">
            <button 
                type="button" 
                class="btn btn-secondary" 
                @click="handleClose"
            >
                取消
            </button>
            <button 
                type="submit" 
                class="btn btn-primary"
            >
                確定提交
            </button>
            </div>
        </div>
        </form>
    </div>
</template>