package ph.edu.auf.activityrealmdb.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import ph.edu.auf.activityrealmdb.navigation.AppNavRoutes

@Composable
fun HomeScreen(navController: NavController) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .border(3.dp, Color.Black),
        shape = RectangleShape,
        color = Color.White
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text(
                text = "PET REALM APP",
                style = TextStyle(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(bottom = 48.dp)
            )

            HomeScreenButton(
                text = "PET LIST",
                onClick = { navController.navigate(AppNavRoutes.PetList.route) },
                isPrimary = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            HomeScreenButton(
                text = "OWNER LIST",
                onClick = { navController.navigate(AppNavRoutes.OwnerList.route) },
                isPrimary = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            )
        }
    }
}

@Composable
fun HomeScreenButton(
    text: String,
    onClick: () -> Unit,
    isPrimary: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .border(2.dp, Color.Black)
            .background(if (isPrimary) Color.Black else Color.White)
            .clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = text,
                style = TextStyle(
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                    letterSpacing = 1.sp,
                    color = if (isPrimary) Color.White else Color.Black
                )
            )
        }
    }
}