    <template>
        <div class="modal-bg" v-if="show">
            <div class="modal-dialog modal-dialog-centered">
                <div class="modal-content p-4">
                    <div class="modal-header border-0 pb-0">
                        <h4 class="modal-title fw-bold">歡迎！</h4>
                        <button type="button" class="btn-close custom-close" @click="emit('close')"></button>
                    </div>
                    <div class="modal-body">
                        <div class="mb-2 text-start text-secondary">註冊或登入</div>
                        <!-- Google 按鈕 -->
                        <button class="google-btn mb-3" @click="emit('google-login')">
                            <img src="https://www.svgrepo.com/show/475656/google-color.svg" alt="Google"
                                class="google-icon">
                            <span class="google-text">透過 Google 帳戶繼續操作</span>
                        </button>
                        <div class="divider">
                            <span>或</span>
                        </div>
                        <button class="btn-main w-100 mb-2" @click="emit('login')">登入</button>
                        <button class="btn-outline w-100" @click="emit('register')">註冊</button>
                    </div>
                </div>
            </div>
        </div>
    </template>

<script setup>
const props = defineProps({ show: Boolean })
const emit = defineEmits(['close', 'google-login', 'login', 'register'])
console.log('[RegisterModal loaded]')
</script>

<style scoped>
/* 背景遮罩 */
.modal-bg {
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.08);
    z-index: 900;
    display: flex;
    align-items: center;
    justify-content: center;
}

/* 卡片白底 */
.modal-content {
    background: #fff !important;
    border-radius: 20px;
    box-shadow: 0 2px 24px 4px rgba(0, 0, 0, 0.10);
    border: none;
    padding: 2rem;
}

/* Modal框尺寸 */
.modal-dialog {
    width: 400px;
    margin: 0 auto;
}

/* 關閉鈕 */
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

/* Google 按鈕 */
.google-btn {
    width: 100%;
    display: flex;
    align-items: center;
    background: #fff;
    border: 1.5px solid #ddd;
    border-radius: 8px;
    height: 48px;
    cursor: pointer;
    padding: 0 12px;
    transition: box-shadow 0.1s;
    margin-bottom: 8px;
}

.google-btn:hover {
    box-shadow: 0 2px 10px 0 rgba(236, 29, 111, 0.10);
    border-color: #ffba20;
}

.google-icon {
    width: 24px;
    height: 24px;
    margin-right: 8px;
}

.google-text {
    flex-grow: 1;
    text-align: center;
    font-weight: 500;
    color: #333;
    font-size: 1rem;
}

/* 分隔線與「或」: 一定要白底 */
.divider {
    position: relative;
    text-align: center;
    margin: 20px 0 18px 0;
}

.divider span {
    background: #fff;
    position: relative;
    z-index: 1;
    padding: 0 15px;
    color: #888;
    font-size: 15px;
    letter-spacing: 2px;
    font-weight: 400;
}

.divider:before {
    content: "";
    display: block;
    border-top: 1px solid #eee;
    position: absolute;
    top: 50%;
    left: 0;
    width: 100%;
    z-index: 0;
    transform: translateY(-50%);
}

/* 主要粉色登入按鈕 */
.btn-main {
    background: #ffba20;
    color: #fff;
    font-weight: bold;
    font-size: 20px;
    height: 48px;
    border-radius: 10px;
    border: none;
    margin-bottom: 8px;
    letter-spacing: 2px;
    transition: filter 0.15s;
}

.btn-main:hover {
    filter: brightness(1.12);
    background: #f1cd78;
}

/* 黑框白底註冊 */
.btn-outline {
    background: #fff;
    color: #222;
    font-weight: bold;
    font-size: 20px;
    height: 48px;
    border-radius: 10px;
    border: 2px solid #222;
    letter-spacing: 2px;
    transition: background 0.12s, color 0.12s, border 0.12s;
}

.btn-outline:hover {
background: #F2F2F2;
color: #ffba20;
border-color: #ffba20;
}
</style>
