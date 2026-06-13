// const url = 'http://192.168.8.236:9999' // 代理url

// //二期测试
// const platformUrl = 'http://192.168.8.113:6030';
// const appUrl = 'http://192.168.8.113:6020';
// const url = 'http://10.1.20.110:8090';
const url = 'https://xuchang.szyuto.com'

// 配置转发代理
const devServer = {
  port: '6062',
  compress: true,
  disableHostCheck: true,
  open: 'Chrome',
  headers: {
    'Service-Worker-Allowed': '/'
  },
  proxy: {
    // '/platform': {
    //   target: platformUrl,
    //   ws: true,
    //   pathRewrite: {
    //     '^/platform': '/'
    //   }
    // },
    // '/app': {
    //   target: appUrl,
    //   ws: true,
    //   pathRewrite: {
    //     '^/app': '/'
    //   }
    // },
    '/': {
      target: url,
      ws: true,
      pathRewrite: {
        '^/': '/'
      }
    }
  }
}

module.exports = devServer
