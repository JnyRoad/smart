const path = require('path')
const devServer = require('./proxy')
const compilerJson = require('./compiler.json')

const conditionalCompilerOptions = {
  isDebug: process.env.NODE_ENV === 'development' // optional, this expression is default
  // DORM: process.env.BUILD_MODULE === 'DORM' // any prop name you want, used for /* IFTRUE_evnTest ...js code... FITRUE_evnTest */
}

compilerJson.modules.forEach(key => {
  if (process.env.BUILD_MODULE) {
    key = key.toLocaleUpperCase()
    conditionalCompilerOptions[key] = process.env.BUILD_MODULE.toLocaleUpperCase() === key
  }
})

const conditionalCompiler = {
  options: {
    ...conditionalCompilerOptions
  }
}

let pages = { // h5入口
  index: {
    entry: 'src/main.js',
    chunks: ['vue-commons', 'vendors', 'index']
  }
}

module.exports = {
  publicPath: process.env.NODE_ENV === 'production' ? '/' : '/',
  css: {
    // extract: true,
    loaderOptions: {
      sass: {
        additionalData: `@use "@/sass/_mixin.scss" as *;`,
        sassOptions: {
          silenceDeprecations: ['legacy-js-api']
        }
      }
    }
  },
  pages: pages,
  configureWebpack: {
    resolve: {
      symlinks: false,
      alias: {
        lrz: path.resolve(__dirname, 'src/util/lrz.all.bundle'),
        '@views-mobile': path.resolve(__dirname, 'src/views-mobile'),
        '@services': path.resolve(__dirname, 'src/services')
      }
    },
    externals: {},
    performance: {
      maxAssetSize: 1024 * 1024,
      maxEntrypointSize: 1024 * 1024
    },
    optimization: {
      splitChunks: {
        maxInitialRequests: 1,
        cacheGroups: {
          vueCommons: {
            test: (module) => {
              return /[\\/]vue|vuex|vue-router|axios/.test(module.context)
            },
            name: 'vue-commons',
            chunks: 'initial',
            maxInitialRequests: 5
          },
          vendors: {
            name: 'vendors',
            chunks: 'initial'
          }
        }
      }
    }
  },
  chainWebpack: config => {
    config.module
      .rule('vue')
      .use('cache-loader')
      .tap((options) => {
        options.cacheIdentifier = `${new Date().valueOf()}`
        return options
      })
      .end()
    config.module
      .rule('vue')
      .use('js-conditional-compile-loader')
      .loader('js-conditional-compile-loader')
      .tap(() => {
        return conditionalCompiler.options
      })
      .end()
    config.module
      .rule('js')
      .use('cache-loader')
      .tap((options) => {
        options.cacheIdentifier = `${new Date().valueOf()}`
        return options
      })
      .end()
    config.module
      .rule('js')
      .use('js-conditional-compile-loader')
      .loader('js-conditional-compile-loader')
      .tap(() => {
        return conditionalCompiler.options
      })
      .end()

    if (
      process.env.NODE_ENV === 'production' &&
      config.optimization.minimizers &&
      config.optimization.minimizers.has('terser')
    ) {
      const terserMinimizer = config.optimization.minimizer('terser')
      if (terserMinimizer && typeof terserMinimizer.tap === 'function') {
        terserMinimizer.tap(args => {
          const options = args[0] || {}
          options.terserOptions = options.terserOptions || {}
          options.terserOptions.compress = {
            ...options.terserOptions.compress,
            drop_console: true,
            drop_debugger: true,
            pure_funcs: ['console.log', 'console.info', 'console.debug', 'console.warn']
          }
          args[0] = options
          return args
        })
      }
    }
  },
  lintOnSave: true,
  productionSourceMap: false,
  // 配置转发代理
  devServer: devServer
}
