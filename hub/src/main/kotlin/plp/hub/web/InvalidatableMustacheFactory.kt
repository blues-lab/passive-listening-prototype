package plp.hub.web

import com.github.mustachejava.DefaultMustacheFactory

/**
 * A DefaultMustacheFactory whose cache you can invalidate
 * based on https://github.com/spullara/mustache.java/issues/117
 * @see DefaultMustacheFactory
 */
class InvalidatableMustacheFactory(classpathResourceRoot: String) : DefaultMustacheFactory(classpathResourceRoot) {
    fun clearCache() {
        mustacheCache.clear()
    }
}
