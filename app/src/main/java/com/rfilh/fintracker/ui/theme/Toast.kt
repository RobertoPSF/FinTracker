package com.rfilh.fintracker.ui.theme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Fila de 1 mensagem, some sozinha — equivalente ao `showToast()` do protótipo. */
object ToastBus {
    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    fun show(text: String) { _message.value = text }
    fun clear() { _message.value = null }
}

/** Monta uma vez na raiz da navegação; renderiza a pill flutuante sobre a tela atual. */
@Composable
fun AppToastHost(modifier: Modifier = Modifier) {
    val message by ToastBus.message.collectAsStateWithLifecycle()

    LaunchedEffect(message) {
        if (message != null) {
            delay(2200)
            ToastBus.clear()
        }
    }

    Box(modifier = modifier.fillMaxSize().padding(bottom = 44.dp, start = 16.dp, end = 16.dp)) {
        AnimatedVisibility(
            visible = message != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .background(ToastBg, RoundedCornerShape(14.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(message.orEmpty(), color = ToastText, style = MaterialTheme.typography.bodyMedium.copy(color = ToastText))
            }
        }
    }
}
