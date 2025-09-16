<template>
  <!-- 常用系统导航面板（满宽） -->
  <Card class="w-full" title="常用系统导航">
    <div class="nav-grid">
      <div
        v-for="app in apps"
        :key="app.name"
        class="nav-item"
        :title="app.name"
        role="button"
        tabindex="0"
        @click="open(app.url)"
        @keyup.enter="open(app.url)"
      >
        <img :src="app.icon || defaultIcon" alt="icon" class="nav-icon" />
        <div class="nav-name">{{ app.name }}</div>
      </div>
    </div>
  </Card>
</template>
<style scoped>
.w-full {
  border: 1px solid #d9d9d9;
  border-radius: 4px;
}
.nav-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 5px 5px; /* 行间距48，列间距64，接近示意图 */
  padding: 10px 8px 8px;
}
.nav-item {
  width: 100px;
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  user-select: none;
  transition: transform .15s ease, box-shadow .15s ease;
}
.nav-item:hover {
  transform: translateY(-2px);
}
.nav-icon {
  width: 48px;
  height: 48px;
  object-fit: contain;
}
.nav-name {
  margin-top: 12px;
  font-size: 16px;
  color: #666;
  text-align: center;
}
</style>
<script lang="ts">
import { defineComponent, ref } from 'vue';
import { Card } from 'ant-design-vue';

type AppLink = { name: string; url: string; icon?: string };

export default defineComponent({
  components: { Card },
  setup() {
    const defaultIcon = '/resource/img/logo.png';
    const apps = ref<AppLink[]>([
      { name: 'AIOPS', url: 'https://your-itil.example.com',icon: defaultIcon },
      { name: 'OA', url: 'https://your-cloud-disk.example.com' },
      { name: '混沌工程', url: 'https://mail.example.com' },
      { name: '日志易', url: 'https://oa.example.com' },
      { name: '集中监控', url: 'https://hr.example.com' },
      { name: '流量分析', url: 'https://cmdb.example.com' },
      { name: 'CMDB', url: 'https://cmdb.example.com' },
    ]);

    function open(url: string) {
      if (!url) return;
      window.open(url, '_blank');
    }

    return { apps, defaultIcon, open };
  },
});
</script>
