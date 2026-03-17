package com.hzm.yuchao.biz.utils;

import com.hzm.yuchao.simple.BizException;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 自动计算图片数量的A4图片合成工具
 * 根据图片实际宽高自动计算每张A4可放置的最大数量，支持子文件夹独立处理
 */
@Slf4j
public class A4ImageMerger {
    // A4标准尺寸（DPI=300，210mm×297mm → 像素：2480×3508）
    private static final int A4_WIDTH_PX = 2480;
    private static final int A4_HEIGHT_PX = 3508;
    // 配置参数（可调整）
    private static final int GAP_PX = 20; // 图片间距（像素）
    private static final Color BACKGROUND_COLOR = Color.WHITE; // 背景色
    private static final String OUTPUT_FORMAT = "png"; // 输出格式

    private String rootDirPath;
    private String outputRootDir;

    // ------------------------------ 测试入口 ------------------------------
    public static void main(String[] args) {
        try {
            // 输入根文件夹（包含子文件夹，每个文件夹下是固定尺寸的图片）
            String inputDir = "/Users/henry/IdeaProjects/yuchao/images/20251010_123017";

            A4ImageMerger merger = new A4ImageMerger();
            merger.mergeAll(inputDir);
            System.out.println("所有图片处理完成！");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 入口方法：遍历所有子文件夹并处理
     */
    public void mergeAll(String basePath) throws IOException {
        // 输入根文件夹、输出根文件夹
        this.rootDirPath = basePath + File.separator + "2";
        this.outputRootDir = basePath + File.separator + "3";
        // 创建输出目录
        new File(outputRootDir).mkdirs();

        File rootDir = new File(rootDirPath);
        if (!rootDir.exists() || !rootDir.isDirectory()) {
            throw new BizException("根文件夹不存在：" + rootDirPath);
        }
        traverseDirs(rootDir);

        ZipFolderUtil.zipFolder(outputRootDir);
    }

    /**
     * 递归遍历文件夹
     */
    private void traverseDirs(File dir) throws IOException {
        // 处理当前文件夹
        List<File> imageFiles = getImageFiles(dir);
        if (!imageFiles.isEmpty()) {
            processDir(dir, imageFiles);
        }

        // 递归处理子文件夹
        File[] subDirs = dir.listFiles(File::isDirectory);
        if (subDirs != null) {
            for (File subDir : subDirs) {
                traverseDirs(subDir);
            }
        }
    }

    /**
     * 处理单个文件夹：自动计算布局并合成图片
     */
    private void processDir(File dir, List<File> imageFiles) throws IOException {
        // 1. 获取图片尺寸（假设所有图片尺寸相同，取第一张的尺寸）
        Dimension imgDim = getImageDimension(imageFiles.get(0));
        int imgWidth = imgDim.width;
        int imgHeight = imgDim.height;
        System.out.printf("处理文件夹[%s]：图片尺寸=%dx%dpx，共%d张%n",
                dir.getAbsolutePath(), imgWidth, imgHeight, imageFiles.size());

        // 2. 自动计算A4纸可放置的最大图片数量（cols×rows）
        int[] grid = calculateMaxGrid(imgWidth, imgHeight);
        int cols = grid[0]; // 列数
        int rows = grid[1]; // 行数
        int maxPerA4 = cols * rows; // 单张A4最大容量
        log.info("自动计算布局：{}列 × {}行，每张A4可放 {} 张", cols, rows, maxPerA4);

        // 3. 分组处理（按最大容量分组）
        List<List<File>> groups = splitIntoGroups(imageFiles, maxPerA4);

        // 4. 合成每组图片到A4
        String outputDir = dir.getAbsolutePath().replace(rootDirPath, outputRootDir);
        new File(outputDir).mkdirs();
        for (int i = 0; i < groups.size(); i++) {
            BufferedImage a4Image = mergeGroupToA4(groups.get(i), cols, rows, imgWidth, imgHeight);
            File outputFile = new File(outputDir, dir.getName() + "_page" + (i + 1) + "." + OUTPUT_FORMAT);
            ImageIO.write(a4Image, OUTPUT_FORMAT, outputFile);
            log.info("生成A4图片：{}（包含 {} 张）", dir.getName(), groups.get(i).size());
        }
    }

    /**
     * 计算A4纸可放置的最大行列数（核心逻辑）
     */
    private int[] calculateMaxGrid(int imgWidth, int imgHeight) {
        // 计算有效空间（扣除边缘间距，假设边缘留1个间距的空白）
        int effectiveWidth = A4_WIDTH_PX - 2 * GAP_PX;
        int effectiveHeight = A4_HEIGHT_PX - 2 * GAP_PX;

        // 计算水平方向可放列数：(有效宽度) / (图片宽度 + 列间距)
        int cols = effectiveWidth / (imgWidth + GAP_PX);
        // 计算垂直方向可放行数：(有效高度) / (图片高度 + 行间距)
        int rows = effectiveHeight / (imgHeight + GAP_PX);

        // 边界处理：至少保证1列1行
        cols = Math.max(cols, 1);
        rows = Math.max(rows, 1);

        // 检查是否需要缩放图片（如果计算结果为1x1但图片仍超出有效空间）
        if (cols == 1 && rows == 1) {
            Dimension scaled = scaleToFit(imgWidth, imgHeight, effectiveWidth, effectiveHeight);
            return new int[]{1, 1}; // 已缩放，仍保持1x1
        }

        return new int[]{cols, rows};
    }

    /**
     * 图片过大时缩放至A4有效空间内（保持比例）
     */
    private Dimension scaleToFit(int imgWidth, int imgHeight, int maxWidth, int maxHeight) {
        double scale = Math.min((double) maxWidth / imgWidth, (double) maxHeight / imgHeight);
        if (scale < 1.0) { // 只有图片大于有效空间时才缩放
            return new Dimension(
                    (int) Math.round(imgWidth * scale),
                    (int) Math.round(imgHeight * scale)
            );
        }
        return new Dimension(imgWidth, imgHeight); // 无需缩放
    }

    // ------------------------------ 辅助方法 ------------------------------

    /**
     * 将一组图片合成到A4画布
     */
    private BufferedImage mergeGroupToA4(List<File> group, int cols, int rows, int origImgWidth, int origImgHeight) throws IOException {
        BufferedImage a4 = new BufferedImage(A4_WIDTH_PX, A4_HEIGHT_PX, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = a4.createGraphics();
        g2d.setColor(BACKGROUND_COLOR);
        g2d.fillRect(0, 0, A4_WIDTH_PX, A4_HEIGHT_PX);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 计算有效空间和实际图片尺寸（可能需要缩放）
        int effectiveWidth = A4_WIDTH_PX - 2 * GAP_PX;
        int effectiveHeight = A4_HEIGHT_PX - 2 * GAP_PX;
        Dimension scaledDim = scaleToFit(origImgWidth, origImgHeight,
                (effectiveWidth - (cols - 1) * GAP_PX) / cols,  // 单列可用宽度
                (effectiveHeight - (rows - 1) * GAP_PX) / rows  // 单行可用高度
        );
        int imgW = scaledDim.width;
        int imgH = scaledDim.height;

        // 绘制每张图片
        for (int i = 0; i < group.size(); i++) {
            int row = i / cols;
            int col = i % cols;

            // 计算位置（左上角坐标）
            int x = GAP_PX + col * (imgW + GAP_PX);
            int y = GAP_PX + row * (imgH + GAP_PX);

            // 绘制图片
            BufferedImage img = ImageIO.read(group.get(i));
            g2d.drawImage(img, x, y, imgW, imgH, null);
        }

        g2d.dispose();
        return a4;
    }

    /**
     * 获取文件夹下所有图片文件
     */
    private List<File> getImageFiles(File dir) {
        List<File> images = new ArrayList<>();
        File[] files = dir.listFiles();
        if (files == null) {
            return images;
        }

        String[] exts = {"jpg", "jpeg", "png", "bmp", "gif"};
        for (File f : files) {
            if (f.isFile()) {
                String name = f.getName().toLowerCase();
                for (String ext : exts) {
                    if (name.endsWith("." + ext)) {
                        images.add(f);
                        break;
                    }
                }
            }
        }
        return images;
    }

    /**
     * 获取图片尺寸（宽×高）
     */
    private Dimension getImageDimension(File imgFile) throws IOException {
        BufferedImage img = ImageIO.read(imgFile);
        return new Dimension(img.getWidth(), img.getHeight());
    }

    /**
     * 按指定数量分组
     */
    private List<List<File>> splitIntoGroups(List<File> list, int groupSize) {
        List<List<File>> groups = new ArrayList<>();
        for (int i = 0; i < list.size(); i += groupSize) {
            int end = Math.min(i + groupSize, list.size());
            groups.add(list.subList(i, end));
        }
        return groups;
    }
}
