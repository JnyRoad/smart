package com.tce.smart.common.core.util;

import com.tce.smart.common.core.constant.enums.ExceptionType;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.model.Count;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.util.CollectionUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 读写Excel
 * 如果Excel 不存在 需要先调用createExcel 创建Excel
 */
@Slf4j
public class ExcelUtils {
    /**
     * 日志对象
     */
    /**
     * 2003- 版本的excel
     */
    public final static String OFFICE_EXCEL_2003_POSTFIX = ".xls";
    /**
     * 2007+ 版本的excel
     */
    public final static String OFFICE_EXCEL_2007_POSTFIX = ".xlsx";
    /**
     * 默认sheet名称
     */
    private final static String OFFICE_EXCEL_DEFAULT_SHEET = "sheet1";
    /**
     * 默认第一个sheet
     */
    private final static int DEFAULT_SHEET_INDEX = 0;

    /**
     * 描述：获取IO流中的数据，组装成List<List<Object>>对象
     *
     * @param in,fileName
     * @return
     */
    /**
     * 描述：获取IO流中的数据，组装成List<List<Object>>对象
     *
     * @param in,fileName
     * @return
     */
    public static void handleExcel(InputStream in, String fileName, String[] titles, ExcelHandle excelHandle) {
        Count count = new Count();
        List<List<Object>> datas = readExcelByTitle(in, fileName, titles);
        if (!CollectionUtils.isEmpty(datas)) {
            for (int i = 0; i < datas.size(); i++) {
                //处理每一行数据
                excelHandle.handle(count, i, datas.get(i));
            }
            if (count.fail() == 0) {
                return;
            }
        }
        throw new TCEException(ExceptionType.EXCEL_CONTENT_EMPTY);
    }

    public static List<List<Object>> readExcelByTitle(InputStream in, String fileName, String[] titles) {
        List<List<Object>> datas = readExcel(in, fileName, null);
        if (!CollectionUtils.isEmpty(datas) && checkTemplate(titles, datas.get(0))) {
            //去掉标题行
            datas.remove(0);
            return filterRows(datas, titles);
        }
        return new ArrayList<>();
    }

    /**
     * 去掉空行
     *
     * @param datas
     * @param titles
     * @return
     */
    public static List<List<Object>> filterRows(List<List<Object>> datas, String[] titles) {
        List<List<Object>> data = new ArrayList<>();
        for(List<Object> d : datas){
            List<Object> dt = filterRow(d, titles);
            final String str = dt.stream().map(Object::toString).collect(Collectors.joining());
            if(!str.equals(StringUtils.EMPTY)){
                data.add(dt);
            }
        }
        return data;
    }
    public static List<Object> filterRow(List<Object> row, String[] titles){
        List<Object> rows = new ArrayList<>();
        for (int i = 0; i < titles.length; i++) {
            if(i < row.size() && i < titles.length){
                rows.add(row.get(i));
            }
        }
        return rows;
    }

    /**
     * 描述：获取IO流中的数据，组装成List<List<Object>>对象
     *
     * @param in,fileName
     * @return
     */
    public static List<List<Object>> readExcel(InputStream in, String fileName) {
        return readExcel(in, fileName, null);
    }

