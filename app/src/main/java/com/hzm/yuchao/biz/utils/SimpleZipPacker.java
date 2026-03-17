package com.hzm.yuchao.biz.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
public class SimpleZipPacker {

    /**
     * 将指定文件夹下的直接zip文件（不包含子文件夹）打包成一个新的zip文件
     * 新zip文件与源文件夹同级，解压后包含一个文件夹和所有原始zip文件
     * @param sourceDir 源文件夹路径
     * @throws IOException 处理过程中发生的IO异常
     */
    public static String packZipFiles(String sourceDir) throws IOException {
        // 验证源文件夹
        Path sourcePath = Paths.get(sourceDir);
        if (!Files.exists(sourcePath) || !Files.isDirectory(sourcePath)) {
            throw new IllegalArgumentException("源路径不存在或不是一个目录: " + sourceDir);
        }

        // 确定目标zip文件路径（与源文件夹同级）
        String targetZipPath = sourceDir + ".zip";
        Path targetPath = Paths.get(targetZipPath);

        // 如果目标文件已存在，先删除
        if (Files.exists(targetPath)) {
            Files.delete(targetPath);
        }

        // 获取源文件夹下的所有直接zip文件（不包含子文件夹）
        File[] files = sourcePath.toFile().listFiles(file ->
                file.isFile() && file.getName().toLowerCase().endsWith(".zip")
        );

        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("源文件夹中没有找到zip文件: " + sourceDir);
        }

        // 创建新的zip文件
        try (ZipOutputStream zos = new ZipOutputStream(
                new BufferedOutputStream(Files.newOutputStream(targetPath)))) {

            // 2. 添加所有zip文件（直接放在zip根目录下）
            byte[] buffer = new byte[8192]; // 8KB缓冲区
            for (File zipFile : files) {
                try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(zipFile))) {
                    // 创建zip条目，使用原始文件名
                    ZipEntry entry = new ZipEntry(zipFile.getName());
                    entry.setTime(zipFile.lastModified()); // 保留修改时间
                    zos.putNextEntry(entry);

                    // 写入文件内容
                    int bytesRead;
                    while ((bytesRead = bis.read(buffer)) != -1) {
                        zos.write(buffer, 0, bytesRead);
                    }

                    zos.closeEntry();
                }
            }
        }

        log.info("打包完成，目标文件: {}", targetZipPath);

        return targetZipPath;
    }

    // 使用示例
    public static void main(String[] args) {
        // 源文件夹路径
        String sourceDir = "/Users/henry/IdeaProjects/yuchao/images/20251010_123017";

        try {
            packZipFiles(sourceDir);
        } catch (IOException e) {
            System.err.println("打包失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
