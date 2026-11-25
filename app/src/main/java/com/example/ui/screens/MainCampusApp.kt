package com.example.ui.screens

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.R
import com.example.data.firebase.CampusRepository
import com.example.data.firebase.authStateFlow
import com.example.data.firebase.signOutCampus
import com.example.data.model.UiState
import com.example.ui.theme.AutonomaOrange
import com.example.ui.theme.AutonomaOrangeContainer
import com.example.ui.theme.AutonomaOrangeDeep
import com.example.ui.viewmodel.CampusViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

private data class NavDestinationItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
)

@Composable
fun AppNavigation(
    isDarkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit,
    auth: FirebaseAuth = Firebase.auth
) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser

    if (user == null) {
        AuthScreen(onAuthSuccess = {})
    } else {
        key(user.uid) {
            AuthenticatedCampusShell(
                currentUserId = user.uid,
                isDarkTheme = isDarkTheme,
                onToggleDarkTheme = onToggleDarkTheme
            )
        }
    }
}

@Composable
private fun AuthenticatedCampusShell(
    currentUserId: String,
    isDarkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit
) {
    val viewModel: CampusViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                CampusViewModel(CampusRepository(db), currentUserId)
            }
        }
    )

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val snackbarHostState = remember { SnackbarHostState() }

    val profileState by viewModel.profileState.collectAsStateWithLifecycle()
    val coursesState by viewModel.coursesState.collectAsStateWithLifecycle()
    val modulesState by viewModel.modulesState.collectAsStateWithLifecycle()
    val tasksState by viewModel.tasksState.collectAsStateWithLifecycle()
    val forumPostsState by viewModel.forumPostsState.collectAsStateWithLifecycle()
    val attendanceLogsState by viewModel.attendanceLogsState.collectAsStateWithLifecycle()
    val paymentsState by viewModel.paymentsState.collectAsStateWithLifecycle()
    val reservationsState by viewModel.reservationsState.collectAsStateWithLifecycle()
    val messagesState by viewModel.messagesState.collectAsStateWithLifecycle()
    val selectedCourseId by viewModel.selectedCourseId.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.snackbarEvents.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    var currentTab by rememberSaveable { mutableIntStateOf(0) }
    var servicesTab by rememberSaveable { mutableIntStateOf(0) }

    val navItems = remember {
        listOf(
            NavDestinationItem("Inicio", Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
            NavDestinationItem("Cursos", Icons.Filled.Book, Icons.Outlined.Book, "nav_courses"),
            NavDestinationItem("Agenda", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth, "nav_agenda"),
            NavDestinationItem("Servicios", Icons.Filled.Apps, Icons.Outlined.Apps, "nav_services"),
            NavDestinationItem("Perfil", Icons.Filled.Person, Icons.Outlined.Person, "nav_profile")
        )
    }

    val selectedCourse = (coursesState as? UiState.Success)?.data?.firstOrNull { it.id == selectedCourseId }
    if (selectedCourse != null) {
        val courseModules = (modulesState as? UiState.Success)?.data.orEmpty().filter { it.courseId == selectedCourse.id }
        val courseTasks = (tasksState as? UiState.Success)?.data.orEmpty().filter { it.courseId == selectedCourse.id }
        val coursePosts = (forumPostsState as? UiState.Success)?.data.orEmpty().filter { it.courseId == selectedCourse.id }
        val courseLogs = (attendanceLogsState as? UiState.Success)?.data.orEmpty().filter { it.courseId == selectedCourse.id }
        val studentName = (profileState as? UiState.Success)?.data?.fullName ?: "Estudiante"

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            contentWindowInsets = WindowInsets.navigationBars
        ) { innerPadding ->
            CourseDetailScreen(
                course = selectedCourse,
                studentName = studentName,
                modules = courseModules,
                tasks = courseTasks,
                forumPosts = coursePosts,
                attendanceLogs = courseLogs,
                onBack = { viewModel.closeCourse() },
                onToggleModule = { id, done -> viewModel.toggleModuleCompleted(id, done) },
                onAddModule = { week, title, desc, res, type ->
                    viewModel.addModule(selectedCourse.id, week, title, desc, res, type)
                },
                onSubmitTask = { id, note, att -> viewModel.submitTask(id, note, att) },
                onCreateTask = { title, desc, type, due ->
                    viewModel.createTask(selectedCourse, title, desc, type, due)
                },
                onUpdateGrades = { ec1, ep, ec2, ef ->
                    viewModel.updateCourseGrades(selectedCourse.id, ec1, ep, ec2, ef)
                },
                onCreatePost = { title, content ->
                    viewModel.createForumPost(selectedCourse, studentName, title, content)
                },
                onLikePost = { id -> viewModel.likeForumPost(id) },
                onRecordAttendance = { title, date, status ->
                    viewModel.recordAttendance(selectedCourse, title, date, status)
                },
                modifier = Modifier.padding(innerPadding)
            )
        }
        return
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (!useRail) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        windowInsets = WindowInsets.navigationBars
                    ) {
                        navItems.forEachIndexed { index, item ->
                            NavigationBarItem(
                                selected = currentTab == index,
                                onClick = { currentTab = index },
                                icon = {
                                    Icon(
                                        imageVector = if (currentTab == index) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label
                                    )
                                },
                                label = { Text(item.label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = AutonomaOrangeDeep,
                                    selectedTextColor = AutonomaOrangeDeep,
                                    indicatorColor = AutonomaOrangeContainer
                                ),
                                modifier = Modifier.testTag(item.tag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail) {
                    NavigationRail(containerColor = MaterialTheme.colorScheme.surface) {
                        navItems.forEachIndexed { index, item ->
                            NavigationRailItem(
                                selected = currentTab == index,
                                onClick = { currentTab = index },
                                icon = {
                                    Icon(
                                        imageVector = if (currentTab == index) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label
                                    )
                                },
                                label = { Text(item.label) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = AutonomaOrangeDeep,
                                    selectedTextColor = AutonomaOrangeDeep,
                                    indicatorColor = AutonomaOrangeContainer
                                ),
                                modifier = Modifier.testTag(item.tag)
                            )
                        }
                    }
                }

                when (currentTab) {
                    0 -> HomeScreen(
                        profileState = profileState,
                        coursesState = coursesState,
                        tasksState = tasksState,
                        postsState = forumPostsState,
                        paymentsState = paymentsState,
                        onOpenCourse = { viewModel.openCourse(it) },
                        onNavigateToTab = { currentTab = it },
                        onOpenServiceSection = { tab ->
                            servicesTab = tab
                            currentTab = 3
                        },
                        onRetrySeed = { viewModel.ensureSeeded() },
                        modifier = Modifier.weight(1f)
                    )
                    1 -> CoursesScreen(
                        coursesState = coursesState,
                        onOpenCourse = { viewModel.openCourse(it) },
                        onEnrollCourse = { code, name, prof, cr, mod, room, day, hour, sched ->
                            viewModel.enrollCourse(code, name, prof, cr, mod, room, day, hour, sched)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    2 -> AgendaScreen(
                        coursesState = coursesState,
                        tasksState = tasksState,
                        onOpenCourse = { viewModel.openCourse(it) },
                        onSubmitTask = { id, note, att -> viewModel.submitTask(id, note, att) },
                        onCreateTask = { c, t, d, type, due -> viewModel.createTask(c, t, d, type, due) },
                        modifier = Modifier.weight(1f)
                    )
                    3 -> ServicesScreen(
                        selectedTab = servicesTab,
                        onSelectTab = { servicesTab = it },
                        profileState = profileState,
                        paymentsState = paymentsState,
                        reservationsState = reservationsState,
                        messagesState = messagesState,
                        onPayInstallment = { viewModel.payInstallment(it) },
                        onCreateReservation = { s, l, d, t -> viewModel.createReservation(s, l, d, t) },
                        onCancelReservation = { viewModel.cancelReservation(it) },
                        onSendMessage = { r, c, s, b -> viewModel.sendMessage(r, c, s, b) },
                        modifier = Modifier.weight(1f)
                    )
                    4 -> ProfileScreen(
                        profileState = profileState,
                        coursesState = coursesState,
                        isDarkTheme = isDarkTheme,
                        onToggleDarkTheme = onToggleDarkTheme,
                        onUpdateProfile = { fn, fac, car, cyc, cam, mod, ph ->
                            viewModel.updateProfile(fn, fac, car, cyc, cam, mod, ph)
                        },
                        onSignOut = {
                            signOutCampus(credentialManager = credentialManager, scope = scope)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
