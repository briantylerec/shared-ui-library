package com.redbull.lab.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.redbull.lab.ui.theme.Colors

/** Applies the shared platform color scheme to a consumer screen. */
@Composable
fun RedBullTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(primary = Colors.Primary, secondary = Colors.Secondary),
        content = content
    )
}
