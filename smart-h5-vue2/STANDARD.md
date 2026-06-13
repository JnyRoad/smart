
<!-- TOC -->

- [基本规范](#基本规范)
    - [使用浏览器全局变量时加上 window. 前缀](#使用浏览器全局变量时加上-window-前缀)
    - [始终使用 === 替代 ==](#始终使用--替代-)
    - [不要丢掉异常处理中err参数](#不要丢掉异常处理中err参数)
    - [每个 var 关键字单独声明一个变量](#每个-var-关键字单独声明一个变量)
    - [对于变量和函数名统一使用驼峰命名法](#对于变量和函数名统一使用驼峰命名法)
    - [键值对当中冒号与值之间要留空白](#键值对当中冒号与值之间要留空白)
    - [构造函数要以大写字母开头](#构造函数要以大写字母开头)
    - [避免使用 arguments.callee 和 arguments.caller](#避免使用-argumentscallee-和-argumentscaller)
    - [同一模块有多个导入时一次性写完](#同一模块有多个导入时一次性写完)
    - [~~尽量不要使用 eval()~~ 兼容现有框架](#尽量不要使用-eval-兼容现有框架)
    - [switch 一定要使用 break 来将条件分支正常中断](#switch-一定要使用-break-来将条件分支正常中断)
    - [~~不要省去小数点前面的0~~](#不要省去小数点前面的0)
    - [嵌套的代码块中禁止再定义函数](#嵌套的代码块中禁止再定义函数)
    - [~~正确使用 ES6 中的字符串模板~~ 兼容现有框架](#正确使用-es6-中的字符串模板-兼容现有框架)
    - [用 throw 抛错时，抛出 Error 对象而不是字符串](#用-throw-抛错时抛出-error-对象而不是字符串)
    - [禁止使用 with](#禁止使用-with)
    - [自调用匿名函数 (IIFEs) 使用括号包裹](#自调用匿名函数-iifes-使用括号包裹)
    - [yield * 中的 * 前后都要有空格](#yield--中的--前后都要有空格)
- [额外规范](#额外规范)
    - [UTF-8 编码](#utf-8-编码)
    - [养成 let 和 const 使用习惯](#养成-let-和-const-使用习惯)
    - [异步操作 async/await  promise](#异步操作-asyncawait--promise)
    - [函数参数尽量添加默认值](#函数参数尽量添加默认值)
    - [添加注释](#添加注释)
    - [文件命名](#文件命名)

<!-- /TOC -->

# 基本规范

## 使用浏览器全局变量时加上 window. 前缀

  * document、console 和 navigator 除外。

```js
window.alert('hi') // ok
alert('h1'); // error
```

  ## 除需要转义的情况外，字符串统一使用单引号。

```js
console.log('hello there')
$("<div class='box'>")
```

## 始终使用 === 替代 ==

```js
if (name === 'John')   // ✓ ok
if (name == 'John')    // ✗ avoid

if (name !== 'John')   // ✓ ok
if (name != 'John')    // ✗ avoid
```

## 不要丢掉异常处理中err参数

```js
// ✓ ok
run(function (err) {
  if (err) throw err
  window.alert('done')
})

// ✗ avoid
run(function (err) {
  window.alert('done')
})
```

## 每个 var 关键字单独声明一个变量

```js
// ✓ ok
var silent = true
var verbose = true

// ✗ avoid
var silent = true, verbose = true
```

## 对于变量和函数名统一使用驼峰命名法

```js
function my_function () { }    // ✗ avoid
function myFunction () { }     // ✓ ok

var my_var = 'hello'           // ✗ avoid
var myVar = 'hello'            // ✓ ok
```

## 键值对当中冒号与值之间要留空白

```js
var obj = { 'key' : 'value' }    // ✗ avoid
var obj = { 'key' :'value' }     // ✗ avoid
var obj = { 'key':'value' }      // ✗ avoid
var obj = { 'key': 'value' }     // ✓ ok
```

## 构造函数要以大写字母开头

```js
function animal () {}
var dog = new animal()    // ✗ avoid

function Animal () {}
var dog = new Animal()    // ✓ ok
```

## 避免使用 arguments.callee 和 arguments.caller

## 同一模块有多个导入时一次性写完

```js
import { myFunc1 } from 'module'
import { myFunc2 } from 'module'          // ✗ avoid

import { myFunc1, myFunc2 } from 'module' // ✓ ok
```

## ~~尽量不要使用 eval()~~ 兼容现有框架

* 这个规则已被.eslintrc.js 排除

## switch 一定要使用 break 来将条件分支正常中断

## ~~不要省去小数点前面的0~~

* 这个规则已被.eslintrc.js 排除,原因是css中常规操作被意外修复，很不爽

## 嵌套的代码块中禁止再定义函数

```js
if (authenticated) {
  function setAuthUser () {}    // ✗ avoid
}
```

## ~~正确使用 ES6 中的字符串模板~~ 兼容现有框架

* 这个规则已被.eslintrc.js 排除

## 用 throw 抛错时，抛出 Error 对象而不是字符串

```js
throw 'error'               // ✗ avoid
throw new Error('error')    // ✓ ok
```

## 禁止使用 with

## 自调用匿名函数 (IIFEs) 使用括号包裹

```js
const getName = function () { }()     // ✗ avoid

const getName = (function () { }())   // ✓ ok
const getName = (function () { })()   // ✓ ok

```

## yield * 中的 * 前后都要有空格


# 额外规范

## UTF-8 编码

## 养成 let 和 const 使用习惯

## 异步操作 async/await  promise

## 函数参数尽量添加默认值

```js
const test = function (opton: { p1: '', p2: '' }) { }
```

## 添加注释

* 详情参阅规范文档

```js
/**
 *
 */
const test = function (opton: { p1: '', p2: '' }) { }
```

## 文件命名

* 详情参阅规范文档
