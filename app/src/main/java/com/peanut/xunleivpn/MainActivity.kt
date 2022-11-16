@file:Suppress("SameParameterValue")

package com.peanut.xunleivpn

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Base64
import android.util.Base64.decode
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.peanut.xunleivpn.CommandExecution.execCmdsforResult
import com.peanut.xunleivpn.ui.theme.XunleiVPNTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.net.URLDecoder
import kotlin.concurrent.thread
import kotlin.math.min

class MainActivity : ComponentActivity() {

    @SuppressLint("SdCardPath")
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        SettingManager.init(this)
        val xunleiPathRoot = this@MainActivity.externalCacheDir!!.parentFile!!.parent!! + "/com.xunlei.downloadprovider/files/ThunderDownload"
        setContent {
            XunleiVPNTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
                    val scope = rememberCoroutineScope()
                    val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
                    val userFolder = remember { mutableStateListOf<String>() }
                    var currentUser by remember { mutableStateOf("") }

                    LaunchedEffect(key1 = true, block = {
                        try {
                            scope.launch {
                                rm("/data/data/${this@MainActivity.packageName}/shared_prefs/xl-acc-user.xml")
                                cp(
                                    "/data/data/com.xunlei.downloadprovider/shared_prefs/xl-acc-user.xml",
                                    "/data/data/${this@MainActivity.packageName}/shared_prefs/xl-acc-user.xml"
                                )
                                chmod("/data/data/${this@MainActivity.packageName}/shared_prefs/xl-acc-user.xml", 777)
                                this@MainActivity.getSharedPreferences(
                                    "xl-acc-user",
                                    Context.MODE_PRIVATE
                                ).apply {
                                    currentUser = this.getString("UserID", "") ?: ""
                                    println("currentUser: $currentUser")
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    })

                    BottomSheet(sheetState, sheetContent = {
                        SheetContent(
                            folders = userFolder,
                            currentUser = currentUser
                        ) {
                            scope.launch {
                                delay(300)
                                sheetState.hide()
                                Cache.clearTask()
                                val selectedPath = "$xunleiPathRoot/$it"
                                val dstFolder = this@MainActivity.getExternalFilesDir("JsCaches")!!.path
                                if (File(dstFolder).exists())
                                    rm(dstFolder, "-r")
                                mkdir(dstFolder)
                                ls(selectedPath, param = "-a .*.js") { jsFile ->
                                    mv("$selectedPath$jsFile", "${dstFolder}/$jsFile")
                                }
                                rm(selectedPath, "-r")
                                ls(dstFolder, param = "-a .*.js") { jsFile ->
                                    try {
                                        val raw = File("$dstFolder/$jsFile").readText()
                                        println(raw.substring(0, min(raw.length, 50)))
                                        val jsonObject = JSONObject(String(decode(raw, Base64.DEFAULT)))
                                        val url = jsonObject.getString("Url")
                                        val name = jsFile.substring(1, jsFile.length - 3)
                                        Cache.taskName.add(name)
                                        Cache.task.add(URLDecoder.decode(url, "UTF-8"))
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                        }
                    }) {
                        Scaffold(
                            topBar = {
                                HomeTopAppBar(
                                    title = "迅雷抓包",
                                    scrollBehavior = scrollBehavior,
                                    onSettingClicked = { this@MainActivity.startActivity(Intent(this@MainActivity, SettingActivity::class.java)) },
                                    onLoadFile = { thread { userFolder.clear(); ls(xunleiPathRoot, "-d */") { folder: String -> userFolder.add(folder) } }; scope.launch { sheetState.show() } },
                                )
                            },
                            floatingActionButton = {
                                SendFloatingActionButton {
                                    thread { Cache.forEachTask { name, url -> Cache.send(name = name, url = url, context = this@MainActivity) } }
                                }
                            },
                            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                        ) {
                            CaptureList(modifier = Modifier.padding(it), list = Cache.taskName)
                        }
                    }
                }
            }
        }
        Cache.callback = { Handler(this@MainActivity.mainLooper).post { Toast.makeText(this@MainActivity, it, Toast.LENGTH_SHORT).show() } }
    }

    private fun ls(path: String, param: String = "", onFind: (String) -> Unit) {
        println("cd $path ls $param")
        execCmdsforResult(arrayOf("cd $path", "ls $param")).forEach { name ->
            println(name)
            if (name is String && name.isNotEmpty()) {
                onFind(name)
            }
        }
    }

    private fun cp(srcPath: String, dstPath: String) {
        CommandExecution.execCommand("cp '$srcPath' '$dstPath'", true)
    }

    private fun mv(srcPath: String, dstPath: String) {
        CommandExecution.execCommand("mv '$srcPath' '$dstPath'", true)
    }

    private fun rm(path: String, param: String = "") {
        CommandExecution.execCommand("rm $param '$path'", true)
    }

    private fun mkdir(path: String, param: String = "") {
        CommandExecution.execCommand("mkdir $param '$path'", true)
    }

    private fun chmod(path: String, mode: Int) {
        CommandExecution.execCommand("chmod $mode '$path'", true)
    }
}

@Composable
fun CaptureList(modifier: Modifier = Modifier, list: MutableList<String>) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(list) { name ->
            Text(text = name, modifier = Modifier
                .clickable { }
                .padding(vertical = 4.dp), maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopAppBar(
    title: String,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    onSettingClicked: () -> Unit,
    onLoadFile: () -> Unit
) {
    BaseTopAppBar(
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        actions = {
            SettingAction {
                onSettingClicked()
            }
            RefreshAction {
                onLoadFile()
            }
        },
        scrollBehavior = scrollBehavior
    )
}

@Composable
fun SheetContent(folders: List<String>, currentUser: String, onSelected: (String) -> Unit) {
    Spacer(modifier = Modifier.height(16.dp))
    if (folders.isNotEmpty()) {
        folders.forEach {
            FolderItem(folder = it, isCurrent = it.startsWith(currentUser, true), onSelected = onSelected)
        }
    } else {
        Text(text = "No User Folder Found.", modifier = Modifier.padding(12.dp))
    }
}

@Composable
fun FolderItem(folder: String, isCurrent: Boolean = false, onSelected: (String) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
        .clickable { onSelected(folder) }
        .padding(horizontal = 16.dp, vertical = 4.dp)
        .fillMaxWidth()) {
        Icon(
            painter = painterResource(id = R.drawable.ic_baseline_insert_drive_file_24),
            modifier = Modifier
                .padding(8.dp)
                .size(24.dp),
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(20.dp))
        Text(text = folder, fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal)
    }
}

