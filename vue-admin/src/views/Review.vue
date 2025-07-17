<!-- 0716 JIMMY 新增 整頁-->
<template>
    <h2>評價檢舉審核</h2>
    <div class="container mt-4">

        <div class="bookmark-tabs mb-4">
            <button
                v-for="tab in tabs"
                :key="tab.key"
                class="bookmark-tab"
                :class="{ active: currentTab === tab.key }"
                @click="setTab(tab.key)"
            >
                <span v-html="currentTab === tab.key ? tab.iconActive : tab.icon" class="me-1"></span>
                {{ tab.text }}
                <span class="tab-count">{{ tab.count }}</span>
            </button>
            <div class="bookmark-underline"></div>
        </div>

        <div class="d-flex align-items-center gap-2 mb-3 flex-wrap">
            <div>搜尋：</div>
            <input v-model="keyword" placeholder="檢舉人ID..." class="form-control w-auto" style="min-width:160px;" />
            <div class="ms-2">評價ID：</div>
            <input v-model="identityKeyword" placeholder="評價ID..." class="form-control w-auto" style="min-width:120px;" />
            <button class="btn btn-primary ms-2" @click="resetFilters">清除篩選</button>
        </div>

        <div class="mb-2">
            <input type="checkbox" :checked="isAllSelected" @change="toggleAll" />
            全選
        </div>

        <table class="table table-striped table-hover review-table">
            <thead>
                <tr>
                    <th></th>
                    <th>檢舉ID</th>
                    <th>評價ID (評論ID)</th>
                    <th>檢舉人ID</th>
                    <th>提交身分</th>
                    <th>審核狀態</th>
                    <th>檢舉類型</th>
                    <th>評論內容</th>
                    <th>操作</th>
                </tr>
            </thead>
            <tbody>
                <tr
                    v-for="review in pagedReviews"
                    :key="review.id"
                    @click="showDetail(review)"
                    style="cursor:pointer"
                >
                    <td @click.stop>
                        <input type="checkbox" v-model="selected" :value="review.id" />
                    </td>
                    <td>{{ review.id }}</td>
                    <td>{{ review.commentId }}</td>
                    <td>{{ review.submitterId }}</td>
                    <td>{{ review.submitterType }}</td>
                    <td>
                        {{ getStatusText(review.status) }}
                    </td>
                    <td>{{ review.reportTypeName }}</td>
                    <td>{{ review.commentContent }}</td>
                    <td @click.stop>
                        <template v-if="review.status.toLowerCase() === 'pending'">
                            <span class="text-link me-3 text-success" @click="confirmReview(review)">確認</span>
                            <span class="text-link text-danger" @click="rejectReview(review)">駁回</span>
                        </template>
                        <template v-else-if="review.status.toLowerCase() === 'rejected'">
                            <span class="text-link text-success" @click="confirmReview(review)">確認</span>
                        </template>
                        <template v-else-if="review.status.toLowerCase() === 'approved'">
                            <span class="text-link text-danger" @click="rejectReview(review)">駁回</span>
                        </template>
                        <template v-else>
                            <span>無操作</span>
                        </template>
                    </td>
                </tr>
                <tr v-if="pagedReviews.length === 0">
                    <td colspan="9" class="text-center">查無資料</td>
                </tr>
            </tbody>
        </table>

        <div class="pagination d-flex justify-content-end align-items-center pagebar-wrap">
            <button class="btn btn-outline-secondary me-2" :disabled="page === 1" @click="page > 1 && (page--)">&lt; 上一頁</button>
            <nav>
                <ul class="pagination mb-0">
                    <li class="page-item disabled">
                        <span class="page-link">頁數：{{ page }} / {{ totalPages }}</span>
                    </li>
                </ul>
            </nav>
            <button class="btn btn-outline-secondary ms-2" :disabled="page === totalPages" @click="page < totalPages && (page++)">下一頁 &gt;</button>
            <div class="ms-3">
                <select class="form-select" v-model.number="pageSize" style="min-width:90px;">
                    <option v-for="s in pageSizes" :key="s" :value="s">{{ s }}/每頁</option>
                </select>
            </div>
        </div>

        <div v-if="modalReview" class="modal-mask" @click.self="closeModal">
            <div class="modal-dialog">
                <h5>評價詳細資訊</h5>
                <table class="table table-sm">
                    <tr><th>檢舉ID</th><td>{{ modalReview.id }}</td></tr>
                    <tr><th>評價ID (評論ID)</th><td>{{ modalReview.commentId }}</td></tr>
                    <tr><th>檢舉人ID</th><td>{{ modalReview.submitterId }}</td></tr>
                    <tr><th>提交身分</th><td>{{ modalReview.submitterType }}</td></tr>
                    <tr><th>審核狀態</th><td>{{ getStatusText(modalReview.status) }}</td></tr>
                    <tr><th>檢舉類型</th><td>{{ modalReview.reportTypeName }}</td></tr>
                    <tr><th>評論內容</th><td>{{ modalReview.commentContent }}</td></tr>
                    <tr><th>評論分數</th><td>{{ modalReview.commentScore }}</td></tr>
                    <tr><th>評論隱藏狀態</th><td>{{ modalReview.commentIsHidden ? '已隱藏' : '未隱藏' }}</td></tr>
                    <tr><th>檢舉時間</th><td>{{ formatDateTime(modalReview.reportDate) }}</td></tr>
                </table>
                <div class="text-end">
                    <template v-if="modalReview.status.toLowerCase() === 'pending'">
                        <button class="btn btn-success me-2" @click="confirmReview(modalReview)">確認為已審核</button>
                        <button class="btn btn-danger me-2" @click="rejectReview(modalReview)">駁回為已駁回</button>
                        <button class="btn btn-warning me-2" @click="resetToPending(modalReview)">重置為待審核</button>
                    </template>
                    <template v-else-if="modalReview.status.toLowerCase() === 'rejected'">
                        <button class="btn btn-success me-2" @click="confirmReview(modalReview)">確認為已審核</button>
                    </template>
                    <template v-else-if="modalReview.status.toLowerCase() === 'approved'">
                        <button class="btn btn-danger me-2" @click="rejectReview(modalReview)">駁回為已駁回</button>
                    </template>
                    <button class="btn btn-secondary" @click="closeModal">關閉</button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import axios from '@/plungins/axios.js'; // 引入 axios

