package it.facile.form.ui.adapters.FieldViewHolders

import android.view.View
import it.facile.form.R
import it.facile.form.viewmodel.FieldValue
import it.facile.form.viewmodel.FieldViewModel
import it.facile.form.viewmodel.FieldViewModelStyle
import kotlinx.android.synthetic.main.form_field_checkbox.view.*
import rx.subjects.PublishSubject

class FieldViewHolderCheckBox(itemView: View,
                              private val valueChangesSubject: PublishSubject<Pair<Int, FieldValue>>) :
        FieldViewHolderBase(itemView), CanBeHidden, CanNotifyNewValues {

    override fun bind(viewModel: FieldViewModel, position: Int, errorsShouldBeVisible: Boolean) {
        super.bind(viewModel, position, errorsShouldBeVisible)
        val style = viewModel.style
        itemView.checkboxLabel.text = viewModel.label
        itemView.checkboxSecondLabel.text = style.textDescription
        when (style) {
            is FieldViewModelStyle.Checkbox -> {
                val checkBoxValue = itemView.checkBoxValue
                checkBoxValue.setOnCheckedChangeListener(null)
                checkBoxValue.isChecked = style.bool
                checkBoxValue.setOnCheckedChangeListener { b, value -> notifyNewValue(position, FieldValue.Bool(value)) }
                itemView.setOnClickListener { view -> checkBoxValue.isChecked = !checkBoxValue.isChecked }
            }
        }
    }

    override fun getHeight(): Int {
        return itemView.resources.getDimension(R.dimen.field_height_medium).toInt()
    }

    override fun notifyNewValue(position: Int, newValue: FieldValue) {
        valueChangesSubject.onNext(position to newValue)
    }
}