    <template>
        <div v-if="show" class="modal-backdrop">
            <div class="modal-content">
                <button class="modal-close" @click="emitClose">×</button>
                <h3 class="modal-title">更改電子郵件</h3>
                <p class="mb-1">
                    我們為您準備的電子郵件地址是
                <p class="mb-3" style="margin-top: -6px;">
                    <span class="main-email">{{ email }}</span>
                    。如果您想更改它，請向我們提供您的新電子郵件，我們將發送一個新的驗證信件。
                </p>
                </p>
                <input class="input" v-model="newEmail" type="email" placeholder="請輸入新電子郵件" />
                <button class="btn-submit" @click="emitSend">發送</button>
            </div>
        </div>
    </template>

<script setup>
import { ref, watch } from 'vue'
const props = defineProps({
    show: Boolean,
    email: String,
})
const emits = defineEmits(['close', 'send'])

const newEmail = ref(props.email)

watch(() => props.email, val => { newEmail.value = val })

function emitClose() {
    emits('close')
}
function emitSend() {
    emits('send', newEmail.value)
}
</script>

<style scoped>
.modal-backdrop {
    position: fixed;
    z-index: 9999;
    left: 0;
    top: 0;
    width: 100vw;
    height: 100vh;
    background: rgba(0, 0, 0, 0.18);
    display: flex;
    align-items: center;
    justify-content: center;
}

.modal-content {
    background: #fff;
    border-radius: 16px;
    padding: 36px 32px 28px;
    width: 400px;
    position: relative;
    box-shadow: 0 4px 40px 0 #e7e7e9;
    display: flex;
    flex-direction: column;
    align-items: stretch;
}

.modal-title {
    font-size: 22px;
    font-weight: bold;
    margin-bottom: 12px;
    text-align: center;
}

.main-email {
    color: #ffba20;
    font-weight: bold;
    font-size: 1.07em;
}

.modal-close {
    position: absolute;
    top: 18px;
    right: 18px;
    border: none;
    background: none;
    font-size: 26px;
    color: #aaa;
    cursor: pointer;
    line-height: 1;
}

.input {
    width: 100%;
    padding: 12px 10px;
    font-size: 17px;
    border-radius: 8px;
    border: 1px solid #ddd;
    margin-bottom: 22px;
    outline: none;
}

.input:focus {
    border-color: #ffba20;
}

.btn-submit {
    background: #ffba20;
    color: #fff;
    border: none;
    border-radius: 8px;
    font-size: 18px;
    padding: 11px 0;
    font-weight: 600;
    cursor: pointer;
    transition: filter 0.14s;
}

.btn-submit:hover {
    filter: brightness(1.08);
}

.mb-1 {
    margin-bottom: 4px;
}

.mb-3 {
    margin-bottom: 18px;
}
</style>
