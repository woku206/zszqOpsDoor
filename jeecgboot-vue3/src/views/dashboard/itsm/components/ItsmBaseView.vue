<!--知识库添加页面-->
<template>
  <div class="knowledge">
  
    <a-row :span="24" class="knowledge-row">
      <a-col :xxl="4" :xl="6" :lg="6" :md="6" :sm="12" :xs="24">
        <a-card class="add-knowledge-card" @click="handleProductionFirewall">
          <div class="flex">
            <Icon icon="ant-design:plus-outlined" class="add-knowledge-card-icon" size="20"></Icon>
            <span class="add-knowledge-card-title">防火墙策略开通申请</span>
          </div>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script lang="ts">
import { defineComponent,reactive, ref } from 'vue';
import { useModal } from '/@/components/Modal';
import { Pagination } from 'ant-design-vue';  
import JInput from '@/components/Form/src/jeecg/components/JInput.vue';
import JSelectUser from '@/components/Form/src/jeecg/components/JSelectUser.vue';
import JDictSelectTag from '@/components/Form/src/jeecg/components/JDictSelectTag.vue';
import Icon from '@/components/Icon';
import { useMessage } from "@/hooks/web/useMessage";
import { useRouter } from 'vue-router';

  export default defineComponent({
    components: {
      Icon,
      JDictSelectTag,
      JSelectUser,
      JInput,
      Pagination,
    },
    setup() {
      //模型列表
      const knowledgeList = ref<any[]>([]);

      //注册modal
      const [registerModal, { openModal }] = useModal();
      const [docListRegister, { openModal: openDocModal }] = useModal();

      // 路由
      const router = useRouter();

      //当前页数
      const pageNo = ref<number>(1);
      //每页条数
      const pageSize = ref<number>(10);
      //总条数
      const total = ref<number>(0);
      //可选择的页数
      const pageSizeOptions = ref<any>(['10', '20', '30']);
      //查询参数
      const queryParam = reactive<any>({});
      //查询区域label宽度
      const labelCol = reactive({
        xs: 24,
        sm: 4,
        xl: 6,
        xxl: 6,
      });
      //查询区域组件宽度
      const wrapperCol = reactive({
        xs: 24,
        sm: 20,
      });
      //查询区域表单的ref
      const formRef = ref();
      const { createMessage } = useMessage();


      /**
       * 生产防火墙策略开通申请
       */
      function handleProductionFirewall() {
        // 使用路由跳转到防火墙策略申请页面
        router.push('/dashboard/firewall');
      }

      /**
       * 新增
       */
      async function handleAddKnowled() {
        openModal(true, {});
      }

      /**
       * 编辑
       *
       * @param item
       */
      function handleEditClick(item) {
        console.log(item);
        openModal(true, {
          id: item.id,
          isUpdate: true,
        });
      }

      /**
       * 参数配置点击事件
       *
       * @param id
       */
      function handleDocClick(id) {
        openDocModal(true, { id });
      }

      return {
        router,
        handleProductionFirewall,
        handleAddKnowled,
        handleEditClick,
        registerModal,
        knowledgeList,
        pageNo,
        pageSize,
        pageSizeOptions,
        total,
        queryParam,
        labelCol,
        wrapperCol,
        formRef,
        handleDocClick,
        docListRegister,
      };
    },
  });
</script>

<style scoped lang="less">
  .knowledge {
    height: calc(100vh - 115px);
    background: #f7f8fc;
    padding: 24px;
    overflow-y: auto;

    .knowledge-row {
      max-height: calc(100% - 100px);
      margin-top: 20px;
      overflow-y: auto;
      .knowledge-header {
        position: relative;
        font-size: 14px;
        height: 40px;
        .header-img {
          width: 34px;
          height: 34px;
          margin-right: 6px;
        }
        .header-text {
          width: calc(100% - 80px);
          overflow: hidden;
          position: relative;
          display: grid;
          .header-name {
            font-size: 14px !important;
            font-weight: bold;
            color: #354052 !important;
          }
          .header-text-top {
            height: 22px;
            font-size: 12px;
          }
        }
      }
    }
  }

  .text-desc {
    width: 100%;
    font-weight: 400;
    display: inline-block;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    height: 40px;
    overflow: hidden;
    font-size: 12px;
    color: #676f83;
  }
  
  .knowledge-footer{
    font-size: 12px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    margin-top: 16px;
    .knowledge-footer-icon{
      position: relative;
      top: 2px
    }
    span{
      margin-left: 2px;
    }
  }

  .flex {
    display: flex;
    align-items: center;
  }

  :deep(.ant-card .ant-card-body) {
    padding: 16px;
  }

  .mt-10 {
    margin-top: 10px;
  }

  .ml-4 {
    margin-left: 4px;
  }

  .knowledge-btn {
    position: absolute;
    right: 4px;
    top: 6px;
    height: auto;
    display: none;
  }
  .add-knowledge-card {
    margin-bottom: 20px;
    background: #fcfcfd;
    border: 1px solid #f0f0f0;
    box-shadow: 0 2px 4px #e6e6e6;
    transition: all 0.3s ease;
    border-radius: 10px;
    display: inline-flex;
    justify-content: center;
    align-items: center;
    font-size: 16px;
    cursor: pointer;
    height: 152px;
    width: calc(100% - 20px);
    .add-knowledge-card-icon {
      padding: 8px;
      color: #1f2329;
      background-color: #f5f6f7;
      margin-right: 12px;
    }
    .add-knowledge-card-title {
      font-size: 16px;
      color:#1f2329;
      font-weight: 400;
      align-self: center;
    }
  }

  .add-knowledge-card:hover {
    box-shadow: 0 6px 12px #d0d3d8;
  }

  .knowledge-card {
    margin-right: 20px;
    margin-bottom: 20px;
    height: 152px;
    border-radius: 10px;
    background: #fcfcfd;
    border: 1px solid #f0f0f0;
    box-shadow: 0 2px 4px #e6e6e6;
    transition: all 0.3s ease;
  }
  .knowledge-card:hover {
    box-shadow: 0 6px 12px #d0d3d8;
    .knowledge-btn {
      display: block;
    }
  }
  .pointer {
    cursor: pointer;
  }
  .list-footer {
    text-align: right;
    margin-top: 5px;
  }
  .jeecg-basic-table-form-container {
    padding: 0;
    :deep(.ant-form) {
      background-color: transparent;
    }
    .table-page-search-submitButtons {
      display: block;
      margin-bottom: 24px;
      white-space: nowrap;
    }
  }
  
  .model-icon{
    background-color: unset;
    border: none;
    margin-right: 2px;
  }
  .model-icon:hover{
    color: #000000;
    background-color: #e9ecf2;
    border: none;
  }
  .ant-dropdown-link{
    font-size: 14px;
    height: 24px;
    padding: 0 7px;
    border-radius: 4px;
    align-content: center;
    text-align: center;
  }
  
  .ellipsis{
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
</style>
