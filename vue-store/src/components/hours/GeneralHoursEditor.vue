<script setup>
import { ref, onMounted, watch } from 'vue'; // 引入 watch

const props = defineProps({
  generalHours: {
    type: Array, // 改為 Array 類型
    required: true,
  },
});

const emit = defineEmits(['close', 'save']);

// 內部狀態，用於編輯營業時間
const editableHours = ref([]); // 初始化為空陣列

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
// 新增一個驗證時間的函數
const validateTimeRange = (openTime, closeTime) => {
  if (!openTime || !closeTime) {
    return true; // 如果其中一個為空，我們可能認為它是暫時有效，或在保存時處理
  }
  // 將時間字串轉換為可比較的格式 (例如：分鐘數)
  const parseTime = (timeStr) => {
    const [hours, minutes] = timeStr.split(':').map(Number);
    return hours * 60 + minutes;
  };

  const openMinutes = parseTime(openTime);
  const closeMinutes = parseTime(closeTime);

  // 如果結束時間小於或等於開始時間，則無效
  // 這裡需要考慮跨天的情況，但對於營業時間通常在同一天內
  // 如果需要跨天，邏輯會更複雜 (例如 22:00 - 02:00)
  // 目前假設在同一天內，結束時間必須大於開始時間
  return closeMinutes > openMinutes;
};


onMounted(() => {
  // 深拷貝 props，以便在子組件中修改而不影響父組件
  // 由於數據格式現在是陣列，複製方式也需要調整
  // 並且在複製時，將時間數據初始化為可選的選項，如果 'isOpen' 為 false
  editableHours.value = props.generalHours.map(hour => ({ ...hour })); // 淺拷貝物件陣列
  console.log('editableHours.value',editableHours.value)

  // 初始化選擇時間
  timeOptions.value = generateTimeOptions();
});

// 使用 watch 監聽 props.generalHours 變化，重新初始化 editableHours
// 這在父組件通過非 prop 更新數據時很有用
watch(() => props.generalHours, (newHours) => {
  editableHours.value = newHours.map(hour => ({ ...hour, isTimeInvalid: !hour.isOpen || validateTimeRange(hour.openTimeStr, hour.closeTimeStr) ? false : true }));

}, { deep: true }); // deep: true 可以監聽陣列內部物件的變化

// 處理開關切換
const toggleDayStatus = (item) => {

  if (!item.isOpen) {

    item.isTimeInvalid = false; // 關閉時無所謂時間無效
  } else {
     // 再次檢查，如果預設時間或之前的值導致結束時間不晚于开始时间，则调整结束时间
     console.log('跑到這邊')
    if(item.openTimeStr==null){
          item.openTimeStr='08:00';
        }
        if(item.closeTimeStr==null){
          item.closeTimeStr='17:00';
        } 
    if (!validateTimeRange(item.openTimeStr, item.closeTimeStr)) {
        // 如果發現無效，將結束時間設定為開始時間之後的某個值，例如加 15 分鐘
        
        const [openHour, openMinute] = item.openTimeStr.split(':').map(Number);
        const newCloseMinutes = openHour * 60 + openMinute + 15;
        const newCloseHour = String(Math.floor(newCloseMinutes / 60) % 24).padStart(2, '0');
        const newCloseMinute = String(newCloseMinutes % 60).padStart(2, '0');
        item.closeTimeStr = `${newCloseHour}:${newCloseMinute}`;
       item.isTimeInvalid = true; // 即使自動調整了，也可能需要提示（如果用戶期望更早結束）
    } else {
        item.isTimeInvalid = false; // 時間範圍有效
    }
  }
};
// 處理時間選擇變化
const handleTimeChange = (item) => {
  // 當時間變化時，重新驗證並可能調整
  if (!validateTimeRange(item.openTimeStr, item.closeTimeStr)) {
    // 如果結束時間小於或等於開始時間，自動將結束時間調整為開始時間的 15 分鐘後
    const [openHour, openMinute] = item.openTimeStr.split(':').map(Number);
    let newCloseMinutes = openHour * 60 + openMinute + 15;

    // 確保結束時間不會超過 23:45 (如果沒有跨日邏輯)
    if (newCloseMinutes >= 24 * 60) {
        newCloseMinutes = 23 * 60 + 45; // 或者設置為當天最後一個可選時間
    }

    const newCloseHour = String(Math.floor(newCloseMinutes / 60) % 24).padStart(2, '0');
    const newCloseMinute = String(newCloseMinutes % 60).padStart(2, '0');
    item.closeTimeStr = `${newCloseHour}:${newCloseMinute}`;
      item.isTimeInvalid = true; // 時間範圍無效
  } else {
    item.isTimeInvalid = false; // 時間範圍有效
  }
};


