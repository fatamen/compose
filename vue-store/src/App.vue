<script setup>
// 現在 App.vue 不需要再導入任何特定頁面了
</script>

<template>
    <!-- 
    這個 <router-view> 就是一個佔位符。
    Vue Router 會根據瀏覽器當前的 URL，
    自動把對應的組件 (例如 MenuManagement.vue) 渲染在這裡。
  -->
    <router-view />
</template>

<style>
/* 全域的 CSS 樣式可以保留在這裡 */
body {
    background-color: #f8f9fa;
}
</style>