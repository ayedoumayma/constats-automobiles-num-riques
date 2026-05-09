package com.example.constatauto.model

/**
 * Représente un conducteur impliqué dans l'accident.
 *
 * @property nom Nom de famille.
 * @property prenom Prénom.
 * @property telephone Numéro de téléphone (format tunisien).
 * @property email Adresse email.
 * @property cin Numéro de Carte d'Identité Nationale (8 chiffres).
 * @property permisNumero Numéro du permis de conduire.
 * @property permisCategorie Catégorie du permis (A, B, C, etc.).
 * @property permisDelivreA Ville de délivrance du permis.
 * @property compagnieAssurance Nom de la compagnie d'assurance.
 * @property numeroPolice Numéro de police d'assurance.
 * @property dateExpirationAssurance Date d'expiration de l'assurance.
 */
data class Conducteur(
    val nom: String = "",
    val prenom: String = "",
    val telephone: String = "",
    val email: String = "",
    val cin: String = "",
    val permisNumero: String = "",
    val permisCategorie: String = "",
    val permisDelivreA: String = "",
    val compagnieAssurance: String = "",
    val numeroPolice: String = "",
    val dateExpirationAssurance: String = ""
)
