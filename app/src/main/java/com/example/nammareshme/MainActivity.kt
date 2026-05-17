package com.example.nammareshme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.work.*
import com.example.nammareshme.data.AuthRepository
import com.example.nammareshme.data.BatchRepository
import com.example.nammareshme.data.LanguageManager
import com.example.nammareshme.data.WeatherRepository
import com.example.nammareshme.data.api.RetrofitClient
import com.example.nammareshme.notifications.FarmingWorker
import com.example.nammareshme.ui.screens.*
import com.example.nammareshme.ui.viewmodel.*
import java.util.Locale
import java.util.concurrent.TimeUnit

class AppViewModelFactory(
    private val languageManager: LanguageManager,
    private val authRepository: AuthRepository,
    private val weatherRepository: WeatherRepository,
    private val batchRepository: BatchRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AppViewModel::class.java) -> AppViewModel(languageManager) as T
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> AuthViewModel(authRepository) as T
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> ProfileViewModel(authRepository, batchRepository, languageManager) as T
            modelClass.isAssignableFrom(WeatherViewModel::class.java) -> WeatherViewModel(weatherRepository) as T
            modelClass.isAssignableFrom(BatchViewModel::class.java) -> BatchViewModel(batchRepository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val languageManager = LanguageManager(applicationContext)
        val authRepository = AuthRepository()
        val weatherRepository = WeatherRepository(RetrofitClient.weatherApi)
        val batchRepository = BatchRepository()
        val factory = AppViewModelFactory(languageManager, authRepository, weatherRepository, batchRepository)
        
        scheduleFarmingWorker()

        setContent {
            val appViewModel: AppViewModel = viewModel(factory = factory)
            val authViewModel: AuthViewModel = viewModel(factory = factory)
            val profileViewModel: ProfileViewModel = viewModel(factory = factory)
            val weatherViewModel: WeatherViewModel = viewModel(factory = factory)
            val batchViewModel: BatchViewModel = viewModel(factory = factory)
            
            val authState by authViewModel.authState.collectAsState()
            val currentLanguage by appViewModel.currentLanguage.collectAsState()
            val isDarkMode by appViewModel.isDarkMode.collectAsState()
            
            val locale = remember(currentLanguage) { 
                if (currentLanguage == "Kannada") Locale("kn") else Locale("en") 
            }
            
            LaunchedEffect(locale) {
                Locale.setDefault(locale)
            }

            val configuration = LocalConfiguration.current
            configuration.setLocale(locale)
            val context = LocalContext.current
            
            val localizedContext = remember(configuration, locale) {
                context.createConfigurationContext(configuration)
            }

            CompositionLocalProvider(
                LocalActivityResultRegistryOwner provides this@MainActivity,
                LocalOnBackPressedDispatcherOwner provides this@MainActivity,
                LocalLifecycleOwner provides this@MainActivity,
                LocalContext provides localizedContext
            ) {
                NammaReshmeTheme(darkTheme = isDarkMode) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        if (authState is AuthState.Idle) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        } else {
                            AppNavigation(appViewModel, authViewModel, profileViewModel, weatherViewModel, batchViewModel)
                        }
                    }
                }
            }
        }
    }

    private fun scheduleFarmingWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val farmingRequest = PeriodicWorkRequestBuilder<FarmingWorker>(2, TimeUnit.HOURS)
            .setConstraints(constraints)
            .addTag("farming_worker")
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "FarmingAlerts",
            ExistingPeriodicWorkPolicy.KEEP,
            farmingRequest
        )
    }
}

