package com.vanexa.supercalculator.presentation.converter

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.vanexa.supercalculator.R
import com.vanexa.supercalculator.presentation.theme.VanexaTheme
import com.vanexa.supercalculator.presentation.tools.PdfToolScreen

import com.vanexa.supercalculator.presentation.common.BaseActivity

class ConverterActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Native keyboard is DEAD here.
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            VanexaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ConverterWithTabs(onBackPressed = { finish() })
                }
            }
        }
    }
}

@Composable
private fun ConverterWithTabs(onBackPressed: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabs = listOf(
        TabItem("File Engine") {
            Icon(
                painter = painterResource(id = R.drawable.ic_tab_file_engine),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        },
        TabItem("Unit Converter") {
            Icon(
                painter = painterResource(id = R.drawable.ic_tab_unit_converter),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF06101A), // Darker than main background, but matches the deep navy hue
                contentColor = Color.White
            ) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = tab.icon,
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            unselectedIconColor = Color.White.copy(alpha = 0.5f),
                            unselectedTextColor = Color.White.copy(alpha = 0.5f),
                            indicatorColor = Color(0xFF0D47A1).copy(alpha = 0.4f) // Deeper dark blue transparent
                        )
                    )
                }
            }
        }
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = MaterialTheme.colorScheme.background
        ) {
            when (selectedTab) {
                0 -> PdfToolScreen(onBackPressed = onBackPressed)
                1 -> UnitConverterScreen()
            }
        }
    }
}

private data class TabItem(
    val title: String,
    val icon: @Composable () -> Unit
)
