# -*- coding: utf-8 -*-
# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: plp/proto/Vad.proto
"""Generated protocol buffer code."""
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from google.protobuf import reflection as _reflection
from google.protobuf import symbol_database as _symbol_database
# @@protoc_insertion_point(imports)

_sym_db = _symbol_database.Default()




DESCRIPTOR = _descriptor.FileDescriptor(
  name='plp/proto/Vad.proto',
  package='',
  syntax='proto3',
  serialized_options=None,
  create_key=_descriptor._internal_create_key,
  serialized_pb=b'\n\x13plp/proto/Vad.proto\"\'\n\nVadRequest\x12\n\n\x02id\x18\x01 \x01(\x05\x12\r\n\x05\x61udio\x18\n \x01(\x0c\"+\n\x0bVadResponse\x12\n\n\x02id\x18\x01 \x01(\x05\x12\x10\n\x08isSpeech\x18\n \x01(\x08\x32@\n\nVadService\x12\x32\n\x13\x43heckAudioForSpeech\x12\x0b.VadRequest\x1a\x0c.VadResponse\"\x00\x62\x06proto3'
)




_VADREQUEST = _descriptor.Descriptor(
  name='VadRequest',
  full_name='VadRequest',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  create_key=_descriptor._internal_create_key,
  fields=[
    _descriptor.FieldDescriptor(
      name='id', full_name='VadRequest.id', index=0,
      number=1, type=5, cpp_type=1, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='audio', full_name='VadRequest.audio', index=1,
      number=10, type=12, cpp_type=9, label=1,
      has_default_value=False, default_value=b"",
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto3',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=23,
  serialized_end=62,
)


_VADRESPONSE = _descriptor.Descriptor(
  name='VadResponse',
  full_name='VadResponse',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  create_key=_descriptor._internal_create_key,
  fields=[
    _descriptor.FieldDescriptor(
      name='id', full_name='VadResponse.id', index=0,
      number=1, type=5, cpp_type=1, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='isSpeech', full_name='VadResponse.isSpeech', index=1,
      number=10, type=8, cpp_type=7, label=1,
      has_default_value=False, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto3',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=64,
  serialized_end=107,
)

DESCRIPTOR.message_types_by_name['VadRequest'] = _VADREQUEST
DESCRIPTOR.message_types_by_name['VadResponse'] = _VADRESPONSE
_sym_db.RegisterFileDescriptor(DESCRIPTOR)

VadRequest = _reflection.GeneratedProtocolMessageType('VadRequest', (_message.Message,), {
  'DESCRIPTOR' : _VADREQUEST,
  '__module__' : 'plp.proto.Vad_pb2'
  # @@protoc_insertion_point(class_scope:VadRequest)
  })
_sym_db.RegisterMessage(VadRequest)

VadResponse = _reflection.GeneratedProtocolMessageType('VadResponse', (_message.Message,), {
  'DESCRIPTOR' : _VADRESPONSE,
  '__module__' : 'plp.proto.Vad_pb2'
  # @@protoc_insertion_point(class_scope:VadResponse)
  })
_sym_db.RegisterMessage(VadResponse)



_VADSERVICE = _descriptor.ServiceDescriptor(
  name='VadService',
  full_name='VadService',
  file=DESCRIPTOR,
  index=0,
  serialized_options=None,
  create_key=_descriptor._internal_create_key,
  serialized_start=109,
  serialized_end=173,
  methods=[
  _descriptor.MethodDescriptor(
    name='CheckAudioForSpeech',
    full_name='VadService.CheckAudioForSpeech',
    index=0,
    containing_service=None,
    input_type=_VADREQUEST,
    output_type=_VADRESPONSE,
    serialized_options=None,
    create_key=_descriptor._internal_create_key,
  ),
])
_sym_db.RegisterServiceDescriptor(_VADSERVICE)

DESCRIPTOR.services_by_name['VadService'] = _VADSERVICE

# @@protoc_insertion_point(module_scope)
