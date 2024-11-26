package ph.edu.auf.activityrealmdb.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.realm.kotlin.ext.query
import kotlinx.coroutines.runBlocking
import ph.edu.auf.activityrealmdb.database.RealmHelper
import ph.edu.auf.activityrealmdb.database.realmodel.OwnerModel
import ph.edu.auf.activityrealmdb.ui.theme.brutalistAccent
import ph.edu.auf.activityrealmdb.ui.theme.brutalistError

@Composable
fun AddOwnerDialog(
    onDismiss: () -> Unit,
    onAddOwner: (String) -> Unit,
    ownerName: String,
    onNameChange: (String) -> Unit,
    ownerModel: OwnerModel? = null
) {
    val (error, setError) = remember { mutableStateOf<String?>(null) }
    val existingOwners = runBlocking {
        RealmHelper.getRealmInstance().query<OwnerModel>().find().map { it.name }
    }
    fun validateOwnerName(value: String): String? {
        return when {
            value.trim().isEmpty() -> "Owner name cannot be empty"
            value.trim().length > 50 -> "Owner name must be less than 50 characters"
            existingOwners.contains(value.trim()) -> "This owner name already exists"
            else -> null
        }
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
                    if (ownerModel != null) "EDIT OWNER" else "ADD NEW OWNER",
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    ),
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                OutlinedTextField(
                    singleLine = true,
                    value = ownerName,
                    onValueChange = {
                        onNameChange(it)
                        setError(validateOwnerName(it))
                    },
                    label = { Text("OWNER NAME", fontWeight = FontWeight.Bold) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, if (error != null) brutalistError else brutalistAccent),
                    isError = error != null,
                    textStyle = TextStyle(fontWeight = FontWeight.Bold),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = brutalistAccent,
                        unfocusedBorderColor = brutalistAccent,
                        errorBorderColor = brutalistError
                    )
                )
                if (error != null) {
                    Text(
                        text = error,
                        color = brutalistError,
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(top = 4.dp)
                    )
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
                            val validationError = validateOwnerName(ownerName)
                            if (validationError != null) {
                                setError(validationError)
                            } else {
                                onAddOwner(ownerName.trim())
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
                            if (ownerModel != null) "EDIT" else "ADD",
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