package com.hzm.yuchao.biz.controller.mng;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hzm.yuchao.biz.component.QrcodeComponent;
import com.hzm.yuchao.biz.controller.mng.req.CreateMatchRequest;
import com.hzm.yuchao.biz.controller.mng.req.QrcodeImageRequest;
import com.hzm.yuchao.biz.controller.mng.resp.MatchTotalVO;
import com.hzm.yuchao.biz.enums.MatchStatusEnum;
import com.hzm.yuchao.biz.model.MatchDO;
import com.hzm.yuchao.biz.model.SkuDO;
import com.hzm.yuchao.biz.model.VenueDO;
import com.hzm.yuchao.biz.service.*;
import com.hzm.yuchao.biz.utils.ImageUrlToBase64Utils;
import com.hzm.yuchao.simple.ThreadService;
import com.hzm.yuchao.simple.base.PageResponse;
import com.hzm.yuchao.simple.base.SimpleResponse;
import com.hzm.yuchao.simple.ratelimit.RateLimit;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;


@Slf4j
@Api(tags = "MNG-赛事管理")
@CrossOrigin
@RestController
@RequestMapping("/mng/match/")
public class MatchMngController {

    @Resource
    private MatchService matchService;

    @Resource
    private VenueService venueService;

    @Resource
    private TicketService ticketService;

    @Resource
    private SkuService skuService;

    @Resource
    private ThreadService threadService;

    @Resource
    private UserService userService;

    @Resource
    private QrcodeComponent qrcodeComponent;

    @ApiOperation("新建赛事")
    @PostMapping("add")
    @RateLimit(qps = 1)
    public SimpleResponse<MatchDO> add(@Valid CreateMatchRequest request) {

        MatchDO matchDO = new MatchDO();
        BeanUtils.copyProperties(request, matchDO);
        matchDO.setStatus(MatchStatusEnum.DISABLE);
        matchDO.setId(null);
        matchService.save(matchDO);

        return SimpleResponse.ok(matchDO);
    }

    @ApiOperation("修改赛事")
    @PostMapping("update")
    public SimpleResponse<Object> update(@Valid CreateMatchRequest request) {

        MatchDO matchDO = new MatchDO();
        BeanUtils.copyProperties(request, matchDO);

        matchService.updateById(matchDO);

        return SimpleResponse.ok();
    }

    @ApiOperation("修改赛事状态-上下架")
    @PostMapping("updateStatus")
    public SimpleResponse<Object> updateStatus(Long id, MatchStatusEnum status) {

        LambdaUpdateWrapper<MatchDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(MatchDO::getStatus, status);
        updateWrapper.eq(MatchDO::getId, id);

        matchService.update(updateWrapper);

        return SimpleResponse.ok();
    }

    @ApiOperation("删除赛事")
    @PostMapping("delete")
    public SimpleResponse<Object> delete(Long id) {

        MatchDO matchDO = matchService.getById(id);

        if (matchDO == null) {
            return SimpleResponse.fail("赛事不存在");
        }
        if (matchDO.getStatus() == MatchStatusEnum.ENABLE) {
            return SimpleResponse.fail("请先关闭赛事再删除");
        }

        matchService.removeById(id);

        return SimpleResponse.ok();
    }

    @ApiOperation("查询赛事")
    @PostMapping("list")
    public PageResponse<MatchDO> list(Integer pageSize, Integer pageNumber) {

        LambdaQueryWrapper<MatchDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(MatchDO::getStatus);
        queryWrapper.orderByDesc(MatchDO::getId);

        Page<MatchDO> page = new Page<>(pageNumber == null ? 1 : pageNumber, pageSize == null ? 10 : pageSize);

        Page<MatchDO> pageData = matchService.page(page, queryWrapper);

        return PageResponse.ok(pageData);
    }

    @ApiOperation("查询赛事详情")
    @PostMapping("info")
    public SimpleResponse<MatchTotalVO> info(Long matchId) {

        if (matchId == null) {
            return SimpleResponse.fail("查询条件有误");
        }

        MatchDO matchDO = matchService.getById(matchId);
        if (matchDO == null) {
            return SimpleResponse.fail("赛程不存在");
        }

        VenueDO venueDO = venueService.getById(matchDO.getVenueId());
        if (venueDO == null) {
            return SimpleResponse.fail("场馆不存在");
        }

        LambdaQueryWrapper<SkuDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SkuDO::getMatchId, matchId);
        List<SkuDO> list = skuService.list(queryWrapper);

        int userCount = (int) userService.count();

