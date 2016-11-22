package it.facile.form.ui.utils

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

import rx.Observable
import rx.Subscriber
import rx.android.MainThreadSubscription

class EditTextOnSubscribe(private val editText: EditText, private val initialVal: Boolean) : Observable.OnSubscribe<CharSequence> {

    override fun call(subscriber: Subscriber<in CharSequence>) {

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // If the subscriber is subscribed emit the new text value
                if (!subscriber.isUnsubscribed) {
                    subscriber.onNext(s)
                }
            }

            override fun afterTextChanged(s: Editable) {

            }
        }
        editText.addTextChangedListener(watcher)

        // Add the a main thread safe subscription
        subscriber.add(object : MainThreadSubscription() {
            override fun onUnsubscribe() {
                editText.removeTextChangedListener(watcher)
            }
        })

        if (initialVal) subscriber.onNext(editText.text)
    }
}
