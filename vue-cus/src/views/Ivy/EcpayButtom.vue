<template>
    <button @click="pay">綠界付款</button>
</template>
<script setup>
import axios from '@/plungins/axios.js'
const pay = async () => {
    const res = await axios.post('/api/ecpay/createOrder', {
        amount: 100,
        description: "測試商品"
    })
    const div = document.createElement('div')
    div.innerHTML = res.data
    document.body.appendChild(div)
    div.querySelector('form').submit()
}
</script>