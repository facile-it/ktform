package it.facile.form.model.configurations

import it.facile.form.storage.FormStorage
import it.facile.form.model.CustomPickerId
import it.facile.form.model.FieldConfig
import it.facile.form.model.FieldRule
import it.facile.form.model.FieldRulesValidator
import it.facile.form.storage.FieldValue.Missing
import it.facile.form.storage.FieldValue.Object
import it.facile.form.ui.viewmodel.FieldViewModel
import it.facile.form.ui.viewmodel.FieldViewModelStyle
import it.facile.form.ui.viewmodel.FieldViewModelStyle.CustomPicker
import it.facile.form.ui.viewmodel.FieldViewModelStyle.ExceptionText

class FieldConfigPickerCustom(label: String,
                              val id: CustomPickerId,
                              val placeHolder: String = "Select a value",
                              override val rules: List<FieldRule> = emptyList()) : FieldConfig(label), FieldRulesValidator {

    override fun getViewModel(key: Int, storage: FormStorage): FieldViewModel {
        val value = storage.getValue(key)
        return FieldViewModel(
                label,
                getViewModelStyle(key, storage),
                storage.isHidden(key),
                isValid(value))
    }

    override fun getViewModelStyle(key: Int, storage: FormStorage): FieldViewModelStyle {
        val value = storage.getValue(key)
        return when (value) {
            is Object -> CustomPicker(identifier = id, valueText = value.value.describe())
            is Missing -> CustomPicker(identifier = id, valueText = placeHolder)
            else -> ExceptionText(FieldViewModelStyle.INVALID_TYPE)
        }
    }
}