        return SimpleResponse.ok(new MatchTotalVO(matchDO, venueDO, list, userCount));
    }


    @ApiOperation("生成赠票二维码，异步的，可能会持续1个小时，请勿重复调用")
    @PostMapping("asyncGenerateQrcode")
    public SimpleResponse<Object> asyncGenerateQrcode(QrcodeImageRequest qrcodeImageRequest) {

        MatchDO matchDO = matchService.getById(qrcodeImageRequest.getMatchId());
        if (matchDO == null) {
            return SimpleResponse.fail("赛程不存在");
        }
        if ("GENERATING".equals(matchDO.getGiftTicketUrl())) {
            return SimpleResponse.fail("正在生成中");
        }

        if (StringUtils.isNotEmpty(qrcodeImageRequest.getBgImageUrl())) {
            String convert = ImageUrlToBase64Utils.convert(qrcodeImageRequest.getBgImageUrl());
            qrcodeImageRequest.setBgImage(convert);
        }

        qrcodeComponent.asyncGenerateQrcode(qrcodeImageRequest);

        LambdaUpdateWrapper<MatchDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(MatchDO::getGiftTicketUrl, "GENERATING");
        updateWrapper.eq(MatchDO::getId, qrcodeImageRequest.getMatchId());
        matchService.update(updateWrapper);

        return SimpleResponse.ok();
    }

    @ApiOperation("预览赠票二维码，返回图片base64")
    @PostMapping("previewGenerateQrcode")
    public SimpleResponse<String> previewGenerateQrcode(QrcodeImageRequest qrcodeImageRequest) throws Exception {

        if (StringUtils.isNotEmpty(qrcodeImageRequest.getBgImageUrl())) {
            String convert = ImageUrlToBase64Utils.convert(qrcodeImageRequest.getBgImageUrl());
            qrcodeImageRequest.setBgImage(convert);
        }

        return SimpleResponse.ok(qrcodeComponent.preview(qrcodeImageRequest));
    }


    @ApiOperation("下载赠票二维码，需要新窗口打开，文件大约1G")
    @GetMapping("downloadQrcode")
    public void downloadQrcode(Long matchId, HttpServletResponse response) {

        if (matchId == null) {
            handleError(response, HttpServletResponse.SC_BAD_REQUEST, "参数不正确");
            return;
        }

        MatchDO matchDO = matchService.getById(matchId);
        if (matchDO == null) {
            handleError(response, HttpServletResponse.SC_BAD_REQUEST, "参数不正确");
            return;
        }
        if (StringUtils.isEmpty(matchDO.getGiftTicketUrl())) {
            handleError(response, HttpServletResponse.SC_BAD_REQUEST, "赠票还未开始生成");
            return;
        }
        if ("GENERATING".equals(matchDO.getGiftTicketUrl())) {
            handleError(response, HttpServletResponse.SC_BAD_REQUEST, "正在生成中");
            return;
        }

        File file = new File(matchDO.getGiftTicketUrl());

        // 3. 验证文件是否存在且为正常文件
        if (!file.exists() || !file.isFile()) {
            handleError(response, HttpServletResponse.SC_BAD_REQUEST, "文件已过期");
            return;
        }

        try {
            // 5. 确定文件MIME类型
            String contentType = "application/octet-stream";

            // 6. 处理文件名编码（解决中文乱码）
            String encodedFileName = URLEncoder.encode(file.getName(), StandardCharsets.UTF_8.name())
                    .replaceAll("\\+", "%20");

            // 设置响应头
            response.setContentType(contentType);
            response.setContentLengthLong(file.length());
            response.setHeader("Content-Disposition",
                    "attachment; filename*=UTF-8''" + encodedFileName);
            response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");

            // 5. 流式传输文件内容（核心：不加载整个文件到内存）
            try (InputStream in = new BufferedInputStream(Files.newInputStream(file.toPath()));
                 OutputStream out = new BufferedOutputStream(response.getOutputStream())) {

                byte[] buffer = new byte[5 * 1024 * 1024]; // 1MB缓冲区
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                out.flush(); // 确保所有数据写入响应
            }

        } catch (Exception e) {
            // 记录错误日志
            log.error("下载文件错误", e);
            handleError(response, HttpServletResponse.SC_BAD_REQUEST, "下载文件失败");
        }
    }
    /**
     * 处理错误响应
     */
    private void handleError(HttpServletResponse response, int statusCode, String message) {
        try {
            response.setStatus(statusCode);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write(message);
        } catch (IOException ignored) {
        }
    }
}
