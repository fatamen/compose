    <template>
        <h2>帳號相關審核</h2>
        <div class="container mt-4">
    
        <!-- 書籤 Tabs -->
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
    
        <!-- 搜尋與篩選 -->
        <div class="d-flex align-items-center gap-2 mb-3 flex-wrap">
            <div>搜尋：</div>
            <input v-model="keyword" placeholder="搜尋名稱..." class="form-control w-auto" style="min-width:160px;" />
            <div class="ms-2">帳號身分：</div>
            <select v-model="selectedIdentity" class="form-select w-auto" style="min-width:120px;">
            <option value="">全部</option>
            <option value="商家">商家</option>
            <option value="使用者">使用者</option>
            </select>
            <button class="btn btn-primary ms-2" @click="resetFilters">清除篩選</button>
        </div>
    
        <!-- 全選 -->
        <div class="mb-2">
            <input type="checkbox" :checked="isAllSelected" @change="toggleAll" />
            全選
        </div>
    
        <!-- 表格 -->
        <table class="table table-striped table-hover promotion-table">
            <thead>
            <tr>
                <th></th>
                <th>帳號身分</th>
                <th>帳號名稱</th>
                <th>審核內容</th>
                <th>審核狀態</th>
                <th>操作</th>
            </tr>
            </thead>
            <tbody>
            <tr
                v-for="account in pagedAccounts"
                :key="account.id"
                @click="showDetail(account)"
                style="cursor:pointer"
            >
                <td @click.stop>
                <input type="checkbox" v-model="selected" :value="account.id" />
                </td>
                <td>{{ account.identity }}</td>
                <td>{{ account.name }}</td>
                <td>{{ account.content }}</td>
                <td>
                {{ account.status }}
                <br v-if="account.detail" />
                <small v-if="account.detail">{{ account.detail }}</small>
                </td>
                <td @click.stop>
                    <span class="text-link me-3" @click="confirmAccount(account.id)">確認</span>
                    <span class="text-link text-danger" @click="rejectAccount(account.id)">拒絕</span>
                </td>
            </tr>
            <tr v-if="pagedAccounts.length === 0">
                <td colspan="6" class="text-center">查無資料</td>
            </tr>
            </tbody>
        </table>
    
        <!-- 分頁控制 -->
        <div class="pagination d-flex justify-content-end align-items-center pagebar-wrap">
            <button class="btn btn-outline-secondary me-2" :disabled="page === 1" @click="currentPage > 1 && (page--)">&lt; 上一頁</button>
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
    
        <!-- 彈窗 -->
        <div v-if="modalAccount" class="modal-mask" @click.self="closeModal">
            <div class="modal-dialog">
            <h5>帳號詳細資訊</h5>
            <table class="table table-sm">
                <tr><th>身分</th><td>{{ modalAccount.identity }}</td></tr>
                <tr><th>名稱</th><td>{{ modalAccount.name }}</td></tr>
                <tr><th>內容</th><td>{{ modalAccount.content }}</td></tr>
                <tr><th>狀態</th><td>{{ modalAccount.status }}</td></tr>
                <tr v-if="modalAccount.detail"><th>說明</th><td>{{ modalAccount.detail }}</td></tr>
            </table>
            <div class="text-end">
                <button class="btn btn-secondary" @click="closeModal">關閉</button>
            </div>
            </div>
        </div>
        </div>
    </template>
    
        <script setup>
        import { ref, computed, watch } from 'vue'
        
        // 假資料
        const accounts = ref([
        { id:1, identity:'商家', name:'張三餐飲有限公司', content:'被舉報餐廳環境髒亂', status:'待審核', detail:'待商家進一步提出處理方案' },
        { id:2, identity:'使用者', name:'李四', content:'多次不拿餐點', status:'已停權', detail:'2025-06-01 14:23' },
        { id:3, identity:'商家', name:'王五鍋物', content:'廣告違規', status:'待審核', detail:'2025-07-01 17:40' },
        ])
        
        // 書籤Tab
        const currentTab = ref('all')
        const filteredCounts = computed(() => ({
        all: accounts.value.length,
        approved: accounts.value.filter(a => a.status.includes('已')).length,
        pending: accounts.value.filter(a => a.status.includes('待')).length,
        }))
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
        }
        ])
        const setTab = (key) => {
        currentTab.value = key
        page.value = 1
        selected.value = []
        }
        
        // 篩選
        const keyword = ref('')
        const selectedIdentity = ref('')
        const filteredAccounts = computed(() => {
        let arr = accounts.value
        if (currentTab.value === 'pending') arr = arr.filter(a => a.status.includes('待'))
        else if (currentTab.value === 'approved') arr = arr.filter(a => a.status.includes('已'))
        if (keyword.value) arr = arr.filter(a => a.name.includes(keyword.value))
        if (selectedIdentity.value) arr = arr.filter(a => a.identity === selectedIdentity.value)
        return arr
        })
        
        // 分頁
        const page = ref(1)
        const pageSize = ref(5)
        const pageSizes = [5, 10, 20]
        const pagedAccounts = computed(() => {
        const start = (page.value - 1) * pageSize.value
        const end = start + pageSize.value
        return filteredAccounts.value.slice(start, end)
        })
        const totalPages = computed(() => Math.max(1, Math.ceil(filteredAccounts.value.length / pageSize.value)))
        
        // 全選
        const selected = ref([])
        const isAllSelected = computed(() =>
        selected.value.length === pagedAccounts.value.length && pagedAccounts.value.length !== 0
        )
        const toggleAll = () => {
        if (isAllSelected.value) selected.value = []
        else selected.value = pagedAccounts.value.map(account => account.id)
        }
        watch([page, pageSize], () => { selected.value = [] })
        watch(pageSize, () => { page.value = 1 })
        
        // 彈窗
        const modalAccount = ref(null)
        const showDetail = (account) => { modalAccount.value = { ...account } }
        const closeModal = () => { modalAccount.value = null }
        
        // 操作
        const confirmAccount = (id) => { alert(`確認 ID: ${id}`) }
        const rejectAccount = (id) => { alert(`拒絕 ID: ${id}`) }
        
        // 清除篩選
        function resetFilters() {
        keyword.value = ''
        selectedIdentity.value = ''
        }
        </script>
    
    
    <style scoped>
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
    </style>
    