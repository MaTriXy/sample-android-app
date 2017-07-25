package info.adavis.sampleapp.features.rxbinding

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import com.jakewharton.rxbinding2.view.clicks
import info.adavis.sampleapp.R
import info.adavis.sampleapp.data.Injector.getApi
import info.adavis.sampleapp.data.SampleAPI
import info.adavis.sampleapp.data.User
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.toast
import timber.log.Timber

class RxBindingActivity : AppCompatActivity() {

    val myButton: Button by lazy { findViewById(R.id.button) as Button }
    val usersText: TextView by lazy { findViewById(R.id.textView) as TextView }

    var disposable: Disposable? = null

    lateinit var api: SampleAPI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rxbinding)

        api = getApi()
    }

    override fun onResume() {
        super.onResume()

        disposable = myButton.clicks()
                .observeOn(Schedulers.io())
                .flatMapSingle { getAllUsers() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::handleUsers,
                        this::handleError
                )
    }

    override fun onPause() {
        disposable?.dispose()
        super.onPause()
    }

    fun getAllUsers(): Single<List<User>> {
        return api.getUsers()
                .subscribeOn(Schedulers.io())
                .onErrorResumeNext(Single.just(emptyList()))
                .doOnSuccess { Timber.i("users: $it") }
    }

    fun handleUsers(users: List<User>) {
        toast("There are: ${users.size} users")

        with(StringBuilder()) {
            users.forEach {
                appendln(it.username)
            }
            usersText.text = toString()
        }
    }

    fun handleError(t: Throwable) {
        toast("Unable to retrieve users")

        usersText.text = ""
        Timber.e(t, "there was an error processing the request")
    }

}
