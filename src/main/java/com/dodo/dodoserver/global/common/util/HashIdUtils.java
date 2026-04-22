package com.dodo.dodoserver.global.common.util;

import org.hashids.Hashids;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * DB PK(Long)를 난독화된 문자열로 변환하거나 복구하는 유틸리티
 */
@Component
public class HashIdUtils {

    private final Hashids hashids;

    public HashIdUtils(@Value("${hashids.salt:dodo-salt-key}") String salt) {
        // 최소 8자리 이상의 문자열이 생성되도록 설정
        this.hashids = new Hashids(salt, 8);
    }

    /**
     * Long ID를 난독화된 문자열로 변환
     */
    public String encode(Long id) {
        if (id == null) return null;
        return hashids.encode(id);
    }

    /**
     * 난독화된 문자열을 다시 Long ID로 복구
     */
    public Long decode(String hash) {
        if (hash == null || hash.isEmpty()) return null;
        long[] decoded = hashids.decode(hash);
        if (decoded.length > 0) {
            return decoded[0];
        }
        return null;
    }
}
