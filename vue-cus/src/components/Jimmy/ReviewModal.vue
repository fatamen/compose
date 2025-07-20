<template>
    <div>
        <button class="btn btn-warning btn-sm rounded-pill px-3" @click.stop="openModal">
            <i class="bi bi-star-fill me-1"></i> 評分
        </button>

        <div class="modal fade" id="ratingModal" tabindex="-1" aria-labelledby="ratingModalLabel" aria-hidden="true"
            ref="ratingModalRef">
            <div class="modal-dialog modal-dialog-centered modal-lg">
                <div class="modal-content" @click.stop>
                    <div class="modal-header bg-primary text-black">
                        <h5 class="modal-title" id="ratingModalLabel">評價訂單 - {{ order.store?.name || '未知店家' }}</h5>
                        <button type="button" class="btn-close btn-close-black" data-bs-dismiss="modal" aria-label="Close" @click.stop="closeModal"></button>
                    </div>

                    <div class="modal-body p-4">
                        <div class="d-flex align-items-center mb-4">
                            <img :src="order.store?.photo || 'https://via.placeholder.com/60'" alt="店家圖片"
                                class="me-3 rounded-circle" style="width: 60px; height: 60px; object-fit: cover;">
                            <div>
                                <h6 class="mb-0">{{ order.store?.name || '未知店家' }}</h6>
                                <p class="text-muted small mb-0">訂單時間: {{ formatDateTime(order.createTime) }}</p>
                            </div>
                        </div>

                        <div class="mb-4">
                            <label class="form-label d-block mb-2 fw-bold">店家整體評分：</label>
                            <div class="star-rating">
                                <i v-for="star in 5" :key="star" class="bi"
                                    :class="{ 'bi-star-fill text-warning': star <= currentRating || star <= hoverRating, 'bi-star': star > currentRating && star > hoverRating }"
                                    @mouseover.stop="setHoverRating(star)" @mouseleave.stop="resetHoverRating"
                                    @click.stop="setRating(star)">
                                </i>
                                <span class="ms-2 text-muted">({{ currentRating }} / 5)</span>
                            </div>
                        </div>

                        <div class="mb-4">
                            <label class="form-label d-block mb-2 fw-bold">食物評價：</label>
                            <div v-if="order.orderDetails && order.orderDetails.length > 0">
                                <div v-for="detail in order.orderDetails" :key="detail.id"
                                    class="d-flex align-items-center mb-3 p-2 border rounded">
                                    <img :src="detail.food?.imgResource || 'https://via.placeholder.com/50'" alt="食物圖片"
                                        class="me-3 rounded" style="width: 50px; height: 50px; object-fit: cover;">
                                    <div class="flex-grow-1">
                                        <p class="mb-0 fw-medium">{{ detail.food?.name || '未知食物' }} <span
                                                class="text-muted small">x {{ detail.quantity }}</span></p>
                                    </div>
                                    <div class="btn-group" role="group">
                                        <button type="button" class="btn"
                                            @click.stop="toggleLike(detail.id, true)">
                                            <i class="bi"
                                            :class="{
                                                'bi-hand-thumbs-up-fill': foodLikes[detail.id] === true, // 點讚時為實心
                                                'bi-hand-thumbs-up': foodLikes[detail.id] !== true,     // 未點讚時為空心
                                                'text-danger': foodLikes[detail.id] === true,         // 點讚時為藍色
                                                'text-secondary': foodLikes[detail.id] !== true        // 未點讚時為灰色
                                            }"></i>
                                        </button>
                                        <button type="button" class="btn"
                                            @click.stop="toggleLike(detail.id, false)">
                                            <i class="bi"
                                            :class="{
                                                'bi-hand-thumbs-down-fill': foodLikes[detail.id] === false, // 點倒讚時為實心
                                                'bi-hand-thumbs-down': foodLikes[detail.id] !== false,   // 未點倒讚時為空心
                                                'text-danger': foodLikes[detail.id] === false,          // 點倒讚時為紅色
                                                'text-secondary': foodLikes[detail.id] !== false         // 未點倒讚時為灰色
                                            }"></i>
                                        </button>
                                    </div>
                                </div>
                            </div>
                            <p v-else class="text-muted">此訂單沒有食物明細。</p>
                        </div>

                        <div class="mb-4">
                            <label for="commentText" class="form-label fw-bold">您的評論：</label>
                            <textarea class="form-control" id="commentText" rows="4" v-model="commentContent"
                                placeholder="請輸入您的評論..."></textarea>
                        </div>
                    </div>

                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal" @click.stop="closeModal">取消</button>
                        <button type="button" class="btn btn-primary" @click.stop="submitReview">提交評價</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue';
