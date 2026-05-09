package com.example.constatauto.repository

import android.content.Context
import android.util.Log
import com.example.constatauto.model.Constat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar

/**
 * Repository gérant la persistance des constats et des utilisateurs via SharedPreferences et Gson.
 */
class ConstatRepository(private val context: Context) {
    
    private val prefs = context.getSharedPreferences("constatauto_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    private val KEY_CONSTATS = "constats_list"
    private val KEY_USERS = "users_list"
    private val KEY_COUNTER = "constat_counter"

    /**
     * Récupère tous les constats enregistrés.
     */
    fun getAllConstats(): List<Constat> {
        return try {
            val json = prefs.getString(KEY_CONSTATS, null)
            if (json != null) {
                val type = object : TypeToken<List<Constat>>() {}.type
                gson.fromJson(json, type)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("ConstatRepository", "Erreur lors de la lecture des constats", e)
            emptyList()
        }
    }

    /**
     * Récupère un constat spécifique par son identifiant.
     */
    fun getConstatById(id: String): Constat? {
        return getAllConstats().find { it.id == id }
    }

    /**
     * Enregistre un nouveau constat.
     */
    fun saveConstat(constat: Constat): Boolean {
        return try {
            val currentList = getAllConstats().toMutableList()
            currentList.add(constat)
            saveList(currentList)
            true
        } catch (e: Exception) {
            Log.e("ConstatRepository", "Erreur lors de la sauvegarde du constat", e)
            false
        }
    }

    /**
     * Met à jour un constat existant.
     */
    fun updateConstat(constat: Constat): Boolean {
        return try {
            val currentList = getAllConstats().toMutableList()
            val index = currentList.indexOfFirst { it.id == constat.id }
            if (index != -1) {
                currentList[index] = constat
                saveList(currentList)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("ConstatRepository", "Erreur lors de la mise à jour du constat", e)
            false
        }
    }

    /**
     * Supprime un constat par son identifiant.
     */
    fun deleteConstat(id: String): Boolean {
        return try {
            val currentList = getAllConstats().toMutableList()
            val removed = currentList.removeIf { it.id == id }
            if (removed) {
                saveList(currentList)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("ConstatRepository", "Erreur lors de la suppression du constat", e)
            false
        }
    }

    private fun saveList(list: List<Constat>) {
        val json = gson.toJson(list)
        prefs.edit().putString(KEY_CONSTATS, json).apply()
    }

    /**
     * Génère un numéro de constat unique avec le format "CST-YYYY-XXXX".
     */
    fun generateNumeroConstat(): String {
        val year = Calendar.getInstance().get(Calendar.YEAR)
        val currentCounter = prefs.getInt(KEY_COUNTER, 0)
        val nextCounter = currentCounter + 1
        
        prefs.edit().putInt(KEY_COUNTER, nextCounter).apply()
        
        // Formatage avec des zéros devant, par exemple 0001
        val counterString = nextCounter.toString().padStart(4, '0')
        return "CST-$year-$counterString"
    }

    /**
     * Enregistre un nouvel utilisateur (simulation d'inscription).
     */
    fun saveUser(email: String, password: String, nom: String): Boolean {
        return try {
            val users = getAllUsers().toMutableMap()
            // On stocke les infos sous forme de "email:password|nom" pour simplifier la démo
            users[email] = "$password|$nom"
            val json = gson.toJson(users)
            prefs.edit().putString(KEY_USERS, json).apply()
            true
        } catch (e: Exception) {
            Log.e("ConstatRepository", "Erreur lors de la sauvegarde de l'utilisateur", e)
            false
        }
    }

    /**
     * Récupère un utilisateur par email. Retourne une Map contenant "password" et "nom".
     */
    fun getUser(email: String): Map<String, String>? {
        val users = getAllUsers()
        val userInfo = users[email] ?: return null
        
        val parts = userInfo.split("|")
        return if (parts.size >= 2) {
            mapOf("password" to parts[0], "nom" to parts[1])
        } else {
            null
        }
    }

    /**
     * Vérifie si l'utilisateur existe déjà.
     */
    fun userExists(email: String): Boolean {
        return getAllUsers().containsKey(email)
    }

    private fun getAllUsers(): Map<String, String> {
        val json = prefs.getString(KEY_USERS, null)
        return if (json != null) {
            val type = object : TypeToken<Map<String, String>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyMap()
        }
    }
}
