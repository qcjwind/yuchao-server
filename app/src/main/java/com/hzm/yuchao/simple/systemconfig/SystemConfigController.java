//package com.hzm.yuchao.simple.systemconfig;
//
//
//import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
//import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
//import com.hzm.yuchao.biz.controller.mng.req.CreateVenueRequest;
//import com.hzm.yuchao.biz.model.VenueDO;
//import com.hzm.yuchao.biz.service.MatchService;
//import com.hzm.yuchao.biz.service.SkuService;
//import com.hzm.yuchao.biz.service.TicketService;
//import com.hzm.yuchao.biz.service.VenueService;
//import com.hzm.yuchao.simple.ThreadService;
//import com.hzm.yuchao.simple.base.PageResponse;
//import com.hzm.yuchao.simple.base.SimpleResponse;
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
//import org.springframework.beans.BeanUtils;
//import org.springframework.web.bind.annotation.CrossOrigin;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import javax.annotation.Resource;
//import javax.validation.Valid;
//
//@Api(tags = "MNG-场馆管理")
//@CrossOrigin
//@RestController
//@RequestMapping("/mng/systemConfig/")
//public class SystemConfigController {
//
//    @Resource
//    private SystemConfigService systemConfigService;
//
//    @ApiOperation("新建场馆")
//    @PostMapping("add")
//    public SimpleResponse<VenueDO> add(@Valid CreateVenueRequest request) {
//
//        VenueDO venueDO = new VenueDO();
//        BeanUtils.copyProperties(request, venueDO);
//        venueDO.setId(null);
//        systemConfigService.save(venueDO);
//
//        return SimpleResponse.ok(venueDO);
//    }
//
//    @ApiOperation("修改场馆")
//    @PostMapping("update")
//    public SimpleResponse<Object> update(@Valid CreateVenueRequest request) {
//
//        VenueDO venueDO = new VenueDO();
//        BeanUtils.copyProperties(request, venueDO);
//
//        venueService.updateById(venueDO);
//
//        return SimpleResponse.ok();
//    }
//
////    @ApiOperation("删除赛事")
////    @PostMapping("delete")
////    public SimpleResponse<Object> delete(Long id) {
////
////        venueService.removeById(id);
////
////        return SimpleResponse.ok();
////    }
//
//    @ApiOperation("查询场馆")
//    @PostMapping("list")
//    public PageResponse<VenueDO> list(Integer pageSize, Integer pageNumber) {
//
//        LambdaQueryWrapper<VenueDO> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.orderByDesc(VenueDO::getId);
//
//        Page<VenueDO> page = new Page<>(pageNumber == null ? 1 : pageNumber, pageSize == null ? 10 : pageSize);
//
//        Page<VenueDO> pageData = venueService.page(page, queryWrapper);
//
//        return PageResponse.ok(pageData);
//    }
//}
