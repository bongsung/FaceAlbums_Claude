package com.facealbum.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Watch Folders",
                style = MaterialTheme.typography.titleMedium
            )
            
            // TODO: Implement watch folder list and add button
            
            Divider()
            
            Text(
                text = "Export Options",
                style = MaterialTheme.typography.titleMedium
            )
            
            // TODO: Implement export to external gallery toggle
            // Note: This is a placeholder - actual export functionality is TODO
            Text(
                text = "Export to external gallery: Not implemented",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
