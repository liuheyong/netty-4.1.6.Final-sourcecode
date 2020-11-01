package io.netty.example.byteOrder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.Arrays;

/**
 * @author: wenyixicodedog
 * @create: 2020-11-01
 * @description: netty 大小端
 */
public class ByteOrder {

    private static final int writeInt = 345;

    public static void main(String[] args) {
        ByteBuf byteBuf = Unpooled.buffer(4,4);
        byteBuf.writeIntLE(writeInt);
        byte[] arr = new byte[4];
        byteBuf.readBytes(arr);
        System.out.println(Arrays.toString(arr));
    }

}
