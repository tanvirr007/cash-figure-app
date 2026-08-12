package app.cash.tanvir.info.ui

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricManager
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import app.cash.tanvir.info.data.local.preferences.AppLanguage
import app.cash.tanvir.info.data.local.preferences.AppTheme
import app.cash.tanvir.info.data.local.preferences.PreferencesManager
import app.cash.tanvir.info.domain.model.UpdateManifest
import app.cash.tanvir.info.domain.repository.UpdateRepository
import app.cash.tanvir.info.ui.navigation.NavGraph
import app.cash.tanvir.info.ui.navigation.Screen
import app.cash.tanvir.info.ui.theme.CashFigureTheme
import app.cash.tanvir.info.util.BanglaDigitConverter
import app.cash.tanvir.info.util.HapticHelper
import app.cash.tanvir.info.util.getInstalledVersion
import app.cash.tanvir.info.util.isUpdateAvailable
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

    @Inject
    lateinit var updateRepository: UpdateRepository

    private var isAppLocked by mutableStateOf(false)
    private var backgroundTimestamp: Long = 0L
    private var isFirstLaunch = true
    private var promptInProgress = false
    private var updateCheckDone = false
    private var isUpdateAvailable by mutableStateOf<UpdateManifest?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            isAppLocked = savedInstanceState.getBoolean("is_app_locked", false)
            isFirstLaunch = savedInstanceState.getBoolean("is_first_launch", true)
            backgroundTimestamp = savedInstanceState.getLong("background_timestamp", 0L)
        }

        setContent {
            val appTheme by preferencesManager.themeFlow.collectAsState(initial = AppTheme.SYSTEM)
            val language by preferencesManager.languageFlow.collectAsState(initial = AppLanguage.ENGLISH)
            val screenshotBlockEnabled by preferencesManager.screenshotBlockEnabledFlow.collectAsState(initial = false)
            val hapticEnabled by preferencesManager.hapticFeedbackEnabledFlow.collectAsState(initial = false)
            val hapticIntensity by preferencesManager.hapticFeedbackIntensityFlow.collectAsState(initial = 0.5f)
            val keepScreenOnEnabled by preferencesManager.keepScreenOnEnabledFlow.collectAsState(initial = true)
            val dynamicColorEnabled by preferencesManager.dynamicColorEnabledFlow.collectAsState(initial = true)

            // Reactively apply/remove FLAG_KEEP_SCREEN_ON
            LaunchedEffect(keepScreenOnEnabled) {
                if (keepScreenOnEnabled) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }

            val isDark = when (appTheme) {
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
                AppTheme.SYSTEM -> isSystemInDarkTheme()
            }
            val useDynamic = dynamicColorEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
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

            // One-shot silent OTA check (runs once per process, never while locked)
            val navController = rememberNavController()
            LaunchedEffect(isAppLocked) {
                if (!isAppLocked && !updateCheckDone) {
                    updateCheckDone = true
                    val (installedName, installedCode) = getInstalledVersion(this@MainActivity)
                    val manifest = updateRepository.fetchManifest()
                    if (manifest != null && isUpdateAvailable(
                            manifest.versionName, manifest.versionCode,
                            installedName, installedCode
                        )
                    ) {
                        isUpdateAvailable = manifest
                    }
                }
            }

            // "Update complete" toast when the app was just updated via OTA
            LaunchedEffect(isAppLocked) {
                if (!isAppLocked) {
                    val installedCode = getInstalledVersion(this@MainActivity).second
                    val lastKnown = preferencesManager.lastKnownVersionFlow.first()
                    if (lastKnown != null && installedCode > lastKnown) {
                        Toast.makeText(
                            this@MainActivity,
                            if (isBangla) "আপডেট সম্পন্ন হয়েছে" else "Update complete",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    preferencesManager.setLastKnownVersion(installedCode)
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
                dynamicColor = useDynamic,
                isBangla = isBangla
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
                        NavGraph(navController = navController)
                    }

                    // Lightweight launch update dialog — hands off to the Update screen for the full flow
                    if (!isAppLocked && isUpdateAvailable != null) {
                        val availableManifest = isUpdateAvailable!!
                        Dialog(
                            onDismissRequest = {},
                            properties = DialogProperties(
                                dismissOnBackPress = false,
                                dismissOnClickOutside = false
                            )
                        ) {
                            Surface(
                                shape = RoundedCornerShape(28.dp),
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 6.dp,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Column(modifier = Modifier.padding(24.dp)) {
                                    Text(
                                        if (isBangla) "ওটিএ" else "OTA",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        if (isBangla) {
                                            "নতুন সংস্করণ ${BanglaDigitConverter.toBangla(availableManifest.versionName)} পাওয়া গেছে"
                                        } else {
                                            "New version ${availableManifest.versionName} is available"
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(20.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        TextButton(
                                            onClick = {
                                                HapticHelper.vibrate(this@MainActivity)
                                                isUpdateAvailable = null
                                            }
                                        ) {
                                            Text(if (isBangla) "পরে" else "Later")
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = {
                                                HapticHelper.vibrate(this@MainActivity)
                                                isUpdateAvailable = null
                                                navController.navigate(Screen.Update.route)
                                            },
                                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                                        ) {
                                            Text(if (isBangla) "আপডেট" else "Update")
                                        }
                                    }
                                }
                            }
                        }
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
            } else {
                isAppLocked = false
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
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )) {
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED,
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                val msg = if (isBangla)
                    "এই ডিভাইসে ফিঙ্গারপ্রিন্ট বা স্ক্রিন লক সেটআপ করা নেই।"
                else
                    "No fingerprint or screen lock set up on this device."
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                isAppLocked = false
                return
            }
            BiometricManager.BIOMETRIC_SUCCESS -> Unit
            else -> {
                val msg = if (isBangla)
                    "বায়োমেট্রিক প্রমাণীকরণ এই মুহূর্তে ব্যবহার করা যাচ্ছে না।"
                else
                    "Biometric authentication is currently unavailable."
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                return
            }
        }

        if (promptInProgress) return
        promptInProgress = true
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    promptInProgress = false
                    when (errorCode) {
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_CANCELED -> Unit
                        BiometricPrompt.ERROR_LOCKOUT,
                        BiometricPrompt.ERROR_LOCKOUT_PERMANENT,
                        BiometricPrompt.ERROR_TIMEOUT,
                        BiometricPrompt.ERROR_HW_UNAVAILABLE ->
                            Toast.makeText(this@MainActivity, errString, Toast.LENGTH_LONG).show()
                    }
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
            .setSubtitle(if (isBangla) "আপনার পরিচয় যাচাই করুন" else "Verify it’s you")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
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
