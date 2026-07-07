const formatNumber = function (n) {
  const str = n.toString()
  return str[1] ? str : `0${str}`
}

function formatTime(time) {
	if (typeof time !== 'number' || time < 0) {
		return time
	}

	var hour = parseInt(time / 3600)
	time = time % 3600
	var minute = parseInt(time / 60)
	time = time % 60
	var second = time

	return ([hour, minute, second]).map(function (n) {
		n = n.toString()
		return n[1] ? n : '0' + n
	}).join(':')
}

function formatLocation(longitude, latitude) {
	if (typeof longitude === 'string' && typeof latitude === 'string') {
		longitude = parseFloat(longitude)
		latitude = parseFloat(latitude)
	}

	longitude = longitude.toFixed(2)
	latitude = latitude.toFixed(2)

	return {
		longitude: longitude.toString().split('.'),
		latitude: latitude.toString().split('.')
	}
}
// 获取记录时间的比对当前的时间显示对应的时间
var dateUtils = {
	UNITS: {
		'年': 31557600000,
		'月': 2629800000,
		'天': 86400000,
		'小时': 3600000,
		'分钟': 60000,
		'秒': 1000
	},
	humanize: function (milliseconds,timeType) {
		var humanize = '';
		for (var key in this.UNITS) {
			if (milliseconds >= this.UNITS[key]) {
				humanize = Math.floor(milliseconds / this.UNITS[key]) + key + '前';
				break;
			}
		}
		return humanize || '刚刚';
	},
	format: function (dateStr,timeType) {
		var date = this.parse(dateStr)
		var diff = Date.now() - date.getTime();
		if (diff < this.UNITS['天']) {
			return this.humanize(diff);
		}
		var _format = function (number) {
			return (number < 10 ? ('0' + number) : number);
		};
		return date.getFullYear() + '/' + _format(date.getMonth() + 1) + '/' + _format(date.getDate()) + '-' +
			_format(date.getHours()) + ':' + _format(date.getMinutes());
	},
	parse: function (str) { //将"yyyy-mm-dd HH:MM:ss"格式的字符串，转化为一个Date对象
		var a = str.split(/[^0-9]/);
		switch (a.length){
			case 3:
				return new Date(a[0], a[1] - 1, a[2]);
				break;
			case 4:
				return new Date(a[0], a[1] - 1, a[2], a[3]);
				break;
			case 5:
				return new Date(a[0], a[1] - 1, a[2], a[3], a[4]);
				break;
			case 6:
				return new Date(a[0], a[1] - 1, a[2], a[3], a[4], a[5]);
				break;			
			default:
				break;
		}
	}
};
// 格式化处里获取年月日
function formatDate(time) {
	const year = time.getFullYear()
	const month = time.getMonth()+1
	const day = time.getDate()
	const _format = function (number) {
		return (number < 10 ? ('0' + number) : number);
	};
	return `${year}-${_format(month)}-${_format(day)}`
}

// 通过获取对应的日期转换成对应的数据
function formatWeek (day) {
	let weekStr = ''
	switch (day){
		case 1:
			weekStr = '周一'
			break;
		case 2:
			weekStr = '周二'
			break;
		case 3:
			weekStr = '周三'
			break;
		case 4:
			weekStr = '周四'
			break;
		case 5:
			weekStr = '周五'
			break;
		case 6:
			weekStr = '周六'
			break;
		case 0:
			weekStr = '周日'
			break;						
		default:
			break;
	}
	return weekStr
}
// 处理当月月份天数
function formatMonth (year,month) {
	let days = 0
	switch (month){
		case 1:
		case 3:
		case 5:
		case 7:
		case 8:
		case 10:
		case 12:
			days = 31
			break;
		case 4:
		case 6:
		case 9:
		case 11:
			days = 30
			break;
		case 2: 
			days = (year % 4 == 0) && (year % 100 != 0 || year % 400 == 0)?28:29
		default:
			break;
	}
	return days
}

