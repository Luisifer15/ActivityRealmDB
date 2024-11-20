package ph.edu.auf.activityrealmdb.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.realm.kotlin.ext.query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ph.edu.auf.activityrealmdb.database.RealmHelper
import ph.edu.auf.activityrealmdb.database.realmodel.OwnerModel
import ph.edu.auf.activityrealmdb.database.realmodel.PetModel

class PetViewModel : ViewModel() {

    private val _pets = MutableStateFlow<List<PetModel>>(emptyList())
    val pets: StateFlow<List<PetModel>> get() = _pets.asStateFlow()

    private val _showSnackbar = MutableSharedFlow<String>()
    val showSnackbar: SharedFlow<String> = _showSnackbar

    init {
        loadPets()
    }

    private fun loadPets() {
        viewModelScope.launch(Dispatchers.IO) {
            val realm = RealmHelper.getRealmInstance()
            val results = realm.query(PetModel::class).find()
            _pets.value = results
        }
    }

    fun deletePet(model: PetModel): Boolean {
        var deleteSuccessful = false
        viewModelScope.launch(Dispatchers.IO) {
            val realm = RealmHelper.getRealmInstance()
            val owner = realm.query<OwnerModel>("pets.id == $0", model.id).first().find()
            if (owner != null) {
                _showSnackbar.emit("Cannot delete pet with an owner")
                return@launch
            }
            realm.write {
                val pet = this.query<PetModel>("id == $0", model.id).first().find()
                if (pet != null) {
                    delete(pet)
                    _pets.update {
                        val list = it.toMutableList()
                        list.remove(model)
                        list
                    }
                    deleteSuccessful = true
                }
            }
            if (deleteSuccessful) {
                _showSnackbar.emit("Removed ${model.name}")
            }
        }
        return deleteSuccessful
    }

    fun addPet(
        name: String,
        age: Int,
        petType: String,
        hasOwner: Boolean,
        ownerName: String,
        isOwnerNew: Boolean,
        icon: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val realm = RealmHelper.getRealmInstance()
            realm.write {
                val owner = if (hasOwner) {
                    if (isOwnerNew) {
                        val existingOwner = this.query<OwnerModel>("name == $0", ownerName).first().find()
                        if (existingOwner != null) {
                            viewModelScope.launch(Dispatchers.IO) {
                                _showSnackbar.emit("Owner already exists")
                            }
                            return@write
                        } else {
                            OwnerModel().apply { this.name = ownerName }
                        }
                    } else {
                        this.query<OwnerModel>("name == $0", ownerName).first().find()
                    }
                } else null

                val newPet = PetModel().apply {
                    this.name = name
                    this.age = age
                    this.petType = petType
                    this.icon = icon
                }

                if (owner != null) {
                    owner.pets.add(newPet)
                    copyToRealm(owner)
                } else {
                    copyToRealm(newPet)
                }

                _pets.update { it + newPet }
            }
        }
    }

    fun modifyPet(
        id: String,
        name: String,
        age: Int,
        petType: String,
        hasOwner: Boolean,
        ownerName: String,
        isOwnerNew: Boolean,
        icon: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val realm = RealmHelper.getRealmInstance()
            realm.write {
                val pet = this.query<PetModel>("id == $0", id).first().find()
                if (pet != null) {
                    pet.name = name
                    pet.age = age
                    pet.petType = petType
                    pet.icon = icon

                    // Remove pet from previous owner's pet list
                    val previousOwner = this.query<OwnerModel>("pets.id == $0", id).first().find()
                    previousOwner?.pets?.remove(pet)

                    if (hasOwner) {
                        val owner = if (isOwnerNew) {
                            val existingOwner = this.query<OwnerModel>("name == $0", ownerName).first().find()
                            if (existingOwner != null) {
                                viewModelScope.launch(Dispatchers.IO) {
                                    _showSnackbar.emit("Owner already exists")
                                }
                                return@write
                            } else {
                                OwnerModel().apply { this.name = ownerName }
                            }
                        } else {
                            this.query<OwnerModel>("name == $0", ownerName).first().find()
                        }

                        if (owner != null) {
                            if (!owner.pets.contains(pet)) {
                                owner.pets.add(pet)
                            }
                            copyToRealm(owner)
                        }
                    } else {
                        // If hasOwner is unchecked, pet is added directly to the PetModel table
                        copyToRealm(pet)
                    }

                    _pets.update { currentPets ->
                        currentPets.map { existingPet ->
                            if (existingPet.id == id) {
                                // Create a new pet object with updated properties
                                PetModel().apply {
                                    this.id = id
                                    this.name = name
                                    this.age = age
                                    this.petType = petType
                                    this.icon = icon
                                    // Copy other necessary properties from the existing pet
                                }
                            } else {
                                existingPet
                            }
                        }
                    }
                }
            }
        }
    }

    fun getOwnerByPetId(petId: String): OwnerModel? {
        val realm = RealmHelper.getRealmInstance()
        return realm.query<OwnerModel>("pets.id == $0", petId).first().find()
    }
}