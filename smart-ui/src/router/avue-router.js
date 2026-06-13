import PlatformRouter from './platform/'

const loadPage = () => import(/* webpackChunkName: "layout-main" */ '../page/index')
const loadPageLayout = () => import(/* webpackChunkName: "layout-nested" */ '../page/index/layout')
const loadMenuComponent = component => () => import(
  /* webpackChunkName: "view-[request]" */
  /* webpackMode: "lazy" */
  `../${component}.vue`
)

let RouterPlugin = function () {
  this.$router = null
  this.$store = null
}
RouterPlugin.install = function (router, store) {
  this.$router = router
  this.$store = store

  function objToform (obj) {
    let result = []
    Object.keys(obj).forEach(ele => {
      result.push(`${ele}=${obj[ele]}`)
    })
    return result.join('&')
  }
  this.$router.$avueRouter = {
    // 全局配置
    $website: this.$store.getters.website,
    $defaultTitle: '裕同科技',
    routerList: [],
    group: '',
    safe: this,
    // 设置标题
    setTitle: function (title) {
      title = title ? `${title}-${this.$defaultTitle}` : this.$defaultTitle;
      document.title = title
    },
    closeTag: (value) => {
      const tag = value || this.$store.getters.tag
      this.$store.commit('DEL_TAG', tag)
    },
    // 处理路由
    // getPath: function (params) {
    //   let { src } = params
    //   let result = src || '/'
    //   if (src.includes('http') || src.includes('https')) {
    //     result = `/myiframe/urlPath?${objToform(params)}`
    //   }
    //   return result
    // },
    // 处理路由
    getPath: function (params) {
      let { src } = params
      let result = src || '/'
      return result
    },
    // 正则处理路由
    vaildPath: function (list, path) {
      let result = false
      list.forEach(ele => {
        if (new RegExp('^' + ele + '.*', 'g').test(path)) {
          result = true
        }
      })
      return result
    },
    // 设置路由值
    getValue: function (route) {
      let value = ''
      if (route.query.src) {
        value = route.query.src
      } else {
        value = route.path
      }
      return value
    },
    // 动态路由
    formatRoutes: function (aMenu = [], first) {
      const aRouter = []
      const propsConfig = this.$website.menu.props
      const propsDefault = {
        label: propsConfig.label || 'label',
        path: propsConfig.path || 'path',
        icon: propsConfig.icon || 'icon',
        children: propsConfig.children || 'children',
        meta: propsConfig.meta || 'meta'
      }
      if (aMenu.length === 0) return
      for (let i = 0; i < aMenu.length; i++) {
        const oMenu = aMenu[i]
        if (this.routerList.includes(oMenu[propsDefault.path])) return
        const path = (() => {
          if (first) {
            if (oMenu[propsDefault.path].includes('http') || oMenu[propsDefault.path].includes('https')) {
              return oMenu[propsDefault.path];
            }else{
              return oMenu[propsDefault.path].replace('/index', '')
            }
          } else {
            return oMenu[propsDefault.path]
          }
        })()

        const component = oMenu.component

        const name = oMenu[propsDefault.label]

        const icon = oMenu[propsDefault.icon]

        const children = oMenu[propsDefault.children]

        const meta = {
          keepAlive: Number(oMenu['keepAlive']) === 0
        }
        const isChild = children.length !== 0
        const oRouter = {
          path: path,
          component: first ? loadPage : (isChild ? loadPageLayout : loadMenuComponent(component)),
          name: name,
          icon: icon,
          meta: meta,
          redirect: (() => {
            if (!isChild && first)
            {
              if (`${path}`.includes('http') || `${path}`.includes('https')) {
                return `${path}`
              } else return `${path}/index`
            } else return ''
          })(),
          // 处理是否为一级路由
          children: !isChild ? (() => {
            if (first) {
              if (oMenu[propsDefault.path].includes('http') || oMenu[propsDefault.path].includes('https')) {
                oMenu[propsDefault.path] = `${path}`
              } else  oMenu[propsDefault.path] = `${path}/index`
              return [{
                component: loadMenuComponent(component),
                icon: icon,
                name: name,
                meta: meta,
                path: 'index'
              }]
            }
            return []
          })() : (() => {
            return this.formatRoutes(children, false)
          })()
        }
        aRouter.push(oRouter)
      }
      if (first) {
        if (!this.routerList.includes(aRouter[0][propsDefault.path])) {
          this.safe.$router.addRoutes(aRouter)

          //自定义路由（非菜单直接路由），要写在添加菜单路由的后面，不然首次加载点击左侧菜单就会重定向到子页面
          this.safe.$router.addRoutes(PlatformRouter);

          this.routerList.push(aRouter[0][propsDefault.path])
        }
      } else {
        return aRouter
      }
    }
  }
}
export default RouterPlugin
