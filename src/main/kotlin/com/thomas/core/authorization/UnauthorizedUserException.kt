package com.thomas.core.authorization

import com.thomas.core.exception.ApplicationException
import com.thomas.core.exception.ErrorType.UNAUTHORIZED_ACTION
import com.thomas.core.i18n.CoreMessageI18N.contextCurrentSessionCurrentUserNotAllowed

class UnauthorizedUserException(
    message: String = contextCurrentSessionCurrentUserNotAllowed()
) : ApplicationException(
    message = message,
    type = UNAUTHORIZED_ACTION,
)
