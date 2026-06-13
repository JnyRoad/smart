# 表单补充介绍

## 编写补充表单请参考demo.vue

## 单个组件遵守./mixin.js

* data.formData 为当前表单组件唯一数据
* methods.verification 如需有自定义检验规则,请提供唯一数据校验的方法 返回 Promise
* required 校验规则为:'',undefined,null,[],{}
* methods.verifyFail(info) 数据校验失败时,可以调用以展示失败提示