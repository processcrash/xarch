package com.xarch.web.auth.service;

import cn.hutool.crypto.SecureUtil;
import com.xarch.common.core.util.IdUtil;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Simple token service (in-memory)
 * In production, use Redis for token storage
 */
@Service
public class XarchTokenService {

    private static final long TOKEN_EXPIRE_HOURS = 24;
    private static final long EXPIRE_MILLIS = TOKEN_EXPIRE_HOURS * 60 * 60 * 1000L;

    private final ConcurrentMap<String, TokenInfo> tokenCache = new ConcurrentHashMap<>();

    public String createToken(String username) {
        String token = generateToken(username);
        long expireTime = System.currentTimeMillis() + EXPIRE_MILLIS;
        tokenCache.put(token, new TokenInfo(username, expireTime));
        return token;
    }

    public boolean validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        TokenInfo info = tokenCache.get(token);
        if (info == null) {
            return false;
        }
        if (System.currentTimeMillis() > info.expireTime) {
            tokenCache.remove(token);
            return false;
        }
        return true;
    }

    public String getUsernameByToken(String token) {
        TokenInfo info = tokenCache.get(token);
        return info != null ? info.username : null;
    }

    public void removeToken(String token) {
        tokenCache.remove(token);
    }

    public long getExpireTime(String token) {
        TokenInfo info = tokenCache.get(token);
        return info != null ? info.expireTime : 0;
    }

    private String generateToken(String username) {
        return SecureUtil.md5(username + System.currentTimeMillis() + IdUtil.uuid());
    }

    private static class TokenInfo {
        final String username;
        final long expireTime;

        TokenInfo(String username, long expireTime) {
            this.username = username;
            this.expireTime = expireTime;
        }
    }
}