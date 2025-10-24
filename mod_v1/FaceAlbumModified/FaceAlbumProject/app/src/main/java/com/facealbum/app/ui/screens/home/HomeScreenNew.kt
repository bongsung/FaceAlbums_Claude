package com.facealbum.app.ui.screens.home

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.facealbum.domain.model.Face
import com.facealbum.domain.model.Person
import com.facealbum.domain.model.WatchFolder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onPersonClick: (Long) -> Unit,
    onPhotoClick: (Long) -> Unit,
    onSuggestionsClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val persons by viewModel.persons.collectAsState()
    val suggestionsCount by viewModel.suggestionsCount.collectAsState()
    val watchFolders by viewModel.watchFolders.collectAsState()
    val newFacesCount by viewModel.newFacesCount.collectAsState()
    val unassignedFaces by viewModel.unassignedFaces.collectAsState()
    val faceToAdd by viewModel.faceToAdd.collectAsState()
    
    var selectedTab by remember { mutableStateOf(0) }
    val context = LocalContext.current
    
    // Folder picker launcher
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            // Persist permissions
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, takeFlags)
            
            // Convert URI to path and add to watch folders
            val path = uri.path ?: return@let
            viewModel.addWatchFolder(path)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Face Album") },
                actions = {
                    if (newFacesCount > 0) {
                        Badge(
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("$newFacesCount new")
                        }
                    }
                    if (suggestionsCount > 0) {
                        TextButton(onClick = onSuggestionsClick) {
                            Text("$suggestionsCount Suggestions")
                        }
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { folderPickerLauncher.launch(null) },
                    modifier = Modifier.padding(bottom = 56.dp)
                ) {
                    Icon(Icons.Default.CreateNewFolder, contentDescription = "Select Folder")
                }
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Folder, contentDescription = "Folders") },
                    label = { Text("Folders") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "People") },
                    label = { Text("People") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                NavigationBarItem(
                    icon = { 
                        BadgedBox(
                            badge = {
                                if (newFacesCount > 0) {
                                    Badge { Text("$newFacesCount") }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Face, contentDescription = "New Faces")
                        }
                    },
                    label = { Text("New Faces") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
            }
        }
    ) { paddingValues ->
        when (selectedTab) {
            0 -> FoldersTab(
                watchFolders = watchFolders,
                onFolderClick = { folder ->
                    // Navigate to folder photos
                    viewModel.scanFolder(folder.id)
                },
                onRemoveFolder = viewModel::removeWatchFolder,
                paddingValues = paddingValues
            )
            1 -> PeopleTab(
                persons = persons,
                onPersonClick = onPersonClick,
                paddingValues = paddingValues
            )
            2 -> NewFacesTab(
                unassignedFaces = unassignedFaces,
                onFaceClick = viewModel::showAddFaceDialog,
                paddingValues = paddingValues
            )
        }
    }
    
    // Add Face Dialog
    faceToAdd?.let { face ->
        AddFaceDialog(
            face = face,
            onConfirm = { name ->
                viewModel.addFaceAsPerson(face, name)
            },
            onDismiss = { viewModel.dismissAddFaceDialog() }
        )
    }
}

@Composable
fun FoldersTab(
    watchFolders: List<WatchFolder>,
    onFolderClick: (WatchFolder) -> Unit,
    onRemoveFolder: (Long) -> Unit,
    paddingValues: PaddingValues
) {
    if (watchFolders.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.FolderOpen,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("No folders selected")
                Text(
                    "Tap + to select folders to watch",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(watchFolders) { folder ->
                FolderCard(
                    folder = folder,
                    onClick = { onFolderClick(folder) },
                    onRemove = { onRemoveFolder(folder.id) }
                )
            }
        }
    }
}

@Composable
fun FolderCard(
    folder: WatchFolder,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Folder,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = folder.path.substringAfterLast('/'),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = folder.path,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (folder.isEnabled) {
                    Text(
                        text = "Active • Watching for changes",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun PeopleTab(
    persons: List<Person>,
    onPersonClick: (Long) -> Unit,
    paddingValues: PaddingValues
) {
    if (persons.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.GroupAdd,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("No people yet")
                Text(
                    "Add folders and detect faces to get started",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(persons) { person ->
                PersonCard(
                    person = person,
                    onClick = { onPersonClick(person.id) }
                )
            }
        }
    }
}

@Composable
fun PersonCard(
    person: Person,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Cover photo or placeholder
            if (person.coverPhotoUri != null) {
                AsyncImage(
                    model = person.coverPhotoUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Gradient overlay for text
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = person.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${person.photoCount} photos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun NewFacesTab(
    unassignedFaces: List<Face>,
    onFaceClick: (Face) -> Unit,
    paddingValues: PaddingValues
) {
    if (unassignedFaces.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("All faces organized!")
                Text(
                    "New faces will appear here",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header with count
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = "${unassignedFaces.size} new faces found",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(unassignedFaces) { face ->
                    NewFaceCard(
                        face = face,
                        onClick = { onFaceClick(face) }
                    )
                }
            }
        }
    }
}

@Composable
fun NewFaceCard(
    face: Face,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Face thumbnail would go here
            Icon(
                Icons.Default.Face,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            // Add button overlay
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add",
                    modifier = Modifier
                        .size(20.dp)
                        .padding(2.dp)
                )
            }
        }
    }
}

@Composable
fun AddFaceDialog(
    face: Face,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var isExistingPerson by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.PersonAdd, contentDescription = null)
        },
        title = {
            Text("Add Face to Album")
        },
        text = {
            Column {
                Text("Enter a name for this person:")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isExistingPerson,
                        onCheckedChange = { isExistingPerson = it }
                    )
                    Text("Add to existing person")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name.trim())
                        onDismiss()
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
