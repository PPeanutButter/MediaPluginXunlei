package com.peanut.xunleivpn

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseTopAppBar(
    title: @Composable () -> Unit = {},
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
//    val backgroundColors = TopAppBarDefaults.smallTopAppBarColors()
//    val backgroundColor = backgroundColors.containerColor(
//        scrollFraction = scrollBehavior?.scrollFraction ?: 0f
//    ).value
    val foregroundColors = TopAppBarDefaults.smallTopAppBarColors(
        containerColor = Color.Transparent,
        scrolledContainerColor = Color.Transparent
    )
    // Wrapping in a Surface to handle window insets https://issuetracker.google.com/issues/183161866
    Surface {
        TopAppBar(
            title = title,
            modifier = Modifier.windowInsetsPadding(
                WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Horizontal + WindowInsetsSides.Top
                )
            ),
            navigationIcon = navigationIcon,
            actions = actions,
            colors = foregroundColors,
            scrollBehavior = scrollBehavior
        )
    }
}