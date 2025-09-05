<template>
  <!-- 我的告警面板 -->
  <Card class="lg:w-1/2 w-full" title="我的告警">
    <div class="space-y-4">
      <template v-if="alarmList.length > 0">
        <div
          v-for="alarm in alarmList"
          :key="alarm.id"
          class="flex items-start justify-between border-b pb-2 last:border-b-0 last:pb-0"
        >
          <div class="flex-1">
            <div class="font-medium text-gray-900 flex items-center">
              <span class="mr-2">{{ alarm.businessName }}</span>
              <Tag :color="getLevelColor(alarm.alarm_level)" class="text-xs">
                {{ getLevelText(alarm.alarm_level) }}
              </Tag>
              <Tag :color="getStatusColor(alarm.alarm_status)" class="text-xs">
                {{ getStatusText(alarm.alarm_status) }}
              </Tag>
            </div>
            <div class="text-sm text-gray-500 mt-1">
              告警内容: {{ alarm.content }}
            </div>
            <div class="text-xs text-gray-400 mt-1">
              主机: {{ alarm.host }}
            </div>
          </div>
          <div class="flex flex-col items-end ml-4">
            <span class="text-xs text-gray-500 mb-2">{{ alarm.alarm_time }}</span>
            <!-- 操作按钮 -->
            <div class="flex space-x-2">
              <!-- 待处理状态显示响应按钮 -->
              <Button 
                v-if="alarm.alarm_status === 'dispose'"
                type="primary" 
                size="small"
                :loading="loadingMap[alarm.id]"
                @click="handleDealAlarm(alarm.id)"
              >
                响应
              </Button>
              <!-- 处理中状态显示关闭按钮 -->
              <Button 
                v-if="alarm.alarm_status === 'processing'"
                type="default" 
                size="small"
                :loading="loadingMap[alarm.id]"
                @click="showCloseModal(alarm.id)"
              >
                关闭
              </Button>
            </div>
          </div>
        </div>
      </template>
      <template v-else>
        <div class="text-gray-400 text-center py-4">暂无告警信息</div>
      </template>
    </div>
  </Card>

  <!-- 系统公告面板 -->
  <Card class="lg:w-1/2 w-full" title="我的ITIL工单">
    <div class="space-y-3">
      <div class="flex items-center justify-between">
      </div>
    </div>
  </Card>

  <!-- 关闭告警弹框 -->
  <Modal
    v-model:open="closeModalVisible"
    title="关闭告警"
    :confirm-loading="closeLoading"
    @ok="handleCloseAlarm"
    @cancel="handleCancelClose"
  >
    <div class="py-4">
      <div class="text-sm text-gray-600 mb-2 p-3 border border-gray-200 rounded">正在关闭选中告警。</div>
      <div class="mb-3 p-3 border border-gray-200 rounded">
        <div class="text-sm font-medium mb-2">解决方案</div>
        <a-textarea
          v-model:value="resolution"
          placeholder="请输入解决方案"
          :rows="4"
          class="w-full"
        />
      </div>
    </div>
  </Modal>
</template>
<style scoped>
.w-full {
  border: 1px solid #d9d9d9;  /* 默认边框颜色 */
  border-radius: 4px;         /* 圆角 */
}
</style>
<script lang="ts">
import { defineComponent, ref, onMounted, reactive } from 'vue';
import { Card, Tag, Button, Modal, message, Input } from 'ant-design-vue';
import { EyeOutlined } from '@ant-design/icons-vue';
import { Icon } from '/@/components/Icon';
import { useUserStore } from '/@/store/modules/user';
import { defHttp } from '/@/utils/http/axios';

