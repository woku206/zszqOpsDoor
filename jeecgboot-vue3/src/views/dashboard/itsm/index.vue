<template>
  <PageWrapper>
    <div>
      <ItsmBaseView :loading="loading" class="enter-y" />
    </div>
  </PageWrapper>
</template>
<script lang="ts" setup>
  import { ref } from 'vue';

  import { PageWrapper } from '/@/components/Page';
  import ItsmBaseView from './components/ItsmBaseView.vue';
  const loading = ref(true);

  setTimeout(() => {
    loading.value = false;
  }, 500);
</script>