import { Modal } from 'bootstrap'; // 引入 Bootstrap Modal JavaScript
import axios from '@/plungins/axios.js'; // 確保路徑正確
import { useUserStore } from '@/stores/user.js'; // 假設您有用戶 Pinia Store 來獲取 userId

const props = defineProps({
    order: {
        type: Object,
        required: true
    }
});

const emit = defineEmits(['commentSubmitted', 'ratingUpdated']); // 定義發射事件

// 從 Pinia Store 獲取用戶 ID
const userStore = useUserStore();
const currentUserId = computed(() => userStore.userId); // 假設您在 userStore 中存儲了 userId

// Modal 相關
const ratingModalRef = ref(null); // 用於綁定 Modal 元素的 ref
let bsModal = null; // Bootstrap Modal 實例

// 評分相關
const currentRating = ref(0); // 當前選擇的星級評分 (1-5)
const hoverRating = ref(0); // 滑鼠懸停時的星級評分
const commentContent = ref(''); // 評論內容
const existingCommentId = ref(null); // 如果是編輯現有評論，則存儲評論 ID

// 食物點讚/倒讚狀態
const foodLikes = ref({}); // 儲存每個 orderDetailId 對應的 isLiked 狀態 { orderDetailId: isLiked (boolean) }

// 初始化評分 Modal 數據
const initializeModalData = () => {
    // 初始化評分和評論內容
    if (props.order.comment) {
        currentRating.value = props.order.comment.score || 0;
        commentContent.value = props.order.comment.content || '';
        existingCommentId.value = props.order.comment.id;
    } else {
        currentRating.value = 0;
        commentContent.value = '';
        existingCommentId.value = null;
    }

    // 初始化食物點讚狀態
    foodLikes.value = {};
    if (props.order.orderDetails && props.order.orderDetails.length > 0) {
        props.order.orderDetails.forEach(detail => {
            // 注意：後端返回的 `detail.likeFood.isLiked` 是 `Boolean` 類型
            // 將其轉換為 `true`, `false`, 或 `null` 來匹配 `foodLikes` 的類型
            foodLikes.value[detail.id] = detail.likeFood?.isLiked !== undefined ? detail.likeFood.isLiked : null;
        });
    }
};

// 監聽 order props 變化，當 order 更新時重新初始化數據 (例如，從 API 獲取新訂單數據)
watch(() => props.order, (newOrder) => {
    if (newOrder) {
        initializeModalData();
    }
}, { immediate: true }); // immediate: true 表示組件加載時立即執行一次

// Modal 開啟/關閉
const openModal = () => {
    if (ratingModalRef.value) {
        if (!bsModal) {
            bsModal = new Modal(ratingModalRef.value);
        }
        initializeModalData(); // 每次打開 Modal 都重新初始化數據
        bsModal.show();
    }
};

const closeModal = () => {
    if (bsModal) {
        bsModal.hide();
    }
};

// 星級評分操作
const setRating = (rating) => {
    currentRating.value = rating;
};

const setHoverRating = (rating) => {
    hoverRating.value = rating;
};

const resetHoverRating = () => {
    hoverRating.value = 0;
};

// 食物點讚/倒讚操作
const toggleLike = (orderDetailId, isLiked) => {
    // 如果點擊當前已選的狀態，則重置為 null (未選)
    if (foodLikes.value[orderDetailId] === isLiked) {
        foodLikes.value[orderDetailId] = null;
    } else {
        foodLikes.value[orderDetailId] = isLiked;
    }
};

// 格式化日期時間顯示
const formatDateTime = (dateTimeString) => {
    if (!dateTimeString) return '';
    const date = new Date(dateTimeString);
    return date.toLocaleString(); // 根據用戶本地設置格式化日期時間
};


