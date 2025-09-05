<template>
  <PageWrapper>
    <template #headerContent> <WorkbenchHeader /> </template>
    <div class="lg:flex gap-4 mb-6">
      <AiOpsProjectCard :loading="loading" class="enter-y" />
        <!-- <ProjectCard :loading="loading" class="enter-y" /> -->
        <!-- <DynamicInfo :loading="loading" class="!my-4 enter-y" /> -->
      <!-- <div class="lg:w-3/10 w-full enter-y">
        <QuickNav :loading="loading" class="enter-y" />

        <Card class="!my-4 enter-y" :loading="loading">
          <img class="xl:h-50 h-30 mx-auto" src="../../../assets/svg/illustration.svg" />
        </Card>

        <!-- <SaleRadar :loading="loading" class="enter-y" /> -->
      <!-- </div> --> 
    </div>
  </PageWrapper>
</template>
<script lang="ts" setup>
  import { ref } from 'vue';
  // import { Card, Tag } from 'ant-design-vue';
  // import { EyeOutlined } from '@ant-design/icons-vue';
  import { PageWrapper } from '/@/components/Page';
  import WorkbenchHeader from './components/WorkbenchHeader.vue';
  // import ProjectCard from './components/ProjectCard.vue';
  import AiOpsProjectCard from './components/AiOpsProjectCard.vue';
  // import QuickNav from './components/QuickNav.vue';
  // import DynamicInfo from './components/DynamicInfo.vue';
  // import SaleRadar from './components/SaleRadar.vue';

  const loading = ref(true);

  setTimeout(() => {
    loading.value = false;
  }, 500);
</script>
