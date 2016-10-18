package it.facile.form.model.models

import it.facile.form.logE
import it.facile.form.model.FieldRulesValidator
import it.facile.form.model.FieldsContainer
import it.facile.form.not
import it.facile.form.storage.FieldValue
import it.facile.form.storage.FormStorage
import it.facile.form.ui.viewmodel.FieldPath
import rx.Observable
import java.util.*

data class FormModel(val storage: FormStorage,
                     private val actions: HashMap<String, List<(FieldValue, FormStorage) -> Unit>>) : FieldsContainer {

    val pages = arrayListOf<PageModel>()
    private val interestedKeys: MutableMap<String, MutableList<String>> by lazy { observeActionsKeys() }

    override fun fields(): List<FieldModel> = pages.fold(mutableListOf<FieldModel>(), { models, page ->
        models.addAll(page.fields())
        models
    })

    fun getPage(path: FieldPath): PageModel = pages[path.pageIndex]
    fun getSection(path: FieldPath): SectionModel = pages[path.pageIndex].sections[path.sectionIndex]
    fun getField(path: FieldPath): FieldModel = pages[path.pageIndex].sections[path.sectionIndex].fields[path.fieldIndex]

    fun observeChanges(): Observable<FieldPath> = storage.observe()
            .doOnNext { if (it.second) executeFieldAction(it.first) } // Execute all side effects actions related to the key if user made
            .map { it.first } // Get rid of userMade boolean information
            .flatMap { Observable.just(it).mergeWith(Observable.from(interestedKeys[it] ?: emptyList())) } // Merge with interested keys
            .flatMap { Observable.from(findFieldPathByKey(it)) } // Emit for every FieldPath associated to the field key
            .doOnError { logE(it.message) } // Log errors
            .retry() // Resubscribe if some errors occurs to continue the flow of notifications

    /** Notify the model of a field value change generated from the outside (that is a user made
     * change and not for the example one result of an field Action) */
    fun notifyValueChanged(path: FieldPath, value: FieldValue): Unit {
        if (not(contains(path))) return // The model does not contain the given path
        val key = findFieldModelByFieldPath(path).key
        storage.putValue(key, value, true)
    }

    /** Type-safe builder method to add a page */
    fun page(title: String, init: PageModel.() -> Unit): PageModel {
        val page = PageModel(title)
        page.init()
        pages.add(page)
        return page
    }

    private fun executeFieldAction(key: String) =
            actions[key]?.forEach { it(storage.getValue(key), storage) }


    private fun contains(path: FieldPath): Boolean = path.pageIndex < pages.size &&
            path.sectionIndex < pages[path.pageIndex].sections.size &&
            path.fieldIndex < pages[path.pageIndex].sections[path.sectionIndex].fields.size

    private fun contains(key: String): Boolean = findFieldPathByKey(key).size > 0

    private fun findFieldModelByFieldPath(fieldPath: FieldPath): FieldModel =
            pages[fieldPath.pageIndex].sections[fieldPath.sectionIndex].fields[fieldPath.fieldIndex]

    private fun findFieldPathByKey(key: String): List<FieldPath> = FieldPath.buildForKey(key, this)

    private fun observeActionsKeys(): MutableMap<String, MutableList<String>> {
        val interested: MutableMap<String, MutableList<String>> = mutableMapOf()
        for ((toBeNotifiedKey, config) in fields()) {
            if (config is FieldRulesValidator) {
                config.rules(storage).map {
                    it.observedKeys()
                            .map { it.key }
                            .map {
                                interested[it]?.add(toBeNotifiedKey) ?: interested.put(it, mutableListOf(toBeNotifiedKey))
                            }
                }
            }
        }
        return interested
    }

    companion object {
        fun form(storage: FormStorage, actions: HashMap<String, List<(FieldValue, FormStorage) -> Unit>>, init: FormModel.() -> Unit): FormModel {
            val form = FormModel(storage, actions)
            form.init()
            return form
        }
    }
}