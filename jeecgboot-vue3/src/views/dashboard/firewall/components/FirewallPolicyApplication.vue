<template>
  <div class="firewall-policy-page">
    <div class="page-header">
      <h1 class="page-title">防火墙策略开通申请表</h1>
    </div>

    <!-- 基本信息 -->
    <div class="form-section">
      <div class="section-header">
        <div class="section-title-bar"></div>
        <h2 class="section-title">基本信息</h2>
      </div>
      <div class="form-row">
        <div class="form-item">
          <label class="form-label">申请人</label>
          <a-input v-model:value="formData.applicant" placeholder="请输入申请人" :disabled="true" />
        </div>
        <div class="form-item">
          <label class="form-label">申请日期</label>
          <a-date-picker v-model:value="formData.applicationDate" style="width: 100%" :disabled="isReadOnly" />
        </div>
        <div class="form-item">
          <label class="form-label required">紧急程度</label>
          <a-select v-model:value="formData.urgencyLevel" placeholder="请选择紧急程度" style="width: 100%" :disabled="isReadOnly">
            <a-select-option value="high">高 (1天内处理)</a-select-option>
            <a-select-option value="medium">中 (3天内处理)</a-select-option>
            <a-select-option value="low">低 (7天内处理)</a-select-option>
          </a-select>
        </div>
      </div>
    </div>

    <!-- 策略配置 -->
    <div class="form-section">
      <div class="section-header">
        <div class="section-title-bar"></div>
        <h2 class="section-title">策略配置</h2>
      </div>
      <div class="policy-table">
        <div class="table-header">
          <div class="header-cell">序号</div>
          <div class="header-cell required">源地址类型</div>
          <div class="header-cell required">源地址应用系统</div>
          <div class="header-cell required">源地址</div>
          <div class="header-cell required">目的地址类型</div>
          <div class="header-cell required">目的地址应用系统</div>
          <div class="header-cell required">目的地址</div>
          <div class="header-cell required">端口</div>
          <div class="header-cell required">协议</div>
          <div class="header-cell">长连接</div>
          <div class="header-cell">操作</div>
        </div>
        <div class="table-body">
          <div v-for="(policy, index) in formData.policies" :key="index" class="table-row">
            <div class="table-cell">{{ index + 1 }}</div>
            <div class="table-cell">
              <a-select v-model:value="policy.sourceType" placeholder="请选择" style="width: 100%" :disabled="isReadOnly" @change="() => { policy.sourceSystem = ''; policy.sourceAddress = ''; if(policy.sourceType === 'ipScoperAddress') { policy.sourceSystem = 'internal'; } }">
                <a-select-option value="internalApplicationAddress">内部应用地址</a-select-option>
                <a-select-option value="ipScoperAddress">内部网段地址</a-select-option>
                <a-select-option value="externalAddress">手工输入地址</a-select-option>
              </a-select>
            </div>
            <div class="table-cell">
              <a-input
                v-if="policy.sourceType === 'externalAddress'"
                v-model:value="policy.sourceSystem"
                placeholder="请输入系统名称"
                :disabled="isReadOnly"
              />
              <a-select 
                v-else
                v-model:value="policy.sourceSystem" 
                placeholder="请选择" 
                style="width: 100%"
                :options="getSystemOptions(policy.sourceType)"
                show-search
                :filter-option="(input, option) => option.label.toLowerCase().indexOf(input.toLowerCase()) >= 0"
                :disabled="isReadOnly || policy.sourceType === 'ipScoperAddress'"
                @change="() => { 
                  policy.sourceAddress = ''; 
                  if(policy.sourceType === 'internalApplicationAddress') {
                    fetchCmdbSystemIps(policy.sourceSystem, 'source');
                  }
                }"
              />
              </div>
            <div class="table-cell">
              <a-select 
                v-if="policy.sourceType === 'internalApplicationAddress' || policy.sourceType === 'ipScoperAddress'"
                v-model:value="policy.sourceAddress" 
                placeholder="请选择IP地址" 
                style="width: 100%"
                :options="getAddressOptions(policy.sourceType, policy.sourceSystem, 'source')"
                show-search
                :mode="(policy.sourceType === 'internalApplicationAddress' || policy.sourceType === 'ipScoperAddress') ? 'multiple' : undefined"
                :filter-option="(input, option) => option.label.toLowerCase().indexOf(input.toLowerCase()) >= 0"
                :disabled="isReadOnly"
              />
              <a-input 
                v-else
                v-model:value="policy.sourceAddress" 
                placeholder="IP/网段以逗号分隔，如: 192.168.1.1,10.0.0.1" 
                :disabled="isReadOnly"
              />
            </div>
            <div class="table-cell">
              <a-select v-model:value="policy.destType" placeholder="请选择" style="width: 100%" :disabled="isReadOnly" @change="() => { policy.destSystem = ''; policy.destAddress = ''; if(policy.destType === 'ipScoperAddress') { policy.destSystem = 'internal'; } }">
                <a-select-option value="internalApplicationAddress">内部应用地址</a-select-option>
                <a-select-option value="ipScoperAddress">内部网段地址</a-select-option>
                <a-select-option value="externalAddress">手工输入地址</a-select-option>
              </a-select>
            </div>
            <div class="table-cell">
              <a-input
                v-if="policy.destType === 'externalAddress'"
                v-model:value="policy.destSystem"
                placeholder="请输入系统名称"
                :disabled="isReadOnly"
              />
              <a-select 
                v-else
                v-model:value="policy.destSystem" 
                placeholder="请选择" 
                style="width: 100%"
                :options="getSystemOptions(policy.destType)"
                show-search
                :filter-option="(input, option) => option.label.toLowerCase().indexOf(input.toLowerCase()) >= 0"
                :disabled="isReadOnly || policy.destType === 'ipScoperAddress'"
                @change="() => { 
                  policy.destAddress = ''; 
                  if(policy.destType === 'internalApplicationAddress') {
                    fetchCmdbSystemIps(policy.destSystem, 'dest');
                  }
                }"
              />
            </div>
            <div class="table-cell">
              <a-select 
                v-if="policy.destType === 'internalApplicationAddress' || policy.destType === 'ipScoperAddress'"
                v-model:value="policy.destAddress" 
                placeholder="请选择IP地址" 
                style="width: 100%"
                :options="getAddressOptions(policy.destType, policy.destSystem, 'dest')"
                show-search
                :mode="(policy.destType === 'internalApplicationAddress' || policy.destType === 'ipScoperAddress') ? 'multiple' : undefined"
                :filter-option="(input, option) => option.label.toLowerCase().indexOf(input.toLowerCase()) >= 0"
                :disabled="isReadOnly"
              />
              <a-input 
                v-else
                v-model:value="policy.destAddress" 
                placeholder="IP以逗号分隔，如: 192.168.1.1,10.0.0.1" 
                :disabled="isReadOnly"
              />
            </div>
            <!-- <div class="table-cell">
              <a-select v-model:value="policy.destSystem" placeholder="请选择" style="width: 100%">
                <a-select-option value="email">邮件</a-select-option>
                <a-select-option value="database">数据库</a-select-option>
                <a-select-option value="web">Web服务</a-select-option>
              </a-select>
            </div> -->
            <div class="table-cell">
              <a-input 
                v-model:value="policy.port" 
                placeholder="如: 80,443" 
                :disabled="isReadOnly"
              />
            </div>
            <div class="table-cell">
              <a-select v-model:value="policy.protocol" placeholder="请选择" style="width: 100%" :disabled="isReadOnly">
                <a-select-option value="tcp">TCP</a-select-option>
                <a-select-option value="udp">UDP</a-select-option>
              </a-select>
            </div>
            <div class="table-cell">
              <a-switch v-model:checked="policy.longConnection" :disabled="isReadOnly" />
            </div>
            <div class="table-cell">
              <a-button type="text" @click="copyPolicy(index)" :disabled="isReadOnly" style="margin-right: 8px;">
                <Icon icon="ant-design:copy-outlined" />
              </a-button>
              <a-button type="text" danger @click="removePolicy(index)" :disabled="isReadOnly">
                <Icon icon="ant-design:delete-outlined" />
              </a-button>
            </div>
          </div>
        </div>
        <div class="add-policy-btn">
          <a-button type="primary" @click="addPolicy" :disabled="isReadOnly">
            <Icon icon="ant-design:plus-outlined" />
            添加策略行
          </a-button>
        </div>
      </div>
    </div>


    <!-- 审批备注 -->
    <div class="form-section">
      <div class="section-header">
        <div class="section-title-bar"></div>
        <h2 class="section-title required">申请说明</h2>
      </div>
      <div class="approval-section">
        <a-textarea 
          v-model:value="formData.approvalRemarks"
          placeholder="请说明策略开通的原因和用途..."
          :rows="4"
          class="approval-textarea"
          :disabled="isReadOnly"
        />
        <div v-if="currentTicketId" class="download-button-wrapper">
          <a-button 
            type="primary"
            @click="handleDownloadAttachment"
          >
            <Icon icon="ant-design:download-outlined" />
            下载附件
          </a-button>
        </div>
        <div class="action-buttons">
          <a-button type="primary" size="large" @click="submitApplication" :disabled="isReadOnly">
            <Icon icon="ant-design:check-outlined" />
            提交申请
          </a-button>
          <a-button size="large" @click="saveDraft" :disabled="isReadOnly">
            <Icon icon="ant-design:save-outlined" />
            保存草稿
          </a-button>
          <a-button size="large" @click="resetForm" :disabled="isReadOnly">
            <Icon icon="ant-design:reload-outlined" />
            重置表单
          </a-button>
          <a-button 
            v-if="currentTicketId && !isReadOnly" 
            size="large" 
            danger 
            @click="handleDiscardTicket"
          >
            <Icon icon="ant-design:delete-outlined" />
            废弃
          </a-button>
        </div>
      </div>
    </div>
  </div>
  
