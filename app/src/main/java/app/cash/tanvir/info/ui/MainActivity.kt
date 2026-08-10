package app.cash.tanvir.info.ui

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import app.cash.tanvir.info.data.local.preferences.AppLanguage
import app.cash.tanvir.info.data.local.preferences.AppTheme
import app.cash.tanvir.info.data.local.preferences.PreferencesManager
import app.cash.tanvir.info.ui.navigation.NavGraph
import app.cash.tanvir.info.ui.theme.CashFigureTheme
import app.cash.tanvir.info.util.HapticHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Single activity entry point for Cash Figure.
 * Inherits from FragmentActivity for biometric dialog integration.
 */
@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    private var isAppLocked by mutableStateOf(false)
    private var backgroundTimestamp: Long = 0L
    private var isFirstLaunch = true
    private var promptInProgress = false

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            isAppLocked = savedInstanceState.getBoolean("is_app_locked", false)
            isFirstLaunch = savedInstanceState.getBoolean("is_first_launch", true)
            backgroundTimestamp = savedInstanceState.getLong("background_timestamp", 0L)
        }

        // Keep screen on while the calculator is active
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            val appTheme by preferencesManager.themeFlow.collectAsState(initial = AppTheme.SYSTEM)
            val language by preferencesManager.languageFlow.collectAsState(initial = AppLanguage.ENGLISH)
            val screenshotBlockEnabled by preferencesManager.screenshotBlockEnabledFlow.collectAsState(initial = false)
            val hapticEnabled by preferencesManager.hapticFeedbackEnabledFlow.collectAsState(initial = false)
            val hapticIntensity by preferencesManager.hapticFeedbackIntensityFlow.collectAsState(initial = 0.5f)

            val isDark = when (appTheme) {
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
                AppTheme.SYSTEM -> isSystemInDarkTheme()
            }
            val useDynamic = appTheme == AppTheme.SYSTEM
            val isBangla = language == AppLanguage.BANGLA

            // Reactively apply/remove FLAG_SECURE
            LaunchedEffect(screenshotBlockEnabled) {
                if (screenshotBlockEnabled) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
            }

            // Reactively update HapticHelper fields
            LaunchedEffect(hapticEnabled, hapticIntensity) {
                HapticHelper.isEnabled = hapticEnabled
                HapticHelper.intensity = hapticIntensity
            }

            // Show biometric prompt when locked
            LaunchedEffect(isAppLocked) {
                if (isAppLocked) {
                    showBiometricPrompt(isBangla)
                }
            }

            DisposableEffect(isDark) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT,
                        detectDarkMode = { isDark }
                    ),
                    navigationBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT,
                        detectDarkMode = { isDark }
                    )
                )
                onDispose {}
            }

            CashFigureTheme(
                darkTheme = isDark,
                dynamicColor = useDynamic
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isAppLocked) {
                        LockScreen(
                            isBangla = isBangla,
                            onUnlockClick = { showBiometricPrompt(isBangla) }
                        )
                    } else {
                        NavGraph()
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("is_app_locked", isAppLocked)
        outState.putBoolean("is_first_launch", isFirstLaunch)
        outState.putLong("background_timestamp", backgroundTimestamp)
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            val enabled = preferencesManager.biometricEnabledFlow.first()
            if (enabled) {
                val elapsed = System.currentTimeMillis() - backgroundTimestamp
                if (isFirstLaunch || (backgroundTimestamp != 0L && elapsed > 40000)) {
                    isAppLocked = true
                }
            }
            isFirstLaunch = false
        }
    }

    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations) {
            backgroundTimestamp = System.currentTimeMillis()
        }
    }

    private fun showBiometricPrompt(isBangla: Boolean) {
        if (promptInProgress) return
        promptInProgress = true
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    promptInProgress = false
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    promptInProgress = false
                    isAppLocked = false
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(if (isBangla) "ক্যাশ ফিগার অ্যাপ আনলক করুন" else "Unlock Cash Figure App")
            .setSubtitle(if (isBangla) "আপনার ফিঙ্গারপ্রিন্ট স্ক্যান করুন" else "Scan your fingerprint")
            .setNegativeButtonText(if (isBangla) "বাতিল" else "Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}

@Composable
fun LockScreen(
    isBangla: Boolean,
    onUnlockClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = if (isBangla) "অ্যাপটি লক করা আছে" else "App Locked",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onUnlockClick,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.width(160.dp)
            ) {
                Text(
                    text = if (isBangla) "আনলক করুন" else "Unlock",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
