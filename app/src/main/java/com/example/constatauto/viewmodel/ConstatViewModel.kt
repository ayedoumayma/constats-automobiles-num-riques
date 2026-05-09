package com.example.constatauto.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.constatauto.model.Constat
import com.example.constatauto.model.StatutConstat
import com.example.constatauto.model.Vehicule
import com.example.constatauto.repository.ConstatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ConstatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ConstatRepository(application)

    // State
    private val _constats = MutableStateFlow<List<Constat>>(emptyList())
    val constats: StateFlow<List<Constat>> = _constats.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Filter state
    val searchQuery = MutableStateFlow("")
    val selectedStatut = MutableStateFlow<StatutConstat?>(null)

    // Filtered list (combine constats + filters)
    val filteredConstats: StateFlow<List<Constat>> = combine(
        _constats, searchQuery, selectedStatut
    ) { list, query, statut ->
        list.filter { constat ->
            val matchesSearch = query.isEmpty() ||
                constat.numeroConstat.contains(query, ignoreCase = true) ||
                constat.lieuAccident.contains(query, ignoreCase = true) ||
                constat.conducteurA.nom.contains(query, ignoreCase = true) ||
                constat.conducteurB.nom.contains(query, ignoreCase = true)
            val matchesStatut = statut == null || constat.statut == statut
            matchesSearch && matchesStatut
        }.sortedByDescending { it.dateCreation }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    // Stats for dashboard
    val stats: StateFlow<Map<String, Int>> = combine(_constats) { (list) ->
        mapOf(
            "total" to list.size,
            "en_cours" to list.count { it.statut == StatutConstat.EN_COURS },
            "soumis" to list.count { it.statut == StatutConstat.SOUMIS },
            "acceptes" to list.count { it.statut == StatutConstat.ACCEPTE }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), mapOf("total" to 0, "en_cours" to 0, "soumis" to 0, "acceptes" to 0))


    init {
        initDemoDataIfNeeded()
        loadConstats()
    }

    private fun initDemoDataIfNeeded() {
        if (repository.getAllConstats().isEmpty()) {
            val demoList = listOf(
                Constat(
                    numeroConstat = repository.generateNumeroConstat(),
                    statut = StatutConstat.EN_COURS,
                    lieuAccident = "Tunis",
                    vehiculeA = Vehicule(marque = "Toyota", modele = "Corolla"),
                    vehiculeB = Vehicule(marque = "BMW", modele = "Série 3"),
                    dateAccident = "01/03/2024",
                    dateCreation = "2024-03-01T10:00:00"
                ),
                Constat(
                    numeroConstat = repository.generateNumeroConstat(),
                    statut = StatutConstat.SOUMIS,
                    lieuAccident = "Sfax",
                    vehiculeA = Vehicule(marque = "Renault", modele = "Clio"),
                    vehiculeB = Vehicule(marque = "Peugeot", modele = "208"),
                    dateAccident = "15/02/2024",
                    dateCreation = "2024-02-15T14:30:00"
                ),
                Constat(
                    numeroConstat = repository.generateNumeroConstat(),
                    statut = StatutConstat.ACCEPTE,
                    lieuAccident = "Sousse",
                    vehiculeA = Vehicule(marque = "Mercedes", modele = "Class C"),
                    vehiculeB = Vehicule(marque = "Honda", modele = "Civic"),
                    dateAccident = "10/01/2024",
                    dateCreation = "2024-01-10T09:15:00"
                ),
                Constat(
                    numeroConstat = repository.generateNumeroConstat(),
                    statut = StatutConstat.REFUSE,
                    lieuAccident = "Nabeul",
                    vehiculeA = Vehicule(marque = "Hyundai", modele = "Tucson"),
                    vehiculeB = Vehicule(marque = "Kia", modele = "Sportage"),
                    dateAccident = "05/12/2023",
                    dateCreation = "2023-12-05T16:45:00"
                ),
                Constat(
                    numeroConstat = repository.generateNumeroConstat(),
                    statut = StatutConstat.EN_COURS,
                    lieuAccident = "Monastir",
                    vehiculeA = Vehicule(marque = "Volkswagen", modele = "Golf"),
                    vehiculeB = Vehicule(marque = "Ford", modele = "Focus"),
                    dateAccident = "28/03/2024",
                    dateCreation = "2024-03-28T11:20:00"
                )
            )

            demoList.forEach { repository.saveConstat(it) }
        }
    }

    fun loadConstats() {
        viewModelScope.launch {
            _isLoading.value = true
            _constats.value = repository.getAllConstats()
            _isLoading.value = false
        }
    }

    fun addConstat(constat: Constat): Result<Unit> {
        return try {
            val newConstat = if (constat.numeroConstat.isEmpty()) {
                constat.copy(numeroConstat = repository.generateNumeroConstat())
            } else constat

            if (repository.saveConstat(newConstat)) {
                loadConstats()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Erreur de sauvegarde"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun updateConstat(constat: Constat): Result<Unit> {
        return try {
            if (repository.updateConstat(constat)) {
                loadConstats()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Erreur de mise à jour"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun deleteConstat(id: String): Result<Unit> {
        return try {
            if (repository.deleteConstat(id)) {
                loadConstats()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Erreur de suppression"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun updateStatut(id: String, newStatut: StatutConstat): Result<Unit> {
        return try {
            val constat = repository.getConstatById(id)
            if (constat != null) {
                val updatedConstat = constat.copy(statut = newStatut)
                if (repository.updateConstat(updatedConstat)) {
                    loadConstats()
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Erreur de mise à jour du statut"))
                }
            } else {
                Result.failure(Exception("Constat introuvable"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getConstatById(id: String): Constat? {
        return repository.getConstatById(id)
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