// 格式化处里获取年月日 十分秒
const formatDateTime = function (date, type) {
  const year = date.getFullYear()
  const month = date.getMonth() + 1
  const day = date.getDate()
  const hour = date.getHours()
  const minute = date.getMinutes()
  const second = date.getSeconds()

  const t1 = [year, month, day].map(formatNumber).join('/')
  let t2 = ''
  if (type === 'h') {
	 t2 = [hour].map(formatNumber).join(':')
  } else if (type === 'm') {
	  t2 = [hour, minute].map(formatNumber).join(':')
  } else if (type === 's' || type === undefined) {
	  t2 = [hour, minute, second].map(formatNumber).join(':')
  }
  return `${t1} ${t2}`
}

const formatDateTime2 = function (date) {
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
  return {year,month,day}
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
// 将时间转换成app兼容的时间格式 yyyy/mm/dd hh:ii:ss
const flitterTime = function (date) {
	let time = date.split(' ')
	let date1 = time[0].split('-')
	let date2 = ``
	date1.forEach(el=>{
		date2 += `${el}/`
	})
	if (time.length === 2) {
		date2 = `${date2.substring(0,date2.length-1)} ${time[1]}`
	} else if (time.length === 1) {
		date2 = `${date2.substring(0,date2.length-1)}`
	}
	return date2
}

// 验证手机号码 应用于所以手机号码填写
function checkPhone(tel) {
	const reg = /^1[345678]\d{9}$/
	if (!reg.test(tel)) return false
	else return true
}

// 验证车牌号 应用于所有车牌号填写
function checkCarNum(carNum) {
	const xreg= /^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领A-Z]{1}[A-Z]{1}(([0-9]{5}[DF]$)|([DF][A-HJ-NP-Z0-9][0-9]{4}$))/
	const creg = /^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领A-Z]{1}[A-Z]{1}[A-HJ-NP-Z0-9]{4}[A-HJ-NP-Z0-9挂学警港澳]{1}$/
	if (carNum.length == 7) {
		if (!creg.test(carNum)) return false
		else return true
	} else if (carNum.length == 8) {
		if (!xreg.test(carNum)) return false
		else return true
	}
}

