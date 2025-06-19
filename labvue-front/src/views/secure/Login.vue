<template>
    <h3>登入</h3>
	<table>
        <tbody>
            <tr>
                <td>ID : </td>
                <td><input type="text" name="username" v-model="username" @blur="checkUsername()" @focus="message=''"></td>
                <td><span>{{ message }}</span></td>
            </tr>
            <tr>
                <td>PWD : </td>
                <td><input type="text" name="password" v-model="password"></td>
                <td></td>
            </tr>
            <tr>
                <td> </td>
                <td align="right"><input type="submit" value="login" @click="login()"></td>
            </tr>
        </tbody>
	</table>
</template>
    
<script setup>
    import Swal from 'sweetalert2'
    import axiosapi from "@/plugins/axios.js"
    import { ref } from 'vue';
    import { useRouter } from 'vue-router';
    import useUserStore from "@/stores/user.js";

    const userStore = useUserStore();
    const router = useRouter();

    const username = ref("");
    const password = ref("");
    const message = ref("");

    async function login() {
        Swal.fire({
            title: "Loading......",
            allowOutsideClick: false,
            showConfirmButton: false,
        });

        if(username.value==="") {
            username.value = null;
        }
        if(password.value==="") {
            password.value = null;
        }
        const body = {
            "username": username.value,
            "password": password.value,
        };

        userStore.setToken("");
        userStore.setEmail("");
        userStore.setLogin(false);
        try {
            const response = await axiosapi.post("/ajax/secure/login", body);
            console.log("response1", response);
            if(response.data.success) {
                await Swal.fire({
                    text: response.data.message,
                    icon: "success",
                });

                userStore.setToken(response.data.token);
                userStore.setEmail(response.data.user);
                userStore.setLogin(true);
                router.push("/");
            } else {
                Swal.fire({
                    text: response.data.message,
                    icon: "warning",
                });
            }
        } catch (error) {
            console.log("error", error);
            Swal.fire({
                text: error.message,
                icon: "error",
            })
        }
    }
    async function checkUsername() {
        if(username.value==="") {
            username.value = null;
        }
        const body = {
            "username": username.value,
        };
        try {
            const response = await axiosapi.post("/ajax/secure/login/exists", body);
            message.value = response.data;
        } catch (error) {
            console.log("error", error);
        }
    }
</script>
    
<style>
    
</style>