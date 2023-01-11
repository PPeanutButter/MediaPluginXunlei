package com.peanut.xunleivpn

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.DocumentsContract
import android.util.Base64
import android.util.Base64.decode
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.documentfile.provider.DocumentFile
import com.peanut.xunleivpn.ui.theme.XunleiVPNTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.net.URLDecoder
import java.util.regex.Pattern
import java.util.regex.Pattern.CASE_INSENSITIVE
import kotlin.concurrent.thread
import kotlin.math.min

class MainActivity : ComponentActivity() {
    private val requestDataAccess =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            println(it.resultCode)
            if (it.resultCode == Activity.RESULT_OK) {
                contentResolver.takePersistableUriPermission(
                    it.data!!.data!!, Intent.FLAG_GRANT_READ_URI_PERMISSION
                            or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                permissionDataAccess.value = true
            }
        }

    private var permissionDataAccess = MutableStateFlow(false)

    private fun isGranted(): Boolean {
        for (it in contentResolver.persistedUriPermissions) {
            if (it.uri.toString().startsWith(
                    "content://com.android.externalstorage.documents/tree/primary%3A" +
                            "Android%2Fdata%2Fcom.xunlei.downloadprovider"
                )
            )
                return true
        }
        return false
    }

    private fun requestAccessAndroidData() {
        try {
            val uri: Uri =
                Uri.parse(
                    "content://com.android.externalstorage.documents/document/primary%3A" +
                            "Android%2Fdata%2Fcom.xunlei.downloadprovider"
                )
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri)
            }
            intent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
            )
            requestDataAccess.launch(intent)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    @Composable
    fun DataAccess(content: @Composable () -> Unit) {
        val g by permissionDataAccess.collectAsState()
        if (g || isGranted()) {
            content()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Button(onClick = {
                    requestAccessAndroidData()
                }) {
                    Text(text = "授予Android/data访问权")
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        SettingManager.init(this)
        setContent {
            XunleiVPNTheme {
                DataAccess {
                    // A surface container using the 'background' color from the theme
                    val context = LocalContext.current
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
                        val scope = rememberCoroutineScope()
                        val sheetState =
                            rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
                        val userFolder = remember { mutableStateListOf<DocumentFile>() }
                        val currentUser by remember { mutableStateOf("Unknown") }

                        BottomSheet(sheetState, sheetContent = {
                            SheetContent(
                                folders = userFolder,
                                currentUser = currentUser
                            ) { selectDocumentFile ->
                                scope.launch(Dispatchers.IO) {
                                    delay(300)
                                    sheetState.hide()
                                    Cache.clearTask()
                                    ls(selectDocumentFile, regex = ".*\\.js") { jsFile ->
                                        try {
                                            context.contentResolver.openInputStream(jsFile.uri)
                                                ?.let {
                                                    val raw = it.bufferedReader()
                                                        .use(BufferedReader::readText)
                                                    it.close()
                                                    println(
                                                        raw.substring(
                                                            0,
                                                            min(raw.length, 50)
                                                        )
                                                    )
                                                    val jsonObject =
                                                        JSONObject(
                                                            String(
                                                                decode(
                                                                    raw,
                                                                    Base64.DEFAULT
                                                                )
                                                            )
                                                        )
                                                    val url = jsonObject.getString("Url")
                                                    val name = jsFile.name!!.substring(
                                                        1,
                                                        jsFile.name!!.length - 3
                                                    )
                                                    Cache.taskName.add(name)
                                                    Cache.task.add(
                                                        URLDecoder.decode(
                                                            url,
                                                            "UTF-8"
                                                        )
                                                    )
                                                }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                    ls(selectDocumentFile, regex = ".*") { file ->
                                        file.delete()
                                    }
                                }
                            }
                        }) {
                            Scaffold(
                                topBar = {
                                    HomeTopAppBar(
                                        title = "迅雷抓包",
                                        scrollBehavior = scrollBehavior,
                                        onSettingClicked = {
                                            this@MainActivity.startActivity(
                                                Intent(
                                                    this@MainActivity,
                                                    SettingActivity::class.java
                                                )
                                            )
                                        },
                                        onLoadFile = {
                                            scope.launch(Dispatchers.IO) {
                                                userFolder.clear()
                                                ls(
                                                    DocumentFile.fromTreeUri(
                                                        context,
                                                        Uri.parse(
                                                            "content://com.android.externalstorage.documents/tree/primary%3A" +
                                                                    "Android%2Fdata%2Fcom.xunlei.downloadprovider/document/primary%3AAndroid%2Fdata%2F" +
                                                                    "com.xunlei.downloadprovider%2Ffiles%2FThunderDownload"
                                                        )
                                                    )!!, ".*"
                                                ) { folder: DocumentFile ->
                                                    userFolder.add(
                                                        folder
                                                    )
                                                }
                                            }
                                            scope.launch { sheetState.show() }
                                        },
                                    )
                                },
                                floatingActionButton = {
                                    SendFloatingActionButton {
                                        thread {
                                            Cache.forEachTask { name, url ->
                                                Cache.send(
                                                    name = name,
                                                    url = url,
                                                    context = this@MainActivity
                                                )
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                            ) {
                                CaptureList(
                                    modifier = Modifier.padding(it),
                                    list = Cache.taskName
                                )
                            }
                        }
                    }
                }
            }
        }
        Cache.callback = {
            Handler(this@MainActivity.mainLooper).post {
                Toast.makeText(
                    this@MainActivity,
                    it,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun String.regex(regex: String, mode: Int): Boolean {
        val p = Pattern.compile(regex, mode).matcher(this)
        return p.find()
    }

    private fun ls(
        documentFile: DocumentFile,
        regex: String = ".*",
        onFind: (DocumentFile) -> Unit
    ) {
        println("ls ${documentFile.uri}")
        if (documentFile.isDirectory) {
            documentFile.listFiles().forEach {
                if (it.name?.regex(regex, CASE_INSENSITIVE) == true) {
                    onFind(it)
                }
            }
        } else {
            println("are u ls a File?")
        }
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
fun SheetContent(
    folders: List<DocumentFile>,
    currentUser: String,
    onSelected: (DocumentFile) -> Unit
) {
    Spacer(modifier = Modifier.height(16.dp))
    if (folders.isNotEmpty()) {
        folders.forEach {
            FolderItem(
                folder = it,
                isCurrent = (it.name ?: "").startsWith(currentUser, true),
                onSelected = onSelected
            )
        }
    } else {
        Text(text = "No User Folder Found.", modifier = Modifier.padding(12.dp))
    }
}

@Composable
fun FolderItem(
    folder: DocumentFile,
    isCurrent: Boolean = false,
    onSelected: (DocumentFile) -> Unit
) {
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
        Text(
            text = folder.name ?: "",
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
        )
    }
}

