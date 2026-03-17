package com.hzm.yuchao.biz.controller.mng;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hzm.yuchao.biz.controller.mng.req.CreateBannerRequest;
import com.hzm.yuchao.biz.enums.BooleanEnum;
import com.hzm.yuchao.biz.model.BannerDO;
import com.hzm.yuchao.biz.service.BannerService;
import com.hzm.yuchao.simple.base.SimpleResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@Api(tags = "MNG-Banner管理")
@CrossOrigin
@RestController
@RequestMapping("/mng/banner/")
public class BannerMngController {

    @Resource
    private BannerService bannerService;

    @ApiOperation("新建 Banner")
    @PostMapping("add")
    public SimpleResponse<BannerDO> add(@Valid CreateBannerRequest request) {
        BannerDO bannerDO = new BannerDO();
        BeanUtils.copyProperties(request, bannerDO);
        bannerDO.setId(null);
        bannerDO.setDeleted(BooleanEnum.N);
        bannerService.save(bannerDO);
        return SimpleResponse.ok(bannerDO);
    }

    @ApiOperation("修改 Banner")
    @PostMapping("update")
    public SimpleResponse<Object> update(@Valid CreateBannerRequest request) {
        BannerDO bannerDO = new BannerDO();
        BeanUtils.copyProperties(request, bannerDO);
        bannerService.updateById(bannerDO);
        return SimpleResponse.ok();
    }

    @ApiOperation("逻辑删除 Banner")
    @PostMapping("delete")
    public SimpleResponse<Object> delete(Long id) {
        if (id == null) {
            return SimpleResponse.fail("id 不能为空");
        }
        BannerDO bannerDO = new BannerDO();
        bannerDO.setId(id);
        bannerDO.setDeleted(BooleanEnum.Y);
        bannerService.updateById(bannerDO);
        return SimpleResponse.ok();
    }

    @ApiOperation("查询 Banner 列表（不分页）")
    @PostMapping("list")
    public SimpleResponse<List<BannerDO>> list() {
        LambdaQueryWrapper<BannerDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ne(BannerDO::getDeleted, BooleanEnum.Y);
        queryWrapper.orderByAsc(BannerDO::getSortNumber)
                .orderByDesc(BannerDO::getGmtCreate);
        List<BannerDO> list = bannerService.list(queryWrapper);
        return SimpleResponse.ok(list);
    }
}

