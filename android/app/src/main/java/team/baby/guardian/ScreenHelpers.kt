package team.baby.guardian

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.ext.SdkExtensions
import android.util.Log
import androidx.annotation.RequiresExtension
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import team.baby.guardian.ui.DeviceLiveView
import team.baby.guardian.ui.HomeScreen
import team.baby.guardian.ui.LoginScreen
import team.baby.guardian.ui.SensorLiveView
import team.baby.guardian.ui.SettingsScreen
import team.baby.guardian.ui.readSettings

/**
 * enum values that represent the screens in the app
 */
enum class ScreenHelpers(@StringRes val title: Int) {
    Start(title = R.string.app_name),
    Settings(title = R.string.settings),
    Login(title = R.string.account),
    Edit(title = R.string.edit),
    People(title = R.string.people),
    LiveView(title = R.string.liveView),
    Sensor(title = R.string.sensor)
}

@Composable
fun AlertDialogExample(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
    icon: ImageVector,
) {
    AlertDialog(
        icon = {
            Icon(icon, contentDescription = "Example Icon")
        },
        title = {
            Text(text = dialogTitle, textAlign = TextAlign.Center)
        },
        text = {
            Text(text = dialogText, textAlign = TextAlign.Center)
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text(stringResource(id = R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text(stringResource(R.string.dismiss))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogAppBar(
    currentScreen: ScreenHelpers,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier.verticalScroll(rememberScrollState())
) {
    var showDialog by remember { mutableStateOf(false) }
    if (showDialog){
        AlertDialogExample(
            onDismissRequest = { showDialog = false },
            onConfirmation = { showDialog = false },
            dialogTitle = stringResource(R.string.developer_title),
            dialogText = stringResource(id = R.string.team_info),
            icon = Icons.Default.People
        )
    }

    TopAppBar(
        scrollBehavior = currentScreen.run {
            if (this == ScreenHelpers.LiveView) {
                TopAppBarDefaults.enterAlwaysScrollBehavior()
            } else {
                // Handle other cases or use a default scroll behavior
                // For example:
                TopAppBarDefaults.pinnedScrollBehavior()
            }
        },
        title = {
            Row {
                Text(stringResource(currentScreen.title))
                Spacer(modifier = Modifier.width(16.dp))
                AssistChip(
                    onClick = { Log.d("Assist chip", "hello world") },
                    label = { Text(stringResource(R.string.version)) },
                    leadingIcon = {
//                    Icon(
//                        Icons.Filled.Settings,
//                        contentDescription = "Localized description",
//                        Modifier.size(AssistChipDefaults.IconSize)
//                    )
                    }
                )
                if(stringResource(id = R.string.build_type) == "beta") {
                    FilterChip(
                        onClick = { },
                        label = {
                            Text(stringResource(R.string.in_beta))
                        },
                        selected = true,
                        leadingIcon = run {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Android,
                                    contentDescription = "in Beta",
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        },
                    )
                }
            }
        },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = imageVector,
                        contentDescription = contentDescription
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = {
                showDialog = true
            }) {
                Icon(Icons.Outlined.Info, contentDescription = "Information")
            }
        }
    )
}

@Composable
fun ElevatedAssistChipExample() {
    ElevatedAssistChip(
        onClick = { /* Do something! */ },
        label = { Text("Assist Chip") },
        leadingIcon = {
            Icon(
                Icons.Filled.Settings,
                contentDescription = "Localized description",
                Modifier.size(AssistChipDefaults.IconSize)
            )
        }
    )
}

@Composable
fun BottomAppBarExample(
    navController: NavHostController,
) {
    var selectedTab by remember { mutableStateOf(ScreenHelpers.Start) }

    // Observe changes in the navigation back stack
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    // Update selectedTab when the back stack entry changes
    LaunchedEffect(navBackStackEntry) {
        navBackStackEntry?.destination?.route?.let { route ->
            selectedTab = ScreenHelpers.valueOf(route)
        }
    }

    BottomAppBar(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.1f),
        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
//        contentPadding = BottomAppBarDefaults.ContentPadding,
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + slideInHorizontally(),
            exit = fadeOut() + slideOutHorizontally(),
            modifier = Modifier.weight(1f)
        ) {
            IconButton(
                onClick = {
                    if(selectedTab != ScreenHelpers.Settings) {
                        selectedTab = ScreenHelpers.Settings
                        navController.navigate(ScreenHelpers.Settings.name) {
                            popUpTo(ScreenHelpers.Settings.name) {
                                inclusive = true
                            }
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                if(selectedTab == ScreenHelpers.Settings) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings")
                }else{
                    Icon(Icons.Outlined.Settings, contentDescription = "Settings")
                }
            }
        }

        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + slideInHorizontally(),
            exit = fadeOut() + slideOutHorizontally(),
            modifier = Modifier.weight(1f)
        ) {
            IconButton(
                onClick = {
                    if(selectedTab != ScreenHelpers.Start) {
                        selectedTab = ScreenHelpers.Start
                        navController.navigate(ScreenHelpers.Start.name) {
                            popUpTo(ScreenHelpers.Start.name) {
                                inclusive = true
                            }
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                if(selectedTab == ScreenHelpers.Start) {
                    Icon(Icons.Filled.Home, contentDescription = "Start")
                }else{
                    Icon(Icons.Outlined.Home, contentDescription = "Start")
                }
            }
        }

        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + slideInHorizontally(),
            exit = fadeOut() + slideOutHorizontally(),
            modifier = Modifier.weight(1f)
        ) {
            IconButton(
                onClick = {
                    if(selectedTab != ScreenHelpers.Login) {
                        selectedTab = ScreenHelpers.Login
                        navController.navigate(ScreenHelpers.Login.name) {
                            popUpTo(ScreenHelpers.Login.name) {
                                inclusive = true
                            }
                        }
                    }
                },
                modifier = Modifier
                    .wrapContentWidth(),
//                    .padding(BottomAppBarDefaults.ContentPadding),
//                containerColor = BottomAppBarDefaults.containerColor,
//                elevation = FloatingActionButtonDefaults.elevation()
            ) {
                if(selectedTab == ScreenHelpers.Login) {
                    Icon(Icons.Filled.AccountCircle, contentDescription = "Account")
                }else{
                    Icon(Icons.Outlined.AccountCircle, contentDescription = "Account")
                }
            }
        }
//
//        AnimatedVisibility(
//            visible = true,
//            enter = fadeIn() + slideInHorizontally(),
//            exit = fadeOut() + slideOutHorizontally(),
//            modifier = Modifier.weight(1f)
//        ) {
//            IconButton(
//                onClick = {
//                    if(selectedTab != ScreenHelpers.People) {
//                        selectedTab = ScreenHelpers.People
//                    }
//                },
//                modifier = Modifier.weight(1f)
//            ) {
//                if(selectedTab == ScreenHelpers.People) {
//                    Icon(Icons.Filled.People, contentDescription = "People")
//                }else{
//                    Icon(Icons.Outlined.People, contentDescription = "People")
//                }
//            }
//        }
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@RequiresExtension(extension = Build.VERSION_CODES.R, version = 2)
@Composable
fun LogApp(
    navController: NavHostController = rememberNavController(),
    context: Context,
    windowSizeClass: WindowSizeClass
) {
    // Get current back stack entry
    val backStackEntry by navController.currentBackStackEntryAsState()
    // Get the name of the current screen
    val currentScreen = ScreenHelpers.valueOf(
        backStackEntry?.destination?.route ?: ScreenHelpers.Start.name
    )
    var serial_number by remember { mutableStateOf("") }
    var device_name by remember { mutableStateOf("") }
    var owner_status by remember { mutableStateOf("friend") }
    var user_name by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    coroutineScope.launch {
        serial_number = if(readSettings("serial_number") == null){
            ""
        }else{
            readSettings("serial_number").toString()
        }

        device_name = if(readSettings("device_name") == null){
            ""
        }else{
            readSettings("device_name").toString()
        }

        owner_status = if(readSettings("owner_status") == null){
            "friend"
        }else{
            readSettings("owner_status").toString()
        }

        user_name = if(readSettings("username") == null){
            ""
        }else{
            readSettings("username").toString()
        }
    }

    val showTopAppBar = windowSizeClass.heightSizeClass != WindowHeightSizeClass.Compact

    Scaffold(
        topBar = {
            if(showTopAppBar) {
                when (currentScreen.name) {
                    ScreenHelpers.Start.name -> {
                        //                    CenterAlignedTopAppBarExample(
                        //                        currentScreen = currentScreen,
                        //                        canNavigateBack = navController.previousBackStackEntry != null,
                        //                        imageVector = Icons.Filled.ArrowBack,
                        //                        contentDescription = stringResource(R.string.back_button),
                        //                        navigateUp = { navController.navigateUp() }
                        //                    )
                        //                    HomeAppBar(
                        //                        currentScreen = currentScreen,
                        //                        canNavigateBack = navController.previousBackStackEntry != null,
                        //                        imageVector = Icons.Filled.ArrowBack,
                        //                        contentDescription = stringResource(R.string.back_button),
                        //                        navigateUp = { navController.navigateUp() }
                        //                    )
                    }

                    ScreenHelpers.Settings.name -> {
                        LogAppBar(
                            currentScreen = currentScreen,
                            canNavigateBack = false,
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_button),
                            navigateUp = { navController.navigateUp() }
                        )
                    }

                    ScreenHelpers.LiveView.name -> {
                        LogAppBar(
                            currentScreen = currentScreen,
                            canNavigateBack = true,
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_button),
                            navigateUp = { navController.navigateUp() }
                        )
                    }

                    ScreenHelpers.Sensor.name -> {
                        LogAppBar(
                            currentScreen = currentScreen,
                            canNavigateBack = true,
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_button),
                            navigateUp = { navController.navigateUp() }
                        )
                    }
                }
            }
        },
        bottomBar = {
            when (currentScreen.name) {
                ScreenHelpers.LiveView.name -> {

                }
                ScreenHelpers.Sensor.name -> {

                }
                else -> {
                    BottomAppBarExample(navController)
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ScreenHelpers.Start.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = ScreenHelpers.Start.name) {
                if (SdkExtensions.getExtensionVersion(Build.VERSION_CODES.R) >= 2) {
                    HomeScreen(navController, windowSizeClass)
                }
            }
            composable(route = ScreenHelpers.Settings.name) {
                SettingsScreen(
                    navController
                )
            }
            composable(route = ScreenHelpers.Login.name) {
                LoginScreen(
                    navController
                )
            }
            composable(
                ScreenHelpers.LiveView.name,
            ) { backStackEntry ->
                // Subpage content
//                val serialNumber = backStackEntry.arguments?.getString("serialNumber")
                DeviceLiveView(serialNumber = serial_number, deviceName = device_name, ownerStatus = owner_status, context = LocalContext.current)
            }
            composable(ScreenHelpers.Sensor.name) {
                SensorLiveView(serialNumber = serial_number, deviceName = device_name, ownerStatus = owner_status, context = LocalContext.current)
            }
        }
    }
}