// 解析時間字串和格式化時間字串的函數在新數據格式下可能不再需要
// 因為我們直接操作 openTimeStr 和 closeTimeStr。
// 如果你想用於顯示，可以保留 formatTimeRange

// 保存更改
const saveChanges = () => {
 
  // 遍歷所有營業時間，檢查是否有無效的時間範圍
  const invalidItems = editableHours.value.filter(item =>
    item.isOpen && !validateTimeRange(item.openTimeStr, item.closeTimeStr)
  );

  if (invalidItems.length > 0) {
    alert('部分營業時間設置無效，結束時間必須晚於開始時間。請檢查！');
    // 可以滾動到第一個無效項，或者更詳細的錯誤提示
    return; // 阻止保存操作
  }
  
  emit('save', editableHours.value);
};

// 重置為初始狀態
const resetChanges = () => {
  editableHours.value = props.generalHours.map(hour => ({ ...hour })); // 從 props 重新初始化
  
  // const dayOrder = {
  //   'MONDAY': 1, 'TUESDAY': 2, 'WEDNESDAY': 3, 'THURSDAY': 4,
  //   'FRIDAY': 5, 'SATURDAY': 6, 'SUNDAY': 7
  // };
  // editableHours.value.sort((a, b) => dayOrder[a.dayOfWeek] - dayOrder[b.dayOfWeek]);
};
</script>

<template>
  <div class="h-100 d-flex flex-column p-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h4 class="mb-0 fw-bold">調整一般營業時間</h4>
      <button class="btn-close" @click="emit('close')"></button>
    </div>
    <hr />

    <div class="flex-grow-1 overflow-auto pe-2 py-3">
      <div class="mb-3 form-check form-switch">
        </div>

      <div v-for="item in editableHours" :key="item.id" class="mb-4 p-3 border rounded-3 bg-light">
        <div class="d-flex justify-content-between align-items-center mb-3">
          <h6 class="mb-0 fw-bold">{{ item.dayName }}</h6>
          <div class="form-check form-switch">
            <input
              class="form-check-input"
              type="checkbox"
              :id="`toggle-${item.dayOfWeek}`"
              v-model="item.isOpen"
              @change="toggleDayStatus(item)"
            />
            <label class="form-check-label" :for="`toggle-${item.dayOfWeek}`"></label>
          </div>
        </div>

        <div v-if="item.isOpen" class="d-flex align-items-center gap-2 mb-3">
          <select class="form-select form-select-sm" v-model="item.openTimeStr" @change="handleTimeChange(item)">
            <option v-for="option in timeOptions" :key="option" :value="option">{{ option }}</option>
          </select>
          <span class="text-muted">至</span>
          <select class="form-select form-select-sm" v-model="item.closeTimeStr" @change="handleTimeChange(item)">
            <option v-for="option in timeOptions" :key="option" :value="option">{{ option }}</option>
          </select>
          </div>
          <div v-else class="text-muted text-center py-2">
              關閉
          </div>

          <div v-if="item.isTimeInvalid" class="alert alert-danger py-1 px-2 mb-0" role="alert" style="font-size: 0.85em;">
            開始時間不能晚於或等於結束時間。已自動調整。
          </div>


        </div>
    </div>

    <div class="mt-auto pt-3 border-top d-flex justify-content-between gap-2">
      <button class="btn btn-outline-secondary py-2 flex-grow-1" @click="resetChanges">重置</button>
      <button class="btn btn-primary py-2 flex-grow-1" @click="saveChanges">下一步</button>
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