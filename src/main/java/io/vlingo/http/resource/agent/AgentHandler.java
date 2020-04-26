// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource.agent;

import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import io.vlingo.actors.Logger;
import io.vlingo.http.Body;
import io.vlingo.http.Header;
import io.vlingo.http.Header.Headers;
import io.vlingo.http.Method;
import io.vlingo.http.Request;
import io.vlingo.http.RequestHeader;
import io.vlingo.http.Response;
import io.vlingo.http.Version;
import io.vlingo.wire.channel.RequestResponseContext;
import io.vlingo.wire.channel.ResponseSenderChannel;
import io.vlingo.wire.message.ConsumerByteBuffer;

public class AgentHandler extends SimpleChannelInboundHandler<FullHttpRequest> implements ResponseSenderChannel {
  private static final String AGENT_CONTEXT_NAME = "$AGENT_CONTEXT";
  private static final AttributeKey<AgentChannelContext> AGENT_CONTEXT;

  static {
    AGENT_CONTEXT = AttributeKey.exists(AGENT_CONTEXT_NAME) ?
            AttributeKey.valueOf(AGENT_CONTEXT_NAME) :
            AttributeKey.newInstance(AGENT_CONTEXT_NAME);
  }

  private String contextInstanceId;
  private final Logger logger;
  private final HttpRequestChannelConsumerProvider provider;

  private static final AtomicLong nextInstanceId = new AtomicLong(0);
  private final long instanceId;

  AgentHandler(final HttpRequestChannelConsumerProvider provider, final Logger logger) {
    this.provider = provider;
    this.logger = logger;
    this.instanceId = nextInstanceId.incrementAndGet();
  }

  ////////////////////////////////////
  // SimpleChannelInboundHandler
  ////////////////////////////////////

  @Override
  public void channelActive(final ChannelHandlerContext context) throws Exception {
  logger.debug(">>>>> AgentHandler::channelActive(): " + instanceId + " NAME: " + contextInstanceId(context));
    if (context.channel().isActive()) {
      agentChannelContext(context);
    }
  }

  @Override
  public void channelReadComplete(final ChannelHandlerContext context) {
  logger.debug(">>>>> AgentHandler::channelReadComplete(): " + instanceId + " NAME: " + contextInstanceId(context));
  }

  @Override
  public void exceptionCaught(final ChannelHandlerContext context, final Throwable cause) {
    logger.error("AgentHandler failured because: " + cause.getMessage() + ".\nClosing context: " + contextInstanceId(context), cause);
    context.close();
  }

  @Override
  protected void channelRead0(final ChannelHandlerContext context, final FullHttpRequest request) throws Exception {
    if (HttpUtil.is100ContinueExpected(request)) {
      send100Continue(context);
    }

    provider.httpRequestChannelConsumer().consume(agentChannelContext(context), toConsumable(request));
  }

  @Override
  public void channelUnregistered(final ChannelHandlerContext context) throws Exception {
  logger.debug(">>>>> AgentHandler::channelUnregistered(): " + instanceId + " NAME: " + contextInstanceId(context));
    super.channelUnregistered(context);
  }

  ////////////////////////////////////
  // ResponseSenderChannel
  ////////////////////////////////////

  @Override
  public void abandon(final RequestResponseContext<?> context) {
    final ChannelHandlerContext channelContext = ((AgentChannelContext) context).channelHandlerContext;
//  logger.debug(">>>>> AgentHandler::abandon(): " + instanceId + " NAME: " + contextInstanceId(channelContext));
    channelContext.close();
  }

  @Override
  public void respondWith(final RequestResponseContext<?> context, final ConsumerByteBuffer buffer) {
    respondWith(context, buffer, false);
  }

  @Override
  public void respondWith(final RequestResponseContext<?> context, final ConsumerByteBuffer buffer, final boolean closeFollowing) {
//  logger.debug(">>>>> AgentHandler::respondWith(): " + instanceId + " NAME: " + contextInstanceId + " : CLOSE? " + closeFollowing);

    final AgentChannelContext agentChannelContext = (AgentChannelContext) context;

    final ChannelHandlerContext channelHandlerContext = agentChannelContext.channelHandlerContext();

    final boolean keepAlive = writeResponse(channelHandlerContext, buffer, !closeFollowing);

//  logger.debug(">>>>> AgentHandler::respondWith(): " + instanceId + " NAME: " + contextInstanceId + " : KEEP-ALIVE? " + keepAlive);

    if (!keepAlive) {
      channelHandlerContext.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
//    logger.debug(">>>>> AgentHandler::respondWith(): " + instanceId + " NAME: " + contextInstanceId + " : CLOSED ");
    } else {
      channelHandlerContext.writeAndFlush(Unpooled.EMPTY_BUFFER);
    }
  }