// 原始數據
const allReviews = ref([]); // 用於儲存從 API 獲取的所有檢舉資料

// 書籤Tab
const currentTab = ref('all')
const filteredCounts = computed(() => {
    // *** 這裡修改：基於 allReviews.value 進行計算 ***
    const data = allReviews.value;
    return {
        all: data.length,
        approved: data.filter(r => r.status.toLowerCase() === 'approved').length, // 確保大小寫一致
        pending: data.filter(r => r.status.toLowerCase() === 'pending').length,   // 確保大小寫一致
        rejected: data.filter(r => r.status.toLowerCase() === 'rejected').length, // 確保大小寫一致
    }
})

// 根據後端狀態碼轉換為前端顯示文本
const getStatusText = (status) => {
    switch (status) {
        case 'pending':
            return '待審核';
        case 'approved':
            return '已審核';
        case 'rejected':
            return '已駁回';
        default:
            return status;
    }
};

const tabs = computed(() => [
    {
        key: 'all',
        text: '全部',
        icon: `<svg width="20" height="20" style="vertical-align:-3px" fill="#888" viewBox="0 0 16 16"><path d="M2 2v13.5l6-3.25 6 3.25V2z"/></svg>`,
        iconActive: `<svg width="20" height="20" style="vertical-align:-3px" fill="#111" viewBox="0 0 16 16"><path d="M2 2v13.5l6-3.25 6 3.25V2z"/></svg>`,
        count: filteredCounts.value.all,
    },
    {
        key: 'approved',
        text: '已審核',
        icon: `<svg width="20" height="20" style="vertical-align:-3px" fill="#888" viewBox="0 0 16 16"><circle cx="8" cy="8" r="7.5" stroke="#888" fill="none"/><path d="M6.5 8.8l-1.2-1.2-.7.7 1.9 1.9 3.5-3.5-.7-.7z" fill="#888"/></svg>`,
        iconActive: `<svg width="20" height="20" style="vertical-align:-3px" fill="#111" viewBox="0 0 16 16"><circle cx="8" cy="8" r="7.5" stroke="#111" fill="none"/><path d="M6.5 8.8l-1.2-1.2-.7.7 1.9 1.9 3.5-3.5-.7-.7z" fill="#111"/></svg>`,
        count: filteredCounts.value.approved,
    },
    {
        key: 'pending',
        text: '待審核',
        icon: `<svg width="20" height="20" style="vertical-align:-3px" fill="#888" viewBox="0 0 16 16"><circle cx="8" cy="8" r="7.5" stroke="#888" fill="none"/><path d="M8 4v5h4" stroke="#888" stroke-width="1.5" fill="none"/></svg>`,
        iconActive: `<svg width="20" height="20" style="vertical-align:-3px" fill="#111" viewBox="0 0 16 16"><circle cx="8" cy="8" r="7.5" stroke="#111" fill="none"/><path d="M8 4v5h4" stroke="#111" stroke-width="1.5" fill="none"/></svg>`,
        count: filteredCounts.value.pending,
    },
    {
        key: 'rejected',
        text: '已駁回',
        icon: `<svg width="20" height="20" style="vertical-align:-3px" fill="#888" viewBox="0 0 16 16"><circle cx="8" cy="8" r="7.5" stroke="#888" fill="none"/><path d="M4 8l4 4 4-4" stroke="#888" stroke-width="1.5" fill="none"/></svg>`,
        iconActive: `<svg width="20" height="20" style="vertical-align:-3px" fill="#d12" viewBox="0 0 16 16"><circle cx="8" cy="8" r="7.5" stroke="#d12" fill="none"/><path d="M4 8l4 4 4-4" stroke="#d12" stroke-width="1.5" fill="none"/></svg>`,
        count: filteredCounts.value.rejected,
    }
])
const setTab = (key) => {
    currentTab.value = key
    page.value = 1
    selected.value = []
}

