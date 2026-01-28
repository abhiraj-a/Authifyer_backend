package com.Auth.Util;

import com.Auth.Entity.Session;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RefreshResult {
    private String rawRefreshToken;
    private Session session;
}
