package com.vanexa.supercalculator.presentation.converter

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.res.stringResource
import com.vanexa.supercalculator.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import com.vanexa.supercalculator.domain.model.*
// Phase 4: Import shared responsive utilities
import com.vanexa.supercalculator.presentation.common.ScreenSizeClass
import com.vanexa.supercalculator.presentation.common.rememberScreenSizeClass
import com.vanexa.supercalculator.presentation.common.ConverterResponsiveDimensions
import com.vanexa.supercalculator.presentation.common.rememberConverterDimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterScreen(
    viewModel: UnitConverterViewModel = koinViewModel()
) {
    // Phase 4: Screen size detection using shared ResponsiveUtils
    val sizeClass = rememberScreenSizeClass()
    val dimensions = rememberConverterDimensions(sizeClass)
    val isVeryCompact = sizeClass == ScreenSizeClass.VERY_COMPACT
    
    // Removed unused context and activity variables

    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val inputValue by viewModel.inputValue.collectAsState()
    val resultValue by viewModel.resultValue.collectAsState()
    val fromUnit by viewModel.fromUnit.collectAsState()
    val toUnit by viewModel.toUnit.collectAsState()
    val voiceEnabled by viewModel.voiceEnabled.collectAsState()
    
    // ✅ Shared cursor position state for Value field and Keypad
    var cursorPos by remember { mutableStateOf(inputValue.length) }
    LaunchedEffect(inputValue) {
        // Update cursor position when inputValue changes from keypad
        if (cursorPos > inputValue.length) {
            cursorPos = inputValue.length
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            // Phase 4: Responsive padding based on screen size
            .padding(horizontal = dimensions.horizontalPadding, vertical = dimensions.verticalPadding)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Phase 4: Top Bar with Voice toggle - hide title in VERY_COMPACT to save space
        if (!isVeryCompact) {
            TopAppBar(
                title = { Text(stringResource(R.string.unit_converter), fontSize = dimensions.inputFontSize) },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleVoice() },
                        modifier = Modifier.size(dimensions.swapButtonSize)
                    ) {
                        Icon(
                            imageVector = if (voiceEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = stringResource(R.string.toggle_voice),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(dimensions.iconSize)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                windowInsets = WindowInsets(0)
            )
        } else {
            // In VERY_COMPACT: Just voice toggle, no title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensions.horizontalPadding),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = { viewModel.toggleVoice() },
                    modifier = Modifier.size(dimensions.swapButtonSize)
                ) {
                    Icon(
                        imageVector = if (voiceEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = stringResource(R.string.toggle_voice),
                        tint = if (voiceEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.size(dimensions.iconSize)
                    )
                }
            }
        }
        
        // Phase 4: Category Tabs with CORRECT Centering (BoxWithConstraints)
        val categoryScrollState = rememberScrollState()
        val coroutineScope = rememberCoroutineScope()
        
        // ✅ Use snapshotStateMapOf for stable state (no recomposition loops)
        val tabPositions = remember { androidx.compose.runtime.snapshots.SnapshotStateMap<Int, Pair<Int, Int>>() }
        
        // ✅ Use BoxWithConstraints to get VIEWPORT width (visible area)
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensions.horizontalPadding)
        ) {
            val viewportWidth = constraints.maxWidth
            
            // ✅ Auto-scroll when selectedCategory changes AND positions are ready
            LaunchedEffect(selectedCategory, tabPositions.size) {
                // Wait for all tabs to be measured
                if (tabPositions.size == UnitCategory.values().size) {
                    val categoryIndex = UnitCategory.values().indexOf(selectedCategory)
                    val layout = tabPositions[categoryIndex]
                    if (layout != null && viewportWidth > 0) {
                        val (tabX, tabWidth) = layout
                        val tabCenter = tabX + tabWidth / 2
                        val viewportCenter = viewportWidth / 2
                        val targetScroll = (tabCenter - viewportCenter).coerceAtLeast(0)
                        categoryScrollState.animateScrollTo(targetScroll)
                    }
                }
            }
            
            // ✅ Also scroll when all positions are first populated (initial load)
            LaunchedEffect(tabPositions.size) {
                if (tabPositions.size == UnitCategory.values().size) {
                    val categoryIndex = UnitCategory.values().indexOf(selectedCategory)
                    val layout = tabPositions[categoryIndex]
                    if (layout != null && viewportWidth > 0) {
                        val (tabX, tabWidth) = layout
                        val tabCenter = tabX + tabWidth / 2
                        val viewportCenter = viewportWidth / 2
                        val targetScroll = (tabCenter - viewportCenter).coerceAtLeast(0)
                        categoryScrollState.animateScrollTo(targetScroll)
                    }
                }
            }
            
            Row(
                modifier = Modifier
                    .horizontalScroll(categoryScrollState),
                horizontalArrangement = Arrangement.spacedBy(dimensions.itemSpacing)
            ) {
                UnitCategory.values().forEachIndexed { index, category ->
                    val isSelected = selectedCategory == category
                    Box(
                        modifier = Modifier
                            .onGloballyPositioned { layoutCoordinates ->
                                // ✅ Only update if position actually changed
                                val xPos = layoutCoordinates.positionInParent().x.toInt()
                                val width = layoutCoordinates.size.width
                                val current = tabPositions[index]
                                if (current?.first != xPos || current?.second != width) {
                                    tabPositions[index] = xPos to width
                                }
                            }
                            .clickable { 
                                viewModel.setCategory(category)
                                // ✅ Immediate scroll when tab is clicked
                                val layout = tabPositions[index]
                                if (layout != null && viewportWidth > 0) {
                                    val (tabX, tabWidth) = layout
                                    val tabCenter = tabX + tabWidth / 2
                                    val viewportCenter = viewportWidth / 2
                                    val targetScroll = (tabCenter - viewportCenter).coerceAtLeast(0)
                                    coroutineScope.launch {
                                        categoryScrollState.animateScrollTo(targetScroll)
                                    }
                                }
                            }
                            .background(
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                },
                                shape = RoundedCornerShape(dimensions.cardCornerRadius)
                            )
                            .padding(horizontal = dimensions.cardPadding, vertical = dimensions.itemSpacing),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.labelMedium,
                            fontSize = dimensions.labelFontSize,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(dimensions.itemSpacing))
        
        // Phase 4: SINGLE UNIFIED CARD with responsive dimensions
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensions.horizontalPadding),
            shape = RoundedCornerShape(dimensions.cardCornerRadius),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.03f),
                                Color.Transparent
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.2f),
                                Color.White.copy(alpha = 0.05f)
                            )
                        ),
                        shape = RoundedCornerShape(dimensions.cardCornerRadius)
                    )
                    .padding(dimensions.cardPadding),
                verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing)
            ) {
                // FROM Section - Single Row
                Column {
                    Text(
                        stringResource(R.string.from),
                        style = MaterialTheme.typography.labelMedium,
                        fontSize = dimensions.labelFontSize,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(dimensions.itemSpacing))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(dimensions.itemSpacing),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Unit Dropdown
                        UnitDropdown(
                            category = selectedCategory,
                            selectedUnit = fromUnit,
                            onUnitSelected = { viewModel.setFromUnit(it) },
                            modifier = Modifier.weight(0.35f),
                            dimensions = dimensions
                        )
                        
                        // Input Field - Editable with cursor, copy, paste, cut support
                        key(inputValue) {
                            ConverterValueField(
                                value = inputValue,
                                onValueChange = { viewModel.setInputValue(it) },
                                cursorPosition = cursorPos,
                                onCursorPositionChange = { cursorPos = it },
                                modifier = Modifier.weight(0.65f),
                                dimensions = dimensions
                            )
                        }
                    }
                }
        
                // Phase 4: Swap Button with responsive sizing
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = {
                            val temp = fromUnit
                            val to = toUnit
                            if (temp != null && to != null) {
                                viewModel.setFromUnit(to)
                                viewModel.setToUnit(temp)
                            }
                        },
                        modifier = Modifier
                            .size(dimensions.swapButtonSize)
                            .shadow(8.dp, CircleShape, spotColor = MaterialTheme.colorScheme.primary)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapVert,
                            contentDescription = stringResource(R.string.swap_currencies),
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(dimensions.iconSize)
                        )
                    }
                }
        
                // TO Section - Single Row
                Column {
                    Text(
                        stringResource(R.string.to),
                        style = MaterialTheme.typography.labelMedium,
                        fontSize = dimensions.labelFontSize,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(dimensions.itemSpacing))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(dimensions.itemSpacing),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Unit Dropdown
                        UnitDropdown(
                            category = selectedCategory,
                            selectedUnit = toUnit,
                            onUnitSelected = { viewModel.setToUnit(it) },
                            modifier = Modifier.weight(0.35f),
                            dimensions = dimensions
                        )
                        
                        // Result Display - Read-only with copy support
                        key(resultValue) {
                            Box(
                                modifier = Modifier.weight(0.65f)
                            ) {
                                ConverterResultField(
                                    value = resultValue,
                                    dimensions = dimensions
                                )
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(dimensions.itemSpacing))
        
        // Phase 4: Keypad with responsive dimensions
        ConverterKeypad(
            category = selectedCategory,
            onKeyClick = { key: String ->
                // ✅ CRITICAL: Get cursor position from cursorPos state (synced with ConverterValueField)
                viewModel.onKeypadClick(key, cursorPos)
            },
            modifier = Modifier.padding(horizontal = dimensions.horizontalPadding),
            dimensions = dimensions
        )
        
        Spacer(modifier = Modifier.height(dimensions.itemSpacing))
    }
}

