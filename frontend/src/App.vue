<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup lang="ts">
// 应用根组件
// 主要功能：路由视图容器和全局初始化
</script>

<style lang="css">
#app {
  height: 100vh;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB',
    'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  height: 100%;
  overflow: hidden;
}

.el-message-box {
  max-width: 420px;
}
</style>