/* eslint-disable */
import FileSaver from 'file-saver';
import XLSX from 'xlsx';

require('./Blob');
function generateArray(table) {
    var out = [];
    var rows = table.querySelectorAll('tr');
    var ranges = [];
    for (var R = 0; R < rows.length; ++R) {
        var outRow = [];
        var row = rows[R];
        var columns = row.querySelectorAll('td');
        for (var C = 0; C < columns.length; ++C) {
            var cell = columns[C];
            var colspan = cell.getAttribute('colspan');
            var rowspan = cell.getAttribute('rowspan');
            var cellValue = cell.innerText;
            if (cellValue !== "" && cellValue == +cellValue) cellValue = +cellValue;

            //Skip ranges
            ranges.forEach(function (range) {
                if (R >= range.s.r && R <= range.e.r && outRow.length >= range.s.c && outRow.length <= range.e.c) {
                    for (var i = 0; i <= range.e.c - range.s.c; ++i) outRow.push(null);
                }
            });

            //Handle Row Span
            if (rowspan || colspan) {
                rowspan = rowspan || 1;
                colspan = colspan || 1;
                ranges.push({ s: { r: R, c: outRow.length }, e: { r: R + rowspan - 1, c: outRow.length + colspan - 1 } });
            }
            ;

            //Handle Value
            outRow.push(cellValue !== "" ? cellValue : null);

            //Handle Colspan
            if (colspan) for (var k = 0; k < colspan - 1; ++k) outRow.push(null);
        }
        out.push(outRow);
    }
    return [out, ranges];
};

function datenum(v, date1904) {
    if (date1904) v += 1462;
    var epoch = Date.parse(v);
    return (epoch - new Date(Date.UTC(1899, 11, 30))) / (24 * 60 * 60 * 1000);
}

function sheet_from_array_of_arrays(data, opts) {
    var ws = {};
    var range = { s: { c: 10000000, r: 10000000 }, e: { c: 0, r: 0 } };

    // var merge = { s: { r: 0, c: 0 }, e: { r: 0, c: data[0].length - 1 } };
    // if (range.s.c < 10000000) ws['!ref'] = XLSX.utils.encode_range(range);
    // if (opts && opts.title) {
    //     ws['!merges'] = [merge]; // 添加合并单元格信息
    // }
    var colCount = 0;
    for (var R = 0; R != data.length; ++R) {
        if (data[R].length > colCount) colCount = data[R].length; // 获取最大列数
        for (var C = 0; C != data[R].length; ++C) {
            if (range.s.r > R) range.s.r = R;
            if (range.s.c > C) range.s.c = C;
            if (range.e.r < R) range.e.r = R;
            if (range.e.c < C) range.e.c = C;
            var cell = { v: data[R][C] };
            if (cell.v == null) continue;
            var cell_ref = XLSX.utils.encode_cell({ c: C, r: R });

            if (typeof cell.v === 'number') cell.t = 'n';
            else if (typeof cell.v === 'boolean') cell.t = 'b';
            else if (cell.v instanceof Date) {
                cell.t = 'n';
                cell.z = XLSX.SSF._table[14];
                cell.v = datenum(cell.v);
            }
            else cell.t = 's';

            ws[cell_ref] = cell;
        }
    }
    if (range.s.c < 10000000) ws['!ref'] = XLSX.utils.encode_range(range);


    if (opts && opts.title) {
        var merge = { s: { r: 0, c: 0 }, e: { r: 0, c: colCount - 1 } }; // 根据列数计算合并单元格范围
        ws['!merges'] = [merge]; // 添加合并单元格信息
        data.unshift([opts.title]); // 插入新的标题行
        range.e.r++; // 扩展有效区域范围
        for (var C = 0; C < data[1].length; ++C) {
            var cell_ref = XLSX.utils.encode_cell({ c: C, r: 0 });
            var titleCell = { v: opts.title, t: 's', s: { font: { bold: true, color: {rgb: 'FF0000'} }, alignment: { horizontal: 'center' } } }; // 标题单元格样式
            ws[cell_ref] = titleCell; // 添加新的标题单元格
        }
    }

    return ws;
}

function Workbook() {
    if (!(this instanceof Workbook)) return new Workbook();
    this.SheetNames = [];
    this.Sheets = {};
}

function s2ab(s) {
    var buf = new ArrayBuffer(s.length);
    var view = new Uint8Array(buf);
    for (var i = 0; i != s.length; ++i) view[i] = s.charCodeAt(i) & 0xFF;
    return buf;
}

export function export_table_to_excel(id) {
    var theTable = document.getElementById(id);
    console.log('a')
    var oo = generateArray(theTable);
    var ranges = oo[1];

    /* original data */
    var data = oo[0];
    var ws_name = "SheetJS";
    console.log(data);

    var wb = new Workbook(), ws = sheet_from_array_of_arrays(data);

    /* add ranges to worksheet */
    // ws['!cols'] = ['apple', 'banan'];
    ws['!merges'] = ranges;

    /* add worksheet to workbook */
    wb.SheetNames.push(ws_name);
    wb.Sheets[ws_name] = ws;

    var wbout = XLSX.write(wb, { bookType: 'xlsx', bookSST: false, type: 'binary' });

    FileSaver.saveAs(new Blob([s2ab(wbout)], { type: "application/octet-stream" }), "test.xlsx")
}

function formatJson(jsonData) {
    console.log(jsonData)
}
export function export_json_to_excel(th, jsonData, defaultTitle, tH_title) {

    // 处理defaultTitle里携带了日期的相关问题
    let exName = defaultTitle || '表格'

    try {
        let exNameArr = exName.split('&')
        let time = ''
        if(exNameArr.length > 1 && exNameArr[1].indexOf('undefined') == -1 ) {
            time = exNameArr[1].replace(/_/g, ':').replace(/,/g, ' 至 ');
        }
        exName = `${exNameArr[0]}${time}`
    }catch(e){}

    /* original data */
    var data = jsonData;
    data.unshift(th);
    data.unshift(new Array(th.length)); // 空出一行，让给 tH_title 要不然会覆盖掉表头
    var ws_name = "SheetJS";

    var wb = new Workbook(), ws = sheet_from_array_of_arrays(data, { title: tH_title || exName });


    /* add worksheet to workbook */
    wb.SheetNames.push(ws_name);
    wb.Sheets[ws_name] = ws;

    var wbout = XLSX.write(wb, { bookType: 'xlsx', bookSST: false, type: 'binary' });
    var title = exName || '列表'
    FileSaver.saveAs(new Blob([s2ab(wbout)], { type: "application/octet-stream" }), title + ".xlsx")
}