@Composable
fun AppNavigation(
    appViewModel: AppViewModel,
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel,
    weatherViewModel: WeatherViewModel,
    batchViewModel: BatchViewModel
) {
    val navController = rememberNavController()
    val authState by authViewModel.authState.collectAsState()

    val startDestination = remember { 
        if (authState is AuthState.Authenticated) "home_dashboard" else "splash_screen"
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("splash_screen") {
            SplashScreen(onNext = { 
                navController.navigate("landing_screen") {
                    popUpTo("splash_screen") { inclusive = true }
                }
            })
        }
        composable("landing_screen") {
            LandingScreen(onNavigateToAuth = { 
                navController.navigate("auth_screen") {
                    popUpTo("landing_screen") { inclusive = true }
                }
            })
        }
        composable("auth_screen") {
            AuthScreen(
                appViewModel = appViewModel,
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onLoginSuccess = { 
                    navController.navigate("home_dashboard") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onForgotPassword = { navController.navigate("forgot_password_screen") }
            )
        }
        composable("forgot_password_screen") {
            ForgotPasswordScreen(
                appViewModel = appViewModel,
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("home_dashboard") {
            HomeDashboard(
                appViewModel = appViewModel,
                profileViewModel = profileViewModel,
                weatherViewModel = weatherViewModel,
                batchViewModel = batchViewModel,
                onAddBatch = { navController.navigate("batch_screen") },
                onClimateEntry = { navController.navigate("climate_form_screen") },
                onSmartAdvice = { navController.navigate("advice_screen") },
                onHarvestTimer = { navController.navigate("timer_screen") },
                onBatchHistory = { navController.navigate("history_screen") },
                onProfile = { 
                    profileViewModel.loadUserProfile()
                    navController.navigate("profile_screen") 
                }
            )
        }
        
        composable("profile_screen") {
            ProfileScreen(
                viewModel = profileViewModel,
                appViewModel = appViewModel,
                onBack = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate("home_dashboard") {
                        popUpTo("home_dashboard") { inclusive = true }
                    }
                },
                onNavigateToBatches = { navController.navigate("history_screen") },
                onNavigateToClimate = { navController.navigate("climate_form_screen") },
                onLogoutSuccess = {
                    authViewModel.logout()
                    navController.navigate("landing_screen") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onSettingClick = { route ->
                    navController.navigate(route)
                }
            )
        }

        composable("personal_info") { PersonalInfoScreen(viewModel = profileViewModel, appViewModel = appViewModel, onBack = { navController.popBackStack() }) }
        composable("change_password") { ChangePasswordScreen(appViewModel = appViewModel, onBack = { navController.popBackStack() }) }
        composable("language_settings") { LanguageSettingsScreen(viewModel = profileViewModel, appViewModel = appViewModel, onBack = { navController.popBackStack() }) }
        composable("notification_settings") { NotificationSettingsScreen(viewModel = profileViewModel, appViewModel = appViewModel, onBack = { navController.popBackStack() }) }
        composable("preferences") { PreferencesScreen(viewModel = profileViewModel, appViewModel = appViewModel, onBack = { navController.popBackStack() }) }
        composable("data_sync") { DataSyncScreen(appViewModel = appViewModel, onBack = { navController.popBackStack() }) }
        composable("backup") { BackupRestoreScreen(appViewModel = appViewModel, onBack = { navController.popBackStack() }) }
        composable("export_data") { ExportDataScreen(appViewModel = appViewModel, onBack = { navController.popBackStack() }) }
        composable("help") { HelpSupportScreen(appViewModel = appViewModel, onBack = { navController.popBackStack() }) }
        composable("about") { 
            AboutAppScreen(
                appViewModel = appViewModel, 
                onBack = { navController.popBackStack() },
                onNavigateToTerms = { navController.navigate("terms_conditions") },
                onNavigateToPrivacy = { navController.navigate("privacy_policy") }
            ) 
        }
        composable("terms_conditions") { TermsAndConditionsScreen(appViewModel = appViewModel, onBack = { navController.popBackStack() }) }
        composable("privacy_policy") { PrivacyPolicyScreen(appViewModel = appViewModel, onBack = { navController.popBackStack() }) }
        composable("farm_details") { FarmDetailsScreen(appViewModel = appViewModel, onBack = { navController.popBackStack() }) }
        
        composable("climate_trends") {
            ClimateTrendsScreen(
                appViewModel = appViewModel,
                batchViewModel = batchViewModel,
                onBack = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate("home_dashboard") {
                        popUpTo("home_dashboard") { inclusive = true }
                    }
                },
                onNavigateToBatches = { navController.navigate("history_screen") },
                onNavigateToClimate = { navController.navigate("climate_form_screen") },
                onNavigateToProfile = { navController.navigate("profile_screen") }
            )
        }
        
        composable("advice_screen") {
            SmartAdviceScreen(
                appViewModel = appViewModel,
                weatherViewModel = weatherViewModel,
                batchViewModel = batchViewModel,
                onBack = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate("home_dashboard") {
                        popUpTo("home_dashboard") { inclusive = true }
                    }
                },
                onNavigateToBatches = { navController.navigate("history_screen") },
                onNavigateToHarvestTimer = { navController.navigate("timer_screen") },
                onNavigateToReports = { navController.navigate("climate_trends") },
                onNavigateToProfile = { navController.navigate("profile_screen") },
                onNavigateToClimateEntry = { navController.navigate("climate_form_screen") },
                onRecommendationClick = { type ->
                    navController.navigate("detailed_advice/$type")
                }
            )
        }

        composable("detailed_advice/{type}") { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: ""
            DetailedAdviceScreen(
                appViewModel = appViewModel,
                type = type,
                onBack = { navController.popBackStack() }
            )
        }
        
        composable("batch_screen") { 
            AddBatchScreen(
                appViewModel = appViewModel,
                batchViewModel = batchViewModel,
                onBack = { navController.popBackStack() },
                onSaveSuccess = { 
                    navController.navigate("history_screen") {
                        popUpTo("home_dashboard") { inclusive = false }
                    }
                },
                onNavigateToHome = {
                    navController.navigate("home_dashboard") {
                        popUpTo("home_dashboard") { inclusive = true }
                    }
                },
                onNavigateToBatches = { navController.navigate("history_screen") },
                onNavigateToHarvestTimer = { navController.navigate("timer_screen") },
                onNavigateToReports = { navController.navigate("climate_trends") },
                onNavigateToProfile = { navController.navigate("profile_screen") }
            ) 
        }
        
        composable("climate_form_screen") { 
            ClimateEntryScreen(
                appViewModel = appViewModel,
                weatherViewModel = weatherViewModel,
                batchViewModel = batchViewModel,
                onBack = { navController.popBackStack() },
                onSaveSuccess = { 
                    navController.navigate("advice_screen") {
                        popUpTo("home_dashboard") { inclusive = false }
                    }
                },
                onNavigateToHome = {
                    navController.navigate("home_dashboard") {
                        popUpTo("home_dashboard") { inclusive = true }
                    }
                },
                onNavigateToBatches = { navController.navigate("history_screen") },
                onNavigateToReports = { navController.navigate("climate_trends") },
                onNavigateToProfile = { navController.navigate("profile_screen") }
            ) 
        }

        composable("timer_screen") {
            HarvestTimerScreen(
                appViewModel = appViewModel,
                batchViewModel = batchViewModel,
                onBack = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate("home_dashboard") {
                        popUpTo("home_dashboard") { inclusive = true }
                    }
                },
                onNavigateToBatches = { navController.navigate("history_screen") },
                onNavigateToReports = { navController.navigate("climate_trends") },
                onNavigateToProfile = { navController.navigate("profile_screen") },
                onNavigateToDetails = { batchId -> navController.navigate("batch_details/$batchId") }
            )
        }

        composable("history_screen") {
            BatchHistoryScreen(
                appViewModel = appViewModel,
                batchViewModel = batchViewModel,
                onBack = { navController.popBackStack() },
                onNavigateToDetails = { batchId -> navController.navigate("batch_details/$batchId") },
                onNavigateToHome = {
                    navController.navigate("home_dashboard") {
                        popUpTo("home_dashboard") { inclusive = true }
                    }
                },
                onNavigateToClimate = { navController.navigate("climate_form_screen") },
                onProfile = { navController.navigate("profile_screen") }
            )
        }

        composable("batch_details/{batchId}") { backStackEntry ->
            val batchId = backStackEntry.arguments?.getString("batchId") ?: ""
            BatchDetailsScreen(
                appViewModel = appViewModel,
                batchViewModel = batchViewModel,
                batchId = batchId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1B4332),
    secondary = Color(0xFF2D6A4F),
    tertiary = Color(0xFF40916C),
    background = Color(0xFFF9F7F2),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1B4332),
    onSurface = Color(0xFF1B4332)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF52B788),
    secondary = Color(0xFF74C69D),
    tertiary = Color(0xFF95D5B2),
    background = Color(0xFF081C15),
    surface = Color(0xFF1B4332),
    onPrimary = Color(0xFF081C15),
    onSecondary = Color(0xFF081C15),
    onTertiary = Color(0xFF081C15),
    onBackground = Color(0xFFD8F3DC),
    onSurface = Color(0xFFD8F3DC)
)

@Composable
fun NammaReshmeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
