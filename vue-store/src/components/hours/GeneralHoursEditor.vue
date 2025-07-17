<script setup>
import { ref, onMounted } from 'vue';

const props = defineProps({
  generalHours: {
    type: Object,
    required: true,
  },
});

const emit = defineEmits(['close', 'save']);

// 內部狀態，用於編輯營業時間
const editableHours = ref({});

// 時間選項（分鐘，以 15 分鐘為間隔，用於下拉選單）
const timeOptions = ref([]);
const generateTimeOptions = () => {
  const options = [];
  for (let h = 0; h < 24; h++) {
    for (let m = 0; m < 60; m += 15) {
      const hour = String(h).padStart(2, '0');
      const minute = String(m).padStart(2, '0');
      options.push(`${hour}:${minute}`);
    }
  }
  return options;
};

onMounted(() => {
  // 深拷貝 props，以便在子組件中修改而不影響父組件
  editableHours.value = JSON.parse(JSON.stringify(props.generalHours));
  timeOptions.value = generateTimeOptions();
});

// 處理開關切換
const toggleDayStatus = (day) => {
  if (editableHours.value[day] === '關閉') {
    // 預設設定為 10:00 - 14:00 或其他合理值
    editableHours.value[day] = '10:00 - 14:00';
  } else {
    editableHours.value[day] = '關閉';
  }
};

// 解析時間字串為開始和結束時間
const parseTimeRange = (timeRange) => {
  if (timeRange === '關閉') return { start: '', end: '' };
  const parts = timeRange.split(' - ');
  return { start: parts[0] || '', end: parts[1] || '' };
};

// 格式化時間範圍為字串
const formatTimeRange = (start, end) => {
  if (!start || !end) return '關閉';
  return `${start} - ${end}`;
};

// 添加時間段（圖片中未明確顯示，但編輯可能需要此功能）
const addTimeSegment = (day) => {
  // 這裡需要更複雜的邏輯來管理每個日期的多個時間段
  alert(`為 ${day} 添加時間段功能待實現`);
};

// 刪除時間段（圖片中未明確顯示）
const removeTimeSegment = (day, index) => {
  alert(`刪除 ${day} 的時間段功能待實現`);
};

// 保存更改
const saveChanges = () => {
  // 在這裡可以執行數據驗證
  emit('save', editableHours.value);
};

// 重置為初始狀態
const resetChanges = () => {
  editableHours.value = JSON.parse(JSON.stringify(props.generalHours));
  alert('已重置為初始狀態');
};
</script>

<template>
  <div class="h-100 d-flex flex-column p-4">
    <!-- 頂部標題和關閉按鈕 -->
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h4 class="mb-0 fw-bold">調整一般營業時間</h4>
      <button class="btn-close" @click="emit('close')"></button>
    </div>
    <hr>

    <!-- 可滾動內容區域 -->
    <div class="flex-grow-1 overflow-auto pe-2 py-3">
      <div class="mb-3 form-check form-switch">
        <input class="form-check-input" type="checkbox" id="toggleAllDays">
        <label class="form-check-label" for="toggleAllDays">複製每日開關與時間</label>
      </div>

      <div v-for="(time, day) in editableHours" :key="day" class="mb-4 p-3 border rounded-3 bg-light">
        <div class="d-flex justify-content-between align-items-center mb-3">
          <h6 class="mb-0 fw-bold">{{ day === 'monday' ? '週一' : day === 'tuesday' ? '週二' : day === 'wednesday' ? '週三' : day === 'thursday' ? '週四' : day === 'friday' ? '週五' : '週六' ? '週六' : '週日' }}</h6>
          <div class="form-check form-switch">
            <input
              class="form-check-input"
              type="checkbox"
              :id="`toggle-${day}`"
              :checked="time !== '關閉'"
              @change="toggleDayStatus(day)"
            >
            <label class="form-check-label" :for="`toggle-${day}`"></label>
          </div>
        </div>

        <div v-if="time !== '關閉'" class="d-flex align-items-center gap-2 mb-3">
          <select class="form-select form-select-sm" v-model="parseTimeRange(time).start">
            <option v-for="option in timeOptions" :key="option" :value="option">{{ option }}</option>
          </select>
          <span class="text-muted">至</span>
          <select class="form-select form-select-sm" v-model="parseTimeRange(time).end">
            <option v-for="option in timeOptions" :key="option" :value="option">{{ option }}</option>
          </select>
          <button class="btn btn-outline-danger btn-sm" @click="removeTimeSegment(day, 0)">
            <i class="bi bi-trash"></i> <!-- 已更新為 Bootstrap Icons -->
          </button>
        </div>
        <button class="btn btn-sm btn-outline-primary w-100 py-2" @click="addTimeSegment(day)">
          <i class="bi bi-plus me-2"></i><!-- 已更新為 Bootstrap Icons -->
          新增區段
        </button>
      </div>
    </div>

    <!-- 底部操作按鈕 -->
    <div class="mt-auto pt-3 border-top d-flex justify-content-between gap-2">
      <button class="btn btn-outline-secondary  py-2 flex-grow-1" @click="resetChanges">重置</button>
      <button class="btn btn-primary  py-2 flex-grow-1" @click="saveChanges">下一步</button>
    </div>
  </div>
</template>

<style scoped>
/* 側邊欄內部容器的基本樣式 */
.h-100.d-flex.flex-column {
  background-color: white;
}

/* 時間選擇器調整 */
.form-select-sm {
  max-width: 120px; /* 限制下拉選單寬度 */
}
</style>