// 篩選
const keyword = ref('') // 檢舉人ID (submitterId)
const identityKeyword = ref('') // 評價ID (commentId)

// 篩選 (這裡依然基於 currentTab 過濾)
const filteredReviews = computed(() => {
    let arr = allReviews.value;

    // 根據 Tab 過濾狀態，這裡統一使用 .toLowerCase() 確保前後端狀態匹配
    if (currentTab.value === 'pending') arr = arr.filter(r => r.status.toLowerCase() === 'pending');
    else if (currentTab.value === 'approved') arr = arr.filter(r => r.status.toLowerCase() === 'approved');
    else if (currentTab.value === 'rejected') arr = arr.filter(r => r.status.toLowerCase() === 'rejected');

    // ... (搜尋關鍵字過濾邏輯不變)
    if (keyword.value) {
        arr = arr.filter(r => String(r.submitterId).includes(keyword.value));
    }
    if (identityKeyword.value) {
        arr = arr.filter(r => String(r.commentId).includes(identityKeyword.value));
    }
    return arr;
});


// 分頁
const page = ref(1)
const pageSize = ref(5)
const pageSizes = [5, 10, 20]
const pagedReviews = computed(() => {
    const start = (page.value - 1) * pageSize.value
    const end = start + pageSize.value
    return filteredReviews.value.slice(start, end)
})
const totalPages = computed(() => Math.max(1, Math.ceil(filteredReviews.value.length / pageSize.value)))

// 全選
const selected = ref([])
const isAllSelected = computed(() =>
    selected.value.length === pagedReviews.value.length && pagedReviews.value.length !== 0
)
const toggleAll = () => {
    if (isAllSelected.value) selected.value = []
    else selected.value = pagedReviews.value.map(review => review.id) // 這裡的 ID 是 ReportBean 的 id
}
watch([page, pageSize], () => { selected.value = [] })
watch(pageSize, () => { page.value = 1 })

// 彈窗
const modalReview = ref(null)
const showDetail = (review) => { modalReview.value = { ...review } }
const closeModal = () => { modalReview.value = null }

// 格式化日期時間顯示
const formatDateTime = (dateTimeString) => {
    if (!dateTimeString) return '';
    const date = new Date(dateTimeString);
    return date.toLocaleString(); // 根據用戶本地設置格式化日期時間
};

// --- 後端 API 互動 ---

// 獲取所有檢舉資料
const fetchReviews = async () => {
    try {
        const response = await axios.get('/api/reports');
        // 直接使用從後端返回的 ReportResponseDTO 結構
        allReviews.value = response.data;
        console.log("檢舉資料已加載:", allReviews.value);
    } catch (error) {
        console.error("加載檢舉資料失敗:", error);
        allReviews.value = [];
    }
};

// 處理狀態更新的核心函數
const updateReportAndCommentStatus = async (report, newReportStatus, newCommentIsHidden) => {
    try {
        // 1. 更新檢舉狀態
        const updateReportData = { ...report, status: newReportStatus };
        await axios.put(`/api/reports/${report.id}`, updateReportData);

        // 2. 更新評論的 isHidden 狀態
        // 這裡假設 CommentController 的 PUT 方法可以只接收部分字段來更新
        const updateCommentData = {
            isHidden: newCommentIsHidden
        };
        await axios.put(`/comment/${report.commentId}`, updateCommentData);

        // 更新成功後重新加載數據，並關閉彈窗 (如果打開的話)
        alert(`檢舉 #${report.id} 狀態已更新為 ${getStatusText(newReportStatus)}，相關評論隱藏狀態為 ${newCommentIsHidden ? '已隱藏' : '未隱藏'}。`);
        closeModal(); // 關閉彈窗
        await fetchReviews(); // 更新後重新加載數據
    } catch (error) {
        console.error(`更新檢舉 #${report.id} 失敗:`, error);
        alert(`更新檢舉 #${report.id} 失敗！`);
    }
};


