package com.thomas.core.context

import com.thomas.core.exception.ApplicationException
import com.thomas.core.exception.ErrorType.UNRESOLVED_ORGANIZATION
import com.thomas.core.i18n.CoreMessageI18N.contextCurrentSessionCurrentOrganizationUnresolvedOrganization

class UnresolvedOrganizationException(
    message: String = contextCurrentSessionCurrentOrganizationUnresolvedOrganization()
) : ApplicationException(
    message = message,
    type = UNRESOLVED_ORGANIZATION,
)
