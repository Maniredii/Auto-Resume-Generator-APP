package fm.mrc.resumebuilder.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import fm.mrc.resumebuilder.ui.validation.ValidationState
import fm.mrc.resumebuilder.ui.validation.ValidationType

/**
 * Enhanced text field with validation
 */
@Composable
fun ValidatedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    validationState: ValidationState,
    fieldName: String,
    validationType: ValidationType = ValidationType.REQUIRED,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    isRequired: Boolean = true,
    maxLines: Int = 1,
    supportingText: String? = null
) {
    val errorMessage = validationState.getFieldError(fieldName)
    val hasError = validationState.hasFieldError(fieldName)
    
    // Real-time validation
    LaunchedEffect(value) {
        if (value.isNotEmpty() || validationState.showErrors) {
            validationState.validateField(fieldName, value, validationType)
        }
    }
    
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { 
                Text(
                    text = if (isRequired) "$label *" else label,
                    color = if (hasError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
            },
            placeholder = placeholder?.let { { Text(it) } },
            isError = hasError,
            keyboardOptions = keyboardOptions,
            maxLines = maxLines,
            modifier = Modifier.fillMaxWidth(),
            supportingText = {
                if (hasError) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                } else if (supportingText != null) {
                    Text(
                        text = supportingText,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        )
    }
}

/**
 * Validated text field for email
 */
@Composable
fun ValidatedEmailField(
    value: String,
    onValueChange: (String) -> Unit,
    validationState: ValidationState,
    fieldName: String = "email",
    modifier: Modifier = Modifier,
    label: String = "Email",
    placeholder: String? = "Enter your email address"
) {
    ValidatedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        validationState = validationState,
        fieldName = fieldName,
        validationType = ValidationType.EMAIL,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        modifier = modifier,
        placeholder = placeholder,
        supportingText = "We'll never share your email"
    )
}

/**
 * Validated text field for phone
 */
@Composable
fun ValidatedPhoneField(
    value: String,
    onValueChange: (String) -> Unit,
    validationState: ValidationState,
    fieldName: String = "phone",
    modifier: Modifier = Modifier,
    label: String = "Phone",
    placeholder: String? = "Enter your phone number"
) {
    ValidatedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        validationState = validationState,
        fieldName = fieldName,
        validationType = ValidationType.PHONE,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        modifier = modifier,
        placeholder = placeholder
    )
}

/**
 * Validated text field for URL
 */
@Composable
fun ValidatedUrlField(
    value: String,
    onValueChange: (String) -> Unit,
    validationState: ValidationState,
    fieldName: String,
    modifier: Modifier = Modifier,
    label: String,
    placeholder: String? = null
) {
    ValidatedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        validationState = validationState,
        fieldName = fieldName,
        validationType = ValidationType.URL,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
        modifier = modifier,
        placeholder = placeholder,
        isRequired = false
    )
}

/**
 * Validated text field for multi-line text
 */
@Composable
fun ValidatedMultilineField(
    value: String,
    onValueChange: (String) -> Unit,
    validationState: ValidationState,
    fieldName: String,
    modifier: Modifier = Modifier,
    label: String,
    placeholder: String? = null,
    maxLines: Int = 3,
    maxLength: Int = 500
) {
    var characterCount by remember { mutableStateOf(value.length) }
    
    LaunchedEffect(value) {
        characterCount = value.length
    }
    
    ValidatedTextField(
        value = value,
        onValueChange = { newValue ->
            if (newValue.length <= maxLength) {
                onValueChange(newValue)
            }
        },
        label = label,
        validationState = validationState,
        fieldName = fieldName,
        validationType = ValidationType.MIN_LENGTH,
        modifier = modifier,
        placeholder = placeholder,
        maxLines = maxLines,
        supportingText = "$characterCount/$maxLength characters"
    )
}
