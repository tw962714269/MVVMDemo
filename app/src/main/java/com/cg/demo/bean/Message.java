package com.cg.demo.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author:lee
 * @Date:2026/1/21 15:18
 * @Describe:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Message {
    private String name;
    private String content;
    private boolean isTop; // 是否置顶
}
