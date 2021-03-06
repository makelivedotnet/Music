package com.github.s0nerik.music.binding_adapters

import android.databinding.BindingAdapter
import android.graphics.PorterDuff
import android.support.annotation.ColorInt
import android.support.annotation.MenuRes
import android.support.v7.widget.Toolbar

@BindingAdapter("navIconColor")
fun setToolbarNavIconColor(toolbar: Toolbar, @ColorInt color: Int) {
    toolbar.navigationIcon?.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
}

@BindingAdapter("menu")
fun setToolbarMenu(toolbar: Toolbar, @MenuRes menuId: Int) {
    toolbar.inflateMenu(menuId)
}