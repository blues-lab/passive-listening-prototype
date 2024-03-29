# Generated by the gRPC Python protocol compiler plugin. DO NOT EDIT!
"""Client and server classes corresponding to protobuf-defined services."""
import grpc

from plp.proto import Vad_pb2 as plp_dot_proto_dot_Vad__pb2


class VadServiceStub(object):
    """Missing associated documentation comment in .proto file."""

    def __init__(self, channel):
        """Constructor.

        Args:
            channel: A grpc.Channel.
        """
        self.CheckAudioForSpeech = channel.unary_unary(
                '/VadService/CheckAudioForSpeech',
                request_serializer=plp_dot_proto_dot_Vad__pb2.VadRequest.SerializeToString,
                response_deserializer=plp_dot_proto_dot_Vad__pb2.VadResponse.FromString,
                )


class VadServiceServicer(object):
    """Missing associated documentation comment in .proto file."""

    def CheckAudioForSpeech(self, request, context):
        """Missing associated documentation comment in .proto file."""
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')


def add_VadServiceServicer_to_server(servicer, server):
    rpc_method_handlers = {
            'CheckAudioForSpeech': grpc.unary_unary_rpc_method_handler(
                    servicer.CheckAudioForSpeech,
                    request_deserializer=plp_dot_proto_dot_Vad__pb2.VadRequest.FromString,
                    response_serializer=plp_dot_proto_dot_Vad__pb2.VadResponse.SerializeToString,
            ),
    }
    generic_handler = grpc.method_handlers_generic_handler(
            'VadService', rpc_method_handlers)
    server.add_generic_rpc_handlers((generic_handler,))


 # This class is part of an EXPERIMENTAL API.
class VadService(object):
    """Missing associated documentation comment in .proto file."""

    @staticmethod
    def CheckAudioForSpeech(request,
            target,
            options=(),
            channel_credentials=None,
            call_credentials=None,
            insecure=False,
            compression=None,
            wait_for_ready=None,
            timeout=None,
            metadata=None):
        return grpc.experimental.unary_unary(request, target, '/VadService/CheckAudioForSpeech',
            plp_dot_proto_dot_Vad__pb2.VadRequest.SerializeToString,
            plp_dot_proto_dot_Vad__pb2.VadResponse.FromString,
            options, channel_credentials,
            insecure, call_credentials, compression, wait_for_ready, timeout, metadata)
