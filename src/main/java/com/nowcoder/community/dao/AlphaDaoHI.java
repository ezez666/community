package com.nowcoder.community.dao;

import org.springframework.stereotype.Repository;

@Repository
public class AlphaDaoHI implements AlphaDao {
    @Override
    public String select() {
        return "hi";
    }
}
