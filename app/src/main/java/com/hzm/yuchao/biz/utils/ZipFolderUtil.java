package com.hzm.yuchao.biz.utils;

import com.hzm.yuchao.simple.BizException;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipFolderUtil {

    /**
     * 将源文件夹打包为 ZIP 文件，保留层级结构
     * @param sourceDir 源文件夹路径（需存在且为目录）
     * @throws IOException 处理文件时可能抛出 IO 异常
     */
    public static void zipFolder(String sourceDir) throws IOException {
        File sourceFile = new File(sourceDir);
        // 校验源文件夹是否存在
        if (!sourceFile.exists() || !sourceFile.isDirectory()) {
            throw new BizException("源路径不存在或不是文件夹: " + sourceDir);
        }

        String zipFilePath = sourceFile.getParentFile().getAbsolutePath() + File.separator + sourceFile.getName() + ".zip";

        // 创建 ZIP 输出流（自动关闭资源）
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFilePath))) {
            // 递归处理文件夹
            recursivelyAddToZip(sourceFile, sourceFile, zos);
        }
    }

    /**
     * 递归将文件/文件夹添加到 ZIP 输出流
     * @param rootDir 根目录（用于计算相对路径）
     * @param currentFile 当前处理的文件/文件夹
     * @param zos ZIP 输出流
     * @throws IOException IO 异常
     */
    private static void recursivelyAddToZip(File rootDir, File currentFile, ZipOutputStream zos) throws IOException {
        // 如果是文件夹，递归处理子文件
        if (currentFile.isDirectory()) {
            // 获取当前文件夹相对于根目录的路径（用于保持层级）
            String relativePath = getRelativePath(rootDir, currentFile);
            // 添加文件夹条目（必须以 "/" 结尾，否则解压时可能被识别为文件）
            if (!relativePath.isEmpty()) {
                ZipEntry entry = new ZipEntry(relativePath + "/");
                zos.putNextEntry(entry);
                zos.closeEntry(); // 关闭当前条目
            }

            // 递归处理子文件
            File[] subFiles = currentFile.listFiles();
            if (subFiles != null) {
                for (File subFile : subFiles) {
                    recursivelyAddToZip(rootDir, subFile, zos);
                }
            }
        } 
        // 如果是文件，直接添加到 ZIP
        else {
            // 获取文件相对于根目录的路径（作为 ZIP 内部的路径）
            String relativePath = getRelativePath(rootDir, currentFile);
            ZipEntry entry = new ZipEntry(relativePath);
            zos.putNextEntry(entry);

            // 写入文件内容
            try (InputStream is = new FileInputStream(currentFile)) {
                byte[] buffer = new byte[1024 * 8];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    zos.write(buffer, 0, len);
                }
            }
            zos.closeEntry(); // 关闭当前条目
        }
    }

    /**
     * 计算当前文件相对于根目录的路径（用于保持 ZIP 内部层级）
     * @param rootDir 根目录
     * @param currentFile 当前文件
     * @return 相对路径（如 "a/b/c.txt"）
     * @throws IOException 路径处理异常
     */
    private static String getRelativePath(File rootDir, File currentFile) throws IOException {
        String rootPath = rootDir.getCanonicalPath();
        String currentPath = currentFile.getCanonicalPath();

        // 确保当前文件在根目录下
        if (!currentPath.startsWith(rootPath)) {
            throw new BizException("当前文件不在根目录下: " + currentFile.getPath());
        }

        // 计算相对路径（去掉根目录部分）
        String relativePath = currentPath.substring(rootPath.length());
        // 替换 Windows 路径分隔符为标准 "/"（ZIP 协议使用 "/"）
        return relativePath.replace(File.separator, "/").replaceFirst("^/", "");
    }

    // 测试示例
    public static void main(String[] args) {
        try {
            // 源文件夹（例如：D:/test-folder）
            String sourceDir = "D:/test-folder";
            // 目标 ZIP 文件（例如：D:/test.zip）
            String zipFilePath = "D:/test.zip";
            
            zipFolder(sourceDir);
            System.out.println("文件夹打包成功: " + zipFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
    