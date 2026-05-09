package com.example.constatauto.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.constatauto.repository.ConstatRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ConstatRepository(application)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _currentUser = MutableStateFlow<String?>(null)
    val currentUser: StateFlow<String?> = _currentUser

    init {
        // Ajouter l'utilisateur de démonstration si non existant
        if (!repository.userExists("demo@constatauto.tn")) {
            repository.saveUser("demo@constatauto.tn", "demo123", "Utilisateur Démo")
        }
    }

    /**
     * Tente de connecter un utilisateur avec un email et un mot de passe.
     */
    fun login(email: String, password: String, onSuccess: () -> Unit, onError: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Veuillez remplir tous les champs"
            onError()
            return
        }

        if (!email.contains("@") || !email.contains(".")) {
            _errorMessage.value = "Email invalide"
            onError()
            return
        }

        if (password.length < 6) {
            _errorMessage.value = "Mot de passe trop court (min. 6 caractères)"
            onError()
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            // Simule un délai réseau
            delay(1500)

            val userInfo = repository.getUser(email)
            if (userInfo != null && userInfo["password"] == password) {
                _isLoggedIn.value = true
                _currentUser.value = userInfo["nom"]
                onSuccess()
            } else {
                _errorMessage.value = "Identifiants incorrects"
                onError()
            }

            _isLoading.value = false
        }
    }

    /**
     * Tente d'inscrire un nouvel utilisateur.
     */
    fun register(
        prenom: String,
        nom: String,
        telephone: String,
        email: String,
        password: String,
        confirm: String,
        cguAccepted: Boolean,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        if (prenom.isBlank() || prenom.length < 2 || nom.isBlank() || nom.length < 2) {
            _errorMessage.value = "Prénom et Nom doivent contenir au moins 2 caractères"
            onError()
            return
        }

        // Simplification du format: commence par +216 et a 8 chiffres
        val phoneRegex = "^\\+216[0-9]{8}$".toRegex()
        val plainPhoneRegex = "^[0-9]{8}$".toRegex()
        if (!phoneRegex.matches(telephone) && !plainPhoneRegex.matches(telephone)) {
            _errorMessage.value = "Format de téléphone invalide (8 chiffres)"
            onError()
            return
        }

        if (!email.contains("@") || !email.contains(".")) {
            _errorMessage.value = "Email invalide"
            onError()
            return
        }

        if (password.length < 6) {
            _errorMessage.value = "Mot de passe trop court (min. 6 caractères)"
            onError()
            return
        }

        if (password != confirm) {
            _errorMessage.value = "Les mots de passe ne correspondent pas"
            onError()
            return
        }

        if (!cguAccepted) {
            _errorMessage.value = "Vous devez accepter les conditions"
            onError()
            return
        }

        if (repository.userExists(email)) {
            _errorMessage.value = "Un compte existe déjà avec cet email"
            onError()
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            delay(1000)

            val success = repository.saveUser(email, password, "$prenom $nom")
            if (success) {
                _isLoggedIn.value = true
                _currentUser.value = "$prenom $nom"
                onSuccess()
            } else {
                _errorMessage.value = "Erreur lors de la création du compte"
                onError()
            }

            _isLoading.value = false
        }
    }

    fun logout() {
        _isLoggedIn.value = false
        _currentUser.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
