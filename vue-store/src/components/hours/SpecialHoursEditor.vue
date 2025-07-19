<script setup>
import { ref ,watch  } from 'vue';
import Swal from 'sweetalert2';

const emit = defineEmits(['close', 'save']);

// 確保月份和日期始終是兩位數的格式化函數
const getFormattedDate = (date) => {
  const year = date.getFullYear();
  // 月份從 0 開始，所以要加 1
  const month = String(date.getMonth() + 1).padStart(2, '0'); // 補零，例如 7 -> 07
  const day = String(date.getDate()).padStart(2, '0');     // 補零，例如 6 -> 06
  return `${year}-${month}-${day}`;
};

const today = new Date(); // 取得當前日期
const todayFormatted = getFormattedDate(today);
const storeId = ref(1); // 假設的商店 ID，實際應該從後端獲取

//預設表單資訊
const specialHoursList = ref({
  recordType: 'specific',
  specificDate:  getFormattedDate(today),
  startDate:  getFormattedDate(today),
  endDate: '',
  isClose: false,
  openTime: '08:00',
  closeTime: '17:00',
  isTimeInvalid: false
});

const minEndDate = ref('');

//功能避免選擇過去的時間以及多日選擇的錯誤
// 監聽 startDate 的變化，更新 endDate 的 min 屬性
watch(() => specialHoursList.value.startDate, (newStartDate) => {
  // 當 startDate 改變時，endDate 的最小值就是新的 startDate
  minEndDate.value = newStartDate;

  // 檢查如果 endDate 已經被設定，但它早於新的 startDate，則清除 endDate
  if (specialHoursList.value.endDate && new Date(specialHoursList.value.endDate) < new Date(newStartDate)) {
    specialHoursList.value.endDate = '';
  }
}, { immediate: true }); // immediate: true 確保在組件初始化時也執行一次



/**
 * 將前端特殊營業時間物件轉換為後端 OpenHourDTO 陣列。
 *
 * @param {Object} specialHoursData 前端 specialHoursList 的 ref.value 物件
 * @param {number} storeId 商店 ID
 * @returns {Array<Object>} 轉換後的 OpenHourDTO 陣列
 */

function transformSpecialHours(specialHoursData) {
  const transformedList = [];

  // 輔助函數：將日期字串解析為 Date 物件
  const parseDateString = (dateString) => {
    // 簡單的解析，假設格式總是 yyyy-MM-dd
    const [year, month, day] = dateString.split('-').map(Number);
    // 月份在 Date 物件中是 0-11
    return new Date(year, month - 1, day);
  };

  // 根據 recordType 進行處理
  if (specialHoursData.recordType === 'specific') {
    // 單日模式
    const dto = {
      id: null, // 如果是新增，ID 設為 null，由後端生成
      storeId: storeId.value,
      date: specialHoursData.specificDate, // 將 specificDate 映射到 date
      openTime: specialHoursData.openTime,
      closeTime: specialHoursData.closeTime,
      isClose: specialHoursData.isClose,
    };
    transformedList.push(dto);

  } else if (specialHoursData.recordType === 'range') {
    // 區間模式
    const startDate = parseDateString(specialHoursData.startDate);
    const endDate = parseDateString(specialHoursData.endDate);

    // 遍歷 startDate 和 endDate 之間的每一天
    let currentDate = startDate;
    while (currentDate <= endDate) {
      const dto = {
        id: null, // 如果是新增，ID 設為 null
        storeId: storeId.value,
        date: getFormattedDate(currentDate), // 將當前日期格式化
        openTime: specialHoursData.openTime,
        closeTime: specialHoursData.closeTime,
        isClose: specialHoursData.isClose,
      };
      transformedList.push(dto);

      // 移到下一天
      currentDate.setDate(currentDate.getDate() + 1);
    }
  }

  return transformedList;
}

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
timeOptions.value = generateTimeOptions();
//
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
//時間選項:結束時間不可低於開始時間
// 處理時間選擇變化
const handleTimeChange = (specialHoursList) => {
  // 當時間變化時，重新驗證並可能調整
  console.log(specialHoursList.openTime)
  if (!validateTimeRange(specialHoursList.openTime, specialHoursList.closeTime)) {
    // 如果結束時間小於或等於開始時間，自動將結束時間調整為開始時間的 15 分鐘後
    const [openHour, openMinute] = specialHoursList.openTime.split(':').map(Number);
    let newCloseMinutes = openHour * 60 + openMinute + 15;

    // 確保結束時間不會超過 23:45 (如果沒有跨日邏輯)
    if (newCloseMinutes >= 24 * 60) {
        newCloseMinutes = 23 * 60 + 45; // 或者設置為當天最後一個可選時間
    }

    const newCloseHour = String(Math.floor(newCloseMinutes / 60) % 24).padStart(2, '0');
    const newCloseMinute = String(newCloseMinutes % 60).padStart(2, '0');
    specialHoursList.closeTime = `${newCloseHour}:${newCloseMinute}`;
 
    specialHoursList.isTimeInvalid = true;// 時間範圍無效
  } else {
    specialHoursList.isTimeInvalid = false;// 時間範圍有效
 
  }
};




