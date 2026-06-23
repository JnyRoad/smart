// 从 util.js 拆出的日期格式化工具。
// 注意：另有一个 ./date.js 内含【不同实现】的 dateFormat，二者刻意分开、不可合并。
// 由 util.js re-export，保证 @/util/util 对外路径与公共面不变。

export function dateFormat(date = new Date(), fmt = 'yyyy-MM-dd') {
  var tokenMap = {
    'M+': date.getMonth() + 1, // 月份
    'd+': date.getDate(), // 日
    'h+': date.getHours(), // 小时
    'm+': date.getMinutes(), // 分
    's+': date.getSeconds(), // 秒
    'q+': Math.floor((date.getMonth() + 3) / 3), // 季度
    // 'S' 是毫秒的格式化 token，须与用户传入的 fmt 串匹配，不能改名
    // eslint-disable-next-line id-length
    S: date.getMilliseconds() // 毫秒
  }
  if (/(y+)/.test(fmt)) {
    fmt = fmt.replace(RegExp.$1, (date.getFullYear() + '').substr(4 - RegExp.$1.length))
  }
  for (var token in tokenMap) {
    if (new RegExp('(' + token + ')').test(fmt)) {
      fmt = fmt.replace(RegExp.$1, (RegExp.$1.length === 1) ? (tokenMap[token]) : (('00' + tokenMap[token]).substr(('' + tokenMap[token]).length)))
    }
  }
  return fmt
}

// 获取当前月份（yyyy-MM）
export function getDateMonth(){
  const date = new Date();
  let year = date.getFullYear();
  let month = date.getMonth() + 1;
  month = month > 9 ? month : '0' + month;
  return `${year}-${month}`;
}

// 获取上月月份（yyyy-MM）
export function getDatePreMonth(){
  const date = new Date();
  let year = date.getFullYear();
  let month = date.getMonth() + 1;
  if(month==1){
    year = date.getFullYear() - 1;
    month = 12;
  }else{
    month = month -1;
  }
  month = month > 9 ? month : '0' + month;
  return `${year}-${month}`;
}

// 获取今日（yyyy-MM-dd）
export function getDatePreDay(){
  const date = new Date();
  let year = date.getFullYear();
  let month = date.getMonth() + 1;
  let day = date.getDate();
  month = month > 9 ? month : '0' + month;
  return `${year}-${month}-${day}`;
}
