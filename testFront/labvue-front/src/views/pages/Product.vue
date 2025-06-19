<template>
    <h3>產品功能</h3>
    <div class="container text-center">
        <div class="row">
            <div class="col">
                <button class="btn btn-primary" @click="openModal('insert')">開啟新增</button>
            </div>
            <div class="col">
                <input type="text" placeholder="請輸入產品名稱" v-model="findName" @input="callFind(1)">
            </div>
            <div class="col">
                <ProductSelect v-model="rows" :total="total" :options="[2, 3, 4, 5, 7]" @select-change="callFind(1)">
                </ProductSelect>
            </div>
        </div>
        <br>

        <div class="row">
            <Paginate v-show="total>0"
                :first-last-button="true" first-button-text="&lt;&lt;" last-button-text="&gt;&gt;"
                prev-text="&lt;" next-text="&gt;"
                :page-count="pages"
                :click-handler="callFind"
                :initial-page="current"
                v-model="current">
            </Paginate>
        </div>
        <br>

        <div class="row">
            <div class="col-12 col-sm-12 col-md-6 col-lg-3 col-xl-3 col-xxl-2" v-for="item in products" :key="item.id">
                <ProductCard :product="item"  @open-update="openModal" @delete="callRemove"></ProductCard>
            </div>
        </div>

        <ProductModal ref="productModal"
                :is-show-insert="isShowInsertButton"
                v-model:product="product"
                @insert="callCreate"
                @update="callModify">
        </ProductModal>
    </div>
</template>
    
<script setup>
    import axios from '@/plugins/axios.js';
    import Swal from 'sweetalert2';
    import { onMounted, ref } from 'vue';
    import ProductCard from '@/components/ProductCard.vue';
    import ProductSelect from '@/components/ProductSelect.vue';
    import ProductModal from '@/components/ProductModal.vue';
    import Paginate from 'vuejs-paginate-next';
    import useUserStore from "@/stores/user.js";

    const userStore = useUserStore();
    const productModal = ref(null);
    const findName = ref(null);
    const products = ref([]);
    const product = ref({});
    const isShowInsertButton = ref(true);
    //分頁 start
    const total = ref(0);
    const rows = ref(3);
    const current = ref(7);
    const pages = ref(0);
    const start = ref(0);
    const lastPageRows = ref(0);
    //分頁 end
    
    function openModal(action, id) {
        if(action === "insert") {
            isShowInsertButton.value = true;
            product.value = {};
        } else {
            isShowInsertButton.value = false;
            callFindById(id);
        }
        productModal.value.showModal();
    }
    async function callCreate() {
        Swal.fire({
            title: "Loading......",
            showConfirmButton: false,
            allowOutsideClick: false,
        });

        if(product.value.id === "") {
            product.value.id = null;
        }
        if(product.value.name === "") {
            product.value.name = null;
        }
        if(product.value.price === "") {
            product.value.price = null;
        }
        if(product.value.make === "") {
            product.value.make = null;
        }
        if(product.value.expire === "") {
            product.value.expire = null;
        }
        try {
            const response = await axios.post("/ajax/pages/products", product.value, {
                headers: {
                    "Authorization": `Bearer ${userStore.token}`,
                },
            });
            if(response.data.success) {
                await Swal.fire({
                    text: response.data.message,
                    icon: "success",
                });
                callFind(current.value);
                productModal.value.hideModal();

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
            });
        }
    }
    function callFindById(id) {
        Swal.fire({
            title: "Loading......",
            showConfirmButton: false,
            allowOutsideClick: false,
        });
        axios.get(`/ajax/pages/products/${id}`, {
            headers: {
                "Authorization": `Bearer ${userStore.token}`,
            },
        }).then(function(response) {
            console.log("response", response);
            product.value = response.data.list[0];
            setTimeout(function() {
                Swal.close();
            }, 500);
        }).catch(function(error) {
            console.log("error", error);
            Swal.fire({
                text: error.message,
                icon: "error",
            });
        });
    }
    async function callModify() {
        Swal.fire({
            title: "Loading......",
            showConfirmButton: false,
            allowOutsideClick: false,
        });
        
        if(product.value.id === "") {
            product.value.id = null;
        }
        if(product.value.name === "") {
            product.value.name = null;
        }
        if(product.value.price === "") {
            product.value.price = null;
        }
        if(product.value.make === "") {
            product.value.make = null;
        }
        if(product.value.expire === "") {
            product.value.expire = null;
        }
        try {
            const response = await axios.put(`/ajax/pages/products/${product.value.id}`, product.value, {
                headers: {
                    "Authorization": `Bearer ${userStore.token}`,
                },
            });
            if(response.data.success) {
                await Swal.fire({
                    text: response.data.message,
                    icon: "success",
                });
                callFind(current.value);
                productModal.value.hideModal();

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
            });
        }
    }

    async function callRemove(id) {
        const result = await Swal.fire({
            title: "確定？",
            allowOutsideClick: false,
            showCancelButton: true,
        });
        if(result.isConfirmed) {
            Swal.fire({
                title: "Loading......",
                showConfirmButton: false,
                allowOutsideClick: false,
            });
            try {
                const response = await axios.delete(`/ajax/pages/products/${id}`, {
                    headers: {
                        "Authorization": `Bearer ${userStore.token}`,
                    },
                });
                if(response.data.success) {
                    await Swal.fire({
                        text: response.data.message,
                        icon: "success",
                    });
                    if(current.value>1 && lastPageRows.value===1) {
                        current.value = current.value - 1;
                    }
                    callFind(current.value);

                } else {
                    Swal.fire({
                        text: response.data.message,
                        icon: "warning",
                    });
                }
            } catch(error) {
                console.log("error", error);
                Swal.fire({
                    text: error.message,
                    icon: "error",
                });
            }
        }
    }
    onMounted(function() {
        callFind();
    });
    async function callFind(page) {
        if(page) {
            current.value = page;
            start.value = (page - 1) * rows.value;
        } else {
            current.value = 1;
            start.value = 0;
        }

        if(findName.value==="") {
            findName.value = null;
        }
        const body = {
            "start": start.value,
            "rows": rows.value,
            "name": findName.value,
            "order": "id",
            "dir": false
        };
        try {
            const response = await axios.post("/ajax/pages/products/find", body, {
                headers: {
                    "Authorization": `Bearer ${userStore.token}`,
                },
            });
            console.log("response", response);
            products.value = response.data.list;
            //分頁 start
            total.value = response.data.count;
            pages.value = Math.ceil(total.value / rows.value);
            lastPageRows.value = total.value % rows.value;
            //分頁 end

            setTimeout(function() {
                Swal.close();
            }, 500);
        } catch(error) {
            console.log("error", error);
            Swal.fire({
                text: error.message,
                icon: "error",
            });
        }
    }
</script>

<style>

</style>