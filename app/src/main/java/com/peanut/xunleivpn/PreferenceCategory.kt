package com.peanut.xunleivpn

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp

@Composable
fun PreferenceCategory(title: String = "",
                       painter: Painter? = null,
                       content: @Composable ColumnScope.() -> Unit){
    Column {
        Row(modifier = Modifier.padding(4.dp)) {
            if (painter != null)
                Icon(painter = painter, contentDescription = "Preference Category Icon")
            Text(text = title, color = MaterialTheme.colors.primary)
        }
        Column(modifier = Modifier.fillMaxSize(),content = content)
    }
}