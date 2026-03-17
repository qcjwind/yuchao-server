package com.hzm.yuchao.biz.controller.mng;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hzm.yuchao.biz.component.TicketGeneratorComponent;
import com.hzm.yuchao.biz.controller.mng.req.CreateSkuRequest;
import com.hzm.yuchao.biz.controller.mng.req.UpdateSkuRequest;
import com.hzm.yuchao.biz.enums.MatchSaleStatusEnum;
import com.hzm.yuchao.biz.enums.MatchStatusEnum;
import com.hzm.yuchao.biz.enums.SkuStatusEnum;
import com.hzm.yuchao.biz.enums.TicketSaleStatusEnum;
import com.hzm.yuchao.biz.model.MatchDO;
import com.hzm.yuchao.biz.model.OrderDO;
import com.hzm.yuchao.biz.model.SkuDO;
import com.hzm.yuchao.biz.model.TicketDO;
import com.hzm.yuchao.biz.service.*;
import com.hzm.yuchao.simple.BizException;
import com.hzm.yuchao.simple.ThreadService;
import com.hzm.yuchao.simple.base.PageResponse;
import com.hzm.yuchao.simple.base.SimpleResponse;
import com.hzm.yuchao.tools.ExcelTo2DArray;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Api(tags = "MNG-sku管理")
@CrossOrigin
@RestController
@RequestMapping("/mng/sku/")
public class SkuMngController {

    @Resource
    private MatchService matchService;

    @Resource
    private OrderService orderService;

    @Resource
    private TicketService ticketService;

    @Resource
    private SkuService skuService;

    @Resource
    private ThreadService threadService;

    @Resource
    private UserService userService;

    @Resource
    private TicketGeneratorComponent ticketGeneratorComponent;

    @ApiOperation("新建sku")
    @PostMapping("add")
    public SimpleResponse<SkuDO> add(@Valid CreateSkuRequest request) {

        MatchDO matchDO = matchService.getById(request.getMatchId());
        if (matchDO == null) {
            return SimpleResponse.fail("赛程不存在");
        }

        SkuDO skuDO = new SkuDO();
        BeanUtils.copyProperties(request, skuDO);
        skuDO.setVenueId(matchDO.getVenueId());
        skuDO.setId(null);
        skuService.save(skuDO);

        return SimpleResponse.ok(skuDO);
    }

    @ApiOperation("修改sku")
    @PostMapping("update")
    public SimpleResponse<Object> update(@Valid UpdateSkuRequest request) {

        SkuDO skuDO = new SkuDO();
        BeanUtils.copyProperties(request, skuDO);

        skuService.updateById(skuDO);

        return SimpleResponse.ok();
    }


    @ApiOperation("上下架sku，上架前，前端需弹窗提醒用户先设置金额")
    @PostMapping("updateStatus")
    public SimpleResponse<Object> updateStatus(Long id, SkuStatusEnum status) {

        LambdaUpdateWrapper<SkuDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(SkuDO::getSkuStatus, status);
        updateWrapper.eq(SkuDO::getId, id);

        skuService.update(updateWrapper);

        return SimpleResponse.ok();

    }

    @ApiOperation("删除sku 及 票码")
    @PostMapping("delete")
    @Transactional
    public SimpleResponse<Object> delete(Long id) {

        SkuDO skuDO = skuService.getById(id);
        if (skuDO == null) {
            return SimpleResponse.fail("商品不存在");
        }
        if (skuDO.getSkuStatus() != SkuStatusEnum.DISABLE) {
            return SimpleResponse.fail("请先下架商品，再进行删除");
        }

//        MatchDO matchDO = matchService.getById(skuDO.getMatchId());
//        if (matchDO.getStatus() != MatchStatusEnum.DISABLE) {
//            return SimpleResponse.fail("请先下架赛程，再进行删除");
//        }

        LambdaQueryWrapper<OrderDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDO::getSkuId, id);
        queryWrapper.last("limit 1");
        OrderDO orderDO = orderService.getOne(queryWrapper);
        if (orderDO != null) {
            return SimpleResponse.fail("商品已产生订单，不可删除");
        }

        LambdaQueryWrapper<TicketDO> ticketQueryWrapper = new LambdaQueryWrapper<>();
        ticketQueryWrapper.eq(TicketDO::getSkuId, id);
        ticketQueryWrapper.eq(TicketDO::getSaleStatus, TicketSaleStatusEnum.SOLD);
        ticketQueryWrapper.last("limit 1");
        TicketDO ticketDO = ticketService.getOne(ticketQueryWrapper);
        if (ticketDO != null) {
            return SimpleResponse.fail("商品已有售出票码（含已激活的增票），不可删除");
        }

        // 删除票码
        LambdaUpdateWrapper<TicketDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(TicketDO::getSkuId, id);
        ticketService.remove(updateWrapper);

        // 删除sku
        skuService.removeById(id);

