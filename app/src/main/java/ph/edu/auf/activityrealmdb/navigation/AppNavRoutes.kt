package ph.edu.auf.activityrealmdb.navigation

sealed class AppNavRoutes(val route: String){
    object Home : AppNavRoutes("home")
    object PetList: AppNavRoutes("pet_list")
    object OwnerList: AppNavRoutes("owner_list")
}