// 保存更改
const saveChanges = () => {
 
  let newRecord = {};
  if (specialHoursList.value.recordType === 'specific') {
    console.log(specialHoursList.value.recordType)
    if (!specialHoursList.value.specificDate) {
      Swal.fire({
      icon: 'warning', // 提示類型：'success', 'error', 'warning', 'info', 'question'
      title: '請選擇特定日期！', // 標題
      text: '單日模式下需要指定日期才能保存。', // 提示內容
      confirmButtonText: '確定', // 確認按鈕文字
      customClass: {
        container: 'my-swal-container' // 可選：自定義 CSS 類別
      },
      // showConfirmButton: false, // 如果不需要確認按鈕可以設為 false
      // timer: 1500 // 可選：自動關閉時間（毫秒）
    }); 
      return;
    }
 
  } else {
    if (!specialHoursList.value.startDate || !specialHoursList.value.endDate) {
       Swal.fire({
        icon: 'warning', // 提示類型：'success', 'error', 'warning', 'info', 'question'
        title: '請選擇日期範圍！', // 標題
        text: '區間模式下需要指定開始日期和結束日期才能保存。', // 提示內容
        confirmButtonText: '確定', // 確認按鈕文字
      });
      return;
    }

  }
  // transformSpecialHours(specialHoursList.value, 1); // 假設 storeId 為 1
  console.log('轉換後',transformSpecialHours(specialHoursList.value));
  emit('save', transformSpecialHours(specialHoursList.value)); // 觸發保存事件，並將新記錄傳遞給父組件
};
</script>

<template>
  <div class="h-100 d-flex flex-column p-4">
    <!-- 頂部標題和關閉按鈕 -->
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h4 class="mb-0 fw-bold">新增特殊營業時間</h4>
      <button class="btn-close" @click="emit('close')"></button>
    </div>
    <hr>

    <!-- 可滾動內容區域 -->
    <div class="flex-grow-1 overflow-auto pe-2 py-3">
      <!-- 日期類型選擇 -->
      <div class="btn-group w-100 mb-4" role="group">
        <input type="radio" class="btn-check" name="recordType" id="specificDay" value="specific" v-model="specialHoursList.recordType" autocomplete="off">
        <label class="btn btn-outline-primary" for="specificDay">單日</label>

        <input type="radio" class="btn-check" name="recordType" id="multipleDays" value="range" v-model="specialHoursList.recordType" autocomplete="off">
        <label class="btn btn-outline-primary" for="multipleDays">多日</label>
      </div>

      <!-- 日期輸入 -->
      <div class="mb-4">
        <label for="dateInput" class="form-label text-muted">日期</label>
        <!-- 單日 -->
        <input
        v-if="specialHoursList.recordType === 'specific'"
        type="date"
        class="form-control"
        id="dateInput"
        v-model="specialHoursList.specificDate"
        :min="todayFormatted"
        >
        <!-- 多日 -->
        <div v-else class="d-flex align-items-center gap-2">
          <input type="date" class="form-control" v-model="specialHoursList.startDate" :min="todayFormatted">
          <span class="text-muted">至</span>
          <input type="date" class="form-control" v-model="specialHoursList.endDate"  :min="minEndDate" >
        </div>
      </div>

      <!-- 全日關閉開關 -->
      <div class="form-check form-switch mb-4">
        <input class="form-check-input" type="checkbox" id="closedAllDay" v-model="specialHoursList.isClose">
        <label class="form-check-label" for="closedAllDay">全日關閉</label>
      </div>

      <!-- 營業時間段 -->
      <div v-if="!specialHoursList.isClose" class="mb-4 p-3 border rounded-3 bg-light">
        <h6 class="mb-3 fw-bold">營業時段</h6>
        <div class="d-flex align-items-center gap-2 mb-3">
          <select class="form-select form-select-sm" v-model="specialHoursList.openTime" @change="handleTimeChange(specialHoursList)" >
            <option v-for="option in timeOptions" :key="option" :value="option">{{ option }}</option>
          </select>
          <span class="text-muted">至</span>
          <select class="form-select form-select-sm" v-model="specialHoursList.closeTime" @change="handleTimeChange(specialHoursList)">
            <option v-for="option in timeOptions" :key="option" :value="option">{{ option }}</option>
          </select>
        </div>
        <div v-if="specialHoursList.isTimeInvalid" class="alert alert-danger py-1 px-2 mb-0" role="alert" style="font-size: 0.85em;">
            開始時間不能晚於或等於結束時間。已自動調整。
        </div>
        <!-- <button class="btn btn-sm btn-outline-primary w-100 py-2">
          <i class="fas fa-plus me-2"></i>新增時段
        </button> -->
      </div>

      <p class="text-muted small">
        <i class="fas fa-info-circle me-2"></i>
        請勿下方重複您設定的營業時段，此營業時段將會覆蓋您所選擇的日期中的日期。
      </p>
    </div>

    <!-- 底部操作按鈕 -->
    <div class="mt-auto pt-3 border-top d-flex justify-content-end">
      <button class="btn btn-primary  py-2 px-4" @click="saveChanges">儲存</button>
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
