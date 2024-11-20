package ph.edu.auf.activityrealmdb.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddOwnerDialog(
    onDismiss: () -> Unit,
    onAddOwner: (String) -> Unit,
    ownerName: String,
    onNameChange: (String) -> Unit
) {

    val (error, setError) = remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Add New Owner") },
        text = {
            Column {
                OutlinedTextField(
                    value = ownerName,
                    onValueChange = {
                        onNameChange(it)
                        if (it.isNotBlank()) setError(null)
                    },
                    label = { Text("Owner Name") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = error != null
                )
                if (error != null) {
                    Text(text = error, color = MaterialTheme.colorScheme.error)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        },
        confirmButton = {
            Button(onClick = {
                if (ownerName.isBlank()) {
                    setError("Owner name cannot be empty")
                } else {
                    onAddOwner(ownerName)
                }
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}