package com.hzm.yuchao.simple;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenDTO {

    private long userId;

    private String username;

    // APP, MNG
    private PlatformEnum platform;

    private long currentTime;

    public enum PlatformEnum {
        APP,

        MNG,
    }


}
