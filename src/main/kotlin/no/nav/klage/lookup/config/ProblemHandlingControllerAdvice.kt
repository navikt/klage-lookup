package no.nav.klage.lookup.config

import no.nav.klage.lookup.util.getLogger
import no.nav.klage.lookup.util.getTeamLogger
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice
class ProblemHandlingControllerAdvice : ResponseEntityExceptionHandler() {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val ourLogger = getLogger(javaClass.enclosingClass)
        private val teamLogger = getTeamLogger()
    }

    @ExceptionHandler
    fun handleUserNotFound(
        ex: UserNotFoundException,
    ): ProblemDetail {
        return create(HttpStatus.NOT_FOUND, ex)
    }

    @ExceptionHandler
    fun handleEnhetNotFoundException(
        ex: EnhetNotFoundException,
    ): ProblemDetail {
        return create(HttpStatus.NOT_FOUND, ex)
    }

    private fun create(httpStatus: HttpStatus, ex: Exception): ProblemDetail {
        val errorMessage = ex.message ?: "No error message available"

        logError(
            httpStatus = httpStatus,
            errorMessage = errorMessage,
            exception = ex
        )

        return ProblemDetail.forStatus(httpStatus).apply {
            title = errorMessage
        }
    }

    private fun logError(httpStatus: HttpStatus, errorMessage: String, exception: Exception) {
        when {
            exception is UserNotFoundException -> {
                ourLogger.debug("UserNotFoundException thrown to client. See team-logs for more details.")
                teamLogger.debug("Exception thrown to client: ${httpStatus.reasonPhrase}, $errorMessage", exception)
            }
            exception is EnhetNotFoundException -> {
                ourLogger.debug("EnhetNotFoundException thrown to client. See team-logs for more details.")
                teamLogger.debug("Exception thrown to client: ${httpStatus.reasonPhrase}, $errorMessage", exception)
            }
            httpStatus.is5xxServerError -> {
                ourLogger.error("Exception thrown to client: ${exception.javaClass.name}. See team-logs for more details.")
                teamLogger.error("Exception thrown to client: ${httpStatus.reasonPhrase}, $errorMessage", exception)
            }
            else -> {
                ourLogger.warn("Exception thrown to client: ${exception.javaClass.name}. See team-logs for more details.")
                teamLogger.warn("Exception thrown to client: ${httpStatus.reasonPhrase}, $errorMessage", exception)
            }
        }
    }
}

class UserNotFoundException(msg: String) : RuntimeException(msg)

class EnhetNotFoundException(msg: String) : RuntimeException(msg)