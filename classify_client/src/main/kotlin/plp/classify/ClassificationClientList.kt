package plp.classify

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import plp.proto.Classification

typealias ClassificationClientList = List<ClassificationClient>

suspend fun ClassificationClientList.classify(text: String): List<Classification.ClassificationResponse> {
    val clients: ClassificationClientList = this
    return coroutineScope {
        clients.map { client ->
            async { client.classifyText(text) }
        }.awaitAll()
    }
}
