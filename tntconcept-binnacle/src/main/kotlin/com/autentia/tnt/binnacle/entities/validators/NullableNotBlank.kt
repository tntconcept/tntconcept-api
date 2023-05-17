package com.autentia.tnt.binnacle.entities.validators

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@MustBeDocumented
@Constraint(validatedBy = [NullableNotBlankValidator::class])
annotation class NullableNotBlank(
        val message: String = "{jakarta.validation.constraints.NotBlank.message}",
        val groups: Array<KClass<*>> = [],
        val payload: Array<KClass<out Payload>> = []
)

class NullableNotBlankValidator : ConstraintValidator<NullableNotBlank, String> {
    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        if (value == null) return true
        return value.isNotBlank()
    }
}
