<template>
    <span>
        <span v-if="total<=0">查無資料</span>
        <span v-else>查到 {{ total }} 筆資料</span>
        ，每頁顯示
        <select @change="dochange($event)" :value="modelValue">
            <option v-for="option in options" :key="option" :value="option">{{ option }}</option>
        </select>
        筆
    </span>
</template>
    
<script setup>
    const props = defineProps(["modelValue", "total", "options"]);
    const emits = defineEmits(["update:modelValue", "selectChange"]);

    function dochange(event) {
        emits("update:modelValue", event.target.value);
        emits("selectChange");
    }
</script>
    
<style>
    
</style>