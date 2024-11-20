package ph.edu.auf.activityrealmdb.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
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
import ph.edu.auf.activityrealmdb.components.AddPetDialog
import ph.edu.auf.activityrealmdb.components.ItemPet
import ph.edu.auf.activityrealmdb.database.realmodel.PetModel
import ph.edu.auf.activityrealmdb.viewmodels.PetViewModel

@Composable
fun PetScreen(petViewModel: PetViewModel = viewModel()) {

    val pets by petViewModel.pets.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var snackbarShown by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var petToEdit by remember { mutableStateOf<PetModel?>(null) }

    LaunchedEffect(petViewModel.showSnackbar) {
        petViewModel.showSnackbar.collect { message ->
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

    val filteredPets = pets.filter { it.name.contains(searchQuery, ignoreCase = true) }

    Column(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Pets") },
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                )
                Button(onClick = {
                    petToEdit = null
                    showDialog = true
                }, modifier = Modifier.padding(16.dp)) {
                    Text("Add New Pet")
                }
                LazyColumn {
                    itemsIndexed(
                        items = filteredPets,
                        key = { _, item -> item.id }
                    ) { _, petContent ->
                        ItemPet(
                            petModel = petContent,
                            onRemove = petViewModel::deletePet,
                            onModify = {
                                petToEdit = it
                                showDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddPetDialog(
            onDismiss = { showDialog = false },
            onAddPet = { name, age, petType, hasOwner, ownerName, isOwnerNew ->
                if (petToEdit != null) {
                    petViewModel.modifyPet(petToEdit!!.id, name, age, petType, hasOwner, ownerName, isOwnerNew)
                } else {
                    petViewModel.addPet(name, age, petType, hasOwner, ownerName, isOwnerNew)
                }
                showDialog = false
                petToEdit = null
            },
            pet = petToEdit
        )
    }
}