const formatNumber = function (n) {
  const str = n.toString()
  return str[1] ? str : `0${str}`
}

const formatMonth = function (date) {
  console.log(date);
  let year = date.getFullYear()
  let month = date.getMonth() + 1
  let day = date.getDate()
  const isLeapYear = year % 4 === 0 && (year % 100 !== 0 || year % 400 === 0)
  const curMonthDay = fliterDate(isLeapYear, month)
  if (day > curMonthDay) {
    month++
    if (month > 12) {
      year++
    }
  }
  return year + '-' + month
}

const fliterDate = function (isLeapYear, mouth) {
  let monthDay = 0
  switch (mouth) {
    case 1:
    case 3:
    case 5:
    case 7:
    case 8:
    case 10:
    case 12:
      monthDay = 31
      break
    case 4:
    case 6:
    case 9:
    case 11:
      monthDay = 30
      break
    case 2:
      isLeapYear ? (monthDay = 29) : (monthDay = 28)
      break
  }
  return monthDay
}

const formatTel = function (tel) {
  const str = tel.substring(0, 3) + '****' + tel.substring(7)
  return str
}

const regTel = function (tel) {
  const reg = /^1[345678]\d{9}$/
  return reg.test(tel)
}

const regIdNum = function (idNum) {
  const reg = /^[1-9]\d{5}(18|19|([23]\d))\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$/
  return reg.test(idNum)
}



const formatDate = function (date) {
  const year = date.getFullYear()
  const month = date.getMonth() + 1
  const day = date.getDate()
  const t1 = [year, month, day].map(formatNumber).join('-')
  return `${t1}`
}

const formatNextTime = function (date) {
  const time = formatMonth(date)
  const t1 = [time.year, time.month, time.day].map(formatNumber).join('/')
  return `${t1} 00:00:00`
}

const formatHMS = function (date) {
  const time = new Date(date)
  let h = Math.floor(time / 3600000)
  h = h > 9 ? h : `0${h}`
  let m = Math.floor((time / 60000) % 60)
  m = m > 9 ? m : `0${m}`
  let s = Math.floor((time / 1000) % 60)
  s = s > 9 ? s : `0${m}`
  return { h, m, s }
}
const formatTime = function (date) {

  var h = date.getHours()
  var minute = date.getMinutes()
  minute = minute < 10 ? ('0' + minute) : minute
  return h + ':' + minute
}
export {
  formatTel,
  formatDate,
  regTel,
  formatMonth,
  formatNextTime,
  formatHMS,
  regIdNum,
  formatTime
}
