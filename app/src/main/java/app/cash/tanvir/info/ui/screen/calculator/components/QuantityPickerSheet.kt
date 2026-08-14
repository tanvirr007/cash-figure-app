package app.cash.tanvir.info.ui.screen.calculator.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cash.tanvir.info.ui.animation.pressScale
import app.cash.tanvir.info.ui.components.AutoShrinkText
import app.cash.tanvir.info.ui.components.VerticalScrollbarIndicator
import app.cash.tanvir.info.ui.screen.calculator.CalculatorViewModel
import app.cash.tanvir.info.util.BanglaDigitConverter
import app.cash.tanvir.info.util.CurrencyFormatter
import app.cash.tanvir.info.util.HapticHelper
import app.cash.tanvir.info.util.NumberToWordsConverter
import kotlinx.coroutines.withTimeoutOrNull

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class
)
@Composable
fun QuantityPickerSheet(
    denominationLabel: String,
    denominationValue: Int,
    quantityText: String,
    isBangla: Boolean,
    onQuantityChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var showCustom by remember { mutableStateOf(false) }
    var customInput by remember { mutableStateOf("") }
    var pendingQty by remember { mutableStateOf(quantityText) }
    var presetActive by remember { mutableStateOf(false) }
    val pendingValue = pendingQty.toIntOrNull()
    val customFocusRequester = remember { FocusRequester() }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusManager = LocalFocusManager.current

    fun setPending(value: String) {
        pendingQty = value
        if (showCustom) customInput = value
    }

    fun commit() {
        val value = pendingValue ?: return
        onQuantityChange(value.coerceIn(1, CalculatorViewModel.MAX_QUANTITY).toString())
        focusManager.clearFocus()
        onDismiss()
    }

    LaunchedEffect(showCustom) {
        if (showCustom) {
            withFrameNanos { }
            customFocusRequester.requestFocus()
            bringIntoViewRequester.bringIntoView()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = {
            BottomSheetDefaults.windowInsets.exclude(WindowInsets.ime)
        }
    ) {
        val scrollState = rememberScrollState()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clipToBounds(),
            contentAlignment = Alignment.CenterEnd
        ) {
            CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .clipToBounds()
                        .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 16.dp)
                ) {
                    // Header: denomination + Clear
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = denominationLabel,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = {
                            HapticHelper.vibrate(context)
                            pendingQty = ""
                            customInput = ""
                            presetActive = false
                            focusManager.clearFocus()
                        }) {
                            Text(
                                text = if (isBangla) "মুছুন" else "Clear",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    // Stepper with hold-to-repeat
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        HoldToRepeatButton(
                            icon = Icons.Rounded.Remove,
                            contentDescription = if (isBangla) "এক কম করুন" else "Decrease by one",
                            enabled = (pendingValue ?: 0) > 1,
                            onPress = {
                                HapticHelper.vibrate(context)
                                presetActive = false
                                setPending(((pendingValue ?: 0) - 1).coerceAtLeast(1).toString())
                            },
                            onRepeat = { setPending(((pendingValue ?: 0) - 1).coerceAtLeast(1).toString()) }
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AutoShrinkText(
                                text = if (isBangla) BanglaDigitConverter.toBangla(pendingValue ?: 0) else (pendingValue ?: 0).toString(),
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontSize = 44.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (pendingValue != null) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                                },
                                minFontSize = 26.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = when {
                                    isBangla -> "টি নোট"
                                    pendingValue == 1 -> "piece"
                                    else -> "pieces"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        HoldToRepeatButton(
                            icon = Icons.Rounded.Add,
                            contentDescription = if (isBangla) "এক বেশি করুন" else "Increase by one",
                            enabled = (pendingValue ?: 0) < CalculatorViewModel.MAX_QUANTITY,
                            onPress = {
                                HapticHelper.vibrate(context)
                                presetActive = false
                                setPending(((pendingValue ?: 0) + 1).coerceAtMost(CalculatorViewModel.MAX_QUANTITY).toString())
                            },
                            onRepeat = { setPending(((pendingValue ?: 0) + 1).coerceAtMost(CalculatorViewModel.MAX_QUANTITY).toString()) }
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Preset grid (4 per row) + Custom
                    FlowRow(
                        maxItemsInEachRow = 4,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CalculatorViewModel.QUANTITY_PRESETS.forEach { preset ->
                            PresetChip(
                                label = if (isBangla) BanglaDigitConverter.toBangla(preset) else preset.toString(),
                                selected = !showCustom && pendingValue == preset,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    HapticHelper.vibrate(context)
                                    showCustom = false
                                    customInput = ""
                                    focusManager.clearFocus()
                                    if (pendingValue == preset) {
                                        presetActive = false
                                        setPending("")
                                    } else {
                                        presetActive = true
                                        setPending(preset.toString())
                                    }
                                }
                            )
                        }
                        PresetChip(
                            label = if (isBangla) "নিজের সংখ্যা" else "Custom",
                            selected = showCustom,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                HapticHelper.vibrate(context)
                                showCustom = !showCustom
                                presetActive = false
                                if (showCustom) {
                                    customInput = if (pendingValue != null) pendingValue.toString() else ""
                                }
                            }
                        )
                    }

                    // Inline custom input
                    if (showCustom) {
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = customInput,
                            onValueChange = { input ->
                                val western = BanglaDigitConverter.toWestern(input)
                                val filtered = western.filter { it.isDigit() }
                                if (filtered.length <= 5) {
                                    presetActive = false
                                    setPending(filtered)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(customFocusRequester)
                                .bringIntoViewRequester(bringIntoViewRequester),
                            label = { Text(if (isBangla) "নিজের সংখ্যা লিখুন" else "Type a number") },
                            placeholder = {
                                Text(
                                    text = if (isBangla) "যেমন: ৭৩" else "e.g. 73",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                }
                            ),
                            supportingText = {
                                Text(
                                    text = if (isBangla) "সর্বনিম্ন ১, সর্বোচ্চ ৯৯,৯৯৯" else "Min 1, max 99,999",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                    }

                    // Commit button
                    Spacer(modifier = Modifier.height(16.dp))
                    val okInteractionSource = remember { MutableInteractionSource() }
                    Button(
                        onClick = {
                            HapticHelper.vibrate(context)
                            commit()
                        },
                        interactionSource = okInteractionSource,
                        enabled = pendingValue != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .pressScale(okInteractionSource),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = if (isBangla) "ঠিক আছে" else "OK",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            VerticalScrollbarIndicator(
                state = scrollState,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}

@Composable
private fun HoldToRepeatButton(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onPress: () -> Unit,
    onRepeat: () -> Unit
) {
    val latestOnRepeat by rememberUpdatedState(onRepeat)
    var repeatActive by remember { mutableStateOf(false) }

    val background = if (enabled) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val tint = if (enabled) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
    }

    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(background)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            )
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var repeatDelayMillis = 400L
                    while (true) {
                        val event = withTimeoutOrNull(repeatDelayMillis) {
                            awaitPointerEvent(PointerEventPass.Main)
                        }
                        if (event == null) {
                            repeatActive = true
                            latestOnRepeat()
                            repeatDelayMillis = 60L
                        } else {
                            if (event.changes.any { it.changedToUp() || it.isConsumed }) {
                                break
                            }
                        }
                    }
                }
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = {
                    if (!repeatActive) {
                        onPress()
                    }
                    repeatActive = false
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(26.dp)
        )
    }
}

@Composable
private fun PresetChip(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val background = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
    }
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(background)
            .border(if (selected) 2.dp else 1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onClick
            )
            .semantics(mergeDescendants = true) {
                this.selected = selected
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
