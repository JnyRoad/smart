<template>
    <div>
      <!-- Embed the DataEase panel using an iframe -->
      <iframe
        :src="panelUrl"
        width="100%"
        height="100%"
        frameborder="0"
      ></iframe>
    </div>
  </template>

  <script>
  export default {
    computed: {
      panelUrl() {
        // Get the panel name from the route parameter
        // const panelName = this.$route.params.panelName;
        // Replace with your DataEase base URL
        const dataEaseBaseUrl = window.location.origin+'/link/qxlu57rV';
        return `${dataEaseBaseUrl}`;
      },
    },
  };
  </script>