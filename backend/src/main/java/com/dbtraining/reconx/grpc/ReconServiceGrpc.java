package com.dbtraining.reconx.grpc;

import com.dbtraining.reconx.grpc.ReconGrpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@io.grpc.stub.annotations.GrpcGenerated
public final class ReconServiceGrpc {

  private ReconServiceGrpc() {}

  public static final String SERVICE_NAME = "ReconService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<ReconGrpc.TradeGrpc,
      ReconGrpc.ReconResultGrpc> getReconcileTradesGrpcMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ReconcileTradesGrpc",
      requestType = ReconGrpc.TradeGrpc.class,
      responseType = ReconGrpc.ReconResultGrpc.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<ReconGrpc.TradeGrpc,
      ReconGrpc.ReconResultGrpc> getReconcileTradesGrpcMethod() {
    io.grpc.MethodDescriptor<ReconGrpc.TradeGrpc, ReconGrpc.ReconResultGrpc> getReconcileTradesGrpcMethod;
    if ((getReconcileTradesGrpcMethod = ReconServiceGrpc.getReconcileTradesGrpcMethod) == null) {
      synchronized (ReconServiceGrpc.class) {
        if ((getReconcileTradesGrpcMethod = ReconServiceGrpc.getReconcileTradesGrpcMethod) == null) {
          ReconServiceGrpc.getReconcileTradesGrpcMethod = getReconcileTradesGrpcMethod =
              io.grpc.MethodDescriptor.<ReconGrpc.TradeGrpc, ReconGrpc.ReconResultGrpc>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ReconcileTradesGrpc"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ReconGrpc.TradeGrpc.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ReconGrpc.ReconResultGrpc.getDefaultInstance()))
              .setSchemaDescriptor(new ReconServiceMethodDescriptorSupplier("ReconcileTradesGrpc"))
              .build();
        }
      }
    }
    return getReconcileTradesGrpcMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static ReconServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ReconServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ReconServiceStub>() {
        @Override
        public ReconServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ReconServiceStub(channel, callOptions);
        }
      };
    return ReconServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports all types of calls on the service
   */
  public static ReconServiceBlockingV2Stub newBlockingV2Stub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ReconServiceBlockingV2Stub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ReconServiceBlockingV2Stub>() {
        @Override
        public ReconServiceBlockingV2Stub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ReconServiceBlockingV2Stub(channel, callOptions);
        }
      };
    return ReconServiceBlockingV2Stub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static ReconServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ReconServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ReconServiceBlockingStub>() {
        @Override
        public ReconServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ReconServiceBlockingStub(channel, callOptions);
        }
      };
    return ReconServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static ReconServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ReconServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ReconServiceFutureStub>() {
        @Override
        public ReconServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ReconServiceFutureStub(channel, callOptions);
        }
      };
    return ReconServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default io.grpc.stub.StreamObserver<ReconGrpc.TradeGrpc> reconcileTradesGrpc(
        io.grpc.stub.StreamObserver<ReconGrpc.ReconResultGrpc> responseObserver) {
      return io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall(getReconcileTradesGrpcMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service ReconService.
   */
  public static abstract class ReconServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @Override public final io.grpc.ServerServiceDefinition bindService() {
      return ReconServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service ReconService.
   */
  public static final class ReconServiceStub
      extends io.grpc.stub.AbstractAsyncStub<ReconServiceStub> {
    private ReconServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected ReconServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ReconServiceStub(channel, callOptions);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<ReconGrpc.TradeGrpc> reconcileTradesGrpc(
        io.grpc.stub.StreamObserver<ReconGrpc.ReconResultGrpc> responseObserver) {
      return io.grpc.stub.ClientCalls.asyncBidiStreamingCall(
          getChannel().newCall(getReconcileTradesGrpcMethod(), getCallOptions()), responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service ReconService.
   */
  public static final class ReconServiceBlockingV2Stub
      extends io.grpc.stub.AbstractBlockingStub<ReconServiceBlockingV2Stub> {
    private ReconServiceBlockingV2Stub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected ReconServiceBlockingV2Stub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ReconServiceBlockingV2Stub(channel, callOptions);
    }

    /**
     */
    @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/10918")
    public io.grpc.stub.BlockingClientCall<ReconGrpc.TradeGrpc, ReconGrpc.ReconResultGrpc>
        reconcileTradesGrpc() {
      return io.grpc.stub.ClientCalls.blockingBidiStreamingCall(
          getChannel(), getReconcileTradesGrpcMethod(), getCallOptions());
    }
  }

  /**
   * A stub to allow clients to do limited synchronous rpc calls to service ReconService.
   */
  public static final class ReconServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<ReconServiceBlockingStub> {
    private ReconServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected ReconServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ReconServiceBlockingStub(channel, callOptions);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service ReconService.
   */
  public static final class ReconServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<ReconServiceFutureStub> {
    private ReconServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected ReconServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ReconServiceFutureStub(channel, callOptions);
    }
  }

  private static final int METHODID_RECONCILE_TRADES_GRPC = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }

    @Override
    @SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_RECONCILE_TRADES_GRPC:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.reconcileTradesGrpc(
              (io.grpc.stub.StreamObserver<ReconGrpc.ReconResultGrpc>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getReconcileTradesGrpcMethod(),
          io.grpc.stub.ServerCalls.asyncBidiStreamingCall(
            new MethodHandlers<
              ReconGrpc.TradeGrpc,
              ReconGrpc.ReconResultGrpc>(
                service, METHODID_RECONCILE_TRADES_GRPC)))
        .build();
  }

  private static abstract class ReconServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    ReconServiceBaseDescriptorSupplier() {}

    @Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return ReconGrpc.getDescriptor();
    }

    @Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("ReconService");
    }
  }

  private static final class ReconServiceFileDescriptorSupplier
      extends ReconServiceBaseDescriptorSupplier {
    ReconServiceFileDescriptorSupplier() {}
  }

  private static final class ReconServiceMethodDescriptorSupplier
      extends ReconServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    ReconServiceMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (ReconServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new ReconServiceFileDescriptorSupplier())
              .addMethod(getReconcileTradesGrpcMethod())
              .build();
        }
      }
    }
    return result;
  }
}