@Composable
fun ConverterKeypad(
    category: UnitCategory,
    onKeyClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    dimensions: ConverterResponsiveDimensions
) {
    // ✅ Dynamic keypad layout based on category
    // Temperature needs minus (-) button, others don't
    // Fixed: All rows now have 3 buttons for balanced layout
    val buttons = if (category == UnitCategory.TEMPERATURE) {
        // Temperature: Include minus button and ± toggle for negative values
        listOf(
            listOf("C", "⌫", "±"),       // 3 tombol - ± untuk toggle positive/negative
            listOf("7", "8", "9"),
            listOf("4", "5", "6"),
            listOf("1", "2", "3"),
            listOf("-", "0", ".")        // 3 tombol
        )
    } else {
        // All other categories: Standard numeric keypad with balanced rows
        listOf(
            listOf("C", "⌫", "00"),      // 3 tombol - 00 untuk double zero
            listOf("7", "8", "9"),
            listOf("4", "5", "6"),
            listOf("1", "2", "3"),
            listOf(".", "0", "000")      // 3 tombol - 000 untuk triple zero
        )
    }
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimensions.buttonSpacing)
    ) {
        buttons.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimensions.buttonSpacing)
            ) {
                row.forEach { button ->
                    ConverterKeypadButton(
                        text = button,
                        onClick = { onKeyClick(button) },
                        modifier = Modifier.weight(1f),
                        dimensions = dimensions
                    )
                }
            }
        }
    }
}

