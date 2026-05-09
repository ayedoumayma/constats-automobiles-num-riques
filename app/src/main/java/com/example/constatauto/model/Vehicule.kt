package com.example.constatauto.model

/**
 * Représente un véhicule impliqué dans l'accident.
 *
 * @property immatriculation Numéro d'immatriculation (format tunisien).
 * @property marque Marque du véhicule (ex: Toyota, Peugeot).
 * @property modele Modèle du véhicule (ex: Corolla, 208).
 * @property annee Année de fabrication.
 * @property couleur Couleur du véhicule.
 * @property typeVehicule Type de carrosserie (Berline, SUV, etc.).
 */
data class Vehicule(
    val immatriculation: String = "",
    val marque: String = "",
    val modele: String = "",
    val annee: Int = 2020,
    val couleur: String = "",
    val typeVehicule: String = ""
)
