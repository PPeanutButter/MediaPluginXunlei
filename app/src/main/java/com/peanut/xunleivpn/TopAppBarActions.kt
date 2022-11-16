package com.peanut.xunleivpn

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.twotone.Refresh
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun SettingAction(onClicked: () -> Unit) {
    IconButton(onClick = { onClicked() }) {
        Icon(imageVector = Icons.Rounded.Edit, contentDescription = null)
    }
}

@Composable
fun SendFloatingActionButton(onClicked: () -> Unit) {
    FloatingActionButton(
        modifier = Modifier.padding(16.dp),
        onClick = { onClicked() },
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Icon(imageVector = Icons.Rounded.Send, contentDescription = null)
    }
}

@Composable
fun RefreshAction(onClicked: () -> Unit){
    IconButton(onClick = { onClicked() }) {
        Icon(imageVector = Icons.Rounded.Refresh, contentDescription = null)
    }
}
