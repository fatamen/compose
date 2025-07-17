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
                    <img src="https://cdn-icons-png.flaticon.com/512/561/561127.png" alt="mail"
                        class="email-img mb-3">
                    <div class="fw-bold title mb-1" style="font-size:2rem;">查看email</div>
                    <div class="desc mb-4" style="color:#757575;">我們已將重設密碼步驟的 email 發送給你。別忘了查看你的垃圾郵件箱喔。</div>
                    <button class="btn btn-main w-100 my-2" @click="emit('back')">Back to login</button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
const props = defineProps({ show: Boolean });
const emit = defineEmits(['close', 'back']);
</script>

<style scoped>
.modal-bg {
position: fixed;
inset: 0;
background: rgba(0, 0, 0, 0.08);
z-index: 900;
display: flex;
align-items: center;
justify-content: center;
}

.modal-dialog {
max-width: 420px;
margin: 0 auto;
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
filter: brightness(1.12);
background: #f1cd78;
}
</style>
