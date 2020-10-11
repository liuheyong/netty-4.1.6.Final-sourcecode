package io.netty.example.discard;

import java.util.Optional;

/**
 * @author: wenyixicodedog
 * @create: 2020-10-11
 * @description:
 */
public class Test {

    public static void main(String[] args) throws Exception {
        String a = "   ";
        Optional.ofNullable(a).orElseThrow(() -> new Exception("sf"));
    }
}
