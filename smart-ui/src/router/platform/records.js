import Layout from '@/page/index/'

export const recordsIscCardImportIndexRoute = {
    path: '/platform/records/isc_card_import',
    component: Layout,
    redirect: '/platform/records/isc_card_import/index',
    children: [{
      path: 'index',
      component: () =>
        import ('@/views/platform/records/isc_card_import/index')
    }]
  }

export const recordsIscAccessCleanupIndexRoute = {
    path: '/platform/records/isc_access_cleanup',
    component: Layout,
    redirect: '/platform/records/isc_access_cleanup/index',
    children: [{
      path: 'index',
      component: () =>
        import ('@/views/platform/records/isc_access_cleanup/index')
    }]
  }
