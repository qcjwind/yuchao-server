package com.hzm.yuchao.biz.controller.mng;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hzm.yuchao.biz.component.TicketGeneratorComponent;
import com.hzm.yuchao.biz.controller.mng.req.OrderListRequest;
import com.hzm.yuchao.biz.dto.OrderExportDTO;
import com.hzm.yuchao.biz.dto.OrderStatisticsDTO;
import com.hzm.yuchao.biz.mapper.OrderMapper;
import com.hzm.yuchao.biz.model.OrderDO;
import com.hzm.yuchao.biz.service.*;
import com.hzm.yuchao.biz.utils.AmountConverter;
import com.hzm.yuchao.simple.ThreadService;
import com.hzm.yuchao.simple.base.PageResponse;
import com.hzm.yuchao.simple.base.SimpleResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Api(tags = "MNG-订单管理")
@CrossOrigin
@RestController
@RequestMapping("/mng/order/")
public class OrderMngController {

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
    private OrderMapper orderMapper;

    @Resource
    private TicketGeneratorComponent ticketGeneratorComponent;

    @ApiOperation("统计")
    @PostMapping("statistics")
    public SimpleResponse<List<OrderStatisticsDTO>> statistics(Long matchId) {

        List<OrderStatisticsDTO> statisticsDTO = orderMapper.statistics(matchId);

        return SimpleResponse.ok(statisticsDTO);
    }

    @ApiOperation("查询列表")
    @PostMapping("list")
    public PageResponse<OrderDO> list(OrderListRequest request) {

        LambdaQueryWrapper<OrderDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDO::getMatchId, request.getMatchId());
        queryWrapper.like(StringUtils.isNotEmpty(request.getOrderNo()), OrderDO::getOrderNo, request.getOrderNo());
        queryWrapper.like(StringUtils.isNotEmpty(request.getName()), OrderDO::getName, request.getName());
        queryWrapper.eq(request.getOrderStatus() != null, OrderDO::getOrderStatus, request.getOrderStatus());
        queryWrapper.gt(request.getBeginTime() != null, OrderDO::getOrderTime, request.getBeginTime());
        queryWrapper.lt(request.getEndTime() != null, OrderDO::getOrderTime, request.getEndTime());
        queryWrapper.orderByDesc(OrderDO::getId);

        Page<OrderDO> page = new Page<>(request.getPageNumber(), request.getPageSize());

        Page<OrderDO> pageData = orderService.page(page, queryWrapper);

        return PageResponse.ok(pageData);
    }


    @ApiOperation("导出")
    @GetMapping("export")
    public void export(Long matchId, HttpServletResponse response) throws IOException {

        LambdaQueryWrapper<OrderDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDO::getMatchId, matchId);
        queryWrapper.last("limit 20000");
        List<OrderDO> orderDOList = orderService.list(queryWrapper);

        List<OrderExportDTO> exportDTOList = orderDOList.stream().map(t -> {

            OrderExportDTO exportDTO = new OrderExportDTO();

            BeanUtils.copyProperties(t, exportDTO);
            // 转化为元
            exportDTO.setTotalPrice(AmountConverter.toYuan(t.getTotalPrice()));
            exportDTO.setRefundPrice(AmountConverter.toYuan(t.getRefundPrice()));

            return exportDTO;

        }).collect(Collectors.toList());

        // 1. 设置响应头信息
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");

        // 2. 处理中文文件名乱码问题
        String fileName = URLEncoder.encode("订单信息", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        // 3. 使用EasyExcel写入响应输出流
        EasyExcel.write(response.getOutputStream(), OrderExportDTO.class)
                .sheet(0) // 工作表名称
                .doWrite(exportDTOList); // 写入数据

    }
}
