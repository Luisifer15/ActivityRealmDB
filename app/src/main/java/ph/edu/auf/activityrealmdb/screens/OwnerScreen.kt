package ph.edu.auf.activityrealmdb.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import ph.edu.auf.activityrealmdb.components.AddOwnerDialog
import ph.edu.auf.activityrealmdb.components.ItemOwner
import ph.edu.auf.activityrealmdb.database.realmodel.OwnerModel
import ph.edu.auf.activityrealmdb.ui.theme.brutalistAccent
import ph.edu.auf.activityrealmdb.ui.theme.brutalistBackground
import ph.edu.auf.activityrealmdb.ui.theme.brutalistError
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
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(ownerViewModel.showSnackbar) {
        ownerViewModel.showSnackbar.collect { message ->
            if (!snackbarShown) {
                snackbarShown = true
                coroutineScope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = message,
                        actionLabel = "CLOSE",
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

    val filteredOwners = owners.filter { it.name.contains(searchQuery, ignoreCase = true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brutalistBackground)
    ) {
        Scaffold(
            containerColor = brutalistBackground,
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    Snackbar(
                        modifier = Modifier
                            .border(2.dp, brutalistAccent)
                            .padding(0.dp),
                        containerColor = Color.White,
                        contentColor = brutalistAccent,
                        actionColor = brutalistError,
                        snackbarData = data
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    singleLine = true,
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("SEARCH OWNERS", fontWeight = FontWeight.Bold) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, brutalistAccent),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = brutalistAccent,
                        unfocusedBorderColor = brutalistAccent,
                        focusedLabelColor = brutalistAccent,
                        unfocusedLabelColor = brutalistAccent
                    ),
                    textStyle = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                )
                Button(
                    onClick = {
                        ownerToEdit = null
                        newOwnerName = ""
                        showDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = brutalistAccent
                    ),
                    shape = RectangleShape,
                    border = BorderStroke(2.dp, Color.White)
                ) {
                    Text(
                        "ADD NEW OWNER",
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(
                        items = filteredOwners,
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
            onNameChange = { newOwnerName = it },
            ownerModel = ownerToEdit
        )
    }
}