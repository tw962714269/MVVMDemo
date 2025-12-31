package com.cg.demo.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author:lee
 * @Date:2025/8/22 10:05
 * @Describe:
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageEvent {
    private Object msg;
    private Object sender;
}
