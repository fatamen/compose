<template>
    <div ref="modal" class="modal fade" id="exampleModal" tabindex="-1" aria-labelledby="exampleModalLabel" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h1 class="modal-title fs-5" id="exampleModalLabel">產品內容</h1>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <table>
                        <tbody>
                            <tr>
                                <td>Id : </td>
                                <td><input type="text" name="id" :value="product.id" @input="doinput('id', $event)"></td>
                            </tr>
                            <tr>
                                <td>Name : </td>
                                <td><input type="text" name="name" :value="product.name" @input="doinput('name', $event)"></td>
                            </tr>
                            <tr>
                                <td>Price : </td>
                                <td><input type="text" name="price" :value="product.price" @input="doinput('price', $event)"></td>
                            </tr>
                            <tr>
                                <td>Make : </td>
                                <td><input type="text" name="make" :value="product.make" @input="doinput('make', $event)"></td>
                            </tr>
                            <tr>
                                <td>Expire : </td>
                                <td><input type="text" name="expire" :value="product.expire" @input="doinput('expire', $event)"></td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-primary" @click="emits('insert')" v-show="isShowInsert">新增</button>
                    <button type="button" class="btn btn-primary" @click="emits('update')" v-show="!isShowInsert">修改</button>
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">關閉</button>
                </div>
            </div>
        </div>
    </div>
</template>
    
<script setup>
    const props = defineProps(["product", "isShowInsert"]);
    const emits = defineEmits(["update:product", "insert", "update"]);


    function doinput(action, event) {
        emits("update:product", {
            ...props.product,
            [action]: event.target.value
        });
    }

    import { ref, onMounted } from 'vue';
    import bootstrap from "bootstrap/dist/js/bootstrap.bundle.min.js"
    const modal = ref(null);
    const bootstrapModal = ref(null);
    onMounted(function() {
        bootstrapModal.value = new bootstrap.Modal(modal.value);

    });
    function showModal() {
        bootstrapModal.value.show();
    }
    function hideModal() {
        bootstrapModal.value.hide();
    }
    defineExpose({
        showModal,
        hideModal
    });
</script>
    
<style>
    
</style>