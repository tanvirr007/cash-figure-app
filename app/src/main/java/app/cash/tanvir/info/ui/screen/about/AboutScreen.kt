package app.cash.tanvir.info.ui.screen.about

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cash.tanvir.info.R
import app.cash.tanvir.info.ui.animation.pressScale
import app.cash.tanvir.info.ui.components.CompactBackTopBar
import app.cash.tanvir.info.ui.components.VerticalScrollbarIndicator
import app.cash.tanvir.info.util.HapticHelper
import app.cash.tanvir.info.util.getInstalledVersion
import java.util.Calendar
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Full-page About screen: author profile, app purpose, and contact links.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val (installedName, installedCode) = remember(context) { getInstalledVersion(context) }

    // Android-style hidden easter egg: tap the Version row 7 times quickly
    // to launch the Money Master game directly, just like Android's own
    // easter eggs. The back button is the only way out.
    var versionTapCount by remember { mutableIntStateOf(0) }
    var lastVersionTap by remember { mutableLongStateOf(0L) }
    var showMoneyEgg by remember { mutableStateOf(false) }

    fun openUrl(url: String) {
        HapticHelper.vibrate(context)
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (_: Exception) {
            Toast.makeText(
                context,
                "Couldn't open the link",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun openEmail() {
        HapticHelper.vibrate(context)
        try {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:tanvirhasan2005@proton.me"))
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(
                context,
                "No email app found",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Scaffold(
        topBar = {
            CompactBackTopBar(
                title = "Author",
                onBack = {
                    HapticHelper.vibrate(context)
                    onNavigateBack()
                }
            )
        }
    ) { padding ->
        val scrollState = rememberScrollState()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .clip(CircleShape)
                        .border(3.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.profile_avatar),
                        contentDescription = "Tanvir Hasan",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Tanvir Hasan",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "Due to Google's new policy, third-party apps are marked as a risk by default. This app is open source and completely safe.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        AboutLinkRow(
                            iconRes = R.drawable.ic_globe,
                            title = "Website",
                            subtitle = "tanvir.info",
                            onClick = { openUrl("https://tanvir.info") }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        AboutLinkRow(
                            iconRes = R.drawable.ic_github,
                            title = "GitHub",
                            subtitle = "tanvirr007",
                            onClick = { openUrl("https://github.com/tanvirr007") }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        AboutLinkRow(
                            iconRes = R.drawable.ic_telegram,
                            title = "Telegram",
                            subtitle = "tanvirr007",
                            onClick = { openUrl("https://t.me/tanvirr007") }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        AboutLinkRow(
                            iconRes = R.drawable.ic_email,
                            title = "Email",
                            subtitle = "tanvirhasan2005@proton.me",
                            onClick = { openEmail() }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        AboutLinkRow(
                            iconRes = R.drawable.ic_code,
                            title = "Source Code",
                            subtitle = "GitHub",
                            onClick = { openUrl("https://github.com/tanvirr007/cash-figure-app") }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        AboutLinkRow(
                            iconRes = R.drawable.ic_code,
                            iconVector = Icons.Rounded.NewReleases,
                            title = "Version",
                            subtitle = "v$installedName (Build $installedCode)",
                            onClick = {
                                HapticHelper.vibrate(context)
                                val now = System.currentTimeMillis()
                                if (now - lastVersionTap > 3000) {
                                    versionTapCount = 0
                                }
                                lastVersionTap = now
                                versionTapCount++
                                if (versionTapCount >= 7) {
                                    versionTapCount = 0
                                    showMoneyEgg = true
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "© ${Calendar.getInstance().get(Calendar.YEAR)} Tanvir Hasan · MIT License",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            VerticalScrollbarIndicator(
                state = scrollState,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }

    // Full-screen easter egg game — only the back button exits
    if (showMoneyEgg) {
        MoneyEggScreen(
            onDismiss = {
                HapticHelper.vibrate(context)
                showMoneyEgg = false
            }
        )
    }
}

/** Acceleration (m/s²) that triggers the emoji scatter burst. */
private const val EASTER_EGG_SHAKE_THRESHOLD = 14f

/**
 * Full-screen Android-style easter egg game: money emojis you can drag
 * anywhere, with a gentle idle sway. Tilt the device to make them drift,
 * shake it for a scatter burst. Exit only via the back button or gesture.
 */
@Composable
private fun MoneyEggScreen(onDismiss: () -> Unit) {
    BackHandler(onBack = onDismiss)
    val context = LocalContext.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val emojiPool = listOf("💵", "💸", "💰", "🏦", "🏧", "💳", "🪙", "🧾", "💎", "📈", "📉", "🧮", "📊")
    val sizePool = listOf(24f, 38f, 54f)

    var tiltX by remember { mutableStateOf(0f) }
    var tiltY by remember { mutableStateOf(0f) }
    var shakeOffsets by remember { mutableStateOf<Map<Int, Offset>>(emptyMap()) }
    val shakeAnim = remember { Animatable(0f) }

    // Entrance: whole game fades in and scales up once.
    val screenAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        screenAnim.animateTo(
            1f,
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
        )
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0F1218), Color(0xFF1C2431))
                )
            )
            .graphicsLayer {
                alpha = screenAnim.value
                scaleX = 0.92f + 0.08f * screenAnim.value
                scaleY = 0.92f + 0.08f * screenAnim.value
            }
    ) {
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }

        val items = remember(maxWidth, maxHeight) {
            val random = Random(System.currentTimeMillis())
            List(16) { index ->
                val size = sizePool[random.nextInt(sizePool.size)]
                val sizePx = with(density) { size.sp.toPx() }
                MoneyEggItem(
                    id = index,
                    emoji = emojiPool[random.nextInt(emojiPool.size)],
                    size = size,
                    initial = Offset(
                        x = random.nextFloat() * (widthPx - sizePx * 1.2f).coerceAtLeast(0f),
                        y = random.nextFloat() * (heightPx - sizePx * 1.4f).coerceAtLeast(0f)
                    ),
                    bobMillis = random.nextInt(2600, 4200),
                    swayMillis = random.nextInt(3800, 6400)
                )
            }
        }

        items.forEach { item ->
            var position by remember(item.initial) { mutableStateOf(item.initial) }
            val transition = rememberInfiniteTransition(label = "money-idle-${item.id}")
            val bobPhase by transition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = item.bobMillis, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "bob"
            )
            val swayPhase by transition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = item.swayMillis, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "sway"
            )
            // Staggered pop-in: each emoji springs to full size a bit after the last.
            val popAnim = remember { Animatable(0f) }
            LaunchedEffect(Unit) {
                delay(item.id * 40L)
                popAnim.animateTo(
                    1f,
                    spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)
                )
            }
            val sizePx = with(density) { item.size.sp.toPx() }
            val maxX = (widthPx - sizePx * 1.2f).coerceAtLeast(0f)
            val maxY = (heightPx - sizePx * 1.4f).coerceAtLeast(0f)
            Text(
                text = item.emoji,
                fontSize = item.size.sp,
                modifier = Modifier
                    .offset { IntOffset(position.x.roundToInt(), position.y.roundToInt()) }
                    .pointerInput(maxX, maxY) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            position = Offset(
                                x = (position.x + dragAmount.x).coerceIn(0f, maxX),
                                y = (position.y + dragAmount.y).coerceIn(0f, maxY)
                            )
                        }
                    }
                    .graphicsLayer {
                        val bobRad = bobPhase * PI.toFloat() / 180f
                        val swayRad = swayPhase * PI.toFloat() / 180f
                        val shake = shakeOffsets[item.id] ?: Offset.Zero
                        translationX = sin(bobRad) * 3f + tiltX * 36f + shake.x * shakeAnim.value
                        translationY = cos(bobRad) * 4f + tiltY * 36f + shake.y * shakeAnim.value
                        rotationZ = sin(swayRad) * 8f
                        val popScale = popAnim.value.coerceAtLeast(0.01f)
                        scaleX = popScale
                        scaleY = popScale
                    }
            )
        }

        Text(
            text = "Press back to exit",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
        )

        // Sensors: tilt drifts the emojis, a hard shake scatters them.
        // Missing sensors simply no-op — dragging and idle sway still work.
        DisposableEffect(Unit) {
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            val gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
            val linearAccel = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
            var smoothX = 0f
            var smoothY = 0f
            var shaking = false

            val gravityListener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val alpha = 0.18f
                    smoothX += alpha * (event.values[0] - smoothX)
                    smoothY += alpha * (event.values[1] - smoothY)
                    tiltX = smoothX / SensorManager.GRAVITY_EARTH
                    tiltY = smoothY / SensorManager.GRAVITY_EARTH
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }

            val shakeListener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]
                    val magnitude = sqrt(x * x + y * y + z * z)
                    if (magnitude > EASTER_EGG_SHAKE_THRESHOLD && !shaking) {
                        shaking = true
                        val random = Random(System.currentTimeMillis())
                        shakeOffsets = items.associate { item ->
                            item.id to Offset(
                                random.nextFloat() * 90f - 45f,
                                random.nextFloat() * 90f - 45f
                            )
                        }
                        scope.launch {
                            shakeAnim.snapTo(1f)
                            shakeAnim.animateTo(0f, tween(durationMillis = 550))
                            shaking = false
                        }
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }

            gravitySensor?.let {
                sensorManager.registerListener(gravityListener, it, SensorManager.SENSOR_DELAY_GAME)
            }
            linearAccel?.let {
                sensorManager.registerListener(shakeListener, it, SensorManager.SENSOR_DELAY_GAME)
            }

            onDispose {
                sensorManager.unregisterListener(gravityListener)
                sensorManager.unregisterListener(shakeListener)
            }
        }
    }
}

/**
 * One draggable emoji in the easter egg game: emoji, visual size and
 * randomized idle-motion timings.
 */
private data class MoneyEggItem(
    val id: Int,
    val emoji: String,
    val size: Float,
    val initial: Offset,
    val bobMillis: Int,
    val swayMillis: Int
)

@Composable
private fun AboutLinkRow(
    iconRes: Int,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    iconVector: ImageVector? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(interactionSource)
            .clip(RoundedCornerShape(14.dp))
            .clickable(interactionSource = interactionSource, indication = LocalIndication.current, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (iconVector != null) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                maxLines = 1
            )
        }
    }
}