// 时间格式化 (未使用)
function dateMonment(format, timestamp){  
    var a, jsdate=((timestamp) ? new Date(timestamp*1000) : new Date()); 
    var pad = function(n, c){ 
        if((n = n + "").length < c){ 
            return new Array(++c - n.length).join("0") + n; 
        } else { 
            return n; 
        } 
    }; 
    var txt_weekdays = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"]; 
    var txt_ordin = {1:"st", 2:"nd", 3:"rd", 21:"st", 22:"nd", 23:"rd", 31:"st"}; 
    var txt_months = ["", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];  
    var f = { 
        // Day 
        d: function(){return pad(f.j(), 2)}, 
        D: function(){return f.l().substr(0,3)}, 
        j: function(){return jsdate.getDate()}, 
        l: function(){return txt_weekdays[f.w()]}, 
        N: function(){return f.w() + 1}, 
        S: function(){return txt_ordin[f.j()] ? txt_ordin[f.j()] : 'th'}, 
        w: function(){return jsdate.getDay()}, 
        z: function(){return (jsdate - new Date(jsdate.getFullYear() + "/1/1")) / 864e5 >> 0}, 
        
        // Week 
        W: function(){ 
            var a = f.z(), b = 364 + f.L() - a; 
            var nd2, nd = (new Date(jsdate.getFullYear() + "/1/1").getDay() || 7) - 1; 
            if(b <= 2 && ((jsdate.getDay() || 7) - 1) <= 2 - b){ 
                return 1; 
            } else{ 
                if(a <= 2 && nd >= 4 && a >= (6 - nd)){ 
                    nd2 = new Date(jsdate.getFullYear() - 1 + "/12/31"); 
                    return date("W", Math.round(nd2.getTime()/1000)); 
                } else{ 
                    return (1 + (nd <= 3 ? ((a + nd) / 7) : (a - (7 - nd)) / 7) >> 0); 
                } 
            } 
        }, 
        
        // Month 
        F: function(){return txt_months[f.n()]}, 
        m: function(){return pad(f.n(), 2)}, 
        M: function(){return f.F().substr(0,3)}, 
        n: function(){return jsdate.getMonth() + 1}, 
        t: function(){ 
            var n; 
            if( (n = jsdate.getMonth() + 1) == 2 ){ 
                return 28 + f.L(); 
            } else{ 
                if( n & 1 && n < 8 || !(n & 1) && n > 7 ){ 
                    return 31; 
                } else{ 
                    return 30; 
                } 
            } 
        }, 
        
        // Year 
        L: function(){var y = f.Y();return (!(y & 3) && (y % 1e2 || !(y % 4e2))) ? 1 : 0}, 
        //o not supported yet 
        Y: function(){return jsdate.getFullYear()}, 
        y: function(){return (jsdate.getFullYear() + "").slice(2)}, 
        
        // Time 
        a: function(){return jsdate.getHours() > 11 ? "pm" : "am"}, 
        A: function(){return f.a().toUpperCase()}, 
        B: function(){ 
            // peter paul koch: 
            var off = (jsdate.getTimezoneOffset() + 60)*60; 
            var theSeconds = (jsdate.getHours() * 3600) + (jsdate.getMinutes() * 60) + jsdate.getSeconds() + off; 
            var beat = Math.floor(theSeconds/86.4); 
            if (beat > 1000) beat -= 1000; 
            if (beat < 0) beat += 1000; 
            if ((String(beat)).length == 1) beat = "00"+beat; 
            if ((String(beat)).length == 2) beat = "0"+beat; 
            return beat; 
        }, 
        g: function(){return jsdate.getHours() % 12 || 12}, 
        G: function(){return jsdate.getHours()}, 
        h: function(){return pad(f.g(), 2)}, 
        H: function(){return pad(jsdate.getHours(), 2)}, 
        i: function(){return pad(jsdate.getMinutes(), 2)}, 
        s: function(){return pad(jsdate.getSeconds(), 2)}, 
        //u not supported yet 
        
        // Timezone 
        //e not supported yet 
        //I not supported yet 
        O: function(){ 
            var t = pad(Math.abs(jsdate.getTimezoneOffset()/60*100), 4); 
            if (jsdate.getTimezoneOffset() > 0) t = "-" + t; else t = "+" + t; 
            return t; 
        }, 
        P: function(){var O = f.O();return (O.substr(0, 3) + ":" + O.substr(3, 2))}, 
        //T not supported yet 
        //Z not supported yet 
        
        // Full Date/Time 
        c: function(){return f.Y() + "-" + f.m() + "-" + f.d() + "T" + f.h() + ":" + f.i() + ":" + f.s() + f.P()}, 
        //r not supported yet 
        U: function(){return Math.round(jsdate.getTime()/1000)} 
    }; 
        
    return format.replace(/[\\]?([a-zA-Z])/g, function(t, s){ 
		let ret = ''
        if( t!=s ){ 
            // escaped 
            ret = s; 
        } else if( f[s] ){ 
            // a date function exists 
            ret = f[s](); 
        } else{ 
            // nothing special 
            ret = s; 
        } 
        return ret; 
    }); 
}

const formatTime2 = function (date) {

  var h = date.getHours()
  var minute = date.getMinutes()
  minute = minute < 10 ? ('0' + minute) : minute
  return h + ':' + minute
}
module.exports = {
	formatTime: formatTime,
	formatTime2 : formatTime2,
	formatLocation: formatLocation,
	dateUtils: dateUtils,
	formatDate: formatDate,
	formatDateTime: formatDateTime,
	checkPhone: checkPhone,
	checkCarNum: checkCarNum,
	dateMonment: dateMonment,
	flitterTime: flitterTime,
	formatWeek: formatWeek,
	formatMonth: formatMonth,
	formatDateTime2 : formatDateTime2
}
