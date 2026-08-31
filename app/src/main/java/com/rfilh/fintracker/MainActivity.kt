package com.rfilh.fintracker

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rfilh.fintracker.data.db.AppLockPrefs
import com.rfilh.fintracker.ui.nav.FinTrackerNavHost
import com.rfilh.fintracker.ui.theme.AppToastHost
import com.rfilh.fintracker.ui.theme.FinTrackerTheme
import com.rfilh.fintracker.ui.theme.appColors

/** FragmentActivity (não ComponentActivity) porque BiometricPrompt exige. */
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as FinTrackerApp).container

        val lockRequired = AppLockPrefs.isEnabled(this) && canAuthenticate()

        setContent {
            var unlocked by remember { mutableStateOf(!lockRequired) }
            val darkMode by container.darkMode.collectAsStateWithLifecycle()

            FinTrackerTheme(darkMode = darkMode) {
                Surface(color = MaterialTheme.appColors.bg) {
                    if (unlocked) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            FinTrackerNavHost(container)
                            AppToastHost()
                        }
                    } else {
                        LockScreen(onUnlock = { authenticate { unlocked = true } })
                    }
                }
            }
        }
    }

    private fun canAuthenticate(): Boolean =
        BiometricManager.from(this).canAuthenticate(BIOMETRIC_WEAK or DEVICE_CREDENTIAL) ==
            BiometricManager.BIOMETRIC_SUCCESS

    private fun authenticate(onSuccess: () -> Unit) {
        val prompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }
            }
        )
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.app_name))
            .setSubtitle("Desbloqueie para continuar")
            .setAllowedAuthenticators(BIOMETRIC_WEAK or DEVICE_CREDENTIAL)
            .build()
        prompt.authenticate(info)
    }
}

@Composable
private fun LockScreen(onUnlock: () -> Unit) {
    LaunchedEffect(Unit) { onUnlock() }
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.appColors.bg).padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("FinTracker bloqueado", color = MaterialTheme.appColors.text)
        Button(onClick = onUnlock, modifier = Modifier.padding(top = 16.dp)) { Text("Desbloquear") }
    }
}
