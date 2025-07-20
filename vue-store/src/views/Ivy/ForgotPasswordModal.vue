    <template>
        <div class="modal-bg" v-if="show">
            <div class="modal-dialog modal-dialog-centered">
                <div class="modal-content p-4">
                    <div class="modal-header border-0 pb-0 justify-content-between">
                        <button class="btn nav-btn" @click="emit('back')">
                            <svg width="28" height="28" fill="none" viewBox="0 0 24 24">
                                <path d="M15 6l-6 6 6 6" stroke="#222" stroke-width="2" stroke-linecap="round"
                                    stroke-linejoin="round" />
                            </svg>
                        </button>
                        <button type="button" class="btn-close custom-close" @click="emit('close')"></button>
                    </div>
                    <div class="modal-body d-flex flex-column align-items-center pt-0">
                        <img src="https://cdn-icons-png.flaticon.com/512/3524/3524622.png" alt="lock"
                            class="icon-img mb-3">
                        <div class="fw-bold title mb-2">忘記密碼了嗎？</div>
                        <div class="desc mb-3">輸入你的 email，我們重新設定新密碼</div>
                        <form class="w-100" @submit.prevent="onSubmit">
                            <div class="mb-3 text-start w-100">
                                <label class="form-label label-strong">電子郵件</label>
                                <input type="email" v-model="email" class="form-control custom-input" required
                                    placeholder="eattiy@msn.com">
                            </div>
                            <button type="submit" class="btn btn-main w-100 mb-1">重設密碼</button>
                        </form>
                        <button class="btn-link mt-2" @click="emit('back')">回到登入頁</button>
                    </div>
                </div>
            </div>
        </div>
    </template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({ show: Boolean, email: String })

const emit = defineEmits(['close', 'back', 'submit'])

const email = ref('')

// 關鍵：只要 show/email 變動時自動同步
watch(
    () => [props.show, props.email],
    ([show, newEmail]) => {
        if (show) email.value = newEmail || ''
    },
    { immediate: true }
)

function onSubmit() {
    emit('submit', email.value)
}
</script>

<style scoped>
.modal-bg {
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.08);
    z-index: 9999;
    display: flex;
    align-items: center;
    justify-content: center;
}

.modal-dialog {
    width: 400px;
    /* margin: 0 auto; */
}

.modal-content {
    background: #fff !important;
    border-radius: 20px;
    box-shadow: 0 2px 24px 4px rgba(0, 0, 0, 0.10);
    border: none;
    padding: 2.2rem 2rem 2rem 2rem;
    position: relative;
}

.close-btn {
    position: absolute;
    top: 14px;
    right: 14px;
    background: #fff;
    border: none;
    border-radius: 50%;
    box-shadow: 0 2px 8px 1px rgba(0, 0, 0, 0.10);
    width: 40px;
    height: 40px;
    padding: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    z-index: 10;
    transition: background 0.15s;
}

.nav-btn {
    background: none;
    border: none;
    padding: 0;
    margin-left: -5px;
    margin-top: -5px;
    box-shadow: none;
    outline: none;
}

.icon-img {
    width: 62px;
    height: 62px;
    object-fit: contain;
    margin-bottom: 8px;
}

.title {
    font-size: 2rem;
    font-weight: bold;
    color: #222;
}

.desc {
    color: #999;
    font-size: 1.1rem;
    font-weight: 400;
}

.label-strong {
    font-weight: bold;
    color: #222;
    font-size: 1.08rem;
}

.custom-input {
    font-size: 1.1rem;
    border: 2px solid #222;
    border-radius: 8px;
    height: 46px;
    padding: 7px 12px;
    margin-top: 5px;
    background: #fff;
    box-shadow: none;
}

.custom-input:focus {
    border-color: #ffba20;
    box-shadow: 0 0 0 1px #ffba2021;
}

.btn-main {
    background: #ffba20;
    color: #fff;
    font-weight: bold;
    font-size: 20px;
    height: 48px;
    border-radius: 10px;
    border: none;
    letter-spacing: 2px;
    transition: filter 0.15s;
    box-shadow: 0 2px 8px 1px #ffba200f;
}

.btn-main:hover {
    filter: brightness(1.08);
    background: #ffba20;
}

.btn-link {
    color: #ffba20;
    background: none;
    border: none;
    font-weight: bold;
    font-size: 1rem;
    text-decoration: underline;
    cursor: pointer;
    padding: 0;
    margin: 0;
}
</style>
