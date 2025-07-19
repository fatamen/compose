<script setup>
import { ref, watchEffect, computed } from 'vue';

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

// 建立一個本地的響應式物件來處理表單資料
// 這樣才不會直接修改到 prop，這是 Vue 的一個重要實踐
const form = ref({});

// 使用 watchEffect 來監聽 prop 的變化，並更新本地 form
watchEffect(() => {
    if (props.item) {
        // 編輯模式：複製 prop item 的資料到 form
        form.value = { ...props.item };
    } else {
        // 新增模式：設定預設空值
        form.value = {
            name: '',
            price: 0,
            status: '供應中',
            stock: 0,
            categoryId: props.categories.length > 0 ? props.categories[0].id : null, // 設定預設類別為第一個類別
            description: '',
            img: ''
        };
    }
});

const title = computed(() => props.item ? '編輯品項' : '新增品項');

const handleSave = () => {
    // 把表單資料透過 event 傳回給父組件
    emit('save', form.value);
};

const handleDelete = () => {
    // 把要刪除的 id 傳回去
    if (props.item && props.item.id) {
        emit('delete', props.item.id);
    }
};

const handleClose = () => {
    emit('close');
};

</script>


<template>
    <!-- Modal 的背景遮罩 -->
    <div class="modal-backdrop fade show"></div>
    <!-- Modal 主體 -->
    <div class="modal fade show d-block" tabindex="-1">
        <div class="modal-dialog modal-dialog-scrollable">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">{{ title }}</h5>
                    <button type="button" class="btn-close" @click="handleClose"></button>
                </div>
                <div class="modal-body">
                    <!-- 品項內容 -->
                    <h6>品項內容</h6>
                    <div class="text-center mb-3">
                        <div class="border rounded d-flex justify-content-center align-items-center"
                            style="width: 150px; height: 150px; margin: auto; cursor: pointer;">
                            <span v-if="!form.img" class="fs-1">+</span>
                            <img v-else :src="form.img" class="img-fluid">
                        </div>
                        <small class="form-text text-muted">請上傳低於1000*731像素，大小在200kb-20mb之間的(.JPG)圖片檔案</small>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">品項名稱*</label>
                        <input type="text" class="form-control" v-model="form.name">
                    </div>
                    <div class="mb-3">
                        <label class="form-label">價格*</label>
                        <input type="number" class="form-control" v-model.number="form.price" min="0"> <!--最小值為0-->
                    </div>
                    <div class="row">
                        <div class="col">
                            <label class="form-label">供應狀態*</label>
                            <select class="form-select" v-model="form.status">
                                <option>供應中</option>
                                <option>暫停供應</option>
                            </select>
                        </div>
                        <div class="col">
                            <label class="form-label">庫存</label>
                            <input type="number" class="form-control" v-model.number="form.stock" min="0"> <!--最小值為0-->
                        </div>
                    </div>

                    <div class="mb-3 mt-3"> <!-- 多加一個 mt-3 來增加與上方欄位的間距 -->
                        <label class="form-label">品項類別*</label>
                        <select class="form-select" v-model="form.categoryId"> <!-- <--- 綁定到 form.categoryId -->
                            <!-- 使用 v-for 來動態生成選項 -->
                            <option v-for="category in categories" :key="category.id" :value="category.id">
                            {{ category.name }}
                            </option>
                        </select>
                    </div>

                    <div>
                        <label class="form-label">品項敘述*</label>
                        <textarea class="form-control" rows="3" v-model="form.description"></textarea>
                    </div>

                    <hr>

                    <!-- 客製化規格 -->
                    <h6>客製化規格</h6>
                    <!-- (此處省略規格勾選的實作，但您可以輕易地用 v-for 和 checkbox 擴充) -->
                    <p class="text-muted">客製化規格列表...</p>
                </div>
                <div class="modal-footer justify-content-between">
                    <button v-if="item" type="button" class="btn btn-danger" @click="handleDelete">刪除品項</button>
                    <div v-else></div> <!-- 占位符，讓按鈕保持在右邊 -->
                    <div>
                        <button type="button" class="btn btn-secondary me-2" @click="handleClose">取消</button>
                        <button type="button" class="btn btn-primary" @click="handleSave">確定提交</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
