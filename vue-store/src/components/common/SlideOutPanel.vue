<script setup>
// 1. 定義 Props
const props = defineProps({
    // 使用 v-model:isOpen 來雙向綁定開關狀態，這是 Vue 3 的推薦作法
    isOpen: {
        type: Boolean,
        required: true,
    },
    title: {
        type: String,
        default: '詳細資訊'
    }
});

// 2. 定義 Emits
// 為了讓 v-model 生效，必須定義一個 'update:PROP_NAME' 的事件
const emit = defineEmits(['update:isOpen', 'close']);

// 3. 事件處理函式
const handleClose = () => {
    emit('update:isOpen', false); // 更新 v-model 的值
    emit('close'); // 同時也觸發一個 close 事件，給予更多彈性
};
</script>

<template>
    <!-- 
        使用 <Teleport> 組件，這是一個 Vue 3 的高級功能。
        它可以將其內部的 HTML "傳送" 到 DOM 樹的任何地方，通常是 <body> 的末尾。
        這可以完美解決 z-index 和父元素 overflow:hidden 帶來的各種定位問題。
        讓我們的滑出面板真正地"浮動"在所有內容之上。
    -->
    <Teleport to="body">
    <!-- 使用 <Transition> 組件來為遮罩層和面板的出現/消失添加動畫 -->
        <Transition name="fade">
            <!-- 遮罩層 -->
            <div 
                v-if="isOpen" 
                class="slide-out-panel-overlay" 
                @click="handleClose"
            ></div>
        </Transition>

        <Transition name="slide-from-right">
        <!-- 面板主體 -->
            <div 
                v-if="isOpen"
                class="slide-out-panel"
                role="dialog"
                aria-modal="true"
                :aria-label="title"
            >
                <!-- 面板頭部 -->
                <header class="panel-header">
                    <h3 class="panel-title">{{ title }}</h3>
                    <button class="close-button" @click="handleClose" aria-label="關閉面板">
                        × <!-- 這是 "X" 符號 -->
                    </button>
                </header>

                <!-- 面板內容 (預設插槽) -->
                <main class="panel-content">
                    <slot></slot>
                </main>

                <!-- 面板頁腳 (頁腳插槽) -->
                <footer v-if="$slots.footer" class="panel-footer">
                    <slot name="footer"></slot>
                </footer>
            </div>
        </Transition>
    </Teleport>
</template>

<style scoped>
/* 遮罩層樣式 */
.slide-out-panel-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    background-color: rgba(0, 0, 0, 0.5);
  z-index: 1000; /* 使用一個非常高的 z-index */
}

/* 面板主體樣式 */
.slide-out-panel {
    position: fixed;
    top: 0;
    right: 0;
    height: 100vh;
    width: 90%;
    max-width: 500px;
    background-color: white;
    box-shadow: -0.5rem 0 1rem rgba(0,0,0,.15);
  z-index: 1001; /* 必須比遮罩層高 */
    display: flex;
    flex-direction: column;
}

/* 面板頭部 */
.panel-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1rem 1.5rem;
    border-bottom: 1px solid #dee2e6;
    flex-shrink: 0;
}
.panel-title {
    font-size: 1.25rem;
    font-weight: 600;
    margin: 0;
}
.close-button {
    background: none;
    border: none;
    font-size: 2rem;
    line-height: 1;
    cursor: pointer;
    padding: 0;
    color: #6c757d;
}

/* 面板內容區 */
.panel-content {
    flex-grow: 1;
    overflow-y: auto;
    padding: 1.5rem;
}

/* 面板頁腳 */
.panel-footer {
    padding: 1rem 1.5rem;
    border-top: 1px solid #dee2e6;
    background-color: #f8f9fa;
    display: flex;
    justify-content: flex-end; /* 預設按鈕靠右 */
    gap: 0.5rem;
    flex-shrink: 0;
}

/* --- 動畫效果 --- */
/* 遮罩層的淡入淡出 */
.fade-enter-active,
.fade-leave-active {
    transition: opacity 0.3s ease;
}
.fade-enter-from,
.fade-leave-to {
    opacity: 0;
}

/* 面板的滑入滑出 */
.slide-from-right-enter-active,
.slide-from-right-leave-active {
    transition: transform 0.3s ease-in-out;
}
.slide-from-right-enter-from,
.slide-from-right-leave-to {
    transform: translateX(100%);
}
</style>