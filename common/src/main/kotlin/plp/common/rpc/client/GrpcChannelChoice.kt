package plp.common.rpc.client

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext
import plp.common.toPath
import plp.logging.KotlinLogging
import kotlin.io.path.ExperimentalPathApi

private val logger = KotlinLogging.logger {}

/**
 * This class represents a choice made between the transport protocols for a channel
 * (insecure, TLS, or mutual TLS)
 * but that has not yet been instantiated into a channel.
 * (For that, @see #makeChannel)
 */
sealed class GrpcChannelChoice {
    /** Return a new channel connecting the chosen transport with the provided server */
    abstract fun makeChannel(host: String, port: Int): ManagedChannel

    companion object {
        @ExperimentalPathApi
        @Suppress("ReturnCount")
        fun fromArgs(root: String?, key: String?, cert: String?): GrpcChannelChoice {
            if (root != null) {
                if ((key != null) && (cert != null)) {
                    logger.debug { "for RPC, using mutual authentication with parameters root = $root, cert = $cert, key = $key" }
                    return MutualAuthChannel(root = root, cert = cert, key = key)
                }

                logger.debug { "for RPC, using TLS with parameter root = $root, cert = $cert, key = $key" }
                return TlsChannel(root)
            }

            logger.warning("for RPC, using no authentication (not secure over the network!)")
            return InsecureChannel
        }
    }
}

class MutualAuthChannel(private val root: String, private val cert: String, private val key: String) : GrpcChannelChoice() {
    @ExperimentalPathApi
    override fun makeChannel(host: String, port: Int): ManagedChannel {
        logger.debug { "creating a mutual TLS channel to $host:$port" }

        val sslContext: SslContext = GrpcSslContexts.forClient()
            .trustManager(this.root.toPath().toFile())
            .keyManager(this.cert.toPath().toFile(), this.key.toPath().toFile())
            .build()

        return NettyChannelBuilder.forAddress(host, port)
            .useTransportSecurity()
            .sslContext(sslContext)
            .build()
    }
}

class TlsChannel(private val root: String) : GrpcChannelChoice() {
    @ExperimentalPathApi
    override fun makeChannel(host: String, port: Int): ManagedChannel {
        logger.debug { "creating a TLS channel to $host:$port" }

        val sslContext: SslContext = GrpcSslContexts.forClient()
            .trustManager(this.root.toPath().toFile())
            .build()

        return NettyChannelBuilder.forAddress(host, port)
            .useTransportSecurity()
            .sslContext(sslContext)
            .build()
    }
}

object InsecureChannel : GrpcChannelChoice() {
    @ExperimentalPathApi
    override fun makeChannel(host: String, port: Int): ManagedChannel {
        logger.debug { "creating an insecure channel to $host:$port" }

        return ManagedChannelBuilder.forAddress(host, port).usePlaintext().build()
    }
}
