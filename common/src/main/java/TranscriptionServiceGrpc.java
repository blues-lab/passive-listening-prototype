import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.34.0)",
    comments = "Source: stt.proto")
public final class TranscriptionServiceGrpc {

  private TranscriptionServiceGrpc() {}

  public static final String SERVICE_NAME = "TranscriptionService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<Stt.TranscriptionRequest,
      Stt.TranscriptionResponse> getTranscribeFileMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "TranscribeFile",
      requestType = Stt.TranscriptionRequest.class,
      responseType = Stt.TranscriptionResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<Stt.TranscriptionRequest,
      Stt.TranscriptionResponse> getTranscribeFileMethod() {
    io.grpc.MethodDescriptor<Stt.TranscriptionRequest, Stt.TranscriptionResponse> getTranscribeFileMethod;
    if ((getTranscribeFileMethod = TranscriptionServiceGrpc.getTranscribeFileMethod) == null) {
      synchronized (TranscriptionServiceGrpc.class) {
        if ((getTranscribeFileMethod = TranscriptionServiceGrpc.getTranscribeFileMethod) == null) {
          TranscriptionServiceGrpc.getTranscribeFileMethod = getTranscribeFileMethod =
              io.grpc.MethodDescriptor.<Stt.TranscriptionRequest, Stt.TranscriptionResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "TranscribeFile"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  Stt.TranscriptionRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  Stt.TranscriptionResponse.getDefaultInstance()))
              .setSchemaDescriptor(new TranscriptionServiceMethodDescriptorSupplier("TranscribeFile"))
              .build();
        }
      }
    }
    return getTranscribeFileMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static TranscriptionServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<TranscriptionServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<TranscriptionServiceStub>() {
        @java.lang.Override
        public TranscriptionServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new TranscriptionServiceStub(channel, callOptions);
        }
      };
    return TranscriptionServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static TranscriptionServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<TranscriptionServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<TranscriptionServiceBlockingStub>() {
        @java.lang.Override
        public TranscriptionServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new TranscriptionServiceBlockingStub(channel, callOptions);
        }
      };
    return TranscriptionServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static TranscriptionServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<TranscriptionServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<TranscriptionServiceFutureStub>() {
        @java.lang.Override
        public TranscriptionServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new TranscriptionServiceFutureStub(channel, callOptions);
        }
      };
    return TranscriptionServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public static abstract class TranscriptionServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public void transcribeFile(Stt.TranscriptionRequest request,
        io.grpc.stub.StreamObserver<Stt.TranscriptionResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getTranscribeFileMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getTranscribeFileMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                Stt.TranscriptionRequest,
                Stt.TranscriptionResponse>(
                  this, METHODID_TRANSCRIBE_FILE)))
          .build();
    }
  }

  /**
   */
  public static final class TranscriptionServiceStub extends io.grpc.stub.AbstractAsyncStub<TranscriptionServiceStub> {
    private TranscriptionServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected TranscriptionServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new TranscriptionServiceStub(channel, callOptions);
    }

    /**
     */
    public void transcribeFile(Stt.TranscriptionRequest request,
        io.grpc.stub.StreamObserver<Stt.TranscriptionResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getTranscribeFileMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class TranscriptionServiceBlockingStub extends io.grpc.stub.AbstractBlockingStub<TranscriptionServiceBlockingStub> {
    private TranscriptionServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected TranscriptionServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new TranscriptionServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public Stt.TranscriptionResponse transcribeFile(Stt.TranscriptionRequest request) {
      return blockingUnaryCall(
          getChannel(), getTranscribeFileMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class TranscriptionServiceFutureStub extends io.grpc.stub.AbstractFutureStub<TranscriptionServiceFutureStub> {
    private TranscriptionServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected TranscriptionServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new TranscriptionServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<Stt.TranscriptionResponse> transcribeFile(
        Stt.TranscriptionRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getTranscribeFileMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_TRANSCRIBE_FILE = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final TranscriptionServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(TranscriptionServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_TRANSCRIBE_FILE:
          serviceImpl.transcribeFile((Stt.TranscriptionRequest) request,
              (io.grpc.stub.StreamObserver<Stt.TranscriptionResponse>) responseObserver);
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

  private static abstract class TranscriptionServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    TranscriptionServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return Stt.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("TranscriptionService");
    }
  }

  private static final class TranscriptionServiceFileDescriptorSupplier
      extends TranscriptionServiceBaseDescriptorSupplier {
    TranscriptionServiceFileDescriptorSupplier() {}
  }

  private static final class TranscriptionServiceMethodDescriptorSupplier
      extends TranscriptionServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    TranscriptionServiceMethodDescriptorSupplier(String methodName) {
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
      synchronized (TranscriptionServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new TranscriptionServiceFileDescriptorSupplier())
              .addMethod(getTranscribeFileMethod())
              .build();
        }
      }
    }
    return result;
  }
}
