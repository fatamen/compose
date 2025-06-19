<template>
  <Navigation></Navigation>

  <div>
    <RouterView></RouterView>
  </div>
</template>
  
<script setup>
  import { RouterView } from 'vue-router';
  import "bootstrap/dist/js/bootstrap.bundle.min.js"
  import "bootstrap/dist/css/bootstrap.min.css"
  import Navigation from './views/Navigation.vue';

</script>
  
<style>
  
</style>