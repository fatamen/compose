<script setup>
defineProps({
    title: {
        type: String,
        required: true
    }
});
</script>

<template>
    <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
        <!-- 左側內容：標題和其他東西都從這裡傳入 -->
        <div class="d-flex align-items-center">
            <h2 class="h3 mb-0">{{ title }}</h2> <!-- mb-0 移除 h3 預設的下邊距 --> 
            <!-- "插槽" (Slot)：允許父組件向這裡插入自訂內容 (例如店鋪選擇器) -->
            <div class="ms-3">
                <slot name="actions"></slot>
            </div>
        </div>

        <!-- 右側內容：也用插槽，方便未來擴充 -->
        <div>
            <slot name="right-side"></slot>
        </div>
    </div>
</template>