package ph.edu.auf.activityrealmdb.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.realm.kotlin.ext.query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ph.edu.auf.activityrealmdb.database.RealmHelper
import ph.edu.auf.activityrealmdb.database.realmodel.OwnerModel

class OwnerViewModel : ViewModel() {
    private val _owners = MutableStateFlow<List<OwnerModel>>(emptyList())
    val owners : StateFlow<List<OwnerModel>> get() = _owners

    private val _showSnackbar = MutableSharedFlow<String>()
    val showSnackbar: SharedFlow<String> = _showSnackbar


    init {
        loadOwners()
    }

    private fun loadOwners() {
        viewModelScope.launch(Dispatchers.IO){
            val realm = RealmHelper.getRealmInstance()
            val results = realm.query(OwnerModel::class).find()
            _owners.value = results
        }
    }

    fun addOwner(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val realm = RealmHelper.getRealmInstance()
            realm.write {
                val newOwner = OwnerModel().apply {
                    this.name = name
                }
                copyToRealm(newOwner)
                _owners.update { it + newOwner }
            }
        }
    }

    fun deleteOwner(model: OwnerModel) {
        viewModelScope.launch(Dispatchers.IO) {
            val realm = RealmHelper.getRealmInstance()
            realm.write {
                val owner = this.query<OwnerModel>("id == $0", model.id).first().find()
                if (owner != null) {
                    owner.pets.forEach { pet ->
                        copyToRealm(pet)
                    }
                    delete(owner)
                    _owners.update {
                        val list = it.toMutableList()
                        list.remove(model)
                        list
                    }
                }
            }
            _showSnackbar.emit("Removed ${model.name}")
        }
    }

    fun modifyOwner(id: String, newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val realm = RealmHelper.getRealmInstance()
            realm.write {
                val owner = this.query<OwnerModel>("id == $0", id).first().find()
                if (owner != null) {
                    owner.name = newName
                    copyToRealm(owner)
                    _owners.update { currentOwners ->
                        currentOwners.map { existingOwner ->
                            if (existingOwner.id == id) {
                                OwnerModel().apply {
                                    this.id = id
                                    this.name = newName
                                    this.pets = existingOwner.pets
                                }
                            } else {
                                existingOwner
                            }
                        }
                    }
                }
            }
        }
    }
}