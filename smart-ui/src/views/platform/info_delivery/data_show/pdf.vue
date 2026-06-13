<template>
  <div>
    <input v-model.number="page" type="number" style="width: 5em" /> /{{ numPages }}
    <button @click="rotate += 90">&#x27F3;</button>
    <button @click="rotate -= 90">&#x27F2;</button>
    <button @click="$refs.pdf.print()">print</button>
    <div style="width: 50%">
      <div v-if="loadedRatio > 0 && loadedRatio < 1" style="background-color: green; color: white; text-align: center" :style="{ width: loadedRatio * 100 + '%' }">
        {{ Math.floor(loadedRatio * 100) }}%
      </div>
      <pdf
        ref="pdf"
        style="border: 1px solid red;"
        :src="src"
        :page="page"
        :rotate="rotate"
        @progress="loadedRatio = $event"
        @error="error"
        @num-pages="numPages = $event"
        @link-clicked="page = $event"
      ></pdf>
    </div>
  </div>
</template>

<script>
import pdf from 'vue-pdf'
export default {
  components: {
    pdf
  },
  data() {
    return {
      src: `${window.location.origin}/news/file/stream/1502106498012745730`,
      loadedRatio: 0,
      page: 1,
      numPages: 0,
      rotate: 0
    }
  },
  methods: {
    error: function (err) {
    }
  },
  mounted() {},
  created() {}
}
</script>