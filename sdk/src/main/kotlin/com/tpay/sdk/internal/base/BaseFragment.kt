package com.tpay.sdk.internal.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.tpay.sdk.R
import com.tpay.sdk.di.injectFields
import com.tpay.sdk.extensions.isInvisible
import com.tpay.sdk.extensions.px
import com.tpay.sdk.internal.ActivityResultHandler
import com.tpay.sdk.internal.LanguageSwitcher
import com.tpay.sdk.internal.SheetFragment
import java.util.*
import javax.inject.Inject


internal abstract class BaseFragment(layout: Int) : Fragment(layout) {
    protected abstract val binding: ViewBinding
    protected abstract val viewModel: BaseViewModel

    protected val sheetFragment
        get() = parentFragment as SheetFragment

    init {
        injectFields()
    }

    @Inject
    lateinit var languageSwitcher: LanguageSwitcher

    @Inject
    lateinit var activityResultHandler: ActivityResultHandler

    private lateinit var modifiedCtx: Context

    override fun getContext(): Context? {
        return modifiedCtx
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        modifiedCtx = getModifiedContextForLocale(languageSwitcher.localeObservable.value ?: Locale.getDefault())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(LayoutInflater.from(modifiedCtx), container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val scrollView = view.findViewById<NestedScrollView>(R.id.scrollView)
        val shadowView = view.findViewById<View>(R.id.shadowView)
        val spaceAboveLogo = view.findViewById<View>(R.id.spaceAboveLogo)
        val bottomBar = view.findViewById<LinearLayout>(R.id.bottomBarLayout)


        if (scrollView != null && shadowView != null) {
            scrollView.post {
                manageLayoutChanges()

                sheetFragment.onSlide.observe {
                    val spaceAboveLogoBottom = spaceAboveLogo.bottom
                    val scrollViewBottom = scrollView.bottom
                    val scrollViewChildBottom = scrollView.getChildAt(0).bottom

                    shadowView.isInvisible =
                        (spaceAboveLogoBottom <= scrollViewBottom &&
                                scrollViewChildBottom > spaceAboveLogoBottom) || (it == 1f && !scrollView.canScrollVertically(1))

                    if (scrollView.canScrollVertically(1)){
                        spaceAboveLogo.layoutParams =
                            (spaceAboveLogo.layoutParams as LinearLayout.LayoutParams).also { params ->
                                params.height =
                                    (sheetFragment.binding.bottomSheet.height - HEIGHT_DIFFERENCE - bottomBar.height) - spaceAboveLogo.top
                            }
                    }
                }
            }

            scrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
                shadowView.isInvisible = !scrollView.canScrollVertically(1)
                sheetFragment.isShadowBelowHeaderVisible = scrollY > 0
            })
        }

        observeViewModelFields()
    }

    protected fun manageLayoutChanges() {
        if (view != null) {
            val scrollView = requireView().findViewById<NestedScrollView>(R.id.scrollView)
            val shadowView = requireView().findViewById<View>(R.id.shadowView)
            val bottomBar = requireView().findViewById<LinearLayout>(R.id.bottomBarLayout)
            val spaceAboveLogo = requireView().findViewById<View>(R.id.spaceAboveLogo)

            spaceAboveLogo.layoutParams =
                (spaceAboveLogo.layoutParams as LinearLayout.LayoutParams).also {
                    it.height = 0
                    it.weight = 1f
                }

            spaceAboveLogo.post {
                shadowView.isInvisible =
                    !(scrollView.canScrollVertically(1) || scrollView.canScrollVertically(
                        -1
                    ))
                if (spaceAboveLogo.top != 0
                    && spaceAboveLogo.height == 0
                    && spaceAboveLogo.top < (sheetFragment.binding.bottomSheet.height - HEIGHT_DIFFERENCE - bottomBar.height)
                ) {
                    spaceAboveLogo.layoutParams =
                        (spaceAboveLogo.layoutParams as LinearLayout.LayoutParams).also {
                            it.height =
                                (sheetFragment.binding.bottomSheet.height - HEIGHT_DIFFERENCE - bottomBar.height) - spaceAboveLogo.top
                        }
                }
            }
        }
    }

    private fun observeViewModelFields(){
        sheetFragment.run {
            viewModel.run {
                screenClickable.observe { clickable ->
                    isClickBlockerVisible = !clickable
                }
                errorMessageId.observe { id ->
                    if(id != -1){
                        showErrorMessage(
                            getModifiedContextForLocale(
                                languageSwitcher.localeObservable.value ?: Locale.getDefault()
                            ).getString(id)
                        )
                    }
                }
                errorMessage.observe { message ->
                    if(message.isNotBlank()){
                        showErrorMessage(message)
                    }
                }
            }
        }
    }

    protected fun getModifiedContextForLocale(locale: Locale): Context {
        val configuration = requireActivity().resources.configuration.also { it.setLocale(locale) }
        val context = requireActivity().createConfigurationContext(configuration)
        context.setTheme(com.google.android.material.R.style.Theme_MaterialComponents_Light_NoActionBar)
        return context
    }

    companion object {
        private val HEIGHT_DIFFERENCE = 120.px
    }
}