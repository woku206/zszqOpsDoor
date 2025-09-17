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
  gap: 17px 17px; /* 行间距48，列间距64，接近示意图 */
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
    const aiopsIcon = '/resource/img/AIOPS.jpeg';
    const cmdbIcon = '/resource/img/CMDB.jpeg';
    const itilIcon = '/resource/img/ITIL.jpeg';
    const hundunIcon = '/resource/img/混沌工程.jpeg';
    const riyijyIcon = '/resource/img/日志易.jpeg';
    const jizhongjiankongIcon = '/resource/img/集中监控.jpeg';
    const liuliangfenxiIcon = '/resource/img/流量分析.jpeg';
    const quanxianyinheIcon = '/resource/img/权限稽核.jpeg';
    const zcloudIcon = '/resource/img/ZCLOUD.jpeg';
    const kelaIcon = '/resource/img/科来.jpeg';
    const hengweiIcon = '/resource/img/恒维.jpeg';



    const apps = ref<AppLink[]>([
      { name: 'AIOPS', url: 'http://10.56.190.109:8080/authentication/login',icon: aiopsIcon},
      { name: 'ITIL', url: 'http://10.56.190.193/',icon: itilIcon},
      { name: '混沌工程', url: 'https://10.56.82.73/#/login?redirect=%2Fdashboard',icon: hundunIcon},
      { name: '日志易', url: 'http://10.55.43.40/auth/login/',icon: riyijyIcon},
      { name: '集中监控', url: 'http://10.55.59.70:8081/alert/active',icon: jizhongjiankongIcon},
      { name: '流量分析', url: 'http://10.55.59.50:8089/#/',icon: liuliangfenxiIcon},
      { name: 'CMDB', url: 'http://10.55.74.108/next/next-cmdb-instance-management/discard', icon: cmdbIcon},
      { name: '权限稽核', url: 'http://10.55.188.69:8080/login',icon: quanxianyinheIcon},
      { name: 'ZCLOUD', url: 'http://10.55.59.152:8080/enmoLogin',icon: zcloudIcon}, 
      { name: '科来', url: 'https://10.56.83.56/login.html',icon: kelaIcon},
      { name: '恒维', url: 'http://10.56.83.5/hengwei/sessions/login',icon: hengweiIcon},
    ]);

    function open(url: string) {
      if (!url) return;
      window.open(url, '_blank');
    }

    return { apps, defaultIcon, open };
  },
});
</script>
