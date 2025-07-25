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
                        <img src="https://cdn-icons-png.flaticon.com/512/3059/3059446.png" alt="lock"
                            class="email-img mb-3">
                        <div class="fw-bold title mb-1">歡迎回來！</div>
                        <div class="desc mb-2">請輸入密碼登入</div>
                        <div class="mb-2 fw-bold" style="font-size:18px;">{{ email }}</div>
                        <form class="w-100" @submit.prevent="onSubmit">
                            <div class="mb-3 position-relative">
                                <input :type="showPassword ? 'text' : 'password'" v-model="password"
                                    class="form-control custom-input" placeholder="密碼" required />
                                <button type="button" class="eye-btn" @click="showPassword = !showPassword"
                                    tabindex="-1">
                                    <!-- 眼睛 icon 開關 -->
                                    <svg v-if="!showPassword" width="22" height="22" fill="none" viewBox="0 0 24 24">
                                        <path d="M1 12s4-7 11-7 11 7 11 7-4 7-11 7S1 12 1 12Z" stroke="#ffba20"
                                            stroke-width="2" />
                                        <circle cx="12" cy="12" r="3" stroke="#ffba20" stroke-width="2" />
                                    </svg>
                                    <svg v-else width="22" height="22" fill="none" viewBox="0 0 24 24">
                                        <path
                                            d="M17.94 17.94C16.14 19.25 14.12 20 12 20c-7 0-11-8-11-8a19.89 19.89 0 0 1 5.06-5.94M10.11 6.11A7.01 7.01 0 0 1 12 6c7 0 11 8 11 8a19.89 19.89 0 0 1-4.13 5.11M1 1l22 22"
                                            stroke="#ffba20" stroke-width="2" stroke-linecap="round" />
                                        <circle cx="12" cy="12" r="3" stroke="#ffba20" stroke-width="2" />
                                    </svg>
                                </button>
                            </div>
                            <div class="mb-3 text-start">
                                <a href="#" style="color: #ffba20;font-weight:600;text-decoration: underline"
                                    @click.prevent="emit('forgot')">
                                    忘記密碼？
                                </a>
                            </div>
                            <button type="submit" class="btn btn-main w-100 mt-2" :disabled="!password || loading">
                            <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
                            登入
                        </button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </template>

<script setup>
import { ref } from 'vue'
const props = defineProps({ show: Boolean, email: String, errorMsg: String, loading: Boolean })
const emit = defineEmits(['close', 'back', 'login'])
const password = ref('')
const showPassword = ref(false)
function onSubmit() {
    emit('login', { email: props.email, password: password.value })
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

.email-img {
    width: 62px;
    height: 62px;
    object-fit: contain;
    margin-bottom: 8px;
}

.title {
    font-size: 2rem;
    font-weight: bold;
    color: #222;
    margin-bottom: 4px;
}

.custom-input {
    border-radius: 10px;
    border: 2px solid #ddd;
    font-size: 17px;
    padding: 12px 13px;
    margin-bottom: 0;
}

.btn-main {
    background: #ffba20;
    color: #fff;
    font-weight: bold;
    font-size: 20px;
    height: 48px;
    border-radius: 12px;
    border: none;
    letter-spacing: 2px;
    transition: filter 0.15s;
    box-shadow: 0 2px 8px 1px #ffba200f;
}

.btn-main:disabled {
    background: #d5d5d5;
    color: #fff;
}

.btn-main:hover:enabled {
    filter: brightness(1.08);
    background: #ffba20;
}

.eye-btn {
    position: absolute;
    top: 14px;
    right: 22px;
    background: transparent;
    border: none;
    padding: 0;
    cursor: pointer;
    z-index: 10;
    outline: none;
    display: flex;
    align-items: center;
}
</style>
