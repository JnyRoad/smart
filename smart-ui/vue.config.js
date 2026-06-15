// Environment URLs are injected by vue-cli from .env.[mode] (VUE_APP_*) BEFORE this
// file is evaluated — see .env.development (test) and .env.production (prod). The
// devServer proxy targets below read them; no hardcoded addresses or comment toggles.
const platformUrl = process.env.VUE_APP_PLATFORM_URL
const url = process.env.VUE_APP_BASE_URL
// Fail fast, but only for commands that actually consume these: `serve` uses the
// proxy targets below and `build` bakes them into the bundle. Other commands (lint,
// inspect) load this file without a mode/.env, so guard on the subcommand to avoid
// throwing there.
const usesBackendUrls = ['serve', 'build'].some(c => process.argv.includes(c))
if (usesBackendUrls && (!platformUrl || !url)) {
  throw new Error('Missing VUE_APP_PLATFORM_URL / VUE_APP_BASE_URL — set them in .env.[mode] (see .env.example)')
}

const TerserPlugin = require('terser-webpack-plugin');

const oneMiB = 1024 * 1024;

module.exports = {
  // 生产构建不通过 eslint-loader 重放遗留 lint 基线，避免构建日志被既有 warning 淹没。
  // 代码质量检查仍保留在独立的 `pnpm lint` 和 staged lint 流程里。
  lintOnSave: process.env.NODE_ENV !== 'production',
  productionSourceMap: false,
  // 配置 CSS 相关选项
  css: {
    loaderOptions: {
      sass: {
        sassOptions: {
          // 静默 legacy JS API 警告
          silenceDeprecations: ['legacy-js-api']
        },
        // 全局引用 variables.scss
        additionalData: `@use "@/styles/variables.scss" as *;`
      },
      scss: {
        sassOptions: {
          // 静默 legacy JS API 警告
          silenceDeprecations: ['legacy-js-api']
        },
        // 全局引用 variables.scss
        additionalData: `@use "@/styles/variables.scss" as *;`
      }
    }
  },
  // 配置静态资源处理
  publicPath: process.env.NODE_ENV === 'production' ? './' : '/',
  chainWebpack: config => {
    // 忽略的打包文件
    config.externals({
      'vue': 'Vue',
      'vue-router': 'VueRouter',
      'vuex': 'Vuex',
      'axios': 'axios',
      'element-ui': 'ELEMENT'
    })

    // 配置别名
    config.resolve.alias
      .set('@', require('path').resolve(__dirname, 'src'))
      .set('xlsx$', require.resolve('xlsx/dist/xlsx.full.min.js'))

    // 移除 resolve-url-loader 配置，避免 source-map 问题
    // 如果需要处理相对路径，建议使用绝对路径或 webpack alias

    const entry = config.entry('app')
    entry
      .add('babel-polyfill')
      .end()
    entry
      .add('classlist-polyfill')
      .end()
  },
  // 配置优化选项
  configureWebpack: config => {
    const optimization = {
      ...config.optimization,
      splitChunks: {
        chunks: 'all',
        maxInitialRequests: 8,
        maxAsyncRequests: 16,
        minSize: 30000,
        cacheGroups: {
          xlsx: {
            name: 'chunk-xlsx',
            test: /[\\/]node_modules[\\/]xlsx[\\/]/,
            chunks: 'all',
            priority: 40,
            enforce: true
          },
          echarts: {
            name: 'chunk-echarts',
            test: /[\\/]node_modules[\\/]echarts[\\/]/,
            chunks: 'all',
            priority: 35,
            enforce: true
          },
          pdf: {
            name: 'chunk-pdf',
            test: /[\\/]node_modules[\\/](vue-pdf|pdfjs-dist)[\\/]/,
            chunks: 'all',
            priority: 34,
            enforce: true
          },
          editors: {
            name: 'chunk-editors',
            test: /[\\/]node_modules[\\/](wangeditor|vue-json-editor|vue-json-tree-view)[\\/]/,
            chunks: 'all',
            priority: 33,
            enforce: true
          },
          mediaTools: {
            name: 'chunk-media-tools',
            test: /[\\/]node_modules[\\/](html2canvas|lrz|exif-js|vue-awesome-swiper|swiper)[\\/]/,
            chunks: 'all',
            priority: 32,
            enforce: true
          },
          vendors: {
            name: 'chunk-vendors',
            test: /[\\/]node_modules[\\/]/,
            chunks: 'initial',
            priority: -10,
            reuseExistingChunk: true
          },
          common: {
            name: 'chunk-common',
            minChunks: 2,
            chunks: 'all',
            priority: -20,
            reuseExistingChunk: true
          }
        }
      }
    }

    // 生产环境下移除 console 语句
    if (process.env.NODE_ENV === 'production') {
      optimization.minimizer = [
        new TerserPlugin({
          terserOptions: {
            compress: {
              // 移除 console
              drop_console: true,
              // 移除 debugger
              drop_debugger: true,
              // 移除无用代码
              pure_funcs: ['console.log', 'console.info', 'console.debug', 'console.warn']
            }
          }
        })
      ]
    }

    config.optimization = optimization
    config.performance = {
      hints: process.env.NODE_ENV === 'production' ? 'warning' : false,
      maxAssetSize: oneMiB,
      maxEntrypointSize: oneMiB
    }
  },
  // 配置转发代理
  devServer: {
    disableHostCheck: true,
    // 配置静态文件服务
    contentBase: require('path').join(__dirname, 'public'),
    contentBasePublicPath: '/',
    proxy: {
      '/platform': {
        target: platformUrl,
        ws: true,
        pathRewrite: {
          // Same host for platform+base -> keep the /platform prefix; if they ever
          // differ (split backends), strip it to root so the target host owns routing.
          '^/platform': platformUrl === url ? "/platform" : "/"
        }
      },
      '/auth': {
        target: url,
        ws: true,
        pathRewrite: {
          '^/auth': '/auth'
        }
      },
      '/admin': {
        target: url,
        ws: true,
        pathRewrite: {
          '^/admin': '/admin'
        }
      },
      '/data': {
        target: url,
        ws: true,
        pathRewrite: {
          '^/data': '/data'
        }
      },
      '/code': {
        target: url,
        ws: true,
        pathRewrite: {
          '^/code': '/code'
        }
      },
      '/algorithm': {
        target: url,
        ws: true,
        pathRewrite: {
          '^/algorithm': '/algorithm'
        }
      },
      '/gen': {
        target: url,
        ws: true,
        pathRewrite: {
          '^/gen': '/gen'
        }
      },
      '/daemon': {
        target: url,
        ws: true,
        pathRewrite: {
          '^/daemon': '/daemon'
        }
      },
      '/tx': {
        target: url,
        ws: true,
        pathRewrite: {
          '^/tx': '/tx'
        }
      },
      '/act': {
        target: url,
        ws: true,
        pathRewrite: {
          '^/act': '/act'
        }
      },
    }
  }
}
