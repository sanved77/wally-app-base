package xyz.nagdibai.superwallpapers

import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FavViewModel (private val repository: FavRepo) : ViewModel() {

    val allFavs: LiveData<List<Favorite>> = repository.allFavs.asLiveData()

    fun insert(link: Favorite) = viewModelScope.launch {
        repository.insert(link)
    }

    private val _dataToUi = MutableLiveData<Favorite?>()
    val dataToUi: MutableLiveData<Favorite?>
        get() = _dataToUi

    suspend fun getOneFav(link: String): Favorite? {
        return withContext(Dispatchers.IO) {
            repository.getOneFav(link)
        }
    }

    fun setFliterQuery(query: String) {
        viewModelScope.launch {
            _dataToUi.value = getOneFav(query)
        }
    }

    fun getAllFavs() = viewModelScope.launch {
        repository.getAllFavs()
    }
    fun delete(link: String) = viewModelScope.launch {
        repository.delete(link)
    }
    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
    }
}

class FavViewModelFactory(private val repository: FavRepo) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FavViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}