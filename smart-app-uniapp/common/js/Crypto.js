// 登录密码加密插件
import Vue from 'vue'
import CryptoJS from 'crypto-js'
export default {//加密
  encrypt(word, keyStr){ 
    keyStr = keyStr ? keyStr : '^KOpI6xf$hPUp%yi';
    var key  = CryptoJS.enc.Latin1.parse(keyStr);//Latin1 w8m31+Yy/Nw6thPsMpO5fg==
    var srcs = CryptoJS.enc.Latin1.parse(word)
		var iv = key;
    var encrypted = CryptoJS.AES.encrypt(srcs, key, {iv: iv, mode:CryptoJS.mode.CBC, padding: CryptoJS.pad.ZeroPadding});
		return encodeURIComponent(encrypted)
		// return 
		// var str = encrypted.toString()
		// var str1 = str.split('=')[0]
		// var temStr = encrypted.toString().substr(str.length-2, 2)
		// return str1 + encodeURIComponent(temStr)
  }
}
