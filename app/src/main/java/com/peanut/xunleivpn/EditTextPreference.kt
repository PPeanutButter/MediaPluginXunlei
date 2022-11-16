package com.peanut.xunleivpn

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun EditTextPreference(
    id: String,
    title: String = "",
    summary: String = "",
    painter: Painter? = null
) {
    var clicked by remember { mutableStateOf(false) }
    Row(modifier = Modifier
        .padding(4.dp)
        .fillMaxWidth()
        .clickable { clicked = !clicked }) {
        if (painter != null)
            Icon(painter = painter, contentDescription = "Preference Category Icon")
        Column {
            Text(text = title, color = MaterialTheme.colors.onSurface)
            Text(
                text = summary,
                color = MaterialTheme.colors.onSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
    if (clicked)
        Dialog(onDismissRequest = { clicked = false }) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium.copy(all = CornerSize(10.dp)),
                color = MaterialTheme.colors.background
            ) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column {
                        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        var inputs by remember { mutableStateOf(SettingManager.getValue(id, "")) }
                        OutlinedTextField(value = inputs, onValueChange = {
                            inputs = it
                            SettingManager[id] = it
                        }, label = {
                            Text(
                                text = "IP:PORT"
                            )
                        })
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(onClick = { clicked = false }) {
                                Text(text = "OK")
                            }
                        }
                    }
                }
            }
        }
}