@Composable
fun ConverterKeypadButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    dimensions: ConverterResponsiveDimensions
) {
    val isClear = text == "C"
    val isBackspace = text == "⌫"
    val isMinus = text == "-"
    
    Button(
        onClick = onClick,
        modifier = modifier.height(dimensions.keypadButtonHeight),
        colors = ButtonDefaults.buttonColors(
            containerColor = when {
                isClear -> MaterialTheme.colorScheme.error
                isBackspace -> MaterialTheme.colorScheme.surfaceVariant
                isMinus -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = when {
                isClear -> Color.White
                isMinus -> MaterialTheme.colorScheme.onSecondary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        ),
        shape = RoundedCornerShape(dimensions.cardCornerRadius),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isMinus) 0.dp else 2.dp,
            pressedElevation = if (isMinus) 0.dp else 1.dp
        )
    ) {
        Text(
            text = text,
            fontSize = dimensions.keypadFontSize,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Clip,
            softWrap = false
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitDropdown(
    category: UnitCategory,
    selectedUnit: Any?,
    onUnitSelected: (Any) -> Unit,
    modifier: Modifier = Modifier,
    dimensions: ConverterResponsiveDimensions
) {
    var expanded by remember { mutableStateOf(false) }
    
    val units = when (category) {
        UnitCategory.WEIGHT -> WeightUnit.values().toList()
        UnitCategory.LENGTH -> LengthUnit.values().toList()
        UnitCategory.TEMPERATURE -> TemperatureUnit.values().toList()
        UnitCategory.VOLUME -> VolumeUnit.values().toList()
        UnitCategory.AREA -> AreaUnit.values().toList()
        UnitCategory.SPEED -> SpeedUnit.values().toList()
        UnitCategory.TIME -> TimeUnit.values().toList()
        UnitCategory.DATA -> DataUnit.values().toList()
    }
    
    val selectedUnitText = when (selectedUnit) {
        is WeightUnit -> selectedUnit.symbol
        is LengthUnit -> selectedUnit.symbol
        is TemperatureUnit -> selectedUnit.symbol
        is VolumeUnit -> selectedUnit.symbol
        is AreaUnit -> selectedUnit.symbol
        is SpeedUnit -> selectedUnit.symbol
        is TimeUnit -> selectedUnit.symbol
        is DataUnit -> selectedUnit.symbol
        else -> stringResource(R.string.select_unit)
    }
    
    // Phase 4: Responsive dropdown height based on screen size
    val dropdownMaxHeight = when {
        dimensions.keypadButtonHeight < 36.dp -> 150.dp  // VERY_COMPACT
        dimensions.keypadButtonHeight < 40.dp -> 180.dp  // COMPACT
        else -> 200.dp  // MEDIUM/EXPANDED
    }
        
    Box(modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            // Phase 4: Responsive OutlinedTextField
            OutlinedTextField(
                value = selectedUnitText,
                onValueChange = {},
                readOnly = true,
                enabled = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensions.dropdownHeight)
                    .menuAnchor(),
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = expanded
                    )
                },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(dimensions.cardCornerRadius),
                textStyle = TextStyle(
                    fontSize = dimensions.inputFontSize,
                    fontWeight = FontWeight.Bold
                ),
                singleLine = true
            )
            
            // Phase 4: Responsive dropdown menu
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = dropdownMaxHeight)
            ) {
                units.forEach { unit ->
                    val unitText = when (unit) {
                        is WeightUnit -> unit.symbol
                        is LengthUnit -> unit.symbol
                        is TemperatureUnit -> unit.symbol
                        is VolumeUnit -> unit.symbol
                        is AreaUnit -> unit.symbol
                        is SpeedUnit -> unit.symbol
                        is TimeUnit -> unit.symbol
                        is DataUnit -> unit.symbol
                        else -> ""
                    }
                    
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = unitText,
                                fontSize = dimensions.inputFontSize,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        onClick = {
                            onUnitSelected(unit)
                            expanded = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Suppress("UNUSED_PARAMETER")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterValueField(
    value: String,
    onValueChange: (String) -> Unit,
    cursorPosition: Int,
    onCursorPositionChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    dimensions: ConverterResponsiveDimensions
) {
    // ✅ Clone exact method from Currency "From" field - copy, paste, cut support
    val keyboardController = LocalSoftwareKeyboardController.current
    val clipboardManager = LocalClipboardManager.current
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    var textFieldValue by remember { mutableStateOf(TextFieldValue(value, TextRange(value.length))) }
    var currentCursorPosition by remember { mutableStateOf(value.length) }
    
    // ✅ Sync TextFieldValue when value changes from ViewModel (keypad clicks) - same logic as Currency
    LaunchedEffect(value) {
        if (textFieldValue.text != value) {
            val oldText = textFieldValue.text
            val oldCursorPos = textFieldValue.selection.start
            val insertedLength = value.length - oldText.length
            
            // New cursor position: old position + inserted length (move cursor after inserted text)
            val newCursorPos = (oldCursorPos + insertedLength).coerceAtLeast(0).coerceAtMost(value.length)
            currentCursorPosition = newCursorPos
            onCursorPositionChange(newCursorPos)
            
            // Maintain selection if still valid, otherwise use new cursor position
            val newSelection = if (textFieldValue.selection.end <= value.length) {
                val selectionLength = textFieldValue.selection.end - textFieldValue.selection.start
                if (selectionLength > 0) {
                    TextRange(newCursorPos, (newCursorPos + selectionLength).coerceAtMost(value.length))
                } else {
                    TextRange(newCursorPos)
                }
            } else {
                TextRange(newCursorPos)
            }
            
            textFieldValue = TextFieldValue(text = value, selection = newSelection)
            keyboardController?.hide()
        }
    }
    
    // ✅ Update cursor position when selection changes
    LaunchedEffect(textFieldValue.selection.start) {
        currentCursorPosition = textFieldValue.selection.start.coerceAtLeast(0).coerceAtMost(textFieldValue.text.length)
        onCursorPositionChange(currentCursorPosition)
    }
    
    // ✅ STATE-BASED IME POLICY: UnitConverterScreen uses IN_APP_KEYPAD_ONLY
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    // Focus-based IME hiding - matches Calculator tab pattern
    LaunchedEffect(isFocused) {
        if (isFocused) {
            // Hide native keyboard immediately when field gains focus
            activity?.let { act ->
                val windowInsetsController = WindowCompat.getInsetsController(act.window, act.window.decorView)
                windowInsetsController.hide(androidx.core.view.WindowInsetsCompat.Type.ime())
            }
        }
    }
    
    CompositionLocalProvider(
        LocalTextSelectionColors provides TextSelectionColors(
            handleColor = MaterialTheme.colorScheme.primary,
            backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )
    ) {
        SelectionContainer {
            BasicTextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    keyboardController?.hide()
                    
                    if (newValue.text == textFieldValue.text) {
                        // Only selection changed
                        textFieldValue = newValue
                        currentCursorPosition = newValue.selection.start.coerceAtLeast(0).coerceAtMost(newValue.text.length)
                        onCursorPositionChange(currentCursorPosition)
                    } else {
                        // ✅ Text changed - check if paste (same logic as Currency)
                        val oldText = textFieldValue.text
                        val oldCursorPos = textFieldValue.selection.start
                        val newText = newValue.text
                        
                        val textDiff = newText.length - oldText.length
                        if (textDiff > 0) {
                            // Likely paste - extract and process
                            val pastedText = try {
                                val clipboardText = clipboardManager.getText()?.toString() ?: ""
                                if (clipboardText.isNotEmpty() && newText.contains(clipboardText)) {
                                    clipboardText
                                } else {
                                    if (oldCursorPos >= 0 && oldCursorPos <= oldText.length) {
                                        val beforeCursor = oldText.substring(0, oldCursorPos)
                                        val afterCursor = oldText.substring(oldCursorPos)
                                        if (newText.startsWith(beforeCursor) && newText.endsWith(afterCursor)) {
                                            newText.substring(beforeCursor.length, newText.length - afterCursor.length)
                                        } else {
                                            if (oldCursorPos + textDiff <= newText.length) {
                                                newText.substring(oldCursorPos, oldCursorPos + textDiff)
                                            } else { "" }
                                        }
                                    } else { "" }
                                }
                            } catch (e: Exception) { "" }
                            
                            if (pastedText.isNotEmpty()) {
                                // ✅ Process pasted text - filter only digits, decimal, and minus (for temperature)
                                val filteredText = pastedText.filter { 
                                    it.isDigit() || it == '.' || it == ',' || it == '-' || it == ' '
                                }.replace(',', '.').replace(" ", "")
                                
                                if (filteredText.isNotEmpty()) {
                                    // Insert filtered text at cursor position
                                    val currentValue = textFieldValue.text
                                    val beforeCursor = if (oldCursorPos >= 0 && oldCursorPos <= currentValue.length) {
                                        currentValue.substring(0, oldCursorPos)
                                    } else {
                                        currentValue
                                    }
                                    val afterCursor = if (oldCursorPos >= 0 && oldCursorPos <= currentValue.length) {
                                        currentValue.substring(oldCursorPos)
                                    } else {
                                        ""
                                    }
                                    
                                    // Combine and update
                                    val newValueText = beforeCursor + filteredText + afterCursor
                                    // Remove leading zero if needed
                                    val finalValue = if (newValueText.replace("-", "").startsWith("0") && newValueText.replace("-", "").length > 1 && newValueText.replace("-", "")[1] != '.') {
                                        val hasMinus = newValueText.startsWith("-")
                                        val withoutMinus = newValueText.replace("-", "").drop(1)
                                        if (hasMinus) "-$withoutMinus" else withoutMinus
                                    } else {
                                        newValueText
                                    }
                                    
                                    onValueChange(finalValue)
                                    val newCursorPos = (oldCursorPos + filteredText.length).coerceAtMost(finalValue.length)
                                    textFieldValue = TextFieldValue(text = finalValue, selection = TextRange(newCursorPos))
                                    currentCursorPosition = newCursorPos
                                    onCursorPositionChange(newCursorPos)
                                    keyboardController?.hide()
                                }
                            }
                        } else {
                            // ✅ Text decreased or same - handle cut/backspace (same as Currency)
                            val safeStart = newValue.selection.start.coerceAtLeast(0).coerceAtMost(textFieldValue.text.length)
                            val safeEnd = newValue.selection.end.coerceAtLeast(0).coerceAtMost(textFieldValue.text.length)
                            textFieldValue = TextFieldValue(
                                text = textFieldValue.text,
                                selection = TextRange(safeStart, safeEnd)
                            )
                            currentCursorPosition = safeStart
                            onCursorPositionChange(safeStart)
                            keyboardController?.hide()
                        }
                    }
                },
                modifier = modifier
                    .height(dimensions.dropdownHeight)
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        isFocused = focusState.isFocused
                        // ✅ STATE-BASED: Hide IME using WindowInsetsController - reliable, single source of truth
                        if (focusState.isFocused) {
                            // Trigger explicit hide once when touched
                            keyboardController?.hide()
                            // Safety net for some devices
                            val imm = context.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as? android.view.inputmethod.InputMethodManager
                            imm?.hideSoftInputFromWindow((context as? android.app.Activity)?.window?.decorView?.windowToken, 0)

                            activity?.let { act ->
                                val windowInsetsController = WindowCompat.getInsetsController(act.window, act.window.decorView)
                                windowInsetsController.hide(androidx.core.view.WindowInsetsCompat.Type.ime())
                            }
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { _ ->
                                // ✅ STATE-BASED: Hide IME immediately on tap
                                activity?.let { act ->
                                    val windowInsetsController = WindowCompat.getInsetsController(act.window, act.window.decorView)
                                    windowInsetsController.hide(androidx.core.view.WindowInsetsCompat.Type.ime())
                                }
                            }
                        )
                    },
                textStyle = TextStyle(
                    fontSize = dimensions.inputFontSize,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.None
                ),
                decorationBox = { innerTextField ->
                    OutlinedTextFieldDefaults.DecorationBox(
                        value = textFieldValue.text,
                        visualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
                        innerTextField = innerTextField,
                        placeholder = { Text("0", fontSize = dimensions.inputFontSize) },
                        singleLine = true,
                        enabled = true,
                        isError = false,
                        interactionSource = interactionSource,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        container = {
                            OutlinedTextFieldDefaults.ContainerBox(
                                enabled = true,
                                isError = false,
                                interactionSource = interactionSource,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                ),
                                shape = RoundedCornerShape(dimensions.cardCornerRadius)
                            )
                        }
                    )
                }
            )
        }
    }
    
    // ✅ Update keypad click handler to use current cursor position
    LaunchedEffect(Unit) {
        // This will be handled by the keypad button clicks
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterResultField(
    value: String,
    modifier: Modifier = Modifier,
    dimensions: ConverterResponsiveDimensions
) {
    // ✅ Clone exact method from Currency converted amount field
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    var resultTextFieldValue by remember { mutableStateOf(TextFieldValue(value)) }
    
    // ✅ Sync with value from ViewModel (same as Currency)
    LaunchedEffect(value) {
        if (resultTextFieldValue.text != value) {
            resultTextFieldValue = TextFieldValue(
                text = value,
                selection = resultTextFieldValue.selection // ✅ Preserve selection for copy
            )
        }
    }
    
    // ✅ STATE-BASED: Result field also uses IN_APP_KEYPAD_ONLY policy
    // No additional IME handling needed - screen-level policy covers this
    
    // ✅ Keep existing styling (decorationBox) but add copy logic
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    CompositionLocalProvider(
        LocalTextSelectionColors provides TextSelectionColors(
            handleColor = MaterialTheme.colorScheme.primary,
            backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )
    ) {
        SelectionContainer {
            BasicTextField(
                value = resultTextFieldValue,
                onValueChange = { newValue ->
                    keyboardController?.hide()
                    // ✅ Only allow selection changes, not text input (same as Currency)
                    if (newValue.text == resultTextFieldValue.text) {
                        // Only selection changed - allow it (for copy)
                        resultTextFieldValue = newValue
                    } else {
                        // Text changed - reject and keep original text
                        val safeStart = newValue.selection.start.coerceAtLeast(0).coerceAtMost(resultTextFieldValue.text.length)
                        val safeEnd = newValue.selection.end.coerceAtLeast(0).coerceAtMost(resultTextFieldValue.text.length)
                        resultTextFieldValue = TextFieldValue(
                            text = resultTextFieldValue.text,
                            selection = TextRange(safeStart, safeEnd)
                        )
                        keyboardController?.hide()
                    }
                },
                readOnly = true,
                enabled = true,
                modifier = modifier
                    .fillMaxWidth()
                    .height(dimensions.dropdownHeight)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            // Trigger explicit hide once when touched
                            keyboardController?.hide()
                            // Safety net for some devices
                            val imm = context.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as? android.view.inputmethod.InputMethodManager
                            imm?.hideSoftInputFromWindow((context as? android.app.Activity)?.window?.decorView?.windowToken, 0)
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { _ ->
                                // IME hiding handled by screen-level state-based policy
                            }
                        )
                    },
                textStyle = TextStyle(
                    fontSize = dimensions.resultFontSize,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                ),
                cursorBrush = SolidColor(Color.Transparent),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.None
                ),
                decorationBox = { innerTextField ->
                    OutlinedTextFieldDefaults.DecorationBox(
                        value = resultTextFieldValue.text,
                        visualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
                        innerTextField = innerTextField,
                        placeholder = { Text("0", fontSize = dimensions.resultFontSize) },
                        singleLine = true,
                        enabled = false,
                        isError = false,
                        interactionSource = interactionSource,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            unfocusedTextColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        container = {
                            OutlinedTextFieldDefaults.ContainerBox(
                                enabled = false,
                                isError = false,
                                interactionSource = interactionSource,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                ),
                                shape = RoundedCornerShape(dimensions.cardCornerRadius)
                            )
                        }
                    )
                }
            )
        }
    }
}


