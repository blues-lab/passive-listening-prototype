import argparse
import typing
from concurrent import futures
from pathlib import Path

import grpc
from sclog import getLogger

from grpc_helper import get_server_for_args
from plp.proto import Vad_pb2_grpc
import VadService

logger = getLogger(__name__)


def main():
    parser = argparse.ArgumentParser(__doc__)
    parser.add_argument(
        "--key",
        type=str,
        help="Path to private key",
    )
    parser.add_argument(
        "--cert",
        type=str,
        help="Path to certificate",
    )
    parser.add_argument(
        "--root",
        type=str,
        help="Path to root certificates",
    )
    args = parser.parse_args()

    server = get_server_for_args(
        VadService.CLASSIFICATION_SERVICE_PORT,
        args.key,
        args.cert,
        args.root,
    )

    Vad_pb2_grpc.add_VadServiceServicer_to_server(
        servicer=VadService.VadService(),
        server=server,
    )

    server.start()
    server.wait_for_termination()


if __name__ == "__main__":
    main()
