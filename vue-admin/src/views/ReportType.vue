<template>
    <div class="container mt-4">
        <h2>檢舉項目管理</h2>
        <Message v-if="errorMessage" severity="error" :closable="false">{{ errorMessage }}</Message>
    
        <!-- 新增標籤表單 -->
        <div class="card p-4 mb-4">
        <h3>{{ editMode ? '編輯檢舉項目' : '新增檢舉項目' }}</h3>
        <div class="p-fluid">
            <div class="field">
            <label for="type">檢舉項目</label>
            <InputText id="type" v-model="newReport.type" placeholder="輸入檢舉項目名稱" />
            <label for="description">項目說明</label>
            <InputText id="description" v-model="newReport.description" placeholder="輸入檢舉項目說明" style="width: 550px"/>
            </div>
            <Button :label="editMode ? '更新' : '新增'" icon="pi pi-check" class="p-button-success mt-2" @click="saveReport" :disabled="!newReport.type" />
            <Button v-if="editMode" label="取消" icon="pi pi-times" class="p-button-secondary mt-2 ml-2" @click="cancelEdit" />
        </div>
        </div>
    
        <!-- 標籤列表 -->
        <div class="card">
        <h3>檢舉項目列表</h3>
        <table class="table table-striped">
            <thead>
            <tr>           
                <th><button @click="saveSortOrder" class="p-button-success mt-2">確認排序</button></th>
                <th>檢舉項目</th>
                <th>項目說明</th>
                <th>操作</th>
            </tr>
            </thead>
            <tbody>
            <tr
                v-for="report in displayedReports"
                :key="report.id"
                draggable="true"
                @dragstart="onDragStart(report)"
                @dragover.prevent
                @drop="onDrop(report)"
                @dragenter.prevent
            >            
                <td>{{ report.prime }}</td>
                <td>{{ report.type }}</td>
                <td>{{ report.description }}</td>
                <td>
                <Button icon="pi pi-pencil" class="p-button-rounded p-button-warning mr-2" @click="editReport(report)" />
                <Button icon="pi pi-trash" class="p-button-rounded p-button-danger" @click="deleteReport(report.id)" />
                </td>
            </tr>
            </tbody>
        </table>
        </div>
    </div>
    </template>
    
    <script setup>
    import { ref, computed, onMounted } from 'vue';
    import axios from 'axios';
    import InputText from 'primevue/inputtext';
    import Button from 'primevue/button';
    import Message from 'primevue/message';
    
    // API 基礎 URL
    const API_URL = import.meta.env.VITE_REPOTYPE_URL;
    
    // 狀態管理
    const reports = ref([]);
    const newReport = ref({ type: '', description: '' });
    const editMode = ref(false);
    const errorMessage = ref('');
    const draggedTag = ref(null);
    const tempOrder = ref([]);
    
    // 計算顯示的標籤（根據 tempOrder 或原始 tags 排序）
    const displayedReports = computed(() => {
        if (tempOrder.value.length > 0) {
            return tempOrder.value;
        }
        return reports.value.sort((a, b) => a.prime - b.prime);
    });
    
    // 獲取所有標籤
    const fetchReports = async () => {
        try {
            const response = await axios.get(API_URL);
            reports.value = response.data;
            tempOrder.value = []; // 重置臨時排序
        } catch (error) {
            errorMessage.value = '無法獲取標籤列表：' + error.message;
        }
    };
    
    // 拖曳開始
    const onDragStart = (tag) => {
        draggedTag.value = tag;
        if (tempOrder.value.length === 0) {
            tempOrder.value = [...reports.value].sort((a, b) => a.prime - b.prime);
        }
    };
    
    // 拖曳放置
    const onDrop = (dropTag) => {
        if (!draggedTag.value) return;
        const fromIndex = tempOrder.value.findIndex(t => t.id === draggedTag.value.id);
        const toIndex = tempOrder.value.findIndex(t => t.id === dropTag.id);
        const newOrder = [...tempOrder.value];
        newOrder.splice(fromIndex, 1);
        newOrder.splice(toIndex, 0, draggedTag.value);
        tempOrder.value = newOrder;
        draggedTag.value = null;
    };
    
    // 保存排序
    const saveSortOrder = async () => {
        try {
            const updatedTags = tempOrder.value.map((tag, index) => ({
                ...tag,
                prime: index + 1
            }));
            await axios.post(`${API_URL}/batch-update`, updatedTags);
            reports.value = updatedTags;
            tempOrder.value = [];
            errorMessage.value = '排序已保存';
        } catch (error) {
            errorMessage.value = `保存排序失敗：${error.response?.data?.message || error.message}`;
        }
    };
    
    // 新增或更新標籤
    const saveReport = async () => {
        try {
            if (editMode.value) {
                await axios.put(`${API_URL}/${newReport.value.id}`, newReport.value);
            } else {
                const maxPrime = Math.max(...reports.value.map(t => t.prime || 0), 0);
                await axios.post(API_URL, { ...newReport.value, prime: maxPrime + 1 });
            }
            newReport.value = { type: '', description: '' };
            editMode.value = false;
            await fetchReports();
            errorMessage.value = '';
        } catch (error) {
            errorMessage.value = `操作失敗：${error.response?.data?.message || error.message}`;
        }
    };
    
    // 編輯標籤
    const editReport = (tag) => {
        newReport.value = { ...tag };
        editMode.value = true;
    };
    
    // 取消編輯
    const cancelEdit = () => {
        newReport.value = { type: '', description: '' };
        editMode.value = false;
        errorMessage.value = '';
    };
    
    // 刪除標籤
    const deleteReport = async (id) => {
        if (confirm('確定要刪除此標籤嗎？')) {
            try {
                await axios.delete(`${API_URL}/${id}`);
                await fetchReports();
                errorMessage.value = '';
            } catch (error) {
                errorMessage.value = `刪除失敗：${error.response?.data?.message || error.message}`;
            }
        }
    };
    
    // 頁面加載時獲取標籤
    onMounted(fetchReports);
    </script>
    
    
    <style scoped>
    .table {
    width: 100%;
    }
    .field {
    margin-bottom: 1rem;
    }
    .ml-2 {
    margin-left: 0.5rem;
    }
    </style>