// 提交評論和點讚狀態
const submitReview = async () => {
    if (currentUserId.value === null) {
        alert('請先登入才能進行評論和點讚。');
        return;
    }

    // 1. 提交或更新評論
    try {
        const commentData = {
            content: commentContent.value,
            score: currentRating.value,
            orderId: props.order.id,
            userId: currentUserId.value,
            storeId: props.order.store?.id // 使用 ?. 確保安全訪問
        };

        let commentResponse;
        if (existingCommentId.value) {
            // 更新現有評論
            commentResponse = await axios.put(`/comment/${existingCommentId.value}`, commentData);
            alert('店家評論更新成功！');
        } else {
            // 創建新評論
            commentResponse = await axios.post('/comment', commentData);
            existingCommentId.value = commentResponse.data.id; // 保存新創建的評論 ID
            alert('店家評論提交成功！');
        }
        console.log('評論提交成功:', commentResponse.data);
        emit('commentSubmitted', commentResponse.data); // 通知父組件評論已提交
    } catch (error) {
        console.error('提交評論失敗:', error);
        alert('提交評論失敗！');
        return; // 如果評論失敗，則停止後續操作
    }

    // 2. 提交或更新食物點讚/倒讚狀態
    if (props.order.orderDetails) {
        for (const detail of props.order.orderDetails) {
            const likedStatus = foodLikes.value[detail.id];
            const originalLikedFood = detail.likeFood; // 確保這裡使用 detail.likeFood

            // 檢查原始狀態與目前選取狀態是否不同
            const isStatusChanged = originalLikedFood ? originalLikedFood.isLiked !== likedStatus : likedStatus !== null;

            if (!isStatusChanged) {
                continue; // 如果狀態沒變，則跳過此食物的處理
            }

            const foodId = detail.food?.id;
            if (!foodId) {
                console.warn(`食物 ID 未找到，跳過 orderDetailId: ${detail.id}`);
                continue;
            }

            const likedFoodData = {
                userId: currentUserId.value,
                foodId: foodId,
                orderDetailId: detail.id,
                isLiked: likedStatus,
            };

            try {
                if (originalLikedFood && originalLikedFood.id) {
                    if (likedStatus === null) {
                        // 如果現在狀態是 null，表示取消點讚/倒讚，則刪除後端記錄
                        await axios.delete(`/liked-food/${originalLikedFood.id}`);
                        console.log(`食物 ${detail.food.name} 點讚記錄已刪除.`);
                    } else {
                        // 否則，更新現有記錄
                        await axios.put(`/liked-food/${originalLikedFood.id}`, likedFoodData);
                        console.log(`食物 ${detail.food.name} 點讚狀態更新成功.`);
                    }
                } else if (likedStatus !== null) {
                    // 如果不存在 likedFood 記錄 (沒有 id)，且用戶選擇了點讚/倒讚 (非 null)，則創建新記錄
                    await axios.post('/liked-food', likedFoodData);
                    console.log(`食物 ${detail.food.name} 點讚記錄創建成功.`);
                }
            } catch (error) {
                console.error(`提交食物 ${detail.food?.name} 點讚狀態失敗:`, error);
            }
        }
    }

    // alert('感謝您的評分和評論！');
    closeModal();
    emit('ratingUpdated');
};
</script>

<style scoped>
/* 確保 Bootstrap Icons 載入 */
@import 'bootstrap-icons/font/bootstrap-icons.css';

/* 星級評分樣式 */
.star-rating .bi {
    font-size: 1.8rem;
    cursor: pointer;
    margin-right: 5px;
    color: #ccc;
    /* 預設灰色 */
}

.star-rating .bi-star-fill.text-warning {
    color: #ffc107 !important;
    /* 黃色星星 */
}

/* 覆寫 Bootstrap Modal Header 背景色 */
.modal-header.bg-primary {
    background-color: #fbfbfc !important;
    /* 你可以根據專案主題調整這個顏色 */
}

.modal-header .btn-close-white {
    filter: invert(1) grayscale(100%) brightness(200%);
    /* 讓關閉按鈕變為白色 */
}

/* 食物圖片圓角 */
.modal-body img.rounded-circle {
    border-radius: 50% !important;
}

/* 點讚/倒讚按鈕組微調 */
.btn-group .btn {
    padding: .375rem .75rem;
    font-size: 1rem;
    background-color: transparent; /* 確保背景是透明的 */
    border: none; /* 確保沒有邊框 */
}

.btn-group .btn i {
    font-size: 1.8rem; /* 你可以稍微調大圖示大小讓它更顯眼 */
    transition: color 0.15s ease-in-out; /* 添加顏色過渡效果 */
}

/* 可以為未選中的圖示設定一個預設顏色，例如深灰色 */
.btn-group .btn .bi {
    color: #6c757d; /* Bootstrap secondary text color */
}

.btn-outline-secondary {
    color: #6c757d;
    border-color: #6c757d;
}
</style>