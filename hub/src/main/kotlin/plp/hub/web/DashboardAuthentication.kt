package plp.hub.web

import io.ktor.auth.Authentication
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.basic
import plp.common.GLOBAL_CONFIG

/**
 * Enable HTTP Basic (password) authentication for the current application
 *
 * This sets up a single authentication configuration, with the username and password set in the gobal config.
 *
 * @param authName the name to use for the authentication configuration
 * (These are supposed to distinguish the different configurations,
 * but we only have one (right now), so it doesn't really matter.)
 */
fun Authentication.Configuration.enableBasicAuthentication(authName: String) {
    basic(authName) {
        realm = "dashboard"
        validate { credentials ->
            if ((credentials.name == GLOBAL_CONFIG.dashboardCredentials.username) &&
                (credentials.password == GLOBAL_CONFIG.dashboardCredentials.password)
            ) {
                UserIdPrincipal(credentials.name)
            } else {
                null
            }
        }
    }
}