  @Override
  public void respondWith(final RequestResponseContext<?> context, final Object response, final boolean closeFollowing) {
    final Response typedResponse = toResponse(response);

    final FullHttpResponse writable = toWritable(typedResponse);

//  logger.debug("============> AGENT RESPONSE: \n" + writable);

    ChannelHandlerContext channelHandlerContext = agentChannelContext(context).channelHandlerContext();

    channelHandlerContext.write(writable);

    if (closeFollowing) {
      channelHandlerContext.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
//    logger.debug(">>>>> AgentHandler::respondWith(): " + instanceId + " NAME: " + contextInstanceId + " : CLOSED ");
    } else {
      channelHandlerContext.writeAndFlush(Unpooled.EMPTY_BUFFER);
//    logger.debug(">>>>> AgentHandler::respondWith(): " + instanceId + " NAME: " + contextInstanceId + " : FLUSHED ");
    }
  }

  private AgentChannelContext agentChannelContext(final ChannelHandlerContext context) {
    final Channel channel = context.channel();

    if (!channel.hasAttr(AGENT_CONTEXT)) {
      channel.attr(AGENT_CONTEXT).set(new AgentChannelContext(context, this));
    }

    return channel.attr(AGENT_CONTEXT).get();
  }

  private AgentChannelContext agentChannelContext(final RequestResponseContext<?> context) {
    return (AgentChannelContext) context;
  }

  private String contextInstanceId(final ChannelHandlerContext context) {
    if (contextInstanceId == null) {
      contextInstanceId = context.name() + ":" + instanceId;
    }
    return contextInstanceId;
  }

  private static void send100Continue(final ChannelHandlerContext context) {
    final FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, CONTINUE, Unpooled.EMPTY_BUFFER);
    context.write(response);
  }

  private Request toConsumable(final FullHttpRequest request) throws Exception {
    final Method consumableMethod = Method.from(request.method().name());

    final URI consumableURI = new URI(request.uri());

    final Version consumableVersion = Version.Http1_1;

    final Headers<RequestHeader> headers = Headers.empty();

    for (final Map.Entry<String, String> entry : request.headers()) {
      final RequestHeader header = RequestHeader.of(entry.getKey(), entry.getValue());
      headers.add(header);
    }

    final ByteBuf content = request.content();

    final Body body = content.isReadable() ? Body.from(content.toString(CharsetUtil.UTF_8)) : Body.Empty;

    final Request consumableRequest = Request.from(consumableMethod, consumableURI, consumableVersion, headers, body);

//  logger.debug(">>>>> AgentHandler::toConsumable(): " + instanceId + " NAME: " + contextInstanceId + " : REQUEST:\n" + consumableRequest);

    return consumableRequest;
  }

  private <T> Response toResponse(final T response) {
    return (Response) response;
  }

  private FullHttpResponse toWritable(final Response response) {
    final FullHttpResponse httpResponse =
            new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.valueOf(response.status.code),
                    Unpooled.copiedBuffer(response.entity.content(), CharsetUtil.UTF_8),
                    false);

    for (final Header header : response.headers) {
      httpResponse.headers().set(header.name, header.value);
    }

    return httpResponse;
  }

  private boolean writeResponse(final ChannelHandlerContext channelHandlerContext, final ConsumerByteBuffer buffer, final boolean keepAlive) {
    final ByteBuf replyBuffer = channelHandlerContext.alloc().buffer(buffer.limit());

    replyBuffer.writeBytes(buffer.asByteBuffer());

    channelHandlerContext.write(replyBuffer);

    return keepAlive;
  }

  private static class AgentChannelContext extends ChannelInboundHandlerAdapter implements RequestResponseContext<ConsumerByteBuffer> {
    private static final AtomicLong contextId = new AtomicLong(0);

    private final ChannelHandlerContext channelHandlerContext;
    @SuppressWarnings("unused")
    private Object closingData;
    private Object consumerData;
    private final String id;
    private final ResponseSenderChannel sender;

    AgentChannelContext(final ChannelHandlerContext channelHandlerContext, final ResponseSenderChannel sender) {
      this.channelHandlerContext = channelHandlerContext;
      this.sender = sender;
      this.id = "" + contextId.incrementAndGet();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T consumerData() {
      return (T) consumerData;
    }

    @Override
    public <T> T consumerData(final T workingData) {
      this.consumerData = workingData;
      return workingData;
    }

    @Override
    public boolean hasConsumerData() {
      return consumerData != null;
    }

    @Override
    public String id() {
      return id;
    }

    @Override
    public ResponseSenderChannel sender() {
      return this.sender;
    }

    @Override
    public void whenClosing(final Object data) {
      this.closingData = data;
    }

    ChannelHandlerContext channelHandlerContext() {
      return channelHandlerContext;
    }
  }
}
