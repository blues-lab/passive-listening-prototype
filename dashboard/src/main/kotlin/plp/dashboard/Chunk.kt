package plp.dashboard

external interface Chunk {
    val audioId: Int
    val transcriptId: Int
    val timestamp: Int
    val filename: String
    val duration: Float
    val text: String?
    val classifier: String?
    val classification: String?
    val extras: String?
}