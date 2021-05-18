import tempfile
from pathlib import Path

import grpc
from sclog import getLogger

from plp.proto import Transcription_pb2
from plp.proto import Transcription_pb2_grpc

logger = getLogger(__name__)

CLASSIFICATION_SERVICE_PORT = 50059


def save_bytes_as_tmp_wav_file(b: bytes) -> str:
    """
    Save the given bytes to a file in a new temporary directory and return that file's path
    """
    fd, path = tempfile.mkstemp(suffix=".wav")
    with open(fd, "wb") as f:
        f.write(b)
    logger.debug("saved bytes to temporary file %s", path)
    return path


class TranscriptionService(Transcription_pb2_grpc.TranscriptionServiceServicer):
    def TranscribeFile(self, request, context):
        logger.debug("received request %i", request.id)

        tmp_file = Path(save_bytes_as_tmp_wav_file(request.audio))
        # TODO Transcribe wave file at tmp_file here
        tmp_file.unlink()
        
        return Transcription_pb2.TranscriptionResponse(
            id=request.id,
            text=None, # TODO add text 
        )