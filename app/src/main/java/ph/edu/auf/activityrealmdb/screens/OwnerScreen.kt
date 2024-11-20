package ph.edu.auf.activityrealmdb.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import ph.edu.auf.activityrealmdb.components.AddOwnerDialog
import ph.edu.auf.activityrealmdb.components.ItemOwner
import ph.edu.auf.activityrealmdb.database.realmodel.OwnerModel
import ph.edu.auf.activityrealmdb.viewmodels.OwnerViewModel

@Composable
fun OwnerScreen(ownerViewModel: OwnerViewModel = viewModel()) {

    val owners by ownerViewModel.owners.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var showDialog by remember { mutableStateOf(false) }
    var newOwnerName by remember { mutableStateOf("") }
    var ownerToEdit by remember { mutableStateOf<OwnerModel?>(null) }
    var snackbarShown by remember { mutableStateOf(false) }

    LaunchedEffect(ownerViewModel.showSnackbar) {
        ownerViewModel.showSnackbar.collect { message ->
            if (!snackbarShown) {
                snackbarShown = true
                coroutineScope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = message,
                        actionLabel = "Dismiss",
                        duration = SnackbarDuration.Short
                    )
                    when (result) {
                        SnackbarResult.Dismissed, SnackbarResult.ActionPerformed -> {
                            snackbarShown = false
                        }
                    }
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                Button(onClick = {
                    ownerToEdit = null
                    newOwnerName = ""
                    showDialog = true
                }, modifier = Modifier.padding(16.dp)) {
                    Text("Add New Owner")
                }
                LazyColumn {
                    itemsIndexed(
                        items = owners,
                        key = { _, item -> item.id }
                    ) { _, ownerContent ->
                        ItemOwner(
                            ownerModel = ownerContent,
                            onRemove = ownerViewModel::deleteOwner,
                            onModify = {
                                ownerToEdit = it
                                newOwnerName = it.name
                                showDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddOwnerDialog(
            onDismiss = { showDialog = false },
            onAddOwner = { name ->
                if (ownerToEdit != null) {
                    ownerViewModel.modifyOwner(ownerToEdit!!.id, name)
                } else {
                    ownerViewModel.addOwner(name)
                }
                showDialog = false
                ownerToEdit = null
            },
            ownerName = newOwnerName,
            onNameChange = { newOwnerName = it }
        )
    }
}