// 確認檢舉 (將檢舉狀態設為 'approved'，並隱藏評論)
const confirmReview = async (report) => {
    if (confirm(`確定要將檢舉 ID 為 ${report.id} 標記為「已審核」並隱藏相關評論嗎？`)) {
        await updateReportAndCommentStatus(report, 'approved', true);
    }
};

// 駁回檢舉 (將檢舉狀態設為 'rejected'，並取消隱藏評論)
const rejectReview = async (report) => {
    if (confirm(`確定要將檢舉 ID 為 ${report.id} 標記為「已駁回」並取消隱藏相關評論嗎？`)) {
        await updateReportAndCommentStatus(report, 'rejected', false); // 駁回時評論設為不隱藏
    }
};

// 新增：重置為待審核 (將檢舉狀態設為 'pending'，並取消隱藏評論)
const resetToPending = async (report) => {
    if (confirm(`確定要將檢舉 ID 為 ${report.id} 重置為「待審核」並取消隱藏相關評論嗎？`)) {
        await updateReportAndCommentStatus(report, 'pending', false); // 重置為 pending 時評論設為不隱藏
    }
};


// 清除篩選
function resetFilters() {
    keyword.value = ''
    identityKeyword.value = ''
    currentTab.value = 'all'; // 清除篩選時重置到全部tab
}

// 組件掛載時獲取資料
onMounted(() => {
    fetchReviews();
});

// Watch 相關數據變化，重新計算 filteredCounts
watch(filteredReviews, () => {
    // filteredCounts 已經是 computed，它會自動反應 filteredReviews 的變化
}, { deep: true });
</script>

<style scoped>
/* 書籤tab樣式 */
.bookmark-tabs {
    display: flex;
    align-items: flex-end;
    border-bottom: 2px solid #ffcd75;
    background: #fff;
    gap: 0.5rem;
    padding-left: 10px;
    position: relative;
    min-height: 48px;
}
.bookmark-tab {
    border: 1px solid #eee;
    background: #fff;
    border-bottom: none;
    border-radius: 6px 6px 0 0;
    margin-bottom: -2px;
    padding: 0.55rem 2rem 0.45rem 1.5rem;
    font-weight: 500;
    font-size: 1rem;
    color: #888;
    display: flex;
    align-items: center;
    transition: background .12s, color .12s;
    box-shadow: none;
    cursor: pointer;
    position: relative;
}
.bookmark-tab.active {
    background: #fff;
    border-color: #ffcd75 #ffcd75 #fff #ffcd75;
    color: #111;
    font-weight: bold;
    z-index: 2;
}
.bookmark-tab:not(.active):hover {
    background: #faf6ef;
    color: #222;
}
.bookmark-underline {
    position: absolute;
    left: 0; right: 0; bottom: -2px;
    height: 2px;
    background: #ffcd75;
    z-index: 1;
}
.tab-count {
    display: inline-block;
    margin-left: 8px;
    background: #f5ba3a11;
    color: #d48806;
    font-size: .98em;
    padding: 0 8px;
    border-radius: 10px;
    min-width: 32px;
    text-align: center;
}
/* 彈窗 */
.modal-mask {
    position: fixed; left:0; top:0; width:100vw; height:100vh;
    background: rgba(0,0,0,.18);
    z-index: 1000;
    display: flex; align-items: center; justify-content: center;
}
.modal-dialog {
    background: #fff;
    border-radius: 10px;
    padding: 2rem 2rem 1rem 2rem;
    min-width: 320px;
    max-width: 95vw;
    box-shadow: 0 8px 32px rgba(0,0,0,.16);
    animation: modalPop .2s;
}
@keyframes modalPop {
    0% { transform: scale(.85);}
    100% { transform: scale(1);}
}
/* 表格 hover 行效果 */
.review-table tbody tr:hover {
    background: #f8f9fa !important;
}

/* 確保表格標題文字居中 */
.review-table th {
    text-align: center;
}
/* 確保表格內容文字居中 */
.review-table td {
    text-align: center;
}
.text-link {
    cursor: pointer;
    text-decoration: underline;
}
.text-link:hover {
    text-decoration: none;
}
</style>