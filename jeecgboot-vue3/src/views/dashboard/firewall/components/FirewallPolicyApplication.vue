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
          <a-input v-model:value="formData.applicant" placeholder="请输入申请人" />
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
          <div class="header-cell">源地址类型</div>
          <div class="header-cell">源地址应用系统</div>
          <div class="header-cell">源地址</div>
          <div class="header-cell">目的地址类型</div>
          <div class="header-cell">目的地址应用系统</div>
          <div class="header-cell">目的地址</div>
          <div class="header-cell">端口</div>
          <div class="header-cell">协议</div>
          <div class="header-cell">长连接</div>
          <div class="header-cell">操作</div>
        </div>
        <div class="table-body">
          <div v-for="(policy, index) in formData.policies" :key="index" class="table-row">
            <div class="table-cell">{{ index + 1 }}</div>
            <div class="table-cell">
              <a-select v-model:value="policy.sourceType" placeholder="请选择" style="width: 100%">
                <a-select-option value="internalApplicationAddress">内部应用地址</a-select-option>
                <a-select-option value="dbAddress">内部数据库地址</a-select-option>
                <a-select-option value="externalAddress">外部地址</a-select-option>
              </a-select>
            </div>
            <div class="table-cell">
              <a-select v-model:value="policy.sourceSystem" placeholder="请选择" style="width: 100%">
                <a-select-option value="finance">财务</a-select-option>
                <a-select-option value="hr">人力资源</a-select-option>
                <a-select-option value="oa">OA</a-select-option>
              </a-select>
            </div>
            <div class="table-cell">
              <div class="address-input">
                <a-segmented v-model:value="policy.sourceAddress" :options="[{label: 'IPv4', value: 'ipv4'}, {label: 'IPv6', value: 'ipv6'}]" />
                <a-input v-model:value="policy.sourceAddress" placeholder="请输入源地址" />
              </div>
            </div>
            <div class="table-cell">
              <a-select v-model:value="policy.destType" placeholder="请选择" style="width: 100%">
                <a-select-option value="internalApplicationAddress">内部应用地址</a-select-option>
                <a-select-option value="dbAddress">内部数据库地址</a-select-option>
                <a-select-option value="externalAddress">外部地址</a-select-option>
              </a-select>
            </div>
            <div class="table-cell">
              <a-select v-model:value="policy.destSystem" placeholder="请选择" style="width: 100%">
                <a-select-option value="finance">财务</a-select-option>
                <a-select-option value="hr">人力资源</a-select-option>
                <a-select-option value="oa">OA</a-select-option>
              </a-select>
            </div>
            <div class="table-cell">
              <div class="address-input">
                <a-segmented v-model:value="policy.destAddress" :options="[{label: 'IPv4', value: 'ipv4'}, {label: 'IPv6', value: 'ipv6'}]" />
                <a-input v-model:value="policy.destAddress" placeholder="请输入目的地址" />
              </div>
            </div>
            <!-- <div class="table-cell">
              <a-select v-model:value="policy.destSystem" placeholder="请选择" style="width: 100%">
                <a-select-option value="email">邮件</a-select-option>
                <a-select-option value="database">数据库</a-select-option>
                <a-select-option value="web">Web服务</a-select-option>
              </a-select>
            </div> -->
            <div class="table-cell">
              <a-input v-model:value="policy.port" placeholder="如: 80,443" />
            </div>
            <div class="table-cell">
              <a-select v-model:value="policy.protocol" placeholder="请选择" style="width: 100%">
                <a-select-option value="tcp">TCP</a-select-option>
                <a-select-option value="udp">UDP</a-select-option>
                <a-select-option value="icmp">ICMP</a-select-option>
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
          <label class="form-label">生效时间</label>
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
import { defineComponent, reactive } from 'vue';
import { message } from 'ant-design-vue';
import Icon from '@/components/Icon';
import dayjs from 'dayjs';

export default defineComponent({
  name: 'FirewallPolicyApplication',
  components: {
    Icon,
  },
  setup() {
    const formData = reactive({
      applicant: '张安全',
      applicationDate: dayjs(),
      urgencyLevel: 'medium',
      policies: [
        {
          sourceType: 'internalApplicationAddress',
          sourceAddress: '192.168.1.1/24',
          sourceSystem: 'finance',
          destType: 'internalApplicationAddress',
          destAddress: '172.16.2.10',
          destSystem: 'finance',
          port: '80,443',
          protocol: 'tcp',
          longConnection: true,
        }
      ],
      effectiveType: 'immediate',
      effectiveDate: null,
      approvalRemarks: '',
    });

    const addPolicy = () => {
      formData.policies.push({
        sourceType: 'ipv4',
        sourceAddress: '',
        sourceSystem: '',
        destType: 'ipv4',
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
      if (!formData.approvalRemarks.trim()) {
        message.warning('请填写审批备注');
        return;
      }
      message.success('申请提交成功');
      console.log('提交申请:', formData);
    };

    const saveDraft = () => {
      message.success('草稿保存成功');
      console.log('保存草稿:', formData);
    };

    const resetForm = () => {
      Object.assign(formData, {
        applicant: '张安全',
        applicationDate: dayjs(),
        urgencyLevel: 'medium',
        policies: [{
          sourceType: 'ipv4',
          sourceAddress: '192.168.1.1/24',
          sourceSystem: 'finance',
          destType: 'ipv4',
          destAddress: '172.16.2.10',
          destSystem: 'email',
          port: '80,443',
          protocol: 'tcp',
          longConnection: true,
        }],
        effectiveType: 'immediate',
        effectiveDate: null,
        approvalRemarks: '',
      });
      message.info('表单已重置');
    };

    return {
      formData,
      addPolicy,
      removePolicy,
      submitApplication,
      saveDraft,
      resetForm,
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
      grid-template-columns: 20px 130px 150px 1fr 130px 150px 1fr 70px 70px 50px 50px;
      gap: 10px;
      margin-bottom: 16px;
      
      .header-cell {
        color: #333333;
        font-size: 14px;
        font-weight: 600;
        padding: 4px 0;
      }
    }
    
    .table-body {
      .table-row {
        display: grid;
        grid-template-columns: 20px 130px 150px 1fr 130px 150px 1fr 70px 70px 50px 50px;
        gap: 10px;
        margin-bottom: 16px;
        align-items: center;
        
        .table-cell {
          .address-input {
            // display: flex;
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
}
</style>