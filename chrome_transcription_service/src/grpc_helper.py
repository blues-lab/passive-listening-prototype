import typing
from concurrent import futures
from pathlib import Path

import grpc
from sclog import getLogger


logger = getLogger(__name__)


def make_secure_credentials(
    key_path: typing.Union[str, Path],
    certificate_path: typing.Union[str, Path],
    root_path: typing.Union[str, Path],
) -> grpc.ServerCredentials:
    with open(key_path, "rb") as f:
        private_key = f.read()
    with open(certificate_path, "rb") as f:
        certificate_chain = f.read()
    with open(root_path, "rb") as f:
        root_certificates = f.read()
    server_credentials = grpc.ssl_server_credentials(
        ((private_key, certificate_chain),),
        root_certificates=root_certificates,
        require_client_auth=True,
    )

    return server_credentials


def get_server_for_args(
    port: int,
    key: typing.Optional[str],
    cert: typing.Optional[str],
    root: typing.Optional[str],
) -> grpc.Server:
    host_address = f"[::]:{port}"
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=2))

    if not (key and cert and root):
        logger.warning(
            "no key material specified, using insecure server on port %i", port
        )

        server.add_insecure_port(host_address)
    else:
        if not key:
            raise ValueError("missing key file")
        if not cert:
            raise ValueError("missing certificate file")
        if not root:
            raise ValueError("missing root certificates file")

        logger.info("server will use mutual TLS on port %i", port)

        server_credentials = make_secure_credentials(key, cert, root)
        server.add_secure_port(host_address, server_credentials)

    return server
