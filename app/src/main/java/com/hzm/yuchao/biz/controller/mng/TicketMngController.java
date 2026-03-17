package com.hzm.yuchao.biz.controller.mng;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hzm.yuchao.biz.component.OssComponent;
import com.hzm.yuchao.biz.controller.mng.req.DeleteTicketByRowRequest;
import com.hzm.yuchao.biz.controller.mng.resp.TicketSaleCountByRowVO;
import com.hzm.yuchao.biz.controller.mng.resp.TicketSeatVO;
import com.hzm.yuchao.biz.dto.TicketExportDTO;
import com.hzm.yuchao.biz.enums.TicketSaleStatusEnum;
import com.hzm.yuchao.biz.mapper.TicketMapper;
import com.hzm.yuchao.biz.model.TicketDO;
import com.hzm.yuchao.biz.service.MatchService;
import com.hzm.yuchao.biz.service.SkuService;
import com.hzm.yuchao.biz.service.TicketService;
import com.hzm.yuchao.biz.service.VenueService;
import com.hzm.yuchao.simple.ThreadService;
import com.hzm.yuchao.simple.base.SimpleResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.stream.Collectors;

@Api(tags = "MNG-票据管理")
@CrossOrigin
@RestController
@RequestMapping("/mng/ticket/")
public class TicketMngController {

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
    private OssComponent ossComponent;

    @Resource
    private TicketMapper ticketMapper;


    @ApiOperation("按 子区域、排统计销售情况, 用于统计和下钻 ")
    @GetMapping("saleCountByRow")
    public SimpleResponse<List<TicketSaleCountByRowVO>> saleCountByRow(Long skuId) throws IOException {

        List<TicketSaleCountByRowVO> ticketDOList = ticketMapper.saleCountByRow(skuId);

        return SimpleResponse.ok(ticketDOList);
    }

    @ApiOperation("明细, 用于统计和下钻, 两个参数二选一，建议用skuId查，否则数据量会比较大")
    @GetMapping("listAll")
    public SimpleResponse<List<TicketSeatVO>> listAll(Long matchId, Long skuId, String subArea) throws IOException {

        LambdaQueryWrapper<TicketDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(matchId != null, TicketDO::getMatchId, matchId);
        queryWrapper.eq(skuId != null, TicketDO::getSkuId, skuId);
        queryWrapper.eq(StringUtils.isNotEmpty(subArea), TicketDO::getSubArea, subArea);
        queryWrapper.last("limit 5000");
        List<TicketDO> ticketDOList = ticketService.list(queryWrapper);

        List<TicketSeatVO> collect = ticketDOList.stream().map(t -> {
            TicketSeatVO ticketSeatVO = new TicketSeatVO();
            BeanUtils.copyProperties(t, ticketSeatVO);
            return ticketSeatVO;
        }).collect(Collectors.toList());

        return SimpleResponse.ok(collect);
    }

    @ApiOperation("安排删除未售卖的座位，直接物理删除，适用于此票不再售卖的情况")
    @GetMapping("deleteTicketByRow")
    public SimpleResponse<Object> deleteTicketByRow(DeleteTicketByRowRequest request) throws IOException {

        LambdaQueryWrapper<TicketDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TicketDO::getMatchId, request.getMatchId());
        queryWrapper.eq(TicketDO::getSkuId, request.getSkuId());
        queryWrapper.eq(TicketDO::getArea, request.getArea());
        queryWrapper.eq(TicketDO::getSubArea, request.getSubArea());
        queryWrapper.eq(TicketDO::getSeatRow, request.getSeatRow());
        queryWrapper.eq(TicketDO::getSaleStatus, TicketSaleStatusEnum.UNSOLD);
        ticketService.remove(queryWrapper);

        return SimpleResponse.ok();
    }

    @ApiOperation("导出")
    @GetMapping("export")
    public void export(Long matchId, HttpServletResponse response) throws IOException {

        LambdaQueryWrapper<TicketDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TicketDO::getMatchId, matchId);
        queryWrapper.last("limit 20000");
        List<TicketDO> ticketDOList = ticketService.list(queryWrapper);

        List<TicketExportDTO> exportDTOList = ticketDOList.stream().map(t -> {

            TicketExportDTO exportDTO = new TicketExportDTO();

            BeanUtils.copyProperties(t, exportDTO);
            // 转化为元
            exportDTO.setPrice(String.valueOf(t.getPrice() / 100F));

            return exportDTO;

        }).collect(Collectors.toList());

        // 1. 设置响应头信息
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");

        // 2. 处理中文文件名乱码问题
        String fileName = URLEncoder.encode("票据信息", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        // 3. 使用EasyExcel写入响应输出流
        EasyExcel.write(response.getOutputStream(), TicketExportDTO.class)
                .sheet(0) // 工作表名称
                .doWrite(exportDTOList); // 写入数据

    }

}