    /**
     * 描述：获取IO流中的数据，组装成List<List<Object>>对象
     *
     * @param in,fileName,column
     * @return
     */
    public static List<List<Object>> readExcel(InputStream in, String fileName, int[] columns) {
        List<List<Object>> list = new LinkedList<>();
        Workbook work = null;
        try {
            // 创建Excel工作薄
            work = buildWorkbook(in, fileName);
            Objects.requireNonNull(work, "创建Excel工作薄为空!");
            Sheet sheet = null;
            Row row = null;
            Cell cell = null;

            // 遍历Excel中所有的sheet
            for (int i = 0; i < work.getNumberOfSheets(); i++) {
                sheet = work.getSheetAt(i);
                if (Objects.nonNull(sheet)) {
                    // 遍历当前sheet中的所有行
                    for (int j = sheet.getFirstRowNum(); j <= sheet.getLastRowNum(); j++) {
                        row = sheet.getRow(j);
                        if (Objects.isNull(row)) {
                            continue;
                        }

                        // 遍历所有的列
                        List<Object> li = new LinkedList<>();
                        for (int y = 0; y < row.getLastCellNum(); y++) {
                            if (isRead(y, columns)) {
                                cell = row.getCell(y);
                                if (Objects.isNull(cell) || StringUtils.isEmpty(cell.toString())) {
                                    log.info("Excel：{} 的 {} 行，{} 列为空", fileName, i + 1, y + 1);
                                    li.add(StringUtils.EMPTY);
                                    continue;
                                }
                                cell.setCellType(CellType.STRING);
                                li.add(cell.toString());
                            }
                        }
                        list.add(li);
                    }
                }
            }
        } catch (Exception e) {
            log.error("读取Excel：" + fileName + " 出现异常：" + e);
        } finally {

            close(work);
            close(in);
        }
        return list;
    }

    /**
     * 判断当前读取的列 是否是 需要读取的列
     * colunms 为空 则全部列都需要读取
     *
     * @param column
     * @param colunms
     * @return
     */
    private static boolean isRead(int column, int[] colunms) {
        if (Objects.isNull(colunms) || colunms.length == 0) {
            return true;
        }
        for (int i = 0; i < colunms.length; i++) {
            if (colunms[i] == column) {
                return true;
            }
        }
        return false;
    }

    /**
     * 描述：根据文件后缀，自适应上传文件的版本
     *
     * @param inStr
     * @param fileName
     * @return
     * @throws Exception
     */
    public static Workbook buildWorkbook(InputStream inStr, String fileName) throws Exception {
        Workbook wb = null;
        String fileType = fileName.substring(fileName.lastIndexOf("."));
        if (OFFICE_EXCEL_2003_POSTFIX.equals(fileType)) {
            // 2003-
            wb = new HSSFWorkbook(inStr);
        } else if (OFFICE_EXCEL_2007_POSTFIX.equals(fileType)) {
            // 2007+
            wb = new XSSFWorkbook(inStr);
        } else {
            throw new Exception("解析的文件格式有误!");
        }
        return wb;
    }

    /**
     * 创建Excel 并返回文件绝对路径
     *
     * @param fileName
     */
    public static void createExcel(String fileName) throws IOException {
        createExcel(fileName, OFFICE_EXCEL_DEFAULT_SHEET);
    }

    /**
     * 创建Excel 并返回文件绝对路径
     *
     * @param fileName
     * @param sheetName
     */
    public static String createExcel(String fileName, String sheetName) throws IOException {
        String path = fileName.substring(0, fileName.lastIndexOf(File.separator));
        File pathFile = new File(path);
        if (!pathFile.exists()) {
            pathFile.mkdirs();
        }
        File file = new File(fileName);
        if (!file.exists()) {
            file.createNewFile();
            createNonExistExcel(fileName, sheetName);
        } else {
            appendSheet(fileName, sheetName);
        }
        log.info("Excel路径：" + file.getAbsolutePath());
        return file.getAbsolutePath();
    }

    /**
     * 创建Excel (Excel不存在)
     *
     * @param fileName
     * @param sheetName
     */
    public static void createNonExistExcel(String fileName, String sheetName) {
        Workbook wb = null;
        FileOutputStream fos = null;
        try {
            String fileType = fileName.substring(fileName.lastIndexOf("."));
            if (OFFICE_EXCEL_2003_POSTFIX.equals(fileType)) {
                // 2003-
                wb = new HSSFWorkbook();
            } else if (OFFICE_EXCEL_2007_POSTFIX.equals(fileType)) {
                // 2007+
                wb = new XSSFWorkbook();
            } else {
                throw new Exception("错误的Excel后缀,创建失败!");
            }
            // 创建空的sheet
            wb.createSheet(sheetName);

            fos = new FileOutputStream(fileName);
            wb.write(fos);
            fos.flush();
        } catch (Exception e) {
            log.error("创建Excel：" + fileName + " 出现异常：" + e);
        } finally {
            close(fos);
            close(wb);
        }
    }

