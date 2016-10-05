package it.facile.form.ui.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.view.ViewGroup
import it.facile.form.ui.FieldPathWithValue
import it.facile.form.ui.PageFragment
import it.facile.form.ui.viewmodel.FieldPath
import it.facile.form.ui.viewmodel.PageViewModel
import rx.Observable
import rx.subjects.PublishSubject

class PagesAdapter(val pageViewModels: List<PageViewModel>,
                   fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

    private val pageFragments: MutableMap<Int, PageFragment> = mutableMapOf()
    private val valueChangesSubject = PublishSubject.create<FieldPathWithValue>()

    override fun getItem(position: Int): Fragment {
        val pageFragment = PageFragment.newInstance(position)
        pageFragments.put(position, pageFragment)
        return pageFragments[position]!!
    }

    override fun getCount(): Int = pageViewModels.size

    override fun instantiateItem(container: ViewGroup?, position: Int): Any {
        val pageFragment = super.instantiateItem(container, position) as PageFragment
        pageFragment.observeValueChanges().subscribe(valueChangesSubject)
        return pageFragment
    }

    fun getPageFragment(position: Int) = pageFragments[position]

    /**
     * Updates the [PageFragment] at the that contains the fields at the given [FieldPath] using the
     * provided [PageViewModel].
     */
    fun updateField(path: FieldPath, pageViewModel: PageViewModel) {
        pageFragments[path.pageIndex]?.updateField(path, pageViewModel)
    }

    fun observeValueChanges(): Observable<FieldPathWithValue> = valueChangesSubject.asObservable()
}

