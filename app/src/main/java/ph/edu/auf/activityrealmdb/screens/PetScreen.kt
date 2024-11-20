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
import ph.edu.auf.activityrealmdb.components.AddPetDialog
import ph.edu.auf.activityrealmdb.components.ItemPet
import ph.edu.auf.activityrealmdb.database.realmodel.PetModel
import ph.edu.auf.activityrealmdb.ui.theme.brutalistAccent
import ph.edu.auf.activityrealmdb.ui.theme.brutalistBackground
import ph.edu.auf.activityrealmdb.ui.theme.brutalistError
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

    // Brutalist custom colors


    LaunchedEffect(petViewModel.showSnackbar) {
        petViewModel.showSnackbar.collect { message ->
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

    val filteredPets = pets.filter { it.name.contains(searchQuery, ignoreCase = true) }

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
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("SEARCH PETS", fontWeight = FontWeight.Bold) },
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
                        petToEdit = null
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
                        "ADD NEW PET",
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
                        items = filteredPets,
                        key = { _, item -> item.id }
                    ) { _, petContent ->
                        val ownerName = petViewModel.getOwnerByPetId(petContent.id)?.name
                        ItemPet(
                            petModel = petContent,
                            ownerName = ownerName,
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
            onAddPet = { name, age, petType, hasOwner, ownerName, isOwnerNew, icon ->
                if (petToEdit != null) {
                    petViewModel.modifyPet(petToEdit!!.id, name, age, petType, hasOwner, ownerName, isOwnerNew, icon)
                } else {
                    petViewModel.addPet(name, age, petType, hasOwner, ownerName, isOwnerNew, icon)
                }
                showDialog = false
                petToEdit = null
            },
            pet = petToEdit
        )
    }
}