    /**
     * 在Excel中新增Sheet
     *
     * @param fileName
     * @param sheetName
     */
    public static void appendSheet(String fileName, String sheetName) {
        Workbook wb = null;
        FileOutputStream fos = null;
        try {
            File file = new File(fileName);
            wb = WorkbookFactory.create(file);
            wb.createSheet(sheetName);
            fos = new FileOutputStream(file, true);
            wb.write(fos);
            fos.flush();
            log.info("在Excel：" + fileName + " 中成功创建Sheet：" + sheetName);
        } catch (Exception e) {
            log.error("Excel：" + fileName + " 创建Sheet：" + sheetName + " 出现异常：" + e);
        } finally {
            close(fos);
            close(wb);
        }
    }

    /**
     * 写入Excel
     *
     * @param fileName 文件名称
     * @param data     需要写入的数据
     * @param key      字段
     */
    public static void appendRow(String fileName, List<List<Object>> data, String[] key) {
        appendRow(fileName, DEFAULT_SHEET_INDEX, data, key);
    }

    /**
     * 写入Excel
     *
     * @param fileName   文件名称
     * @param sheetIndex sheet的索引
     * @param data       需要写入的数据
     * @param key        字段
     */
    public static void appendRow(String fileName, int sheetIndex, List<List<Object>> data, String[] key) {
        FileInputStream fs = null;
        FileOutputStream fos = null;
        Workbook wb = null;
        try {
            fs = new FileInputStream(fileName);

            String fileType = fileName.substring(fileName.lastIndexOf("."));
            if (OFFICE_EXCEL_2003_POSTFIX.equals(fileType)) {
                // 2003-
                wb = new HSSFWorkbook(fs);
            } else if (OFFICE_EXCEL_2007_POSTFIX.equals(fileType)) {
                // 2007+
                wb = new XSSFWorkbook(fs);
            } else {
                throw new Exception("错误的Excel后缀,创建失败!");
            }
            // 获取到工作表，因为一个excel可能有多个工作表
            Sheet sheet = wb.getSheetAt(sheetIndex);
            // 向d://test.xls中写数据
            fos = new FileOutputStream(fileName);
            int rowIndex = sheet.getLastRowNum();
            if (rowIndex > 0) {
                rowIndex++;
            }
            //第一行字段为key
            if (rowIndex == 0) {
                // 在现有行号后追加数据
                Row row = sheet.createRow(rowIndex);
                for (int j = 0; j < key.length; j++) {
                    row.createCell(j).setCellValue(Objects.isNull(key[j]) ? "" : String.valueOf(key[j]));
                }
                rowIndex++;
            }
            for (List<Object> item : data) {
                // 在现有行号后追加数据
                Row row = sheet.createRow(rowIndex);
                //后面字段 value　值
                for (int i = 0; i < key.length; i++) {
                    // 设置第一个（从0开始）单元格的数据
                    row.createCell(i).setCellValue(Objects.isNull(item.get(i)) ? "" : String.valueOf(item.get(i)));
                }
                rowIndex++;
            }
            log.info("写入Excel：{} 完成，共写入 {} 行。", fileName, rowIndex);
            fos.flush();
            wb.write(fos);
        } catch (Exception e) {
            log.error("写入Excel：" + fileName + " 出现异常：" + e);
        } finally {
            close(fs);
            close(fos);
            close(wb);
        }
    }

    public static boolean checkTemplate(String[] titles, List<Object> firstRow) {
        boolean check = Stream.iterate(0, i -> ++i).limit(titles.length)
                .allMatch(i -> firstRow.get(i).toString().trim().equals(titles[i].trim()));
        if (!check) {
            log.error("Excel 标题和模板不一致!");
            throw new TCEException(ExceptionType.EXCEL_UPLOAD_FAIL_TITLE_ERROR);
        }
        return check;
    }

    public static void close(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}