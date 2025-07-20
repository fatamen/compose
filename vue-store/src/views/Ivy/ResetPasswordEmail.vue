    <template>
        <div class="d-flex flex-column align-items-center justify-content-center" style="min-height: 80vh;">
            <h2 class="mb-4">您的會員密碼</h2>
            <div class="mb-4 text-star greeting">
                嗨 使用者,
                <div class="mb-4 text-star">
                    我們收到了您變更密碼的需求。請點選下方連結完成密碼重設。
                    請留意新密碼可能需要幾分鐘更新，如您無法用新密碼登入，請稍等2分鐘再嘗試。
                </div>
            </div>
            <hr class="w-100" />
            <button class="btn btn-main my-4 px-5" @click="handleVerify">
                變更密碼
            </button>
            <div class="mt-5 text-end w-100" style="max-width: 350px;">
                謝謝，<br>
                <span style="color: #ffba20; font-weight: bold;">您的 foodpanda 團隊</span>
            </div>
            <!--當showReset為true時顯示重設密碼對話框-->
            <ResetPasswordDialog v-if="showReset" @close="showReset = false" @submit="onReset" />
        </div>
    </template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import ResetPasswordDialog from './ResetPasswordDialog.vue'

const router = useRouter()
const route = useRoute()

// email 建議用 query 傳過來
const email = route.query.email || ''

// 控制對話框顯示
const showReset = ref(false)

function handleVerify() {
    // 按下後開對話框，而不是立刻跳頁
    showReset.value = true
}

function onReset(newPassword) {
    // 對話框送出時才跳下一頁
    showReset.value = false
    // 這裡可以先 call API
    router.push({
        path: '/',
        query: { email }
    })
}
</script>

<style scoped>
.btn-main {
    background: #ffba20;
    color: #fff;
    font-weight: bold;
    font-size: 20px;
    height: 48px;
    border-radius: 24px;
    border: none;
    letter-spacing: 2px;
    transition: filter 0.15s;
    box-shadow: 0 2px 8px 1px #ffba200f;
}

.btn-main:hover {
    filter: brightness(1.08);
    background: #ffba20;
}
</style>
