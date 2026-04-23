package br.ufrn.imd.pd.common.proto;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.68.0)",
    comments = "Source: paxos_service.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class PaxosNodeServiceGrpc {

  private PaxosNodeServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "br.ufrn.imd.pd.PaxosNodeService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<br.ufrn.imd.pd.common.proto.GenericRequest,
      br.ufrn.imd.pd.common.proto.GenericResponse> getExchangeMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Exchange",
      requestType = br.ufrn.imd.pd.common.proto.GenericRequest.class,
      responseType = br.ufrn.imd.pd.common.proto.GenericResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<br.ufrn.imd.pd.common.proto.GenericRequest,
      br.ufrn.imd.pd.common.proto.GenericResponse> getExchangeMethod() {
    io.grpc.MethodDescriptor<br.ufrn.imd.pd.common.proto.GenericRequest, br.ufrn.imd.pd.common.proto.GenericResponse> getExchangeMethod;
    if ((getExchangeMethod = PaxosNodeServiceGrpc.getExchangeMethod) == null) {
      synchronized (PaxosNodeServiceGrpc.class) {
        if ((getExchangeMethod = PaxosNodeServiceGrpc.getExchangeMethod) == null) {
          PaxosNodeServiceGrpc.getExchangeMethod = getExchangeMethod =
              io.grpc.MethodDescriptor.<br.ufrn.imd.pd.common.proto.GenericRequest, br.ufrn.imd.pd.common.proto.GenericResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Exchange"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  br.ufrn.imd.pd.common.proto.GenericRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  br.ufrn.imd.pd.common.proto.GenericResponse.getDefaultInstance()))
              .setSchemaDescriptor(new PaxosNodeServiceMethodDescriptorSupplier("Exchange"))
              .build();
        }
      }
    }
    return getExchangeMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static PaxosNodeServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<PaxosNodeServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<PaxosNodeServiceStub>() {
        @java.lang.Override
        public PaxosNodeServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new PaxosNodeServiceStub(channel, callOptions);
        }
      };
    return PaxosNodeServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static PaxosNodeServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<PaxosNodeServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<PaxosNodeServiceBlockingStub>() {
        @java.lang.Override
        public PaxosNodeServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new PaxosNodeServiceBlockingStub(channel, callOptions);
        }
      };
    return PaxosNodeServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static PaxosNodeServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<PaxosNodeServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<PaxosNodeServiceFutureStub>() {
        @java.lang.Override
        public PaxosNodeServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new PaxosNodeServiceFutureStub(channel, callOptions);
        }
      };
    return PaxosNodeServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void exchange(br.ufrn.imd.pd.common.proto.GenericRequest request,
        io.grpc.stub.StreamObserver<br.ufrn.imd.pd.common.proto.GenericResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getExchangeMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service PaxosNodeService.
   */
  public static abstract class PaxosNodeServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return PaxosNodeServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service PaxosNodeService.
   */
  public static final class PaxosNodeServiceStub
      extends io.grpc.stub.AbstractAsyncStub<PaxosNodeServiceStub> {
    private PaxosNodeServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected PaxosNodeServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new PaxosNodeServiceStub(channel, callOptions);
    }

    /**
     */
    public void exchange(br.ufrn.imd.pd.common.proto.GenericRequest request,
        io.grpc.stub.StreamObserver<br.ufrn.imd.pd.common.proto.GenericResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getExchangeMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service PaxosNodeService.
   */
  public static final class PaxosNodeServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<PaxosNodeServiceBlockingStub> {
    private PaxosNodeServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected PaxosNodeServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new PaxosNodeServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public br.ufrn.imd.pd.common.proto.GenericResponse exchange(br.ufrn.imd.pd.common.proto.GenericRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getExchangeMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service PaxosNodeService.
   */
  public static final class PaxosNodeServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<PaxosNodeServiceFutureStub> {
    private PaxosNodeServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected PaxosNodeServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new PaxosNodeServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<br.ufrn.imd.pd.common.proto.GenericResponse> exchange(
        br.ufrn.imd.pd.common.proto.GenericRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getExchangeMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_EXCHANGE = 0;

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

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_EXCHANGE:
          serviceImpl.exchange((br.ufrn.imd.pd.common.proto.GenericRequest) request,
              (io.grpc.stub.StreamObserver<br.ufrn.imd.pd.common.proto.GenericResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getExchangeMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              br.ufrn.imd.pd.common.proto.GenericRequest,
              br.ufrn.imd.pd.common.proto.GenericResponse>(
                service, METHODID_EXCHANGE)))
        .build();
  }

  private static abstract class PaxosNodeServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    PaxosNodeServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return br.ufrn.imd.pd.common.proto.PaxosService.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("PaxosNodeService");
    }
  }

  private static final class PaxosNodeServiceFileDescriptorSupplier
      extends PaxosNodeServiceBaseDescriptorSupplier {
    PaxosNodeServiceFileDescriptorSupplier() {}
  }

  private static final class PaxosNodeServiceMethodDescriptorSupplier
      extends PaxosNodeServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    PaxosNodeServiceMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (PaxosNodeServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new PaxosNodeServiceFileDescriptorSupplier())
              .addMethod(getExchangeMethod())
              .build();
        }
      }
    }
    return result;
  }
}
