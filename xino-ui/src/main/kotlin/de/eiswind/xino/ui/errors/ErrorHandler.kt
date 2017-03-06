package de.eiswind.xino.ui.errors

import com.vaadin.data.ValidationException
import com.vaadin.server.ErrorMessage
import com.vaadin.server.Page
import com.vaadin.ui.Notification
import org.springframework.transaction.TransactionSystemException
import org.springframework.util.StringUtils
import javax.persistence.RollbackException
import javax.validation.ConstraintViolationException

/**
 * Created by thomas on 25.05.15.
 */
object ErrorHandler {

    fun handleCommitException(e: Exception) {
        val msg = when (e) {
//            is FieldGroup.CommitException -> {
//                val errorMessages = ArrayList<ErrorMessage>()
//                if (e.cause is FieldGroup.FieldGroupInvalidValueException) {
//                    for (entry in e.invalidFields.entries) {
//                        errorMessages.add(AbstractErrorMessage.getErrorMessageForException(entry.value))
//                    }
//                } else {
//                    errorMessages.add(AbstractErrorMessage.getErrorMessageForException(e.cause))
//                }
//                extractFormCommitMessages(errorMessages)
//            }
            is TransactionSystemException -> {

                val cause = e.cause
                if (cause != null) {
                    handleTransactionException(cause)
                } else throw IllegalStateException("Must have a cause")
            }
            is ValidationException -> {
                val errorMessages = StringBuffer()

                for(result in e.validationErrors){
                    errorMessages.append(result.errorMessage+"\n")
                }
                errorMessages.toString()
            }
            else -> e.message
        }

        val notification = Notification(INVALID_FORM, msg, Notification.Type.ERROR_MESSAGE, true)

        notification.show(Page.getCurrent())


    }

    private fun handleTransactionException(cause: Throwable): String {
        return when (cause) {
            is RollbackException -> {
                val rollbackCause = cause.cause
                when (rollbackCause) {
                    is ConstraintViolationException -> {
                        val violation = rollbackCause.constraintViolations.first()
                        violation.message + " " + violation.propertyPath + " " + violation.invalidValue
                    }
                    else -> rollbackCause?.message ?: "Unknown error";
                }
            }
            else -> cause.message ?: "Unknown error"
        }
    }

    private fun extractFormCommitMessages(errorMessages: List<ErrorMessage>): String {
        val buf = StringBuffer()
        for (message in errorMessages) {
            val msg = message.formattedHtmlMessage
            if (!StringUtils.isEmpty(msg)) {
                buf.append(msg)
                buf.append(" <br/>")
            }
        }
        return buf.toString()
    }

    const val INVALID_FORM = "Form is not valid"
}
