// Update ItemOwner composable to include a Modify button
package ph.edu.auf.activityrealmdb.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ph.edu.auf.activityrealmdb.database.realmodel.OwnerModel
import ph.edu.auf.activityrealmdb.ui.theme.brutalistAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemOwner(
    ownerModel: OwnerModel,
    onRemove: (OwnerModel) -> Unit,
    onModify: (OwnerModel) -> Unit
) {
    val currentItem by rememberUpdatedState(ownerModel)
    var resetFlag by remember { mutableStateOf(false) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            when(it){
                SwipeToDismissBoxValue.StartToEnd -> {
                    val deleteSuccessful = try {
                        onRemove(currentItem)
                        true
                    } catch (e: Exception) {
                        resetFlag = true
                        false
                    }
                    if (!deleteSuccessful) {
                        resetFlag = true
                        return@rememberSwipeToDismissBoxState false
                    }
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    onModify(currentItem)
                    return@rememberSwipeToDismissBoxState false
                }
                SwipeToDismissBoxValue.Settled -> {
                    return@rememberSwipeToDismissBoxState false
                }
            }
            true
        },
        positionalThreshold = { it * .25f }
    )

    LaunchedEffect(resetFlag) {
        if (resetFlag) {
            dismissState.snapTo(SwipeToDismissBoxValue.Settled)
            resetFlag = false
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd)
                        Color(0xFFFF0000) else Color(0xFF0000FF))
                    .padding(horizontal = 20.dp),
                contentAlignment = if (dismissState.dismissDirection ==
                    SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
            ) {
                Text(
                    text = if (dismissState.dismissDirection ==
                        SwipeToDismissBoxValue.StartToEnd) "DELETE" else "EDIT",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp
                )
            }
        }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, brutalistAccent),
            shape = RectangleShape,
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Name on the left with dynamic text sizing
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    AutoSizeText(
                        text = ownerModel.name.uppercase(),
                        style = TextStyle(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        maxLines = 1
                    )
                }

                // Number of pets on the far right
                Text(
                    text = "${ownerModel.pets.size} PETS",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = brutalistAccent
                    )
                )
            }
        }
    }
}

@Composable
fun AutoSizeText(
    text: String,
    style: TextStyle,
    maxLines: Int = 1,
    modifier: Modifier = Modifier
) {
    var fontSize by remember { mutableStateOf(24.sp) }
    var shouldDraw by remember { mutableStateOf(false) }

    Text(
        text = text,
        style = style.copy(fontSize = fontSize),
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier.drawWithContent {
            if (shouldDraw) {
                drawContent()
            }
        }
    )

    Layout(
        content = {
            Text(
                text = text,
                style = style.copy(fontSize = fontSize),
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis
            )
        }
    ) { measurables, constraints ->
        // Measure the text at the initial font size
        val textPlaceable = measurables[0].measure(constraints)

        // If the text is too wide, reduce font size
        while (textPlaceable.width > constraints.maxWidth && fontSize.value > 12) {
            fontSize = (fontSize.value - 1).sp
            val newTextPlaceable = measurables[0].measure(constraints)
            if (newTextPlaceable.width <= constraints.maxWidth) {
                break
            }
        }

        // Mark that drawing is now safe
        shouldDraw = true

        layout(textPlaceable.width, textPlaceable.height) {
            textPlaceable.place(0, 0)
        }
    }
}