</template>

<script lang="ts">
import { defineComponent, reactive, ref, watch, computed } from 'vue';
import { message, Modal } from 'ant-design-vue';
import Icon from '@/components/Icon';
import dayjs from 'dayjs';
import { useUserStore } from '/@/store/modules/user';
import { defHttp } from '/@/utils/http/axios';
import { useRoute, useRouter } from 'vue-router';
import * as XLSX from 'xlsx';
// import { saveAs } from 'file-saver';
import { downloadByData } from '/@/utils/file/download';

export default defineComponent({
  name: 'FirewallPolicyApplication',
  components: {
    Icon,
  },
  setup() {
    const userStore = useUserStore();
    const username = userStore.getUserInfo?.username;
    const currentUser = userStore.getUserInfo?.realname || userStore.getUserInfo?.username || '当前用户';
    const route = useRoute();
    const router = useRouter();
    const environmentType = '生产'; // 从路由参数获取环境类型

    // CMDB系统数据
    const cmdbSystems = ref<any[]>([]);
    const sourceSystemIps = ref<any[]>([]);
    const destSystemIps = ref<any[]>([]);
    const ipScopeList = ref<any[]>([]);
    const loading = ref(false);
    
    // 当前工单ID和状态
    const currentTicketId = ref<number | null>(null);
    const currentTicketStatus = ref<string>('');
    
    // 判断是否为只读模式（状态为processing/resolved/closed时只读）
    const isReadOnly = computed(() => {
      return currentTicketStatus.value === 'processing' || 
             currentTicketStatus.value === 'resolved' || 
             currentTicketStatus.value === 'closed';
    });

    const formData = reactive({
      applicant: currentUser,
      applicationDate: dayjs(),
      urgencyLevel: 'medium',
      policies: [
        {
          sourceType: 'internalApplicationAddress',
          sourceAddress: '',
          sourceSystem: '',
          destType: 'internalApplicationAddress',
          destAddress: '',
          destSystem: '',
          port: '',
          protocol: 'tcp',
          longConnection: true,
        }
      ],
      effectiveType: 'immediate',
      effectiveDate: null,
      approvalRemarks: '',
    });

    // 获取CMDB系统列表
    const fetchCmdbSystems = async () => {
      try {
        loading.value = true;
        const response = await defHttp.get({
          url: '/sys/home/getCmdbSystem',
        });
        if (response) {
          cmdbSystems.value = response;
        }
      } catch (error) {
        console.error('获取CMDB系统失败:', error);
        message.error('获取CMDB系统失败');
      } finally {
        loading.value = false;
      }
    };

    // 获取CMDB系统IP列表
    const fetchCmdbSystemIps = async (objectId: string, type: 'source' | 'dest') => {
      try {
        const response = await defHttp.post({
          url: '/sys/home/getCmdbSystemIpList',
          data: { objectId }
        });
        if (response) {
          if (type === 'source') {
            sourceSystemIps.value = response;
          } else {
            destSystemIps.value = response;
          }
        }
      } catch (error) {
        console.error('获取CMDB系统IP失败:', error);
        message.error('获取CMDB系统IP失败');
      }
    };

    // 获取CMDB IP网段列表
    const fetchCmdbIpScoperList = async () => {
      try {
        const response = await defHttp.get({
          url: '/sys/home/getCmdbIpScoperList',
        });
        if (response) {
          ipScopeList.value = response;
        }
      } catch (error) {
        console.error('获取IP网段列表失败:', error);
        message.error('获取IP网段列表失败');
      }
    };

    // 根据系统值获取展示名称（用于将objectId映射为中文名称）
    const getSystemDisplayName = (type: string, systemValue: string) => {
      if (!systemValue) return '';
      if (type === 'externalAddress') return systemValue || '手工输入应用系统';
      if (type === 'ipScoperAddress') return '内部网段';
      if (type === 'internalApplicationAddress') {
        const hit = cmdbSystems.value.find((s: any) => s.objectId === systemValue);
        return hit ? hit.businessName : systemValue;
      }
      return systemValue;
    };

    // 获取系统选项
    const getSystemOptions = (type: string) => {
      if (type === 'internalApplicationAddress') {
        return cmdbSystems.value.map(system => ({
          label: system.businessName,
          value: system.objectId,
        }));
      } else if (type === 'externalAddress') {
        return [{ label: '手工输入应用系统', value: 'external' }];
      } else if (type === 'ipScoperAddress') {
        return [{ label: '内部网段', value: 'internal' }];
      }
      return [];
    };

    // 获取地址选项
    const getAddressOptions = (type: string, systemName: string, addressType: 'source' | 'dest') => {
      if (type === 'internalApplicationAddress' && systemName) {
        const ipList = addressType === 'source' ? sourceSystemIps.value : destSystemIps.value;
        return ipList.map(ip => ({
          label: ip,
          value: ip
        }));
      } else if (type === 'ipScoperAddress') {
        return ipScopeList.value.map(scope => ({
          label: scope,
          value: scope
        }));
      }
      return [];
    };

    // 前端格式校验逻辑已移除，统一由后端进行校验

    // 辅助函数：将内部值映射为显示文本
    const mapToDisplayText = (value: any, type: string) => {
      if (type === 'addressType') {
        switch (value) {
          case 'internalApplicationAddress': return '内部应用地址';
          case 'externalAddress': return '手工输入地址';
          case 'ipScoperAddress': return '内部网段地址';
          default: return value;
        }
      } else if (type === 'longConnection') {
        return value ? '是' : '否';
      } else if (type === 'protocol') {
        return value.toUpperCase(); // 例如：'tcp' -> 'TCP'
      }
      return value;
    };

    const formatAddressDisplay = (address: string | string[]) => {
      if (Array.isArray(address)) {
        return address.join(',');
      }
      return address || '未填写';
    };

    // 判断是否可能是“手工输入网段”（简单规则：IPv4 且以 .0 结尾，且不包含掩码）
    const isLikelyNetworkWithoutMask = (addr: string) => {
      if (!addr || addr.includes('/')) return false;
      const parts = addr.split('.');
      if (parts.length !== 4) return false;
      return parts[3] === '0';
    };

    watch(() => formData.policies, () => {
      formData.approvalRemarks = generateApplicationDescription();
    }, { deep: true });

    // 将IP地址分组为连续段的函数
    const groupConsecutiveIPs = (ips: string[]) => {
      if (!ips || ips.length === 0) return [];
      
      // 将IP地址转换为数字进行比较
      const ipToNumber = (ip: string) => {
        const parts = ip.split('.');
        if (parts.length !== 4) return 0;
        return parseInt(parts[0]) * 256 * 256 * 256 + 
               parseInt(parts[1]) * 256 * 256 + 
               parseInt(parts[2]) * 256 + 
               parseInt(parts[3]);
      };
      
      // 获取IP的前三个部分（用于判断是否在同一网段）
      const getIpPrefix = (ip: string) => {
        const parts = ip.split('.');
        if (parts.length !== 4) return '';
        return `${parts[0]}.${parts[1]}.${parts[2]}`;
      };
      
      // 排序IP地址
      const sortedIPs = ips.map(ip => ({
        ip: ip.trim(),
        num: ipToNumber(ip.trim()),
        prefix: getIpPrefix(ip.trim())
      })).sort((a, b) => a.num - b.num);
      
      const groups: string[][] = [];
      let currentGroup: string[] = [sortedIPs[0].ip];
      let currentPrefix = sortedIPs[0].prefix;
      
      for (let i = 1; i < sortedIPs.length; i++) {
        // 连续IP的条件：数值连续且前三个部分相同
        if (sortedIPs[i].num === sortedIPs[i-1].num + 1 && sortedIPs[i].prefix === currentPrefix) {
          // 连续的IP，添加到当前组
          currentGroup.push(sortedIPs[i].ip);
        } else {
          // 不连续或前缀不同，开始新组
          groups.push([...currentGroup]);
          currentGroup = [sortedIPs[i].ip];
          currentPrefix = sortedIPs[i].prefix;
        }
      }
      
      // 添加最后一组
      groups.push(currentGroup);
      
      return groups;
    };

    // 压缩单个连续IP组的函数
    const compressIPGroup = (ipGroup: string[]) => {
      if (ipGroup.length === 1) {
        return ipGroup[0];
      } else {
        // 提取前三个部分作为基础IP
        const startParts = ipGroup[0].split('.');
        const endParts = ipGroup[ipGroup.length - 1].split('.');
        const baseIP = startParts.slice(0, 3).join('.'); // 前三个部分
        const startLastNum = startParts[3]; // 最后一个数字
        const endLastNum = endParts[3]; // 最后一个数字
        return `${baseIP}.${startLastNum}-${endLastNum}`;
      }
    };

    // 自动生成申请说明
    const generateApplicationDescription = () => {
      if (!formData.policies || formData.policies.length === 0) {
        return '';
      }

      const descriptionLines: string[] = [];

      formData.policies.forEach((policy, index) => {
        if (!policy.sourceType || !policy.sourceSystem || !policy.destType || !policy.destSystem) {
          return;
        }

        const sourceName = getSystemDisplayName(policy.sourceType, policy.sourceSystem);
        const destName = getSystemDisplayName(policy.destType, policy.destSystem);
        const sourceAddressText = formatAddressDisplay(policy.sourceAddress);
        const destAddressText = formatAddressDisplay(policy.destAddress);
        const portText = policy.port ? `，端口：${policy.port}` : '';
        const protocolText = policy.protocol ? `，协议：${policy.protocol.toUpperCase()}` : '';

        descriptionLines.push(
          `策略${index + 1}：关于开通${sourceName}（${sourceAddressText}）到${destName}（${destAddressText}）的防火墙策略${portText}${protocolText}`
        );
      });

      if (descriptionLines.length === 0) {
        return '';
      }

      return descriptionLines.join('\n');
    };

    // 监听策略变化，自动更新申请说明
    watch(() => formData.effectiveType, () => {
      formData.approvalRemarks = generateApplicationDescription();
    }, { deep: true });

    // 监听地址类型变化，自动清空外部类型的系统名称，保持手工输入
    watch(() => formData.policies, (newPolicies) => {
      newPolicies.forEach(policy => {
        if (policy.sourceType !== 'externalAddress' && policy.sourceSystem === 'external') {
          policy.sourceSystem = '';
        }
        if (policy.destType !== 'externalAddress' && policy.destSystem === 'external') {
          policy.destSystem = '';
        }
      });
    }, { deep: true });

    const addPolicy = () => {
      formData.policies.push({
        sourceType: 'internalApplicationAddress',
        sourceAddress: '',
        sourceSystem: '',
        destType: 'internalApplicationAddress',
        destAddress: '',
        destSystem: '',
        port: '',
        protocol: 'tcp',
        longConnection: false,
      });
    };

    const removePolicy = (index: number) => {
      formData.policies.splice(index, 1);
    };

    const copyPolicy = (index: number) => {
      if (index < 0 || index >= formData.policies.length) {
        message.warning('无法复制该策略');
        return;
      }
      const policyToCopy = formData.policies[index];
      // 深拷贝策略对象
      const copiedPolicy: any = {
        sourceType: policyToCopy.sourceType,
        sourceSystem: policyToCopy.sourceSystem,
        destType: policyToCopy.destType,
        destSystem: policyToCopy.destSystem,
        port: policyToCopy.port,
        protocol: policyToCopy.protocol,
        longConnection: policyToCopy.longConnection,
      };
      // 处理 sourceAddress 和 destAddress（可能是字符串或数组）
      if (Array.isArray(policyToCopy.sourceAddress)) {
        copiedPolicy.sourceAddress = [...policyToCopy.sourceAddress];
      } else {
        copiedPolicy.sourceAddress = policyToCopy.sourceAddress || '';
      }
      if (Array.isArray(policyToCopy.destAddress)) {
        copiedPolicy.destAddress = [...policyToCopy.destAddress];
      } else {
        copiedPolicy.destAddress = policyToCopy.destAddress || '';
      }
      // 插入到下一行
      formData.policies.splice(index + 1, 0, copiedPolicy);
      message.success('策略已复制到下一行');
    };

    const submitApplication = () => {
      // 校验逻辑已移到后端，前端直接提交
      submitApplicationWithFile();
    };

    // 提交申请（生成文件、上传ITIL、发起流程）
    const submitApplicationWithFile = async () => {
      try {
        loading.value = true;
        
        // 1. 生成 Excel 文件（在内存中）
        const data: any[] = [];
        // 表头（保持原字段数）
        data.push([
          '序号', '源地址', '源地址、端口说明', '源地址类型', '目的地址', '目的端口1', '至目的端口2', '目的地址、端口说明', '策略用途及必要性',
          '传输层协议', '是否为长连接', '动作', '安全性', '策略使用期限', '测试峰值流量'
        ]);

        // 数据行
        let rowIndex = 1;
        formData.policies.forEach((policy) => {
          // 处理源地址和目的地址，支持多选（数组）或单选（字符串）
          let sourceAddressGroups: string[][] = [];
          let destAddressGroups: string[][] = [];
          
          // 处理源地址
          if (Array.isArray(policy.sourceAddress)) {
            sourceAddressGroups = groupConsecutiveIPs(policy.sourceAddress);
          } else if (policy.sourceType === 'externalAddress' && policy.sourceAddress) {
            const ips = policy.sourceAddress.split(',').map(ip => ip.trim()).filter(ip => ip.length > 0);
            if (ips.length > 0) {
              sourceAddressGroups = groupConsecutiveIPs(ips);
            } else {
              sourceAddressGroups = [[policy.sourceAddress]];
            }
          } else {
            sourceAddressGroups = [[policy.sourceAddress || '']];
          }
          
          // 处理目的地址
          if (Array.isArray(policy.destAddress)) {
            destAddressGroups = groupConsecutiveIPs(policy.destAddress);
          } else if (policy.destType === 'externalAddress' && policy.destAddress) {
            const ips = policy.destAddress.split(',').map(ip => ip.trim()).filter(ip => ip.length > 0);
            if (ips.length > 0) {
              destAddressGroups = groupConsecutiveIPs(ips);
            } else {
              destAddressGroups = [[policy.destAddress]];
            }
          } else {
            destAddressGroups = [[policy.destAddress || '']];
          }

          // 处理端口
          let portDisplay = '';
          if (policy.port) {
            const ports = policy.port.split(',').map(p => p.trim()).slice(0, 10);
            portDisplay = ports.join(',');
          }

          // 为每个源地址组和目的地址组的组合生成一行
          for (let sourceIndex = 0; sourceIndex < sourceAddressGroups.length; sourceIndex++) {
            for (let destIndex = 0; destIndex < destAddressGroups.length; destIndex++) {
              const sourceGroup = sourceAddressGroups[sourceIndex];
              const destGroup = destAddressGroups[destIndex];
              
              let sourceAddressDisplay = compressIPGroup(sourceGroup);
              let destAddressDisplay = compressIPGroup(destGroup);

              // 当地址类型为网段(ipScoperAddress)时，导出到 Excel 需要追加 /24（若原值中未包含掩码）
              if (policy.sourceType === 'ipScoperAddress' && sourceAddressDisplay && !sourceAddressDisplay.includes('/')) {
                sourceAddressDisplay = `${sourceAddressDisplay}/24`;
              }
              if (policy.destType === 'ipScoperAddress' && destAddressDisplay && !destAddressDisplay.includes('/')) {
                destAddressDisplay = `${destAddressDisplay}/24`;
              }

              // 手工输入网段（externalAddress 场景）：导出时也自动补 /24
              if (policy.sourceType === 'externalAddress' && sourceAddressDisplay && isLikelyNetworkWithoutMask(sourceAddressDisplay)) {
                sourceAddressDisplay = `${sourceAddressDisplay}/24`;
              }
              if (policy.destType === 'externalAddress' && destAddressDisplay && isLikelyNetworkWithoutMask(destAddressDisplay)) {
                destAddressDisplay = `${destAddressDisplay}/24`;
              }

              data.push([
                rowIndex++,
                sourceAddressDisplay,
                '',
                '',
                destAddressDisplay,
                portDisplay,
                '',
                '',
                '',
                mapToDisplayText(policy.protocol, 'protocol'),
                mapToDisplayText(policy.longConnection, 'longConnection'),
                '',
                '',
                '',
                ''
              ]);
            }
          }
        });

        const ws = XLSX.utils.aoa_to_sheet(data);
        const wb = XLSX.utils.book_new();
        XLSX.utils.book_append_sheet(wb, ws, '防火墙策略申请');

        // 生成文件名
      let sourceSystemName = '未知系统';
      let destSystemName = '未知系统';
      if (formData.policies.length > 0) {
          const firstPolicy = formData.policies[0];
          sourceSystemName = getSystemDisplayName(firstPolicy.sourceType, firstPolicy.sourceSystem);
          destSystemName = getSystemDisplayName(firstPolicy.destType, firstPolicy.destSystem);
        }
        const filename = `${sourceSystemName}到${destSystemName}的防火墙安全策略申请表（${environmentType}环境）.xls`;

        // 将工作簿转换为二进制数据
        const wbout = XLSX.write(wb, { bookType: 'xls', type: 'array' });
        
        // 转换为 Base64 - 使用更安全的方式处理大文件
        let base64Content = '';
        const uint8Array = new Uint8Array(wbout);
        const chunkSize = 8192;
        for (let i = 0; i < uint8Array.length; i += chunkSize) {
          const chunk = uint8Array.slice(i, i + chunkSize);
          base64Content += String.fromCharCode.apply(null, Array.from(chunk));
        }
        base64Content = btoa(base64Content);

        // 2. 构建提交数据
        const urgencyMap: Record<string, string> = {
          'high': '1',
          'medium': '3',
          'low': '4'
        };
        
        const solutionTimeMap: Record<string, string> = {
          'high': '5', // 1天
          'medium': '6', // 3天
          'low': '8' // 7天
        };

        const queryId = route.query.id;
        let ticketId: number | null = null;
        if (queryId) {
          if (typeof queryId === 'string') ticketId = parseInt(queryId);
          else if (Array.isArray(queryId)) ticketId = parseInt(queryId[0] || '0');
          else ticketId = Number(queryId);
          if (isNaN(ticketId) || ticketId <= 0) ticketId = null;
        }

        // 构建基本信息(base_info)
        const baseInfo = {
          applicant: formData.applicant,
          applicationDate: formData.applicationDate ? dayjs(formData.applicationDate).format('YYYY-MM-DD HH:mm:ss') : null,
          urgencyLevel: formData.urgencyLevel,
          environmentType: environmentType,
        };

        // 构建工单信息(ticket_info) - 包含策略配置和生效时间
        const ticketInfo = {
          policies: formData.policies,
          effectiveType: formData.effectiveType,
          effectiveDate: formData.effectiveDate ? dayjs(formData.effectiveDate).format('YYYY-MM-DD HH:mm:ss') : null,
        };
        
        const submitData = {
          fileName: filename,
          fileContent: base64Content,
          ticketId: ticketId,
          applyUser: username,
          title: `防火墙策略申请-${sourceSystemName}到${destSystemName}`,
          typeOne: '网络安全',
          typeTwo: '生产防火墙策略开通',
          priority: urgencyMap[formData.urgencyLevel] || '3',
          solutiontime: solutionTimeMap[formData.urgencyLevel] || '6',
          apply_info: formData.approvalRemarks,
          base_info: baseInfo,
          ticket_info: ticketInfo,
          handler: '',
          status: 'save',
          source: '智能运维门户系统'
        };

        // 3. 调用后端提交接口
        const result = await defHttp.post({
          url: '/sys/home/submitTicket',
          data: submitData
        });

        if (result) {
          message.success('申请已提交，文件已上传到ITIL平台');
          
          // 同时下载文件到本地
          // saveAs(new Blob([wbout], { type: 'application/octet-stream' }), filename);
          
          // 从返回结果中获取 processKey 和 processInstanceId
          const processKey = result.processKey || result.result?.processKey;
          const processInstanceId = result.processInstanceId || result.result?.processInstanceId;
          
          // 如果获取到流程信息，则在新窗口跳转到ITIL平台
          if (processKey && processInstanceId) {
            // const itilUrl = `http://10.58.229.95:8080/uniapp/ssoLogin?token=${username}&processKey=${processKey}&processInstanceId=${processInstanceId}`;
            const itilUrl = `http://10.56.190.193/`;
            window.open(itilUrl, '_blank');
             // 如果没有流程信息，则跳转到工作台
             await router.push('/dashboard/workbench');
          } else {
            // 如果没有流程信息，则跳转到工作台
            await router.push('/dashboard/workbench');
          }
        }
      } catch (error) {
        console.error('提交申请失败:', error);
        message.error('提交申请失败，请重试');
      } finally {
        loading.value = false;
      }
    };

    // 下载工单附件
    const downloadTicketAttachment = async (ticketId: number) => {
      try {
        loading.value = true;
        // 1. 先获取文件路径和文件名
        const data = await defHttp.get({
          url: '/sys/home/downloadTicketAttachment',
          params: { id: ticketId }
        });
        
        if (!data || !data.filePath) {
          message.error('附件不存在');
          return;
        }

        const filePath = data.filePath;
        const fileName = data.fileName || filePath.split('/').pop() || '附件.xls';
        
        // 2. 使用blob方式下载文件，避免页面跳转
        const blobData = await defHttp.get(
          {
            url: '/sys/home/downloadFile',
            params: { filePath: filePath },
            responseType: 'blob',
          },
          { isTransformResponse: false }
        );
        
        // 3. 触发浏览器下载
        downloadByData(blobData, fileName, 'application/vnd.ms-excel');
        
        message.success('附件下载成功');
      } catch (error: any) {
        console.error('下载附件失败:', error);
        const errorMsg = error?.message || error?.response?.data?.message || '下载附件失败';
        message.error(errorMsg);
      } finally {
        loading.value = false;
      }
    };

    // 处理下载附件按钮点击
    const handleDownloadAttachment = () => {
      if (currentTicketId.value) {
        downloadTicketAttachment(currentTicketId.value);
      }
    };

    // 废弃工单
    const handleDiscardTicket = () => {
      if (!currentTicketId.value) {
        message.warning('工单ID不存在');
        return;
      }

      Modal.confirm({
        title: '确认废弃',
        content: '确定要废弃此工单吗？废弃后工单将被删除，此操作不可恢复。',
        okText: '确认',
        cancelText: '取消',
        onOk: async () => {
          try {
            await defHttp.delete({
              url: `/sys/home/deleteTicket?id=${currentTicketId.value}`,
            });
            message.success('工单已废弃');
            router.push('/dashboard/workbench');
          } catch (error) {
            console.error('废弃工单失败:', error);
            message.error('废弃工单失败');
          }
        }
      });
    };

    const saveDraft = async () => {
      try {
        // 构建基本信息(base_info)
        const baseInfo = {
          applicant: formData.applicant,
          applicationDate: formData.applicationDate ? dayjs(formData.applicationDate).format('YYYY-MM-DD HH:mm:ss') : null,
          urgencyLevel: formData.urgencyLevel,
          environmentType: environmentType,
        };

        // 构建工单信息(ticket_info) - 包含策略配置和生效时间
        const ticketInfo = {
          policies: formData.policies,
          effectiveType: formData.effectiveType,
          effectiveDate: formData.effectiveDate ? dayjs(formData.effectiveDate).format('YYYY-MM-DD HH:mm:ss') : null,
        };

        // 构建请求数据
        const requestData = {
          ticket_type: 'firewall',
          create_user: username,
          status: 'pending',
          base_info: baseInfo,
          ticket_info: ticketInfo,
          apply_info: formData.approvalRemarks,
        };

        // 如果是从带有id的URL进入，则进行更新，携带id
        const queryId = route.query.id;
        if (queryId) {
          let id: number | null = null;
          if (typeof queryId === 'string') id = parseInt(queryId);
          else if (Array.isArray(queryId)) id = parseInt(queryId[0] || '0');
          else id = Number(queryId);
          if (!isNaN(id) && id > 0) {
            // @ts-ignore
            requestData.id = id;
          }
        }

        await defHttp.post({
          url: '/sys/home/saveTicket',
          data: requestData
        });
        
      message.success('草稿保存成功');
        console.log('保存草稿:', requestData);
        
        // 跳转到工作台页面
        await router.push('/dashboard/workbench');
      } catch (error) {
        console.error('保存草稿失败:', error);
        message.error('保存草稿失败');
      }
    };

    const resetForm = () => {
      Object.assign(formData, {
        applicant: currentUser,
        applicationDate: dayjs(),
        urgencyLevel: 'medium',
        policies: [{
          sourceType: 'internalApplicationAddress',
          sourceAddress: '',
          sourceSystem: '',
          destType: 'internalApplicationAddress',
          destAddress: '',
          destSystem: '',
          port: '',
          protocol: 'tcp',
          longConnection: true,
        }],
        effectiveType: 'immediate',
        effectiveDate: null,
        approvalRemarks: '',
      });
      message.info('表单已重置');
    };

    // 根据ID查询工单数据并渲染
    const loadTicketData = async (ticketId: number) => {
      try {
        loading.value = true;
        currentTicketId.value = ticketId;
        const data = await defHttp.get({
          url: '/sys/home/getTicketById',
          params: { id: ticketId }
        });
        
        if (data) {
          currentTicketStatus.value = data.status || '';
          // 解析base_info
          if (data.baseInfo) {
            const baseInfo = typeof data.baseInfo === 'string' ? JSON.parse(data.baseInfo) : data.baseInfo;
            formData.applicant = baseInfo.applicant || currentUser;
            formData.applicationDate = baseInfo.applicationDate ? dayjs(baseInfo.applicationDate) : dayjs();
            formData.urgencyLevel = baseInfo.urgencyLevel || 'medium';
            if (baseInfo.environmentType) {
              // 更新环境类型（如果需要）
            }
          }
          
          // 解析ticket_info
          if (data.ticketInfo) {
            const ticketInfo = typeof data.ticketInfo === 'string' ? JSON.parse(data.ticketInfo) : data.ticketInfo;
            if (ticketInfo.policies && Array.isArray(ticketInfo.policies)) {
              formData.policies = ticketInfo.policies;
              
              // 重新获取策略中应用系统的IP列表
              for (const policy of formData.policies) {
                if (policy.sourceType === 'internalApplicationAddress' && policy.sourceSystem) {
                  await fetchCmdbSystemIps(policy.sourceSystem, 'source');
                }
                if (policy.destType === 'internalApplicationAddress' && policy.destSystem) {
                  await fetchCmdbSystemIps(policy.destSystem, 'dest');
                }
              }
            }
            formData.effectiveType = ticketInfo.effectiveType || 'immediate';
            formData.effectiveDate = ticketInfo.effectiveDate ? dayjs(ticketInfo.effectiveDate) : null as any;
          }
          
          // 设置申请说明
          formData.approvalRemarks = data.applyInfo || '';
          
        }
      } catch (error) {
        console.error('加载工单数据失败:', error);
        message.error('加载工单数据失败');
      } finally {
        loading.value = false;
      }
    };

    // 初始化时获取CMDB系统数据
    fetchCmdbSystems();
    fetchCmdbIpScoperList();
    
    // 检查是否有id参数，如果有则加载数据
    const ticketId = route.query.id;
    if (ticketId) {
      let id: number;
      if (typeof ticketId === 'string') {
        id = parseInt(ticketId);
      } else if (Array.isArray(ticketId)) {
        id = parseInt(ticketId[0] || '0');
      } else {
        id = Number(ticketId);
      }
      if (!isNaN(id) && id > 0) {
        loadTicketData(id);
      }
    }

    return {
      formData,
      addPolicy,
      removePolicy,
      copyPolicy,
      submitApplication,
      saveDraft,
      resetForm,
      getSystemOptions,
      getAddressOptions,
      fetchCmdbSystemIps,
      loading,
      downloadTicketAttachment,
      currentTicketId,
      currentTicketStatus,
      handleDownloadAttachment,
      isReadOnly,
      handleDiscardTicket,
    };
  },
});
</script>