        return SimpleResponse.ok();
    }

    @ApiOperation("查询列表")
    @PostMapping("list")
    public PageResponse<SkuDO> list(Long matchId, Integer pageSize, Integer pageNumber) {

        LambdaQueryWrapper<SkuDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SkuDO::getMatchId, matchId);
        queryWrapper.orderByAsc(SkuDO::getSortNumber);
        queryWrapper.orderByDesc(SkuDO::getSkuType);

        Page<SkuDO> page = new Page<>(pageNumber == null ? 1 : pageNumber, pageSize == null ? 10 : pageSize);

        Page<SkuDO> pageData = skuService.page(page, queryWrapper);

        return PageResponse.ok(pageData);
    }


    @ApiOperation("单个sku-配置或增加座位")
    @PostMapping("uploadSeats")
    public SimpleResponse<Object> uploadSeats(Long skuId, @RequestParam("file") MultipartFile file) {

        SkuDO skuDO = skuService.getById(skuId);

        if (skuDO == null) {
            return SimpleResponse.fail("商品不存在");
        }

        try {
            Workbook workbook = ExcelTo2DArray.getWorkbook(file.getInputStream(), file.getOriginalFilename());

            if (workbook.getNumberOfSheets() > 1) {
                return SimpleResponse.fail("excel文件有多个sheet, 安全起见，请删除无用的sheet");
            }

            ticketGeneratorComponent.generateTicket(skuDO,
                ExcelTo2DArray.excelTo2DArray(file.getInputStream(), file.getOriginalFilename(), 0));

            return SimpleResponse.ok();
        } catch (DuplicateKeyException e) {
            log.error("sku生成失败", e);
            return SimpleResponse.fail("座位存在重复，请检查excel内容是否正确");
        } catch (Exception e) {
            log.error("sku生成失败", e);
            return SimpleResponse.fail("商品生成失败，请检查excel内容是否正确");
        }
    }

    @ApiOperation("批量上传-会自动生成sku信息")
    @PostMapping("uploadBatch")
    public SimpleResponse<Object> uploadBatch(Long matchId, @RequestParam("file") MultipartFile file) {

        MatchDO matchDO = matchService.getById(matchId);
        if (matchDO.getStatus() != MatchStatusEnum.DISABLE) {
            return SimpleResponse.fail("请先下架赛程，再进行导入");
        }
        if (matchDO.getSaleStatus() != MatchSaleStatusEnum.NOT_FINISH) {
            return SimpleResponse.fail("活动已结束");
        }

        LambdaQueryWrapper<SkuDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SkuDO::getMatchId, matchId);
        queryWrapper.last("limit 1");
        SkuDO skuDO = skuService.getOne(queryWrapper);
        if (skuDO != null) {
            return SimpleResponse.fail("为了数据安全，每个赛程仅支持一次批量导入");
        }

        try {
            ticketGeneratorComponent.generateTicket(matchDO,
                    ExcelTo2DArray.excelTo2DArray(file.getInputStream(), file.getOriginalFilename(), 0),
                    ExcelTo2DArray.excelTo2DArray(file.getInputStream(), file.getOriginalFilename(), 1));

            return SimpleResponse.ok();
        } catch (Exception e) {
            log.error("sku生成失败", e);
            return SimpleResponse.fail("商品生成失败，请检查excel内容是否正确");
        }
    }

    @ApiOperation("单个sku配置或增加座位-示例文件下载")
    @GetMapping("uploadSeatsDemo")
    public void uploadSeatsDemo(HttpServletResponse response) {
        download("templates/single.xls", "单个上传示例.xls", response);
    }

    @ApiOperation("批量上传-示例文件下载")
    @GetMapping("uploadBatchDemo")
    public void uploadBatchDemo(HttpServletResponse response) {
        download("templates/all.xls", "批量上传示例.xls", response);
    }

    private void download(String fileName, String downloadFileName, HttpServletResponse response) {

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            // 1. 获取resource目录下的文件资源
            ClassPathResource resource = new ClassPathResource(fileName);
            if (!resource.exists()) {
                throw new BizException("文件不存在: " + fileName);
            }

            // 2. 设置响应头
            // 根据文件后缀设置Content-Type
            if (fileName.endsWith(".xlsx")) {
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            } else if (fileName.endsWith(".xls")) {
                response.setContentType("application/vnd.ms-excel");
            }

            // 处理中文文件名
            String encodedFileName = URLEncoder.encode(downloadFileName, StandardCharsets.UTF_8.name())
                    .replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + encodedFileName);

            // 3. 读取文件并写入响应流
            inputStream = resource.getInputStream();
            outputStream = response.getOutputStream();

            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            outputStream.flush();

        } catch (Exception e) {
            // 可以根据需要处理异常，例如返回错误信息
            try {
                response.setContentType("text/plain;charset=utf-8");
                response.getWriter().write("文件下载失败: " + e.getMessage());
            } catch (IOException ignored) {
            }
        } finally {
            // 4. 关闭流
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException ignored) {
            }
        }
    }
}
