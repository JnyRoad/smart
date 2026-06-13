<template>
  <div :class="isMobile ? 'view-mobile' : 'view-desktop'">
    <router-view />
  </div>
</template>

<script>
export default {
  name: 'viewMobile'
}
</script>

<style lang="scss">
@use '@/sass/tce/tce.scss';
@use '@/sass/common.scss';
html,
body {
  height: 100%;
  overflow-y: auto;
  overflow-x: hidden;
}
.view-mobile {
  position: relative;
  font-size: 14px;
  height: 100%;
  width: 100%;
  min-height: rem(600);
}
.view-desktop {
  position: relative;
  font-size: 14px;
  height: 100%;
  width: 100%;
}
</style>
