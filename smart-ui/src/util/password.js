/**
 * 校验密码
 */
export function validatePwd(pwd) {
  let list = []
  let result = true
  let msg = ''
  if (pwd === null || pwd.length < 8) {
    msg = '密码应不少于8位';
  }else{
    let reg = new RegExp(/(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[\W_])/);
    if (!reg.test(pwd)) {
      msg = '密码应包含数字、大写字母、小写字母、特殊符号';
    }
    else{
      //三个相同 数字 字母（aaa、AAA、111 等不允许 Aaa允许）
      let reg2 = new RegExp(/([\da-zA-Z])\1{2}/g);
      if(reg2.test(pwd)){
        msg = '密码不能包含三个及以上相同数字或字母';
      }else{
        //三个连续顺序的 数字 字母（123、abc、ABC 等不允许 Abc允许）
        let str1 = 'abcdefghijklmnopqrstuvwxyz'
        let str2 = '0123456789'
        let str3 = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'
        let arr = []
        for(let i = 0;i<24;i++){
          arr.push(str1[i]+str1[i+1]+str1[i+2])
        }
        for(let i = 0;i<8;i++){
          arr.push(str2[i]+str2[i+1]+str2[i+2])
        }
        for(let i = 0;i<24;i++){
          arr.push(str3[i]+str3[i+1]+str3[i+2])
        }
        let str4 = arr.join('|')
        let reg3 = new RegExp(str4,'g');
        if(reg3.test(pwd)){
          msg = '密码不能包含三个及以上连续数字或字母';
        }else{
          result = false
        }
      }
    }
  }
  list.push(result)
  list.push(msg)
  return list
}
