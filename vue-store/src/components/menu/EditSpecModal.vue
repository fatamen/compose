<script setup>
import { ref, watchEffect } from 'vue';

// 1. 定義 Props：只接收 spec 物件
const props = defineProps({
    spec: {
        type: Object,
        default: null, // 新增時，prop 會是 null
    },
});

// 2. 定義 Emits：事件名稱保持一致
const emit = defineEmits(['close', 'save', 'delete']);

// 3. 建立一個本地的響應式物件來處理表單資料
// 這樣才不會直接修改到 prop，這是 Vue 的一個重要實踐
const form = ref({});

// 4. 使用 watchEffect 來監聽 prop 的變化，並更新本地 form
watchEffect(() => {
    if (props.spec) {
        // 編輯模式：複製傳入的 spec 資料
        // 使用深拷貝 JSON.parse(JSON.stringify(...)) 來確保 options 陣列也是新的
        form.value = JSON.parse(JSON.stringify(props.spec));
    } else {
        // 新增模式：設定規格的預設空值
        form.value = {
            name: '',
            minSelection: 0,
            maxSelection: 1,
            // 規格的選項應該是一個陣列
            options: [
                { id: `opt_${Date.now()}`, name: '', price: 0, status: '供應中' } // 預設給一個空選項
            ]
        };
    }
});

// 5. 動態標題
// const title = computed(() => props.spec ? '編輯客製化規格' : '新增客製化規格');

// 6. 新增/刪除選項的方法
const addOption = () => {
    form.value.options.push({
        id: `opt_${Date.now()}`,
        name: '',
        price: 0,
        status: '供應中'
    });
};
const removeOption = (index) => {
    if (form.value.options.length > 1) { // 至少保留一個選項
        form.value.options.splice(index, 1);
    }
};

// 7. 事件處理函式
const handleSave = () => {
    // 把表單資料透過 event 傳回給父組件
    emit('save', form.value);
};

const handleDelete = () => {
    // 把要刪除的 id 傳回去
    if (props.spec?.id) {  // 使用可選鏈 ?. 更安全
        emit('delete', props.spec.id);
    }
};

// 這個 handleClose 主要是為了讓父組件能接收到關閉訊號，例如在儲存成功後觸發
const handleClose = () => {
    emit('close');
};
</script>

<template>
    <div>
        <form @submit.prevent="handleSave">
            <!-- 規格設定 -->
            <h6 class="mb-3">客製化規格設定</h6>
            <div class="mb-3">
                <label for="specName" class="form-label">規格名稱*</label>
                <input type="text" id="specName" class="form-control" v-model="form.name" required>
            </div>
            <div class="row mb-4">
                <div class="col">
                    <label for="minSelect" class="form-label">最少選擇*</label>
                    <input type="number" id="minSelect" class="form-control" v-model.number="form.minSelect" min="0" required>
                </div>
                <div class="col">
                    <label for="maxSelect" class="form-label">最多選擇*</label>
                    <input type="number" id="maxSelect" class="form-control" v-model.number="form.maxSelection" min="1" required>
                </div>
            </div>

            <hr class="my-4">

            <!-- 規格選項列表 -->
            <h6 class="mb-3">客製化規格選項</h6>

            <!-- 1. 將標籤作為一個固定的表頭 -->
            <div class="row gx-2 mb-2 d-none d-md-flex">
                <div class="col-4"><small class="text-muted">選項名稱*</small></div>
                <div class="col-3"><small class="text-muted">加價*</small></div>
                <div class="col-4"><small class="text-muted">供應狀態*</small></div>
                <div class="col-1"></div>
            </div>

            <!-- 2. 使用 v-for 遍歷選項 -->
            <div v-for="(option, index) in form.options" :key="option.id || index" class="row gx-2 align-items-center mb-2">
                <div class="col-12 col-md-4 mb-2 mb-md-0">
                    <input type="text" class="form-control" placeholder="例如: 全糖" v-model="option.name" required>
                </div>
                <div class="col-12 col-md-3 mb-2 mb-md-0">
                    <input type="number" class="form-control" placeholder="0" v-model.number="option.price" min="0" required>
                </div>
                <div class="col-12 col-md-4 mb-2 mb-md-0">
                    <select class="form-select" v-model="option.status">
                        <option value="供應中">供應中</option>
                        <option value="暫停供應">暫停供應</option>
                    </select>
                </div>
                <div class="col-12 col-md-1 d-flex justify-content-end">
                    <button type="button" class="btn btn-outline-danger btn-sm" @click="removeOption(index)" :disabled="form.options.length <= 1">
                        🗑️
                    </button>
                </div>
            </div>

            <!-- 3. 新增選項按鈕 -->
            <div class="mt-3">
                <button type="button" class="btn btn-outline-primary btn-sm" @click="addOption">
                    ⊕ 新增客製化選項
                </button>
            </div>

            <!-- 4. 頁腳按鈕區，用一個固定的容器把它推到最下面 -->
            <hr class="my-4">
            <div class="d-flex justify-content-between">
                <button v-if="props.spec" type="button" class="btn btn-danger" @click="handleDelete">刪除規格</button>
                <div v-else></div>
                <div class="d-flex gap-2">
                    <button type="button" class="btn btn-secondary" @click="handleClose">取消</button>
                    <button type="submit" class="btn btn-primary">確定提交</button>
                </div>
            </div>
        </form>
    </div>
</template>
