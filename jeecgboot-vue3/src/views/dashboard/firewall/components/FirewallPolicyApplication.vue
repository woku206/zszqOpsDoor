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
          <a-input v-model:value="formData.applicant" placeholder="请输入申请人" disabled />
        </div>
        <div class="form-item">
          <label class="form-label">申请日期</label>
          <a-date-picker v-model:value="formData.applicationDate" style="width: 100%" />
        </div>
        <div class="form-item">
          <label class="form-label required">紧急程度</label>
          <a-select v-model:value="formData.urgencyLevel" placeholder="请选择紧急程度" style="width: 100%">
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
          <div v-for="(policy, index) in formData.policies" :key="index" class="table-row" :class="{ 'row-conflict': conflictingRows.has(index) }">
            <div class="table-cell">{{ index + 1 }}</div>
            <div class="table-cell" :class="{ 'row-conflict': conflictingRows.has(index) }">
              <a-select v-model:value="policy.sourceType" placeholder="请选择" style="width: 100%" @change="() => { policy.sourceSystem = ''; policy.sourceAddress = ''; if(policy.sourceType === 'ipScoperAddress') { policy.sourceSystem = 'internal'; } }">
                <a-select-option value="internalApplicationAddress">内部应用地址</a-select-option>
                <a-select-option value="dbAddress">内部数据库地址</a-select-option>
                <a-select-option value="ipScoperAddress">内部网段地址</a-select-option>
                <a-select-option value="externalAddress">外部地址</a-select-option>
              </a-select>
            </div>
            <div class="table-cell" :class="{ 'row-conflict': conflictingRows.has(index) }">
              <a-select 
                v-model:value="policy.sourceSystem" 
                placeholder="请选择" 
                style="width: 100%"
                :options="getSystemOptions(policy.sourceType)"
                show-search
                :filter-option="(input, option) => option.label.toLowerCase().indexOf(input.toLowerCase()) >= 0"
                :disabled="policy.sourceType === 'externalAddress' || policy.sourceType === 'ipScoperAddress'"
                @change="() => { 
                  policy.sourceAddress = ''; 
                  if(policy.sourceType === 'internalApplicationAddress') {
                    fetchCmdbSystemIps(policy.sourceSystem, 'source');
                  } else if(policy.sourceType === 'externalAddress') {
                    policy.sourceSystem = 'external';
                  }
                }"
              />
              </div>
            <div class="table-cell" :class="{ 'row-conflict': conflictingRows.has(index) }">
              <a-select 
                v-if="policy.sourceType === 'internalApplicationAddress' || policy.sourceType === 'ipScoperAddress'"
                v-model:value="policy.sourceAddress" 
                placeholder="请选择IP地址" 
                style="width: 100%"
                :options="getAddressOptions(policy.sourceType, policy.sourceSystem, 'source')"
                show-search
                :mode="policy.sourceType === 'internalApplicationAddress' ? 'multiple' : undefined"
                :filter-option="(input, option) => option.label.toLowerCase().indexOf(input.toLowerCase()) >= 0"
              />
              <a-input 
                v-else
                v-model:value="policy.sourceAddress" 
                placeholder="IP以逗号分隔，如: 192.168.1.1,10.0.0.1" 
                @blur="() => { if (policy.sourceType === 'externalAddress' && policy.sourceAddress) { validateExternalIp(policy.sourceAddress, '源地址'); } }"
              />
            </div>
            <div class="table-cell" :class="{ 'row-conflict': conflictingRows.has(index) }">
              <a-select v-model:value="policy.destType" placeholder="请选择" style="width: 100%" @change="() => { policy.destSystem = ''; policy.destAddress = ''; if(policy.destType === 'ipScoperAddress') { policy.destSystem = 'internal'; } }">
                <a-select-option value="internalApplicationAddress">内部应用地址</a-select-option>
                <a-select-option value="dbAddress">内部数据库地址</a-select-option>
                <a-select-option value="ipScoperAddress">内部网段地址</a-select-option>
                <a-select-option value="externalAddress">外部地址</a-select-option>
              </a-select>
            </div>
            <div class="table-cell" :class="{ 'row-conflict': conflictingRows.has(index) }">
              <a-select 
                v-model:value="policy.destSystem" 
                placeholder="请选择" 
                style="width: 100%"
                :options="getSystemOptions(policy.destType)"
                show-search
                :filter-option="(input, option) => option.label.toLowerCase().indexOf(input.toLowerCase()) >= 0"
                :disabled="policy.destType === 'externalAddress' || policy.destType === 'ipScoperAddress'"
                @change="() => { 
                  policy.destAddress = ''; 
                  if(policy.destType === 'internalApplicationAddress') {
                    fetchCmdbSystemIps(policy.destSystem, 'dest');
                  } else if(policy.destType === 'externalAddress') {
                    policy.destSystem = 'external';
                  }
                }"
              />
            </div>
            <div class="table-cell" :class="{ 'row-conflict': conflictingRows.has(index) }">
              <a-select 
                v-if="policy.destType === 'internalApplicationAddress' || policy.destType === 'ipScoperAddress'"
                v-model:value="policy.destAddress" 
                placeholder="请选择IP地址" 
                style="width: 100%"
                :options="getAddressOptions(policy.destType, policy.destSystem, 'dest')"
                show-search
                :mode="policy.destType === 'internalApplicationAddress' ? 'multiple' : undefined"
                :filter-option="(input, option) => option.label.toLowerCase().indexOf(input.toLowerCase()) >= 0"
              />
              <a-input 
                v-else
                v-model:value="policy.destAddress" 
                placeholder="IP以逗号分隔，如: 192.168.1.1,10.0.0.1" 
                @blur="() => { if (policy.destType === 'externalAddress' && policy.destAddress) { validateExternalIp(policy.destAddress, '目的地址'); } }"
              />
            </div>
            <!-- <div class="table-cell">
              <a-select v-model:value="policy.destSystem" placeholder="请选择" style="width: 100%">
                <a-select-option value="email">邮件</a-select-option>
                <a-select-option value="database">数据库</a-select-option>
                <a-select-option value="web">Web服务</a-select-option>
              </a-select>
            </div> -->
            <div class="table-cell" :class="{ 'row-conflict': conflictingRows.has(index) }">
              <a-input 
                v-model:value="policy.port" 
                placeholder="如: 80,443" 
                @blur="validatePort(policy.port)"
              />
            </div>
            <div class="table-cell">
              <a-select v-model:value="policy.protocol" placeholder="请选择" style="width: 100%">
                <a-select-option value="tcp">TCP</a-select-option>
                <a-select-option value="udp">UDP</a-select-option>
              </a-select>
            </div>
            <div class="table-cell">
              <a-switch v-model:checked="policy.longConnection" />
            </div>
            <div class="table-cell">
              <a-button type="text" danger @click="removePolicy(index)">
                <Icon icon="ant-design:delete-outlined" />
              </a-button>
            </div>
          </div>
        </div>
        <div class="add-policy-btn">
          <a-button type="primary" @click="addPolicy">
            <Icon icon="ant-design:plus-outlined" />
            添加策略行
          </a-button>
        </div>
      </div>
    </div>

    <!-- 生效时间 -->
    <div class="form-section">
      <div class="section-header">
        <div class="section-title-bar"></div>
        <h2 class="section-title">生效时间</h2>
      </div>
      <div class="effective-time-section">
        <div class="form-item">
          <label class="form-label">生效类型</label>
          <a-radio-group v-model:value="formData.effectiveType">
            <a-radio value="immediate">立即生效</a-radio>
            <a-radio value="scheduled">定时生效</a-radio>
          </a-radio-group>
        </div>
        <div v-if="formData.effectiveType === 'scheduled'" class="form-item">
          <label class="form-label required">生效时间</label>
          <a-date-picker 
            v-model:value="formData.effectiveDate" 
            show-time 
            placeholder="请选择生效时间"
            style="width: 100%" 
          />
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
        />
        <div class="action-buttons">
          <a-button type="primary" size="large" @click="submitApplication">
            <Icon icon="ant-design:check-outlined" />
            提交申请
          </a-button>
          <a-button size="large" @click="saveDraft">
            <Icon icon="ant-design:save-outlined" />
            保存草稿
          </a-button>
          <a-button size="large" @click="resetForm">
            <Icon icon="ant-design:reload-outlined" />
            重置表单
          </a-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, reactive, ref, watch } from 'vue';
