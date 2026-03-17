package com.hzm.yuchao.biz.component;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.hzm.yuchao.biz.controller.mng.req.QrcodeImageRequest;
import com.hzm.yuchao.biz.model.MatchDO;
import com.hzm.yuchao.biz.model.TicketDO;
import com.hzm.yuchao.biz.service.MatchService;
import com.hzm.yuchao.biz.service.TicketService;
import com.hzm.yuchao.biz.utils.SimpleZipPacker;
import com.hzm.yuchao.biz.utils.ZipFolderUtil;
import com.hzm.yuchao.simple.BizException;
import com.hzm.yuchao.simple.ThreadService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * 图片生成、ZIP打包和OSS上传工具类
 */
@Slf4j
@Component
public class QrcodeComponent {

    @Value("${app.config.qrcodeSchema}")
    private String qrcodeSchema = "https://yuchao2025.zszlchina.com/gift?type=gift&ticketBid=";

    @Resource
    private OssComponent ossComponent;

    @Resource
    private TicketService ticketService;

    @Resource
    private MatchService matchService;

    @Resource
    private ThreadService threadService;

    private Font baseFont;

    public QrcodeComponent() throws Exception {
        // 1. 读取字体文件（支持本地文件或项目资源）
        InputStream fontStream = new ClassPathResource("files/思源黑体CN-Bold.otf").getInputStream();
        // 2. 创建字体（TrueType字体）
        this.baseFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);

