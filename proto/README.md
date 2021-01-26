Protos
======
This directory contains [Protocol Buffers](https://developers.google.com/protocol-buffers/) used by the project and the build settings that compile them.

## Compiling for Kotlin

Running `../gradlew generateProto` should generated the appropriate classes and stubs.

## Compiling for Python

### Get the prerequisites

```shell
python3 -m venv venv
. ./venv/bin/activate
pip install grpcio-tools
```

### Compile the protos and generate gRPC stubs

```shell
mkdir -p build/python
python -m grpc_tools.protoc --proto_path=src/main/proto --python_out=./build/python --grpc_python_out=./build/python src/main/proto/plp/proto/*.proto
```

The `build/python/plp/proto` directory should now contain the generated classes and methods.
