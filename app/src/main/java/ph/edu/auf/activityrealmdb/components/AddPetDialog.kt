package ph.edu.auf.activityrealmdb.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.realm.kotlin.ext.query
import kotlinx.coroutines.runBlocking
import ph.edu.auf.activityrealmdb.database.RealmHelper
import ph.edu.auf.activityrealmdb.database.realmodel.OwnerModel
import ph.edu.auf.activityrealmdb.database.realmodel.PetModel
import ph.edu.auf.activityrealmdb.ui.theme.brutalistAccent
import ph.edu.auf.activityrealmdb.ui.theme.brutalistError

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPetDialog(
    onDismiss: () -> Unit,
    onAddPet: (String, Int, String, Boolean, String, Boolean, String) -> Unit,
    pet: PetModel? = null
) {
    val petTypes = listOf("Dog", "Cat", "Bird", "Fish", "Hamster", "Rabbit", "Guinea Pig", "Others")
    val petEmojis = listOf("🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼", "🐨", "🐯", "🦁", "🐮", "🐷", "🐸", "🐵", "🐔", "🐧", "🐦", "🐤", "🐣", "🐥", "🦆", "🦅", "🦉", "🦇", "🐺", "🐗", "🐴", "🦄", "🐝", "🐛", "🦋", "🐌", "🐞", "🐜", "🦟", "🦗", "🕷", "🦂", "🐢", "🐍", "🦎", "🐙", "🦑", "🦐", "🦞", "🦀", "🐡", "🐠", "🐟", "🐬", "🐳", "🐋", "🦈", "🐊", "🐅", "🐆", "🦓", "🦍", "🦧", "🐘", "🦛", "🦏", "🐪", "🐫", "🦒", "🦘", "🦥", "🦦", "🦨", "🦡", "🐁", "🐀", "🐿", "🦔")

    var name by remember { mutableStateOf(pet?.name ?: "") }
    var nameError by remember { mutableStateOf<String?>(null) }

    var age by remember { mutableStateOf(pet?.age?.toString() ?: "") }
    var ageError by remember { mutableStateOf<String?>(null) }

    var petType by remember { mutableStateOf(pet?.petType ?: petTypes[0]) }
    var hasOwner by remember { mutableStateOf(false) }
    var isOwnerNew by remember { mutableStateOf(false) }

    var originalOwnerName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var ownerNameError by remember { mutableStateOf<String?>(null) }

    var icon by remember { mutableStateOf(pet?.icon ?: petEmojis[0]) }
    var expanded by remember { mutableStateOf(false) }
    var ownerExpanded by remember { mutableStateOf(false) }
    var emojiExpanded by remember { mutableStateOf(false) }

    val existingOwners = runBlocking {
        RealmHelper.getRealmInstance().query<OwnerModel>().find().map { it.name }
    }

    LaunchedEffect(pet) {
        if (pet != null) {
            val owner = runBlocking {
                RealmHelper.getRealmInstance().query<OwnerModel>("pets.id == $0", pet.id).first().find()
            }
            if (owner != null) {
                hasOwner = true
                originalOwnerName = owner.name
                ownerName = owner.name
                isOwnerNew = false
            }
        } else {
            hasOwner = false
            ownerName = ""
        }
    }

    LaunchedEffect(isOwnerNew) {
        if (isOwnerNew) {
            ownerNameError = "Enter owner name"
            ownerName = ""
        }
    }

    fun validateName(value: String): String? {
        return when {
            value.trim().isEmpty() -> "Pet name cannot be empty"
            value.trim().length < 2 -> "Pet name must be at least 2 characters"
            value.trim().length > 30 -> "Pet name must be less than 30 characters"
            !value.trim().matches(Regex("^[a-zA-Z0-9 ]*$")) -> "Pet name can only contain letters, numbers and spaces"
            else -> null
        }
    }

    fun validateAge(value: String): String? {
        return try {
            val ageNum = value.toInt()
            when {
                ageNum <= 0 -> "Age must be greater than 0"
                ageNum > 100 -> "Age seems unrealistic. Please check"
                else -> null
            }
        } catch (e: NumberFormatException) {
            "Please enter a valid number"
        }
    }

    fun validateOwnerName(value: String): String? {
        return when {
            value.trim().isEmpty() -> "Owner name cannot be empty"
            value.trim().length > 50 -> "Owner name must be less than 50 characters"
            isOwnerNew && existingOwners.contains(value.trim()) -> "This owner name already exists"
            else -> null
        }
    }

    fun validateInputs(): Boolean {
        val nameValidation = validateName(name)
        val ageValidation = validateAge(age)
        val ownerValidation = if (hasOwner) validateOwnerName(ownerName) else null

        nameError = nameValidation
        ageError = ageValidation
        ownerNameError = ownerValidation

        return nameValidation == null && ageValidation == null &&
                (!hasOwner || ownerValidation == null)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .border(3.dp, brutalistAccent),
            shape = RectangleShape,
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (pet == null) "ADD NEW PET" else "MODIFY PET",
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    ),
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                OutlinedTextField(
                    singleLine = true,
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = validateName(it)
                    },
                    label = { Text("PET NAME", fontWeight = FontWeight.Bold) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, if (nameError != null) brutalistError else brutalistAccent),
                    isError = nameError != null,
                    textStyle = TextStyle(fontWeight = FontWeight.Bold),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = brutalistAccent,
                        unfocusedBorderColor = brutalistAccent,
                        errorBorderColor = brutalistError
                    )
                )
                if (nameError != null) {
                    Text(
                        text = nameError!!,
                        color = brutalistError,
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    singleLine = true,
                    value = age,
                    onValueChange = {
                        age = it
                        ageError = validateAge(it)
                    },
                    label = { Text("PET AGE", fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, if (ageError != null) brutalistError else brutalistAccent),
                    isError = ageError != null,
                    textStyle = TextStyle(fontWeight = FontWeight.Bold),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = brutalistAccent,
                        unfocusedBorderColor = brutalistAccent,
                        errorBorderColor = brutalistError
                    )
                )
                if (ageError != null) {
                    Text(
                        text = ageError!!,
                        color = brutalistError,
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = petType,
                        onValueChange = { },
                        label = { Text("PET TYPE", fontWeight = FontWeight.Bold) },
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .border(2.dp, brutalistAccent),
                        textStyle = TextStyle(fontWeight = FontWeight.Bold),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = brutalistAccent,
                            unfocusedBorderColor = brutalistAccent
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        petTypes.forEach { type ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        type,
                                        style = TextStyle(fontWeight = FontWeight.Bold)
                                    )
                                },
                                onClick = {
                                    petType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                ExposedDropdownMenuBox(
                    expanded = emojiExpanded,
                    onExpandedChange = { emojiExpanded = !emojiExpanded }
                ) {
                    OutlinedTextField(
                        value = icon,
                        onValueChange = { },
                        label = { Text("PET ICON", fontWeight = FontWeight.Bold) },
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .border(2.dp, brutalistAccent),
                        textStyle = TextStyle(fontWeight = FontWeight.Bold),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = emojiExpanded)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = brutalistAccent,
                            unfocusedBorderColor = brutalistAccent
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = emojiExpanded,
                        onDismissRequest = { emojiExpanded = false }
                    ) {
                        petEmojis.forEach { emoji ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        emoji,
                                        style = TextStyle(fontWeight = FontWeight.Bold)
                                    )
                                },
                                onClick = {
                                    icon = emoji
                                    emojiExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, brutalistAccent)
                        .padding(8.dp)
                ) {
                    Checkbox(
                        checked = hasOwner,
                        onCheckedChange = {
                            hasOwner = it
                            if (!it) {
                                ownerNameError = null
                                ownerName = ""
                            }
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = brutalistAccent,
                            uncheckedColor = brutalistAccent
                        )
                    )
                    Text(
                        "HAS OWNER",
                            style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    )
                }

                if (hasOwner) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, brutalistAccent)
                            .padding(8.dp)
                    ) {
                        Checkbox(
                            checked = isOwnerNew,
                            onCheckedChange = {
                                isOwnerNew = it
                                ownerNameError = if (ownerName.isNotEmpty())
                                    validateOwnerName(ownerName) else null
                                if (isOwnerNew){
                                    originalOwnerName = ownerName
                                    ownerName = ""
                                } else {
                                    ownerName = originalOwnerName
                                }
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = brutalistAccent,
                                uncheckedColor = brutalistAccent
                            )
                        )
                        Text(
                            "NEW OWNER",
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isOwnerNew) {
                        OutlinedTextField(
                            singleLine = true,
                            value = ownerName,
                            onValueChange = {
                                ownerName = it
                                ownerNameError = validateOwnerName(it)
                            },
                            label = { Text("OWNER NAME", fontWeight = FontWeight.Bold) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(2.dp, if (ownerNameError != null) brutalistError else brutalistAccent),
                            isError = ownerNameError != null,
                            textStyle = TextStyle(fontWeight = FontWeight.Bold),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = brutalistAccent,
                                unfocusedBorderColor = brutalistAccent,
                                errorBorderColor = brutalistError
                            )
                        )
                        if (ownerNameError != null) {
                            Text(
                                text = ownerNameError!!,
                                color = brutalistError,
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    } else {
                        ExposedDropdownMenuBox(
                            expanded = ownerExpanded,
                            onExpandedChange = { ownerExpanded = !ownerExpanded }
                        ) {
                            OutlinedTextField(
                                singleLine = true,
                                value = ownerName,
                                onValueChange = { },
                                label = { Text("OWNER NAME", fontWeight = FontWeight.Bold) },
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                                    .border(2.dp, if (ownerNameError != null)
                                        brutalistError else brutalistAccent),
                                isError = ownerNameError != null,
                                textStyle = TextStyle(fontWeight = FontWeight.Bold),
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = ownerExpanded)
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = brutalistAccent,
                                    unfocusedBorderColor = brutalistAccent,
                                    errorBorderColor = brutalistError
                                )
                            )
                            if (ownerNameError != null) {
                                Text(
                                    text = ownerNameError!!,
                                    color = brutalistError,
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            ExposedDropdownMenu(
                                expanded = ownerExpanded,
                                onDismissRequest = { ownerExpanded = false }
                            ) {
                                existingOwners.forEach { owner ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                owner,
                                                style = TextStyle(fontWeight = FontWeight.Bold)
                                            )
                                        },
                                        onClick = {
                                            ownerName = owner
                                            ownerNameError = validateOwnerName(owner)
                                            ownerExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }


                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .padding(end = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = brutalistAccent
                        ),
                        shape = RectangleShape,
                        border = BorderStroke(2.dp, brutalistAccent)
                    ) {
                        Text(
                            "CANCEL",
                            style = TextStyle(
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        )
                    }

                    Button(
                        onClick = {
                            if (validateInputs()) {
                                onAddPet(name.trim(), age.toInt(), petType, hasOwner,
                                    ownerName.trim(), isOwnerNew, icon)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .padding(start = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = brutalistAccent
                        ),
                        shape = RectangleShape,
                        border = BorderStroke(2.dp, brutalistAccent)
                    ) {
                        Text(
                            if (pet == null) "ADD" else "MODIFY",
                            style = TextStyle(
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        )
                    }
                }
            }
        }
    }
}