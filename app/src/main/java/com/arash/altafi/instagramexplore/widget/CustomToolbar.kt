package com.arash.altafi.instagramexplore.widget

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageView
import com.arash.altafi.instagramexplore.R
import com.arash.altafi.instagramexplore.databinding.LayoutCustomToolbarBinding
import com.arash.altafi.instagramexplore.ext.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.parcelize.Parcelize

class CustomToolbar : RelativeLayout {

    @Parcelize
    internal class MyState(
        val superSavedState: Parcelable?,
        val isShowSearch: Boolean,
    ) : View.BaseSavedState(superSavedState), Parcelable

    private var isShowSearchBar = false
        set(value) {
            if (field == value)
                return

            field = value
            if (value.not())
                onHideSearchBar?.invoke()
        }

    constructor(context: Context) : super(context) {
        handleAttrs(null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        handleAttrs(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) {
        handleAttrs(attrs)
    }

    private val binding by lazy {
        LayoutCustomToolbarBinding.inflate(LayoutInflater.from(context))
    }

    private var onMenuItemClick: ((drawableId: Int) -> Unit)? = null
    private var onBackClick: (() -> Unit)? = null
    var onHideSearchBar: (() -> Unit)? = null
    var onBackClickToolbar: (() -> Unit)? = null

    override fun onSaveInstanceState(): Parcelable {
        return MyState(super.onSaveInstanceState(), isShowSearchBar)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val myState = state as? MyState
        super.onRestoreInstanceState(myState?.superSavedState ?: state)
        isShowSearchBar = myState?.isShowSearch ?: false

        binding.apply {
            if (!isShowSearchBar) {
                linSearch.toHide()
                rlContent.toShow()
                etQuery.hideKeyboard()
            } else {
                linSearch.toShow()
                rlContent.toHide()
                etQuery.requestFocus()
                etQuery.showKeyboard()
            }
        }
    }

    init {
        setupSearchbar()
        binding.ivBack.setOnClickListener {
            onBackClickToolbar?.invoke()
        }

        addView(binding.root)
    }

    private fun handleAttrs(attrs: AttributeSet?) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.CustomToolbar)
        val n = ta.indexCount
        for (i in 0..n) {
            val attr = ta.getIndex(i)
            if (ta.hasValue(attr)) {
                if (attr == R.styleable.CustomToolbar_title) {
                    binding.tvTitle.text = ta.getString(attr)
                } else if (attr == R.styleable.CustomToolbar_btnBackShow) {
                    if (ta.getBoolean(attr, true)) {
                        binding.ivBack.toShow()
                    } else {
                        binding.ivBack.toGone()
                    }
                }
            }


        }

        ta.recycle()
    }

    private fun clearMenuItems() {
        binding.llMenuContainer.removeAllViews()
    }

    private fun addImageView(@DrawableRes drawableId: Int) {
        binding.run {
            val imageView = AppCompatImageView(context)
            imageView.tag = drawableId
            imageView.setImageResource(drawableId)
            val imageSize = 40.toPx()
            val imagePadding = 8.toPx()
            imageView.layoutParams =
                LayoutParams(imageSize, imageSize)
            imageView.setPadding(imagePadding, imagePadding, imagePadding, imagePadding)
            imageView.setOnClickListener {
                "drawableId = $drawableId".logE("onMenuItemClick")
                onMenuItemClick?.invoke(drawableId)
            }
            llMenuContainer.addView(imageView)
        }

    }

    private fun setupSearchbar() = binding.apply {
        ivClear.setOnClickListener {
            if (etQuery.textString().isEmpty())
                ivClose.performClick()
            else
                etQuery.clear()
        }

        ivClose.setOnClickListener {
            hideSearchbar()
            etQuery.clear()
        }
    }

    fun onChangeSearch(
        waitMs: Long = 500L,
        scope: CoroutineScope,
        destinationFunction: (String) -> Unit,
    ) = binding.etQuery.onChange(waitMs, scope, destinationFunction)

    fun setText(text: String?) = binding.etQuery.setText(text)

    fun setActionListener(listener: TextView.OnEditorActionListener) {
        binding.etQuery.setOnEditorActionListener(listener)
    }

    fun showSearchbar() = binding.apply {
        if (!isShowSearchBar) {
            linSearch.reveal(350, RevealModel.END)
            rlContent.unReveal(350, RevealModel.START)
            etQuery.requestFocus()
            etQuery.showKeyboard()

            isShowSearchBar = true
        }
    }

    fun hideSearchbar() = binding.apply {
        if (isShowSearchBar) {
            rlContent.reveal(350, RevealModel.START)
            linSearch.unReveal(350, RevealModel.END)
            etQuery.hideKeyboard()

            isShowSearchBar = false
        }
    }

    fun isShowSearchbar() = isShowSearchBar

    fun initToolbar(
        title: String? = null,
        menu: List<Int>? = null,
        onBackClick: (() -> Unit)? = null,
        onMenuItemClick: ((drawableId: Int) -> Unit)? = null,
    ) {
        binding.run {
            title?.let { tvTitle.text = it }
            clearMenuItems()
            menu?.reversed()?.forEach { id -> addImageView(id) }
            onBackClick?.apply {
                setOnBackClick(this)
            }
            setOnMenuItemClick(onMenuItemClick)
        }
    }

    fun hideMenu() {
        binding.llMenuContainer.toGone()
    }

    fun showMenu() {
        binding.llMenuContainer.toShow()
    }

    fun hideMenuItem(@DrawableRes drawableId: Int) {
        binding.llMenuContainer.findViewWithTag<ImageView>(drawableId)?.toGone()
    }

    fun showMenuItem(@DrawableRes drawableId: Int) {
        binding.llMenuContainer.findViewWithTag<ImageView>(drawableId)?.toShow()
    }

    fun setTitle(title: String) {
        binding.tvTitle.text = title
    }

    fun setOnMenuItemClick(onMenuItemClick: ((drawableId: Int) -> Unit)? = null) {
        this.onMenuItemClick = onMenuItemClick
    }

    fun setOnBackClick(onBackClick: (() -> Unit)? = null) {
        this.onBackClick = onBackClick
    }
}