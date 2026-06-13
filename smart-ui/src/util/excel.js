//引入依赖
import FileSaver from 'file-saver'
import XLSX from 'xlsx'


// obj:要导出的数据表格对象（id或者class获取都可以）[document.querySelector('#id')/document.querySelector('.class')]，
// title:导出后的表格名称
export const excel = (obj, title) => {
  /* generate workbook object from table */
  //  判断要导出的节点中是否有fixed的表格，如果有，转换excel时先将该dom移除，然后append回去，
  let fix = document.querySelector('.el-table__fixed') || document.querySelector('.el-table__fixed-right') || document.querySelector('.el-table__fixed-left');
  let wb;
  if (fix) {
    wb = XLSX.utils.table_to_book(obj.removeChild(fix));
    obj.appendChild(fix);
  } else {
    wb = XLSX.utils.table_to_book(obj);
  }
  let exName = title || '表格'
  try {
      let exNameArr = exName.split('&')
      let time = ''
      if(exNameArr.length > 1 && exNameArr[1].indexOf('undefined') == -1 ) {
          time = exNameArr[1].replace(/_/g, ':').replace(/,/g, ' 至 ');
      }
      exName = `${exNameArr[0]}${time}`
  }catch(e){ /* 忽略：标题解析失败时回退默认文件名 */ }

  //网上wb = XLSX.utils.table_to_book(document.querySelector('#'+id));直接这样写，如果存在固定列，导出的excel表格会重复两遍

  /* get binary string as output */
  let wbout = XLSX.write(wb, { bookType: 'xlsx', bookSST: true, type: 'array' });
  try {
    FileSaver.saveAs(new Blob([wbout], { type: 'application/octet-stream' }), exName+'.xlsx')
  } catch (e) { /* 忽略：saveAs 在部分浏览器可能失败，仍返回 wbout */ }
  return wbout
};
