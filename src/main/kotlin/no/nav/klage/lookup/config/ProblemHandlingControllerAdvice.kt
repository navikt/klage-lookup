package no.nav.klage.lookup.config

import no.nav.klage.lookup.service.PersonService
import no.nav.klage.lookup.service.nom.NomAnsattNotFoundException
import no.nav.klage.lookup.service.pdl.PDLPersonNotFoundException
import no.nav.klage.lookup.service.regoppslag.RegoppslagAdresseFiltrertException
import no.nav.klage.lookup.service.regoppslag.RegoppslagIngenTilgangException
import no.nav.klage.lookup.service.regoppslag.RegoppslagInternTekniskFeilException
import no.nav.klage.lookup.service.regoppslag.RegoppslagPersonDoedException
import no.nav.klage.lookup.service.regoppslag.RegoppslagTilgangAvvistException
import no.nav.klage.lookup.service.regoppslag.RegoppslagUgyldigInputException
import no.nav.klage.lookup.service.regoppslag.RegoppslagUkjentAdresseException
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
    fun handleUserNotFound(ex: UserNotFoundException): ProblemDetail = create(httpStatus = HttpStatus.NOT_FOUND, ex = ex)

    @ExceptionHandler
    fun handleEnhetNotFoundException(ex: EnhetNotFoundException): ProblemDetail = create(httpStatus = HttpStatus.NOT_FOUND, ex = ex)

    @ExceptionHandler
    fun handlePDLPersonNotFound(ex: PDLPersonNotFoundException): ProblemDetail = create(httpStatus = HttpStatus.NOT_FOUND, ex = ex)

    @ExceptionHandler
    fun handleNomAnsattNotFound(ex: NomAnsattNotFoundException): ProblemDetail = create(httpStatus = HttpStatus.NOT_FOUND, ex = ex)

    @ExceptionHandler
    fun handleRegoppslagAdresseFiltrert(ex: RegoppslagAdresseFiltrertException): ProblemDetail =
        create(httpStatus = HttpStatus.NO_CONTENT, ex = ex)

    @ExceptionHandler
    fun handleRegoppslagUgyldigInput(ex: RegoppslagUgyldigInputException): ProblemDetail =
        create(httpStatus = HttpStatus.BAD_REQUEST, ex = ex)

    @ExceptionHandler
    fun handleRegoppslagIngenTilgang(ex: RegoppslagIngenTilgangException): ProblemDetail =
        create(httpStatus = HttpStatus.UNAUTHORIZED, ex = ex)

    @ExceptionHandler
    fun handleRegoppslagTilgangAvvist(ex: RegoppslagTilgangAvvistException): ProblemDetail =
        create(httpStatus = HttpStatus.FORBIDDEN, ex = ex)

    @ExceptionHandler
    fun handleRegoppslagUkjentAdresse(ex: RegoppslagUkjentAdresseException): ProblemDetail =
        create(httpStatus = HttpStatus.NOT_FOUND, ex = ex)

    @ExceptionHandler
    fun handleRegoppslagPersonDoed(ex: RegoppslagPersonDoedException): ProblemDetail = create(httpStatus = HttpStatus.GONE, ex = ex)

    @ExceptionHandler
    fun handleRegoppslagInternTekniskFeil(ex: RegoppslagInternTekniskFeilException): ProblemDetail =
        create(httpStatus = HttpStatus.INTERNAL_SERVER_ERROR, ex = ex)

    @ExceptionHandler
    fun handleFullmaktMissingAccessException(ex: PersonService.FullmaktMissingAccessException): ProblemDetail =
        create(httpStatus = HttpStatus.FORBIDDEN, ex = ex)

    @ExceptionHandler
    fun handleFullmaktInputException(ex: PersonService.FullmaktInputException): ProblemDetail =
        create(httpStatus = HttpStatus.BAD_REQUEST, ex = ex)

    private fun create(
        httpStatus: HttpStatus,
        ex: Exception,
    ): ProblemDetail {
        val errorMessage = ex.message ?: "No error message available"

        logError(
            httpStatus = httpStatus,
            errorMessage = errorMessage,
            exception = ex,
        )

        return ProblemDetail.forStatus(httpStatus).apply {
            title = errorMessage
        }
    }

    private fun logError(
        httpStatus: HttpStatus,
        errorMessage: String,
        exception: Exception,
    ) {
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

class UserNotFoundException(
    msg: String,
) : RuntimeException(msg)

class EnhetNotFoundException(
    msg: String,
) : RuntimeException(msg)