<style scoped lang="less">
.firewall-policy-page {
  background: #ffffff;
  color: #000000;
  min-height: 100vh;
  padding: 24px;

  .page-header {
    margin-bottom: 32px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    
    .page-title {
      font-size: 24px;
      font-weight: 600;
      color: #000000;
      margin: 0;
    }
  }

  .form-section {
    margin-bottom: 32px;
    
    .section-header {
      display: flex;
      align-items: center;
      margin-bottom: 20px;
      
      .section-title-bar {
        width: 4px;
        height: 20px;
        background: #1890ff;
        margin-right: 12px;
      }
      
      .section-title {
        font-size: 18px;
        font-weight: 600;
        color: #000000;
        margin: 0;
        
        &.required::after {
          content: ' *';
          color: #ff4d4f;
        }
      }
    }
  }

  .form-row {
    display: flex;
    gap: 24px;
    
    .form-item {
      flex: 1;
      
      .form-label {
        display: block;
        margin-bottom: 8px;
        color: #333333;
        font-size: 14px;
        
        &.required::after {
          content: ' *';
          color: #ff4d4f;
        }
      }
    }
  }

  .policy-table {
    background: #f8f9fa;
    border: 1px solid #e9ecef;
    border-radius: 8px;
    padding: 16px;
    
    .table-header {
      display: grid;
      grid-template-columns: 20px 130px 200px 150px 130px 200px 150px 70px 70px 50px 80px;
      gap: 10px;
      margin-bottom: 16px;
      
      .header-cell {
        display: flex;
        color: #333333;
        font-size: 14px;
        font-weight: 600;
        padding: 4px 0;
        
        &.required::after {
          content: ' *';
          color: #ff4d4f;
        }
      }
    }
    
    .table-body {
      font-size: 14px;

      .table-row {
        display: grid;
        grid-template-columns: 20px 130px 200px 150px 130px 200px 150px 70px 70px 50px 80px;
        gap: 10px;
        margin-bottom: 16px;
        align-items: center;
        
        .table-cell {
          .address-input {
            display: flex;
            flex-direction: column;
            gap: 4px;
          }
        }
      }
    }
    
    .add-policy-btn {
      margin-top: 16px;
      text-align: left;
    }
  }

  .effective-time-section {
    .form-item {
      margin-bottom: 16px;
      
      .form-label {
        display: block;
        margin-bottom: 8px;
        color: #333333;
        font-size: 14px;
      }
    }
  }

  .approval-section {
    .approval-textarea {
      margin-bottom: 16px;
    }
    
    .download-button-wrapper {
      margin-bottom: 24px;
    }
    
    .action-buttons {
      display: flex;
      gap: 16px;
      justify-content: flex-start;
    }
  }

  // 覆盖 Ant Design 组件样式
  :deep(.ant-input),
  :deep(.ant-select-selector),
  :deep(.ant-picker),
  :deep(.ant-textarea) {
    background: #ffffff !important;
    border-color: #d9d9d9 !important;
    color: #000000 !important;
    
    &::placeholder {
      color: #bfbfbf !important;
    }
  }

  :deep(.ant-select-selection-item) {
    color: #000000 !important;
  }

  :deep(.ant-segmented) {
    background: #f5f5f5 !important;
    
    .ant-segmented-item {
      color: #666666 !important;
      
      &.ant-segmented-item-selected {
        background: #1890ff !important;
        color: #ffffff !important;
      }
    }
  }

  :deep(.ant-radio-group) {
    .ant-radio-wrapper {
      color: #333333 !important;
      
      .ant-radio-checked .ant-radio-inner {
        background: #1890ff !important;
        border-color: #1890ff !important;
      }
    }
  }

  :deep(.ant-switch) {
    &.ant-switch-checked {
      background: #1890ff !important;
    }
  }

  :deep(.ant-btn-primary) {
    background: #1890ff !important;
    border-color: #1890ff !important;
  }

  :deep(.ant-btn-default) {
    background: #ffffff !important;
    border-color: #d9d9d9 !important;
    color: #333333 !important;
    
    &:hover {
      background: #f5f5f5 !important;
      border-color: #40a9ff !important;
    }
  }

}
</style>