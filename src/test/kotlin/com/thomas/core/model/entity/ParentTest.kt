package com.thomas.core.model.entity

import com.thomas.core.extension.EMAIL_REGEX
import com.thomas.core.extension.isBetween
import com.thomas.core.util.StringUtils.randomString
import java.util.UUID
import java.util.UUID.randomUUID
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import org.junit.jupiter.api.Test

class ParentTest {

    @Test
    fun `Parent test`() {
        val user = UserEntity()
    }

}

abstract class ContactInfo<T : ContactInfo<T>> : BaseEntity<T>() {

    abstract val mainEmail: String
    abstract val mainPhone: String

    fun KClass<T>.parentProperty(
        property: KProperty1<T, *>,
    ) = this.memberProperties.first { it.name == property.name } as KProperty1<T, *>

    fun <E: Any?> parentProp(
        klass: KClass<out ContactInfo<T>>,
        property: KProperty1<T, E>,
    ) = klass.memberProperties.first { it.name == property.name } as KProperty1<T, *>

    fun KProperty1<T, *>.propTest() = this as KProperty1<T, *>

    fun contactInfoValidations(): List<EntityValidation<T>> = listOf(
        EntityValidation(
            ContactInfo<T>::mainEmail as KProperty1<T, String>,
            { "managementContactValidationMainEmailInvalidValue()" },
            { EMAIL_REGEX.matches(it.mainEmail) }
        ),
//        EntityValidation(
//            ContactInfo<*>::mainPhone,
//            { managementContactValidationMainPhoneInvalidValue() },
//            { PHONE_NUMBER_REGEX.matches(it.mainPhone) }
//        ),
    )

}


data class UserEntity(
    override val id: UUID = randomUUID(),
    val firstName: String = randomString(),
    override val mainEmail: String = "test@test.com",
    override val mainPhone: String = "16877995566",
) : ContactInfo<UserEntity>() {

    companion object {
        internal const val MIN_NAME_SIZE = 2
        internal const val MAX_NAME_SIZE = 250
    }

    init {
        validate()
    }

    override fun errorMessage(): String = "managementUserValidationInvalidEntityErrorMessage()"

    override fun validations(): List<EntityValidation<UserEntity>> = listOf<EntityValidation<UserEntity>>(
        EntityValidation(
            UserEntity::firstName,
            { "managementUserValidationFirstNameInvalidLength(MIN_NAME_SIZE, MAX_NAME_SIZE)" },
            { it.firstName.length.isBetween(MIN_NAME_SIZE, MAX_NAME_SIZE) }
        ),
    ) + contactInfoValidations()

}