export default defineComponent({
  components: { 
    Card, 
    CardGrid: Card.Grid, 
    Icon, 
    EyeOutlined, 
    Tag, 
    Button, 
    Modal,
    'a-textarea': Input.TextArea
  },
  setup() {
    const alarmList = ref<any[]>([]);
    const userStore = useUserStore();
    const username = userStore.getUserInfo?.username;
    
    // 弹框相关状态
    const closeModalVisible = ref(false);
    const closeLoading = ref(false);
    const resolution = ref("");
    const currentAlarmId = ref<number | null>(null);
    
    // 按钮加载状态映射
    const loadingMap = reactive<Record<number, boolean>>({});

    // 告警级别映射
    function getLevelText(level: number) {
      if (level >= 30) return '高';
      if (level >= 20) return '中';
      if (level >= 10) return '低';
      return '未知';
    }

    // 告警状态映射
    function getStatusText(status: string) {
      if (status == 'dispose') return '待处理';
      if (status == 'processing') return '处理中';
      if (status == 'resolved') return '已解决';
      if (status == 'closed') return '已关闭';
      return '未知';
    }

    function getLevelColor(level: number) {
      if (level >= 30) return 'red';
      if (level >= 20) return 'orange';
      if (level >= 10) return 'blue';
      return 'default';
    }

    function getStatusColor(status: string) {
      if (status == 'dispose') return 'red';
      if (status == 'processing') return 'orange';
      return 'default';
    }

    // 获取告警列表
    async function fetchAlarmList() {
      try {
        const data = await defHttp.get({
          url: `sys/home/myAlarmList`,
          params: {
            pageNo: 1,
            pageSize: 10,
            userName: `${encodeURIComponent(username)}`,
          },
        });
        if (
          data &&
          data.code == '0000' &&
          Array.isArray(data.data.rows)
        ) {
          alarmList.value = data.data.rows;
        } else {
          alarmList.value = [];
        }
      } catch (e) {
        alarmList.value = [];
      }
    }

    // 响应告警
    async function handleDealAlarm(alarmId: number) {
      loadingMap[alarmId] = true;
      try {
        const data = await defHttp.post({
          url: `sys/home/dealAlarm`,
          data: { id: alarmId }
        });
        
        if (data && data.retCode === '0000') {
          message.success('响应成功');
          // 重新加载告警列表
          await fetchAlarmList();
        } else {
          message.error(data?.message || '响应失败');
        }
      } catch (error) {
        message.error('响应失败');
      } finally {
        loadingMap[alarmId] = false;
      }
    }

    // 显示关闭弹框
    function showCloseModal(alarmId: number) {
      currentAlarmId.value = alarmId;
      resolution.value = "";  // 重置输入值
      closeModalVisible.value = true;
    }

    // 关闭告警
    async function handleCloseAlarm() {
      if (!resolution.value.trim()) {
        message.warning('请输入解决方案');
        return;
      }
      
      if (!currentAlarmId.value) {
        message.error('告警ID不能为空');
        return;
      }

      closeLoading.value = true;
      try {
        const data = await defHttp.post({
          url: `sys/home/closeAlarm`,
          data: { 
            id: currentAlarmId.value, 
            resolution: resolution.value 
          }
        });
        
        if (data && data.retCode === '0000') {
          message.success('关闭成功');
          closeModalVisible.value = false;
          // 重新加载告警列表
          await fetchAlarmList();
        } else {
          message.error(data?.message || '关闭失败');
        }
      } catch (error) {
        message.error('关闭失败');
      } finally {
        closeLoading.value = false;
      }
    }

    // 取消关闭
    function handleCancelClose() {
      closeModalVisible.value = false;
      resolution.value = '';
      currentAlarmId.value = null;
    }

    onMounted(() => {
      fetchAlarmList();
    });

    return {
      alarmList,
      loadingMap,
      closeModalVisible,
      closeLoading,
      resolution,
      getLevelText,
      getStatusText,
      getLevelColor,
      getStatusColor,
      handleDealAlarm,
      showCloseModal,
      handleCloseAlarm,
      handleCancelClose,
    };
  },
});
</script>
