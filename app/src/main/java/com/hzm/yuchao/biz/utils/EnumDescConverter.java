package com.hzm.yuchao.biz.utils;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

/**
 * 修复泛型类型问题的枚举转换器
 */
@SuppressWarnings("unchecked")
public class EnumDescConverter implements Converter<Enum<?>> { // 明确实现Converter<Enum<?>>

    @Override
    public Class<Enum<?>> supportJavaTypeKey() {
        // 添加unchecked强制转换并抑制警告
        return (Class<Enum<?>>) (Class<?>) Enum.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public WriteCellData<String> convertToExcelData(Enum<?> value, ExcelContentProperty contentProperty,
                                                    GlobalConfiguration globalConfiguration) throws Exception {
        if (value == null) {
            return new WriteCellData<>("");
        }
        // 调用枚举的getDesc()方法获取中文描述
        String desc = (String) value.getClass().getMethod("getDesc").invoke(value);
        return new WriteCellData<>(desc);
    }
}

    