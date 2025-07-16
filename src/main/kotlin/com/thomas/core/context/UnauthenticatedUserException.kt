package com.thomas.core.context

import com.thomas.core.exception.ApplicationException
import com.thomas.core.exception.ErrorType.UNAUTHENTICATED_USER
import com.thomas.core.i18n.CoreMessageI18N.contextCurrentSessionCurrentUserNotLogged

class UnauthenticatedUserException(
    message: String = contextCurrentSessionCurrentUserNotLogged()
) : ApplicationException(
    message = message,
    type = UNAUTHENTICATED_USER,
)
