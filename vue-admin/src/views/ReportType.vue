<template>
    <h2>檢舉項目管理</h2>
    <div class="p-4">
      <hr>
  
      <div class="table-card mb-4 p-4">
        <div class="row g-3 align-items-center">
          <div class="col-md-12">
            <label for="newReportType" class="form-label mb-0">新增檢舉項目：</label>
            <div class="input-group">
              <input id="newReportType" v-model="newReport.type" placeholder="輸入檢舉項目名稱" class="form-control" type="text" />
              <input id="newReportDescription" v-model="newReport.description" placeholder="輸入檢舉項目說明" class="form-control ms-2" type="text" style="width: 500px;" />
              <button class="btn btn-custom-add" @click="saveReport" :disabled="!newReport.type">新增</button>
            </div>
            <div v-if="errorMessage" class="alert alert-danger mt-2" role="alert">
              {{ errorMessage }}
            </div>
          </div>
        </div>
      </div>
  
      <div class="table-card p-4">
        <div class="d-flex justify-content-between align-items-center mb-3">
          <h3>檢舉項目列表</h3>
          <button class="btn btn-success" @click="saveSortOrder">確認排序</button>
        </div>
        <div class="row row-cols-1 g-3">
          <div class="col" v-for="report in displayedReports" :key="report.id"
               draggable="true"
               @dragstart="onDragStart(report)"
               @dragover.prevent
               @drop="onDrop(report)"
               @dragenter.prevent>
            <div class="tag-item border rounded p-2 d-flex align-items-center justify-content-between">
              <div v-if="editingReportId !== report.id" class="me-2">
                <span>優先級: {{ report.prime }} - {{ report.type }}</span>
                <br />
                <small class="text-muted">{{ report.description }}</small>
              </div>
              <div v-else class="flex-grow-1 me-2">
                <div class="mb-2">
                  <input v-model="editedReportType" @keyup.enter="saveEdit(report)" class="form-control" type="text" placeholder="輸入檢舉項目名稱" style="width: 200px;" />
                </div>
                <div>
                  <input v-model="editedReportDescription" @keyup.enter="saveEdit(report)" class="form-control" type="text" placeholder="輸入檢舉項目說明" style="width: 500px;" />
                </div>
              </div>
              <div class="tag-actions d-flex gap-2">
                <button v-if="editingReportId !== report.id" class="btn btn-warning btn-sm" @click="startEdit(report)">編輯</button>
                <button v-else class="btn btn-success btn-sm" @click="saveEdit(report)" @mousedown.prevent>儲存</button>
                <button v-if="editingReportId === report.id" class="btn btn-secondary btn-sm" @click="cancelEdit">取消</button>
                <button class="btn btn-danger btn-sm" @click="deleteReport(report.id)">刪除</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, computed, onMounted } from 'vue';
  import axios from 'axios';
  import Swal from 'sweetalert2';
  import InputText from 'primevue/inputtext';
  import Button from 'primevue/button';
  import Message from 'primevue/message';
  
  const API_URL = import.meta.env.VITE_REPOTYPE_URL;
  
  const reports = ref([]);
  const newReport = ref({ type: '', description: '' });
  const errorMessage = ref('');
  const draggedTag = ref(null);
  const tempOrder = ref([]);
  const editingReportId = ref(null);
  const editedReportType = ref('');
  const editedReportDescription = ref('');
  
  const displayedReports = computed(() => {
    if (tempOrder.value.length > 0) {
      return tempOrder.value;
    }
    return [...reports.value].sort((a, b) => (a.prime || 0) - (b.prime || 0));
  });
  
  const fetchReports = async () => {
    try {
      const response = await axios.get(API_URL);
      reports.value = response.data;
      tempOrder.value = [];
    } catch (error) {
      errorMessage.value = '無法獲取檢舉項目列表：' + error.message;
      Swal.fire('錯誤', '無法獲取檢舉項目列表，請稍後再試。', 'error');
    }
  };
  
  const onDragStart = (report) => {
    draggedTag.value = report;
    if (tempOrder.value.length === 0) {
      tempOrder.value = [...reports.value].sort((a, b) => (a.prime || 0) - (b.prime || 0));
    }
  };
  
  const onDrop = (dropReport) => {
    if (!draggedTag.value) return;
    const fromIndex = tempOrder.value.findIndex(t => t.id === draggedTag.value.id);
    const toIndex = tempOrder.value.findIndex(t => t.id === dropReport.id);
    const newOrder = [...tempOrder.value];
    newOrder.splice(fromIndex, 1);
    newOrder.splice(toIndex, 0, draggedTag.value);
    tempOrder.value = newOrder;
    draggedTag.value = null;
  };
  
  const saveSortOrder = async () => {
    const result = await Swal.fire({
      title: '確認保存排序嗎？',
      text: '您將保存當前檢舉項目的顯示順序。',
      icon: 'question',
      showCancelButton: true,
      confirmButtonColor: '#3085d6',
      cancelButtonColor: '#d33',
      confirmButtonText: '是的，保存！',
      cancelButtonText: '取消'
    });
  
    if (result.isConfirmed) {
      try {
        const updatedReports = tempOrder.value.map((report, index) => ({
          ...report,
          prime: index + 1
        }));
        await axios.post(`${API_URL}/batch-update`, updatedReports);
        reports.value = updatedReports;
        tempOrder.value = [];
        errorMessage.value = '排序已保存';
        Swal.fire('成功', '排序已保存！', 'success');
      } catch (error) {
        errorMessage.value = `保存排序失敗：${error.response?.data?.message || error.message}`;
        Swal.fire('錯誤', '保存排序失敗，請稍後再試。', 'error');
      }
    }
  };
  
  const saveReport = async () => {
    if (!newReport.value.type.trim()) {
      Swal.fire('警告', '檢舉項目名稱不能為空。', 'warning');
      return;
    }
    try {
      const maxPrime = Math.max(...reports.value.map(t => t.prime || 0), 0);
      await axios.post(API_URL, { ...newReport.value, prime: maxPrime + 1 });
      Swal.fire('成功', '檢舉項目新增成功！', 'success');
      newReport.value = { type: '', description: '' };
      await fetchReports();
      errorMessage.value = '';
    } catch (error) {
      errorMessage.value = `操作失敗：${error.response?.data?.message || error.message}`;
      Swal.fire('錯誤', '操作失敗，請稍後再試。', 'error');
    }
  };
  
  const startEdit = (report) => {
    editingReportId.value = report.id;
    editedReportType.value = report.type;
    editedReportDescription.value = report.description;
  };
  
  const cancelEdit = () => {
    editingReportId.value = null;
    editedReportType.value = '';
    editedReportDescription.value = '';
  };
  
  const saveEdit = async (report) => {
    if (!editedReportType.value.trim()) {
      Swal.fire('警告', '檢舉項目名稱不能為空。', 'warning');
      return;
    }
    try {
      await axios.put(`${API_URL}/${report.id}`, {
        id: report.id,
        type: editedReportType.value.trim(),
        description: editedReportDescription.value.trim(),
        prime: report.prime
      });
      const index = reports.value.findIndex(t => t.id === report.id);
      if (index !== -1) {
        reports.value[index].type = editedReportType.value.trim();
        reports.value[index].description = editedReportDescription.value.trim();
        reports.value[index].prime = report.prime;
      }
      editingReportId.value = null;
      editedReportType.value = '';
      editedReportDescription.value = '';
      Swal.fire('成功', '檢舉項目修改成功！', 'success');
      await fetchReports();
    } catch (error) {
      errorMessage.value = `操作失敗：${error.response?.data?.message || error.message}`;
      Swal.fire('錯誤', '操作失敗，請稍後再試。', 'error');
    }
  };
  
  const deleteReport = async (id) => {
    const result = await Swal.fire({
      title: '確定要刪除此檢舉項目嗎？',
      text: '刪除後將無法恢復！',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#d33',
      cancelButtonColor: '#3085d6',
      confirmButtonText: '是的，刪除！',
      cancelButtonText: '取消'
    });
  
    if (result.isConfirmed) {
      try {
        await axios.delete(`${API_URL}/${id}`);
        await fetchReports();
        errorMessage.value = '';
        Swal.fire('已刪除！', '檢舉項目已成功刪除。', 'success');
      } catch (error) {
        errorMessage.value = `刪除失敗：${error.response?.data?.message || error.message}`;
        Swal.fire('錯誤', '刪除失敗，請稍後再試。', 'error');
      }
    }
  };
  
  onMounted(fetchReports);
  </script>
  
  <style scoped>
  .table-card {
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  }
  
  .input-group .form-control {
    flex-grow: 1;
  }
  
  .tag-item {
    background-color: #f8f9fa;
    transition: all 0.2s ease-in-out;
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
  
  .tag-item:hover {
    background-color: #e2e6ea;
    transform: translateY(-2px);
  }
  
  .tag-item .form-control {
    flex-grow: 1;
    flex-shrink: 1;
    min-width: 0;
    margin-right: 0.5rem;
  }
  
  .tag-actions {
    flex-shrink: 0;
    display: flex;
    gap: 0.5rem;
  }
  
  .btn-custom-add {
    background-color: #0055b3;
    border-color: #0055b3;
    color: #fff;
  }
  
  .btn-custom-add:hover {
    background-color: #003d82;
    border-color: #003d82;
  }
  
  .btn-custom-add:disabled {
    background-color: #6699cc;
    border-color: #6699cc;
    color: #fff;
  }
  </style>