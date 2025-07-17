<template>
  <div class="notification-panel" v-show="visible">
    <!-- 🔺 小箭頭 -->
    <div class="arrow-up"></div>

    <!-- 通知標題 -->
    <div class="notification-header">最近優惠通知</div>

    <!-- 通知清單 -->
    <ul class="notification-list">
      <li
        v-for="item in notifications"
        :key="item.id"
        :class="{ unread: !item.is_read }"
        class="notification-item d-flex justify-content-between align-items-start"
      >
        <!-- 左側通知內容 -->
        <div class="left-content">
          <div class="title">{{ item.title }}</div>
          <div class="date">{{ item.date }}</div>
        </div>

        <!-- 右側按鈕 -->
        <button class="view-btn" @click="markAsRead(item)">瀏覽</button>
      </li>
    </ul>

    <!-- 查看全部 -->
    <div class="see-all">查看全部</div>
  </div>
</template>

<script setup>
defineProps({
  visible: Boolean,
  notifications: Array
});
const emit = defineEmits(['mark-as-read']);
const markAsRead = (item) => {
  if (!item.is_read) {
    emit('mark-as-read', item);
  }
};
</script>

<style scoped>
/* 通知面板樣式 */
.notification-panel {
  position: fixed;
  top: 70px; /* 根據 bell icon 高度微調 */
  right: 20px;
  width: 320px;
  background: white;
  border: 1px solid #ccc;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 999;
  overflow: hidden;
  font-size: 14px;
}

/* 小箭頭樣式 */
.arrow-up {
  position: absolute;
  top: -10px;
  right: 30px; /* 請依 bell icon 水平位置微調 */
  width: 0;
  height: 0;
  border-left: 10px solid transparent;
  border-right: 10px solid transparent;
  border-bottom: 10px solid white;
}

/* 標題區塊 */
.notification-header {
  background: white;
  color: #ffc94d; /* 主色 */
  padding: 12px 16px;
  font-weight: bold;
  font-size: 16px;
  border-top-left-radius: 12px;
  border-top-right-radius: 12px;
  border-bottom: 1px solid #eee;
}

/* 通知內容 */
.notification-list {
  max-height: 280px;
  overflow-y: auto;
  margin: 0;
  padding: 0;
  list-style: none;
}


.notification-list li {
  padding: 10px;
  border-bottom: 1px solid #eee;
}

.notification-list li.unread {
  background-color: #fff9e6; /* 淺黃色背景 */
  font-weight: bold;
}

.notification-list .title {
  margin-bottom: 4px;
}

.notification-list .date {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}

/* 每則通知 */
.notification-item {
  padding: 12px;
  border-bottom: 1px solid #eee;
}

.notification-item.unread {
  background-color: #fff9e6;
  font-weight: bold;
}

.notification-item:hover {
  background-color: #fef7dc;
}

.left-content {
  flex: 1;
  padding-right: 12px;
}

.title {
  font-weight: bold;
  color: #333;
  margin-bottom: 4px;
  word-break: break-word;
}

.date {
  font-size: 12px;
  color: #888;
}

/* 查看按鈕 */
.view-btn {
  background-color: #ffc94d;
  color: white;
  border: none;
  padding: 6px 12px;
  font-size: 12px;
  border-radius: 6px;
  cursor: pointer;
  white-space: nowrap;
  margin-left: 12px;
}

.view-btn:hover {
  background-color: #e5b53f;
}

/* 查看全部連結 */
.see-all {
  text-align: center;
  padding: 8px;
  font-size: 13px;
  color: #555;
  cursor: pointer;
  border-top: 1px solid #eee;
}
</style>
