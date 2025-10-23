<template>
  <div class="wrap">
    <div class="content">
      <FirewallPolicyApplication></FirewallPolicyApplication>
    </div>
  </div>
</template>

<script setup>
  import FirewallPolicyApplication from './components/FirewallPolicyApplication.vue';
</script>

<style lang="less" scoped>
  .wrap {
    height: 100%;
    width: 100%;
    .content {
      background: #fff;
      width: 100%;
      height: 100%;
    }
  }
</style>
