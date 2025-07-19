    <template>
        <div class="modal-mask">
        <div class="modal-dialog-custom">
            <button class="modal-close" @click="$emit('close')">×</button>
            <div class="p-4 text-center">
            <h3 class="fw-bold mb-2">歡迎！管理者</h3>
            <div class="mb-3 text-muted">請登入</div>
            <input class="form-control mb-2" placeholder="請輸入帳號" v-model="inputUser" />
            <input class="form-control mb-2" placeholder="請輸入密碼" type="password" v-model="inputPwd" />
            <button class="btn btn-login w-100" @click="login">登入</button>
            </div>
        </div>
        </div>
    </template>
    
    <script setup>
    import { ref } from 'vue'
    import fakeAvatar from '@/assets/images/123.jpg' 
    
    const inputUser = ref('')
    const inputPwd = ref('')
    function login() {
    if (inputUser.value && inputPwd.value) {
        // 這裡用 import 的 fakeAvatar
        emit('login-success', { name: inputUser.value, avatar: fakeAvatar })
    } else {
        alert('請輸入帳號與密碼')
    }
}
    const emit = defineEmits(['close', 'login-success'])
    </script>
    
    <style scoped>
    .modal-mask {
        position: fixed;
        inset: 0;
        background: rgba(0,0,0,0.12);
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 2000;
    }
    .modal-dialog-custom {
        background: #fff;
        border-radius: 14px;
        min-width: 340px;
        box-shadow: 0 4px 32px 2px rgba(0,0,0,0.11);
        position: relative;
        animation: fadeIn .22s;
    }
    .modal-close {
        position: absolute;
        top: 14px;
        right: 20px;
        border: none;
        background: transparent;
        font-size: 2rem;
        color: #888;
        cursor: pointer;
    }
    .btn-login {
        background: #ffba20;
        color: #fff;
        font-size: 1.25rem;
        font-weight: bold;
        border-radius: 12px;
        height: 52px;
        margin-top: 16px;
    }
    </style>
    