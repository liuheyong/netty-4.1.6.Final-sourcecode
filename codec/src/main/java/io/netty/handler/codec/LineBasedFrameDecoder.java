/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.handler.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ByteProcessor;

import java.util.List;

/**
 * A decoder that splits the received {@link ByteBuf}s on line endings.
 * <p>
 * Both {@code "\n"} and {@code "\r\n"} are handled.
 * For a more general delimiter-based decoder, see {@link DelimiterBasedFrameDecoder}.
 */
// TODO 基于换行符的解码器
public class LineBasedFrameDecoder extends ByteToMessageDecoder {

    // ==========================关键属性=====================
    /** 我们解码的最大长度*/
    private final int maxLength;
    /** 一旦解码超过maxLength，是否触发快速失败机制 默认为false*/
    private final boolean failFast;
    /** 读取的数据是否要包含换行符 默认为true*/
    private final boolean stripDelimiter;
    /** 标识是否开启丢弃模式，默认false.*/
    private boolean discarding;
    /** 丢弃字节的长度*/
    private int discardedBytes;

    /**
     * Creates a new decoder.
     * @param maxLength  the maximum length of the decoded frame.
     *                   A {@link TooLongFrameException} is thrown if
     *                   the length of the frame exceeds this value.
     */
    public LineBasedFrameDecoder(final int maxLength) {
        this(maxLength, true, false);
    }

    /**
     * Creates a new decoder.
     * @param maxLength  the maximum length of the decoded frame.
     *                   A {@link TooLongFrameException} is thrown if
     *                   the length of the frame exceeds this value.
     * @param stripDelimiter  whether the decoded frame should strip out the
     *                        delimiter or not
     * @param failFast  If <tt>true</tt>, a {@link TooLongFrameException} is
     *                  thrown as soon as the decoder notices the length of the
     *                  frame will exceed <tt>maxFrameLength</tt> regardless of
     *                  whether the entire frame has been read.
     *                  If <tt>false</tt>, a {@link TooLongFrameException} is
     *                  thrown after the entire frame that exceeds
     *                  <tt>maxFrameLength</tt> has been read.
     */
    public LineBasedFrameDecoder(final int maxLength, final boolean stripDelimiter, final boolean failFast) {
        this.maxLength = maxLength;
        this.failFast = failFast;
        this.stripDelimiter = stripDelimiter;
    }

    @Override
    protected final void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        Object decoded = decode(ctx, in);
        if (decoded != null) {
            out.add(decoded);
        }
    }

    /**
     * Create a frame out of the {@link ByteBuf} and return it.
     *
     * @param   ctx             the {@link ChannelHandlerContext} which this {@link ByteToMessageDecoder} belongs to
     * @param   buffer          the {@link ByteBuf} from which to read data
     * @return  frame           the {@link ByteBuf} which represent the frame or {@code null} if no frame could
     *                          be created.
     */
    protected Object decode(ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {
        // 在buffer中搜索换行符
        final int eol = findEndOfLine(buffer);
        // 如果是非丢弃模式,正常往下执行
        if (!discarding) {
            // 如果缓冲区中存在换行符
            if (eol >= 0) {
                // frame用以保存下面读取到的数据
                final ByteBuf frame;
                // readerindex和eol之间就是可读范围
                final int length = eol - buffer.readerIndex();
                //标识\r or \r\n
                final int delimLength = buffer.getByte(eol) == '\r'? 2 : 1;
                // 如果本次可读的范围超过了最大阈值,那么跳过这段数据
                if (length > maxLength) {
                    // 设置readerIndex，跳到该换行符之后
                    buffer.readerIndex(eol + delimLength);
                    // 抛出异常信息
                    fail(ctx, length);
                    return null;
                }
                // 最终读取的数据是否要跳过换行符
                if (stripDelimiter) {
                    //跳过
                    frame = buffer.readRetainedSlice(length);
                    buffer.skipBytes(delimLength);
                } else {
                    //不跳过
                    frame = buffer.readRetainedSlice(length + delimLength);
                }
                return frame;
            } else {
                // 如果在buffer中没有搜索到换行符
                final int length = buffer.readableBytes();
                // 且可读的范围超过了最大阈值,那么直接跳过这段
                if (length > maxLength) {
                    discardedBytes = length;
                    // 将readerindex移动到writerindex,表示这一段数据全部丢弃
                    buffer.readerIndex(buffer.writerIndex());
                    // 进入丢弃模式
                    discarding = true;
                    // 如果开启了快速失败机制，则触发
                    if (failFast) {
                        fail(ctx, "over " + discardedBytes);
                    }
                }
                return null;
            }
            // 如果是丢弃模式
        } else {
            // 如果搜索到换行符, 那么这一段直接丢弃
            if (eol >= 0) {
                // 计算丢弃长度
                final int length = discardedBytes + eol - buffer.readerIndex();
                //标识\r or \r\n
                final int delimLength = buffer.getByte(eol) == '\r'? 2 : 1;
                // 直接跳到换行符之后
                buffer.readerIndex(eol + delimLength);
                // 丢弃模式转正常模式
                discardedBytes = 0;
                discarding = false;
                // 如果开启了快速失败机制，则触发
                if (!failFast) {
                    fail(ctx, length);
                }
            } else {
                // 如果还没有找到换行符, 那么这次的全部丢弃
                discardedBytes += buffer.readableBytes();
                buffer.readerIndex(buffer.writerIndex());
            }
            return null;
        }
    }

    private void fail(final ChannelHandlerContext ctx, int length) {
        fail(ctx, String.valueOf(length));
    }

    private void fail(final ChannelHandlerContext ctx, String length) {
        ctx.fireExceptionCaught(
                new TooLongFrameException(
                        "frame length (" + length + ") exceeds the allowed maximum (" + maxLength + ')'));
    }

    /**
     * Returns the index in the buffer of the end of line found.
     * Returns -1 if no end of line was found in the buffer.
     */
    private static int findEndOfLine(final ByteBuf buffer) {
        int i = buffer.forEachByte(ByteProcessor.FIND_LF);
        if (i > 0 && buffer.getByte(i - 1) == '\r') {
            i--;
        }
        return i;
    }
}