        fontStream.close();
    }

    public void asyncGenerateQrcode(QrcodeImageRequest request) {

        // 查询出本次的赠票
        LambdaQueryWrapper<TicketDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TicketDO::getMatchId, request.getMatchId());
        queryWrapper.eq(TicketDO::getTicketType, request.getTicketType());
        queryWrapper.in(StringUtils.isNotEmpty(request.getSkuIds()), TicketDO::getSkuId, request.getSkuIds().split(","));
        queryWrapper.last("limit 20000");
        List<TicketDO> giftTicketList = ticketService.list(queryWrapper);

        if (CollectionUtils.isEmpty(giftTicketList)) {
            throw new BizException("暂无赠票票信息");
        }

        threadService.execute(() -> {
            doCore(request, giftTicketList);
        });
    }

    private void doCore(QrcodeImageRequest request, List<TicketDO> giftTicketList) {

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String basePath = "images" + File.separator + request.getMatchId() + "_" + sdf.format(new Date());
            Files.createDirectories(Paths.get(basePath));

            // 赠票生成二维码 并打包上传oss.
            // 1. 普通二维码
            generateZip(basePath, giftTicketList, null, false);
            log.info("普通二维码生成完成，matchId: {}", request.getMatchId());
            if (request.getBgImage() != null) {
                // 2. 带底图二维码
                generateZip(basePath, giftTicketList, request, true);
                log.info("带底图二维码生成完成，matchId: {}", request.getMatchId());

                // 3. A4二维码, 不生成A4
//                new A4ImageMerger().mergeAll(basePath);
//                log.info("A4二维码生成完成，matchId: {}", request.getMatchId());
            }

            // 多个zip文件打包
            String packZipFiles = SimpleZipPacker.packZipFiles(basePath);

            // 更新db
            LambdaUpdateWrapper<MatchDO> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(MatchDO::getGiftTicketUrl, packZipFiles);
            updateWrapper.eq(MatchDO::getId, request.getMatchId());
            matchService.update(updateWrapper);

        } catch (Exception e) {
            log.error("赠票文件生成异常, matchId: {}", request.getMatchId(), e);
            throw new BizException("赠票文件生成异常");
        }

        log.info("赠票全部上传完成，matchId: {}", request.getMatchId());
    }

    /**
     * 去除Base64字符串中的协议头（如"data:image/xxx;base64,"）
     * @param base64Str 带协议头的Base64字符串
     * @return 纯Base64编码部分
     */
    private static String stripBase64Header(String base64Str) {
        // 查找协议头分隔符","，如果存在则截取后面的部分
        int commaIndex = base64Str.indexOf(',');
        if (commaIndex != -1 && commaIndex < base64Str.length() - 1) {
            return base64Str.substring(commaIndex + 1).trim();
        }
        // 没有协议头则直接返回原字符串（trim处理空白）
        return base64Str.trim();
    }

    private String generateZip(String basePath, List<TicketDO> giftTicketList, QrcodeImageRequest request, boolean newQrCode) throws Exception {

        String path = basePath + File.separator + (newQrCode ? "2" : "1");

        int count = 0;
        for (TicketDO ticketDO : giftTicketList) {
            String fileName = ticketDO.getArea() + "-" +
                    ticketDO.getSubArea() + "-" +
                    ticketDO.getSeatRow() + "排-" +
                    ticketDO.getSeatNo() + "号" +
                    ".png";

            String filePath = path + File.separator +
                    ticketDO.getArea() + File.separator +
                    ticketDO.getSubArea() + File.separator +
                    ticketDO.getSubArea() + "-" + ticketDO.getSeatRow() + "排" + File.separator;

            Files.createDirectories(Paths.get(filePath));

            String content = qrcodeSchema + ticketDO.getBid();

            if (newQrCode) {
                drawQrCodeAndText(request, content,
                        ticketDO.getArea() + "区",
                        ticketDO.getSubArea() + "区",
                        ticketDO.getSeatRow() + "排",
                        ticketDO.getSeatNo() + "号",
                        filePath + fileName);

                count++;

                if (count % 10 == 0) {
                    log.info("赠票生成完成： {}, 累计：{}", count, giftTicketList.size());
                }
            } else {
                generateQrCodeImageAndSave(content, filePath + fileName);
            }
        }

        ZipFolderUtil.zipFolder(path);

        return path;
    }

    /**
     * 核心方法：在底图指定位置绘制二维码 + 文字
     */
    public String preview(QrcodeImageRequest request) throws Exception {

        // 1. 读取底图（支持JPG/PNG）
        BufferedImage baseImage = getImage(request, "测试", "A区", "A0区", "0排", "0号");

        // 将图像写入字节输出流
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            // 写入图像（格式由formatName指定）
            ImageIO.write(baseImage, "png", outputStream);

            // 转换字节数组为Base64字符串
            byte[] resultImageBytes = outputStream.toByteArray();
            String base64Encoded = Base64.getEncoder().encodeToString(resultImageBytes);

            // 拼接数据协议头（便于前端直接使用）
            return "data:image/png;base64," + base64Encoded;
        }
    }

    /**
     * 核心方法：在底图指定位置绘制二维码 + 文字
     */
    private void drawQrCodeAndText(QrcodeImageRequest request, String qrContent,
                                   String text1, String text2, String text3, String text4,
                                   String outputPath) throws Exception {

        BufferedImage baseImage = getImage(request, qrContent, text1, text2, text3, text4);

        File file = new File(outputPath);
        ImageIO.write(baseImage, "png", file);
    }

    private BufferedImage getImage(QrcodeImageRequest request, String qrContent,
                                   String text1, String text2, String text3, String text4) throws Exception {

        // 去除可能的协议头（如"data:image/png;base64,"）
        String pureBase64 = stripBase64Header(request.getBgImage());

        // 1. 读取底图（支持JPG/PNG）
        BufferedImage baseImage;
        // Base64解码为字节数组
        byte[] imageBytes = Base64.getDecoder().decode(pureBase64);

        // 字节数组转为输入流，再通过ImageIO读取为BufferedImage
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes)) {
            baseImage = ImageIO.read(inputStream);
            if (baseImage == null) {
                throw new BizException("无法解析图像，可能是不支持的格式或无效的Base64编码");
            }
        }

        // 获取底图画笔（用于绘制二维码和文字）
        Graphics2D g2d = baseImage.createGraphics();
        // 抗锯齿配置（让二维码和文字更清晰）
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 2. 生成二维码图片（BufferedImage格式）
        BufferedImage qrImage = generateQrCodeImage(qrContent, request.getQrcodeWidth(), request.getQrcodeWidth());

        // 3. 将二维码绘制到底图指定位置
        g2d.drawImage(qrImage, request.getQrcodeX(), request.getQrcodeY(),
                request.getQrcodeWidth(), request.getQrcodeWidth(),
                null);

        // 4. 在二维码下方绘制文字（文字居中对齐）
        drawTextOnImage(g2d, request, text1, text2, text3, text4);

        // 5. 释放资源 + 保存最终图片
        g2d.dispose();

        return baseImage;
    }

    /**
     * 生成二维码图片（BufferedImage）
     *
     * @param content 二维码内容
     * @param width   宽度
     * @param height  高度
     */
    private BufferedImage generateQrCodeImage(String content, int width, int height) throws WriterException {
        // 二维码配置（容错率、编码格式等）
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8"); // 支持中文
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M); // 中容错
        hints.put(EncodeHintType.MARGIN, 1); // 二维码边距（1=最小边距）

        // 生成二维码矩阵
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);

        // 矩阵转BufferedImage
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }


    /**
     * 生成二维码图片
     *
     * @param content   二维码内容
     * @param imagePath
     * @return 生成的图片文件路径
     */
    private void generateQrCodeImageAndSave(String content, String imagePath) throws Exception {
        // 配置二维码参数
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.MARGIN, 1);

        // 生成二维码矩阵
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, 200, 200, hints);

        // 保存二维码图片到临时目录
        Path path = Paths.get(imagePath);
        MatrixToImageWriter.writeToPath(bitMatrix, "png", path);
    }

    /**
     * 在二维码下方绘制文字（居中对齐）
     *
     * @param g2d 画笔
     */
    private void drawTextOnImage(Graphics2D g2d, QrcodeImageRequest request, String text1, String text2, String text3, String text4) {

        // 文字颜色
        String replace = request.getFontColor().replace("#", "");
        Color textColor = new Color(Integer.parseInt(replace, 16));
        Font customFont = baseFont.deriveFont(Font.BOLD, request.getTextSize());

        // 设置字体和颜色
        g2d.setFont(customFont);
        g2d.setColor(textColor);

        // 绘制文字
        if (request.getAreaX() > 0) {
            g2d.drawString(text1, request.getAreaX(), request.getAreaY());
        }
        if (request.getSubAreaX() > 0) {
            g2d.drawString(text2, request.getSubAreaX(), request.getSubAreaY());
        }
        if (request.getRowX() > 0) {
            g2d.drawString(text3, request.getRowX(), request.getRowY());
        }
        if (request.getSeatX() > 0) {
            g2d.drawString(text4, request.getSeatX(), request.getSeatY());
        }
    }
}
    