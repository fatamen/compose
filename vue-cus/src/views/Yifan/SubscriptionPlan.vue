<template>
    <section class="subscription-landing py-5">
        <div class="container">
        <h1 class="text-center mb-5 fw-bold">
            <i class="bi bi-box-seam me-2"></i>選擇你 金碗 方案
        </h1>
        
        <div class="row g-4">
            <div
            v-for="plan in subscriptionPlans"
            :key="plan.planId"
            class="col-12 col-md-6 col-lg-4"
            >
            <div class="card h-100 shadow-sm">
                <div
                class="card-header text-center text-white"
                :class="plan.isTrial ? 'trial' : 'paid'"
                >
                <h5 class="mb-0">
                    <i
                    :class="plan.isTrial ? 'fas fa-clock me-2' : 'fas fa-star me-2'"
                    ></i>
                    {{ plan.planName }}
                </h5>
                </div>
                <div class="card-body d-flex flex-column">
                <p class="price display-6 text-center mb-3">
                    <i class="bi bi-currency-dollar"></i>
                    {{ plan.planPrice }}
                </p>
                <p class="text-center text-muted mb-4">
                    有效期 <strong>{{ plan.validMonths }}</strong> 個月
                </p>
                <ul class="list-unstyled mb-4">
                    <li v-for="feature in plan.features" :key="feature">
                    <i class="bi bi-check2-circle text-success me-2"></i>
                    {{ feature }}
                    </li>
                </ul>
                <div class="mt-auto text-center">
                    <button
                    class="btn btn-block btn-action"
                    @click="onSubscribe(plan)"
                    >
                    <i
                        :class="plan.isTrial ? 'fas fa-gift me-2' : 'fas fa-shopping-cart me-2'"
                    ></i>
                    {{ plan.isTrial ? '開始免費試用' : '立即訂閱' }}
                    </button>
                </div>
                </div>
            </div>
            </div>
        </div>
        </div>
    </section>
        <!-- 頁腳 -->
    <footer class="footer">
      <p>© 2025 外送平台. 版權所有。</p>
      <p>
        <a href="#">關於我們</a>
        <a href="#">聯繫我們</a>
        <a href="#">隱私政策</a>
        <a href="#">服務條款</a>
      </p>
    </footer>
</template>

<script setup>
import { reactive } from 'vue'

const subscriptionPlans = reactive([
    {
        planId: 0,
        planName: '60 天免費試用',
        planPrice: 0,
        validMonths: 2,
        isTrial: true,
        features: [
        '無限次免外送服務費',
        '生鮮雜貨免外送服務費',
        '知名品牌全品項 7 折優惠',
        ]
    },
    {
        planId: 1,
        planName: '月付方案',
        planPrice: 300,
        validMonths: 1,
        isTrial: false,
        features: [
        '無限次免外送服務費',
        '生鮮雜貨免外送服務費',
        ]
    },
    {
        planId: 2,
        planName: '半年付方案',
        planPrice: 1500,
        validMonths: 6,
        isTrial: false,
        features: [
        '無限次免外送服務費',
        '7 折知名品牌優惠',
        ]
    }
])

function onSubscribe(plan) {
    console.log('使用者選擇方案：', plan)
  // TODO: 串接結帳流程或 API
}
</script>

<style scoped>
/* 訂閱方案的背景顏色-之後再看要不要改顏色 */
.subscription-landing {
    background: #fff9f0;
}

/* 卡片標題顏色 */
.card-header.trial {
    background-color: var(--yellow-light, #ffd67a);
}
.card-header.paid {
    background-color: var(--primary-yellow, #ffba20);
}

/* 價格字體 */
.price {
    color: var(--primary-yellow, #ffba20);
    font-weight: bold;
}

/* 動作按鈕 */
.btn-action {
    width: 100%;
    padding: 0.75rem;
    background-color: var(--primary-yellow, #ffba20);
    border: none;
    color: #fff;
    border-radius: 0.5rem;
    font-size: 1rem;
}
.btn-action:hover {
    background-color: var(--primary-yellow-dark, #edaa00);
    transform: scale(1.05);     /* 按下時等比例縮放 */
    transition: transform 0.2s ease-in-out;  /* 添加動畫效果 */
}

/* 卡片內容 */
.card-body {
    padding: 1rem;
}

/* 卡片內容文字顏色 */
.card-body .text-muted {
    color: #6c757d;
}

/* 重設 list 項目間距 */
ul.list-unstyled li {
    margin-bottom: 0.5rem;
}
  /* 頁腳 */
.footer {
    background-color: #333;
    color: white;
    padding: 20px;
    text-align: center;
}

.footer a {
    color: #ffba20;
    text-decoration: none;
    margin: 0 10px;
}
</style>