import { message } from 'ant-design-vue';
import Icon from '@/components/Icon';
import dayjs from 'dayjs';
import { useUserStore } from '/@/store/modules/user';
import { defHttp } from '/@/utils/http/axios';
import { useRoute, useRouter } from 'vue-router';
import * as XLSX from 'xlsx';
import { saveAs } from 'file-saver';

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
    const environmentType = route.query.type === 'production' ? '生产' : '测试'; // 从路由参数获取环境类型

    // CMDB系统数据
    const cmdbSystems = ref<any[]>([]);
    const sourceSystemIps = ref<any[]>([]);
    const destSystemIps = ref<any[]>([]);
    const ipScopeList = ref<any[]>([]);
    const loading = ref(false);
    
    // 冲突行标记
    const conflictingRows = ref<Set<number>>(new Set());

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

    // 获取数据库关联应用系统（Mock数据）
    const getDatabaseSystems = () => {
      return [
        { label: 'MySQL-财务库', value: 'mysql-finance' },
        { label: 'Oracle-HR库', value: 'oracle-hr' },
        { label: 'PostgreSQL-OA库', value: 'postgresql-oa' },
        { label: 'MongoDB-日志库', value: 'mongodb-log' },
      ];
    };

    // 根据系统值获取展示名称（用于将objectId映射为中文名称）
    const getSystemDisplayName = (type: string, systemValue: string) => {
      if (!systemValue) return '';
      if (type === 'externalAddress') return '外部应用系统';
      if (type === 'ipScoperAddress') return '内部网段';
      if (type === 'dbAddress') {
        const db = getDatabaseSystems().find((d) => d.value === systemValue);
        return db ? db.label : systemValue;
      }
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
      } else if (type === 'dbAddress') {
        return getDatabaseSystems();
      } else if (type === 'externalAddress') {
        return [{ label: '外部应用系统', value: 'external' }];
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

    // IP格式校验
    const validateIpFormat = (ip: string) => {
      const ipRegex = /^(\d{1,3}\.){3}\d{1,3}(\/\d{1,2})?$/;
      const ips = ip.split(',').map(i => i.trim()).filter(i => i.length > 0);
      if (ips.length === 0) return false;
      
      // 验证每个IP地址格式和范围
      return ips.every(i => {
        if (!ipRegex.test(i)) return false;
        const parts = i.split('.');
        return parts.every(part => {
          const num = parseInt(part);
          return num >= 0 && num <= 255;
        });
      });
    };

    // CMDB网段校验（Mock）
    const isInCmdbRange = (ip: string) => {
      const cmdbRanges = ['10.0.0.0/8', '172.16.0.0/12', '192.168.0.0/16'];
      const ips = ip.split(',').map(i => i.trim());
      return ips.some(i => {
        return cmdbRanges.some(range => {
          const [rangeIp] = range.split('/');
          // 简单的网段判断逻辑
          return i.startsWith(rangeIp.split('.').slice(0, 2).join('.'));
        });
      });
    };

    // 端口校验
    const validatePort = (port: string) => {
      const ports = port.split(',').map(p => p.trim());
      return ports.every(p => {
        const num = parseInt(p);
        return !isNaN(num) && num >= 1 && num <= 65535;
      });
    };

    // 校验外部IP地址格式
    const validateExternalIp = (ip: string, addressType: string) => {
      if (!ip || !ip.trim()) {
        return;
      }
      
      if (!validateIpFormat(ip)) {
        message.warning(`${addressType}IP格式不正确，请输入正确的IP地址（多个IP用逗号分隔）`);
        return;
      }
      
      // 检查是否在CMDB网段内
      const ips = ip.split(',').map(i => i.trim()).filter(i => i.length > 0);
      for (const singleIp of ips) {
        if (isInCmdbRange(singleIp)) {
          message.warning(`${addressType} ${singleIp} 不能在CMDB网段内`);
          return;
        }
      }
    };

    // 辅助函数：将内部值映射为显示文本
    const mapToDisplayText = (value: any, type: string) => {
      if (type === 'addressType') {
        switch (value) {
          case 'internalApplicationAddress': return '内部应用地址';
          case 'dbAddress': return '内部数据库地址';
          case 'externalAddress': return '外部地址';
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

    // 检测并标记三字段联合冲突的行
    const detectConflicts = () => {
      conflictingRows.value.clear();
      const comboMap = new Map<string, number[]>();

      formData.policies.forEach((policy, index) => {
        const comboKey = [
          policy.sourceSystem || '',
          policy.destSystem || '',
          policy.port || ''
        ].join('||');
        if (!comboMap.has(comboKey)) {
          comboMap.set(comboKey, []);
        }
        comboMap.get(comboKey)!.push(index);
      });

      const indices = new Set<number>();
      comboMap.forEach(rows => {
        if (rows.length > 1) {
          rows.forEach(row => indices.add(row));
        }
      });

      conflictingRows.value = indices;
    };

    // 监听策略变化，检测冲突
    watch(() => formData.policies, () => {
      detectConflicts();
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

    // 生成XLS文件
    const generateXlsFile = () => {
      const data: any[] = [];
      // 表头
      data.push([
        '序号', '源地址', '源地址、端口说明', '源地址类型', '目的地址', '目的端口1', '至目的端口2', '目的地址、端口说明', '目的地址类型',
        '传输层协议', '是否为长连接', '策略用途及必要性', '安全性', '策略使用期限', '测试峰值流量'
      ]);

      // 数据行
      let rowIndex = 1;
      formData.policies.forEach((policy) => {
        // 处理源地址和目的地址，支持多选（数组）或单选（字符串）
        let sourceAddressGroups: string[][] = [];
        let destAddressGroups: string[][] = [];
        
        // 处理源地址
        if (Array.isArray(policy.sourceAddress)) {
          // 多选情况，分组连续IP
          sourceAddressGroups = groupConsecutiveIPs(policy.sourceAddress);
        } else if (policy.sourceType === 'externalAddress' && policy.sourceAddress) {
          // 外部地址类型，可能是逗号分隔的字符串，需要解析和分组
          const ips = policy.sourceAddress.split(',').map(ip => ip.trim()).filter(ip => ip.length > 0);
          if (ips.length > 0) {
            sourceAddressGroups = groupConsecutiveIPs(ips);
          } else {
            sourceAddressGroups = [[policy.sourceAddress]];
          }
        } else {
          // 单选情况，作为单个组
          sourceAddressGroups = [[policy.sourceAddress || '']];
        }
        
        // 处理目的地址
        if (Array.isArray(policy.destAddress)) {
          // 多选情况，分组连续IP
          destAddressGroups = groupConsecutiveIPs(policy.destAddress);
        } else if (policy.destType === 'externalAddress' && policy.destAddress) {
          // 外部地址类型，可能是逗号分隔的字符串，需要解析和分组
          const ips = policy.destAddress.split(',').map(ip => ip.trim()).filter(ip => ip.length > 0);
          if (ips.length > 0) {
            destAddressGroups = groupConsecutiveIPs(ips);
          } else {
            destAddressGroups = [[policy.destAddress]];
          }
        } else {
          // 单选情况，作为单个组
          destAddressGroups = [[policy.destAddress || '']];
        }

        // 处理端口，最多支持10个端口，用逗号分隔
        let portDisplay = '';
        if (policy.port) {
          const ports = policy.port.split(',').map(p => p.trim()).slice(0, 10); // 最多10个端口
          portDisplay = ports.join(',');
        }

        // 处理策略使用期限
        const policyUsagePeriod = formData.effectiveType === 'immediate'
          ? '立即生效'
          : formData.effectiveDate
            ? `定时生效 (${dayjs(formData.effectiveDate).format('YYYY-MM-DD HH:mm:ss')})`
            : '定时生效 (未选择时间)';

        // 为每个源地址组和目的地址组的组合生成一行
        for (let sourceIndex = 0; sourceIndex < sourceAddressGroups.length; sourceIndex++) {
          for (let destIndex = 0; destIndex < destAddressGroups.length; destIndex++) {
            const sourceGroup = sourceAddressGroups[sourceIndex];
            const destGroup = destAddressGroups[destIndex];
            
            const sourceAddressDisplay = compressIPGroup(sourceGroup);
            const destAddressDisplay = compressIPGroup(destGroup);

            data.push([
              rowIndex++,
              sourceAddressDisplay,
              getSystemDisplayName(policy.sourceType, policy.sourceSystem),
              mapToDisplayText(policy.sourceType, 'addressType'),
              destAddressDisplay,
              portDisplay, // 端口字段，最多10个端口用逗号分隔
              '', // 至目的端口2字段留空
              getSystemDisplayName(policy.destType, policy.destSystem),
              mapToDisplayText(policy.destType, 'addressType'),
              mapToDisplayText(policy.protocol, 'protocol'),
              mapToDisplayText(policy.longConnection, 'longConnection'),
              formData.approvalRemarks,
              '', // 安全性 (图片中为空)
              policyUsagePeriod,
              '' // 测试峰值流量 (图片中为空)
            ]);
          }
        }
      });

      const ws = XLSX.utils.aoa_to_sheet(data); // 将二维数组转换为工作表
      const wb = XLSX.utils.book_new(); // 创建新的工作簿
      XLSX.utils.book_append_sheet(wb, ws, '防火墙策略申请'); // 将工作表添加到工作簿

      // 生成文件名：前端选择的A系统到B系统的防火墙安全策略申请表（XX环境）
      let sourceSystemName = '未知系统';
      let destSystemName = '未知系统';

      if (formData.policies.length > 0) {
        const firstPolicy = formData.policies[0]; // 取第一条策略作为文件名参考
        sourceSystemName = getSystemDisplayName(firstPolicy.sourceType, firstPolicy.sourceSystem);
        destSystemName = getSystemDisplayName(firstPolicy.destType, firstPolicy.destSystem);
      }

      const filename = `${sourceSystemName}到${destSystemName}的防火墙安全策略申请表（${environmentType}环境）.xls`;

      // 写入并下载文件
      const wbout = XLSX.write(wb, { bookType: 'xls', type: 'array' });
      saveAs(new Blob([wbout], { type: 'application/octet-stream' }), filename);
    };

    // 自动生成申请说明
      const generateApplicationDescription = () => {
      const policy = formData.policies[0];
      if (!policy.sourceType || !policy.sourceSystem || !policy.destType || !policy.destSystem) {
        return '';
      }

      const sourceTypeMap = {
        'internalApplicationAddress': '内部应用系统',
        'dbAddress': '内部数据库',
        'externalAddress': '外部系统'
      };

      const destTypeMap = {
        'internalApplicationAddress': '内部应用系统',
        'dbAddress': '内部数据库关联系统',
        'externalAddress': '外部系统'
      };

      const sourceTypeText = sourceTypeMap[policy.sourceType] || '未知类型';
      const destTypeText = destTypeMap[policy.destType] || '未知类型';

      const sourceName = getSystemDisplayName(policy.sourceType, policy.sourceSystem);
      const destName = getSystemDisplayName(policy.destType, policy.destSystem);

      return `关于开通${sourceTypeText}-${sourceName}到${destTypeText}-${destName}的防火墙策略，生效时间为${formData.effectiveType === 'immediate' ? '立即生效' : formData.effectiveDate}`;
    };

    // 监听策略变化，自动更新申请说明
    watch(() => formData.effectiveType, () => {
      formData.approvalRemarks = generateApplicationDescription();
    }, { deep: true });

    // 监听地址类型变化，自动填充系统名称
    watch(() => formData.policies, (newPolicies) => {
      newPolicies.forEach(policy => {
        if (policy.sourceType === 'externalAddress' && policy.sourceSystem !== 'external') {
          policy.sourceSystem = 'external';
        }
        if (policy.destType === 'externalAddress' && policy.destSystem !== 'external') {
          policy.destSystem = 'external';
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

    const submitApplication = () => {
      // 先检测冲突
      detectConflicts();
      
      // 如果有冲突，不允许提交
      if (conflictingRows.value.size > 0) {
        message.error('源/目的地址应用系统、端口三者需保证唯一，不允许分隔多行');
        return;
      }

      // 校验必填字段
      const errors: string[] = [];

      // 校验基本信息
      if (!formData.urgencyLevel) {
        errors.push('紧急程度');
      }

      // 校验策略配置
      for (let i = 0; i < formData.policies.length; i++) {
        const policy = formData.policies[i];
        const policyIndex = i + 1;
        
        if (!policy.sourceType) {
          errors.push(`第${policyIndex}行源地址类型`);
        }
        if (!policy.sourceSystem) {
          errors.push(`第${policyIndex}行源地址应用系统`);
        }
        if (!policy.sourceAddress || (Array.isArray(policy.sourceAddress) && policy.sourceAddress.length === 0)) {
          errors.push(`第${policyIndex}行源地址`);
        }
        if (!policy.destType) {
          errors.push(`第${policyIndex}行目的地址类型`);
        }
        if (!policy.destSystem) {
          errors.push(`第${policyIndex}行目的地址应用系统`);
        }
        if (!policy.destAddress || (Array.isArray(policy.destAddress) && policy.destAddress.length === 0)) {
          errors.push(`第${policyIndex}行目的地址`);
        }
        if (!policy.port) {
          errors.push(`第${policyIndex}行端口`);
        }
        if (!policy.protocol) {
          errors.push(`第${policyIndex}行协议`);
        }
      }

      // 校验生效时间
      if (formData.effectiveType === 'scheduled' && !formData.effectiveDate) {
        errors.push('生效时间');
      }

      // 校验申请说明
      if (!formData.approvalRemarks.trim()) {
        errors.push('申请说明');
      }

      // 如果有必填字段未填写，显示错误信息
      if (errors.length > 0) {
        message.error(`请填写以下必填字段：${errors.join('、')}`);
        return;
      }

      // 校验端口格式
      for (const policy of formData.policies) {
        if (policy.port && !validatePort(policy.port)) {
          message.warning('端口号必须在1-65535范围内');
          return;
        }

        // 校验外部地址IP格式和网段
        if (policy.sourceType === 'externalAddress' && policy.sourceAddress) {
          // 校验IP格式（支持逗号分隔的多个IP）
          if (!validateIpFormat(policy.sourceAddress)) {
            message.warning('源地址IP格式不正确，请输入正确的IP地址（多个IP用逗号分隔）');
            return;
          }
          // 检查是否在CMDB网段内（每个IP都要检查）
          const sourceIps = policy.sourceAddress.split(',').map(ip => ip.trim());
          for (const ip of sourceIps) {
            if (isInCmdbRange(ip)) {
              message.warning(`源地址 ${ip} 不能在CMDB网段内`);
              return;
            }
          }
        }

        if (policy.destType === 'externalAddress' && policy.destAddress) {
          // 校验IP格式（支持逗号分隔的多个IP）
          if (!validateIpFormat(policy.destAddress)) {
            message.warning('目的地址IP格式不正确，请输入正确的IP地址（多个IP用逗号分隔）');
            return;
          }
          // 检查是否在CMDB网段内（每个IP都要检查）
          const destIps = policy.destAddress.split(',').map(ip => ip.trim());
          for (const ip of destIps) {
            if (isInCmdbRange(ip)) {
              message.warning(`目的地址 ${ip} 不能在CMDB网段内`);
              return;
            }
          }
        }
      }

      // 所有校验通过，生成并下载 XLS 文件
      generateXlsFile();

      message.success('申请已提交，文件已生成并下载。');
      console.log('提交申请:', formData);
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
        const data = await defHttp.get({
          url: '/sys/home/getTicketById',
          params: { id: ticketId }
        });
        
        if (data) {
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
          
          // 触发冲突检测
          detectConflicts();
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
      submitApplication,
      saveDraft,
      resetForm,
      getSystemOptions,
      getAddressOptions,
      fetchCmdbSystemIps,
      validatePort,
      validateExternalIp,
      loading,
      conflictingRows,
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
      grid-template-columns: 20px 130px 200px 150px 130px 200px 150px 70px 70px 50px 50px;
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
        grid-template-columns: 20px 130px 200px 150px 130px 200px 150px 70px 70px 50px 50px;
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

  // 冲突行样式
  .row-conflict {
    background-color: #ffe6e6 !important;
  }

  .table-row.row-conflict {
    background-color: #ffe6e6 !important;
    
    .table-cell {
      background-color: #ffe6e6 !important;
    }
  }
}
</style>