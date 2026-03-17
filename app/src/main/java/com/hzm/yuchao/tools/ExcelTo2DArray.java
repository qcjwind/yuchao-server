package com.hzm.yuchao.tools;

import com.hzm.yuchao.simple.BizException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class ExcelTo2DArray {
    
    public static void main(String[] args) throws Exception {
        String filePath = "/Users/henry/IdeaProjects/yuchao/docs/体育馆足球场.xlsx"; // Excel文件路径
        // 起始为0
        String[][] dataArray = excelTo2DArray(filePath, 0);

        ArrayPrinter.print2DArray(dataArray);
    }

    public static Workbook getWorkbook(InputStream is, String filePath) throws Exception {
        Workbook workbook = null;

        // 根据文件扩展名创建对应的Workbook实例
        if (filePath.endsWith(".xls")) {
            workbook = new HSSFWorkbook(is);
        } else if (filePath.endsWith(".xlsx")) {
            // 处理.xlsx格式（Excel 2007及以后）
            workbook = new XSSFWorkbook(is);
        } else {
            throw new BizException("仅支持xls文件格式");
        }

        return workbook;
    }

    /**
     * 将Excel文件内容转换为二维字符串数组
     * @param filePath Excel文件路径
     * @return 包含Excel内容的二维数组，若出错则返回null
     */
    public static String[][] excelTo2DArray(String filePath, int sheetNo) throws Exception {
        return excelTo2DArray(new FileInputStream(filePath), filePath, sheetNo);
    }

    /**
     * 将Excel文件内容转换为二维字符串数组
     * @return 包含Excel内容的二维数组，若出错则返回null
     */
    public static String[][] excelTo2DArray(InputStream is, String filePath, int sheetNo) throws Exception {
        Workbook workbook = null;

        try {
            workbook = getWorkbook(is, filePath);

            Sheet sheet = null;
            try {
                sheet = workbook.getSheetAt(sheetNo);
            } catch (Exception ignore) {
            }

            if (sheet == null) {
                return null;
            }

            // 计算行数和最大列数
            int rowCount = sheet.getLastRowNum() + 1;
            int colCount = getMaxColumnCount(sheet);
            
            // 创建二维数组
            String[][] dataArray = new String[rowCount][colCount];
            
            // 遍历行和列，填充二维数组
            for (int i = 0; i < rowCount; i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    for (int j = 0; j < colCount; j++) {
                        Cell cell = row.getCell(j);
                        dataArray[i][j] = getCellValue(cell);
                    }
                }
            }
            
            return dataArray;
            
        } catch (IOException e) {
            log.error("文件解析失败", e);
            throw e;
        } finally {
            // 关闭资源
            try {
                if (workbook != null) {
                    workbook.close();
                }
            } catch (IOException ignore) {
            }
        }
    }
    
    /**
     * 获取工作表中最大的列数
     */
    private static int getMaxColumnCount(Sheet sheet) {
        int maxCols = 0;
        int rowCount = sheet.getLastRowNum() + 1;
        
        for (int i = 0; i < rowCount; i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                int cols = row.getLastCellNum();
                if (cols > maxCols) {
                    maxCols = cols;
                }
            }
        }
        
        return maxCols;
    }
    
    /**
     * 获取单元格的字符串值
     */
    private static String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        // 根据单元格类型获取值
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((int)(cell.getNumericCellValue()));
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
}
    