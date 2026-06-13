module.exports = {
  root: true,
  env: {
    node: true
  },
  'extends': [
    '@vue/standard',
    'plugin:vue/essential'
  ],
  rules: {
    'no-console': process.env.NODE_ENV === 'production' ? 'error' : 'off', // console.log()
    'no-debugger': process.env.NODE_ENV === 'production' ? 'error' : 'off', // debugger
    'no-template-curly-in-string': 'off', // 禁止使用常规字符串中的模板文字占位符语法 '${test}'
    'space-before-function-paren': 'off', // 在函数括号之前需要或不允许空格 function () {}  function(){}
    'no-floating-decimal': 'off' // 禁止浮动小数（非浮点小数）.5 0.5
  },
  parserOptions: {
    parser: 'babel-eslint'
  },
  globals: {
    tce: true
  }
}
