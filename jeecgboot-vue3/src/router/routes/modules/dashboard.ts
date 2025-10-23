import type { AppRouteModule } from '/@/router/types';
import { PageEnum } from '/@/enums/pageEnum';
import { LAYOUT } from '/@/router/constant';
import { t } from '/@/hooks/web/useI18n';

const dashboard: AppRouteModule = {
  path: '/dashboard',
  name: 'Dashboard',
  component: LAYOUT,
  redirect: PageEnum.BASE_HOME,
  meta: {
    orderNo: 10,
    icon: 'ion:grid-outline',
    title: t('routes.dashboard.dashboard'),
  },
  children: [
    {
      path: 'analysis',
      name: 'Analysis',
      component: () => import('/@/views/dashboard/Analysis/index.vue'),
      meta: {
        // affix: true,
        title: t('routes.dashboard.analysis'),
      },
    },
    {
      path: 'itsm',
      name: 'itsm',
      component: () => import('/@/views/dashboard/itsm/index.vue'),
      meta: {
        title: t('routes.dashboard.itsm'),
      },
    },
    {
      path: 'firewall',
      name: 'firewall',
      component: () => import('/@/views/dashboard/firewall/index.vue'),
      meta: {
        title: '防火墙策略开通申请',
      },
    },
    {
      path: 'workbench',
      name: 'Workbench',
      component: () => import('/@/views/dashboard/workbench/index.vue'),
      meta: {
        title: t('routes.dashboard.workbench'),
      },
    },
  ],
};

export default dashboard;
