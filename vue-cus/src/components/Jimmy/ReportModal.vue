<template>
    <div class="report-modal-overlay" @click.self="$emit('close')">
      <div class="report-modal-content">
        <div class="report-modal-header">
          <h3>檢舉評論</h3>
          <button class="close-button" @click="$emit('close')">&times;</button>
        </div>
        <div class="report-modal-body">
          <Message v-if="message" :severity="message.type" :life="3000" @close="message = null">{{ message.text }}</Message>
  
          <div class="p-field">
            <label for="reportType">檢舉類型:</label>
            <Dropdown
              id="reportType"
              v-model="selectedReportTypeId"
              :options="reportTypes"
              optionLabel="type"
              optionValue="id"
              placeholder="請選擇檢舉類型"
              class="w-full md:w-14rem"
              :class="{'p-invalid': !selectedReportTypeId && submitted}"
            />
            <small v-if="!selectedReportTypeId && submitted" class="p-error">請選擇檢舉類型。</small>
          </div>
  
          <div class="p-field mt-3" v-if="selectedReportDescription">
            <label>詳細描述:</label>
            <p class="report-description-display">{{ selectedReportDescription }}</p>
          </div>
        </div>
        <div class="report-modal-footer">
          <button type="button" class="btn btn-secondary" @click="$emit('close')">取消</button>
          <button type="button" class="btn btn-primary" @click="submitReport">提交檢舉</button>
        </div>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted, computed } from 'vue';
  import axios from 'axios';
  import { useUserStore } from '@/stores/user'; // 假設你有用戶 store
  import Dropdown from 'primevue/dropdown';
  
  // 定義組件 props
  const props = defineProps({
    commentId: {
      type: Number,
      required: true,
    },
  });
  
  // 定義組件 emit 事件
  const emit = defineEmits(['close', 'reportSubmitted']);
  
  const userStore = useUserStore(); // 使用 Pinia 的用戶 store
  
  const API_URL = import.meta.env.VITE_API_URL;
  
  const reportTypes = ref([]);
  const selectedReportTypeId = ref(null);
  const message = ref(null); // 用於顯示成功或錯誤訊息
  const submitted = ref(false); // 用於表單驗證提示
  
  // 從後端獲取檢舉類型列表
  const fetchReportTypes = async () => {
    try {
      const response = await axios.get(`${API_URL}/api/report-type`);
      reportTypes.value = response.data;
    } catch (error) {
      console.error('Error fetching report types:', error);
      message.value = { type: 'error', text: '無法載入檢舉類型。' };
    }
  };
  
  const submitReport = async () => {
    submitted.value = true; // 觸發表單驗證顯示
  
    if (!selectedReportTypeId.value) {
      message.value = { type: 'error', text: '請選擇檢舉類型。' };
      return;
    }
  
    // 根據你的 `submitter_type` 和 `submitter_id` 邏輯
    // 這裡假設 submitter_type 為 'user'，submitter_id 從 Pinia Store 中獲取
    // 如果用戶未登入，你可能需要處理為匿名檢舉或阻止檢舉
    const submitterType = "user"; // 假設為 'user'
    const submitterId = userStore.userId; // 從 Pinia store 獲取用戶 ID
  
    if (!submitterId) {
      message.value = { type: 'warn', text: '請先登入才能提交檢舉。' };
      return;
    }
  
    const reportData = {
      reportTypeId: selectedReportTypeId.value,
      commentId: props.commentId,
      submitterId: submitterId,
      submitterType: submitterType,
      // status 和 reportDate 將由後端生成
      // description 字段並沒有在 ReportBean 中定義，如果需要，需要在 ReportBean 和 DTO 中添加
      // 目前我沒有在 ReportBean 中看到 description，如果需要，請在 ReportBean 和 ReportRequestDTO 中添加此字段
      // 這裡暫時不包含 description，如果你的 ReportBean 中有此字段，可以取消註解下一行
      // content: description.value // 如果 ReportBean 有 content 字段用於描述
    };
  
    try {
      const response = await axios.post(`${API_URL}/api/reports`, reportData);
      if (response.status === 201) { // HTTP 201 Created
        message.value = { type: 'success', text: '檢舉已成功提交！' };
        emit('reportSubmitted'); // 通知父組件檢舉已提交
        setTimeout(() => {
          emit('close'); // 成功後自動關閉模態框
        }, 200);
      } else {
        message.value = { type: 'error', text: '提交檢舉失敗。' };
      }
    } catch (error) {
      console.error('Error submitting report:', error);
      message.value = { type: 'error', text: '提交檢舉時發生錯誤，請稍後再試。' };
      if (error.response && error.response.data) {
        console.error('Backend error details:', error.response.data);
      }
    }
  };

  // 計算屬性：根據 selectedReportTypeId 獲取對應的描述
const selectedReportDescription = computed(() => {
  const selectedType = reportTypes.value.find(
    (type) => type.id === selectedReportTypeId.value
  );
  // 假設 reportType 物件中有一個 'description' 字段
  return selectedType ? selectedType.description : ''; 
});
  
  onMounted(() => {
    fetchReportTypes();
  });
  </script>
  
  <style scoped>
  /* 模態框疊層 */
  .report-modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.6);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1001; /* 比評論模態框高 */
  }
  
  /* 模態框內容 */
  .report-modal-content {
    background-color: white;
    border-radius: 10px;
    width: 90%;
    max-width: 450px;
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.3);
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }
  
  .report-modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px 20px;
    border-bottom: 1px solid #eee;
    background-color: #f8f9fa;
  }
  
  .report-modal-header h3 {
    margin: 0;
    font-size: 1.3em;
    color: #333;
  }
  
  .close-button {
    background: none;
    border: none;
    font-size: 2em;
    cursor: pointer;
    color: #666;
    line-height: 1;
  }
  
  .close-button:hover {
    color: #000;
  }
  
  .report-modal-body {
    padding: 20px;
    flex-grow: 1;
  }
  
  .p-field {
    margin-bottom: 1rem;
  }
  
  .p-field label {
    display: block;
    margin-bottom: 0.5rem;
    font-weight: bold;
    color: #555;
  }
  
  /* PrimeVue InputText */
  .p-inputtext {
    width: 100%;
    padding: 0.75rem;
    border: 1px solid #ced4da;
    border-radius: 6px;
    font-size: 1rem;
    box-sizing: border-box; /* 確保 padding 不增加寬度 */
  }
  .p-inputtext:focus {
    border-color: #007bff;
    box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, 0.25);
    outline: none;
  }
  
  /* PrimeVue Dropdown */
  .p-dropdown {
    width: 100%;
    /* 為了讓 Dropdown 和 InputText 高度一致，可能需要調整 */
    /* height: 2.75rem; */
  }
  /* 覆寫 Dropdown 的驗證樣式 */
  .p-dropdown.p-invalid {
    border-color: #dc3545; /* Bootstrap 的 error 顏色 */
  }
  .p-error {
    color: #dc3545;
    font-size: 0.875em;
    margin-top: 0.25rem;
    display: block;
  }
  
  .report-modal-footer {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    padding: 15px 20px;
    border-top: 1px solid #eee;
    background-color: #f8f9fa;
  } 
  </style>