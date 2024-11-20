package ph.edu.auf.activityrealmdb.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import io.realm.kotlin.ext.query
import kotlinx.coroutines.runBlocking
import ph.edu.auf.activityrealmdb.database.RealmHelper
import ph.edu.auf.activityrealmdb.database.realmodel.OwnerModel
import ph.edu.auf.activityrealmdb.database.realmodel.PetModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPetDialog(
    onDismiss: () -> Unit,
    onAddPet: (String, Int, String, Boolean, String, Boolean) -> Unit,
    pet: PetModel? = null
) {
    val petTypes = listOf("Dog", "Cat", "Bird", "Fish")
    var name by remember { mutableStateOf(pet?.name ?: "") }
    var age by remember { mutableStateOf(pet?.age?.toString() ?: "") }
    var petType by remember { mutableStateOf(pet?.petType ?: petTypes[0]) }
    var hasOwner by remember { mutableStateOf(false) }
    var isOwnerNew by remember { mutableStateOf(false) }
    var ownerName by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var ownerExpanded by remember { mutableStateOf(false) }
    val existingOwners = runBlocking {
        RealmHelper.getRealmInstance().query<OwnerModel>().find().map { it.name }
    }

    if (pet != null) {
        val owner = runBlocking {
            RealmHelper.getRealmInstance().query<OwnerModel>("pets.id == $0", pet.id).first().find()
        }
        if (owner != null) {
            hasOwner = true
            ownerName = owner.name
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (pet == null) "Add New Pet" else "Modify Pet") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Pet Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it },
                    label = { Text("Pet Age") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = petType,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        petTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    petType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = hasOwner,
                        onCheckedChange = { hasOwner = it }
                    )
                    Text("Has Owner")
                }
                if (hasOwner) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = isOwnerNew,
                            onCheckedChange = { isOwnerNew = it }
                        )
                        Text("Is Owner New?")
                    }
                    if (isOwnerNew) {
                        OutlinedTextField(
                            value = ownerName,
                            onValueChange = { ownerName = it },
                            label = { Text("Owner Name") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = existingOwners.contains(ownerName)
                        )
                    } else {
                        ExposedDropdownMenuBox(
                            expanded = ownerExpanded,
                            onExpandedChange = { ownerExpanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextField(
                                value = if (ownerName.isEmpty()) "select owner" else ownerName,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = ownerExpanded) },
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = ownerExpanded,
                                onDismissRequest = { ownerExpanded = false }
                            ) {
                                existingOwners.forEach { owner ->
                                    DropdownMenuItem(
                                        text = { Text(owner) },
                                        onClick = {
                                            ownerName = owner
                                            ownerExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onAddPet(name, age.toInt(), petType, hasOwner, ownerName, isOwnerNew)
            }) {
                Text(if (pet == null) "Add" else "Modify")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}