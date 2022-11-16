package com.peanut.xunleivpn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.peanut.xunleivpn.ui.theme.XunleiVPNTheme

class SettingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            XunleiVPNTheme {
                PreferenceScreen {
                    PreferenceCategory(title = "服务器"){
                        EditTextPreference(id = "ip", title = "树莓派地址")
                    }
                }
            }
        }
    }
}