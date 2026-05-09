package com.example.constatauto.model

import java.util.UUID

/**
 * Représente un constat amiable d'accident de la route.
 *
 * @property id Identifiant unique interne.
 * @property numeroConstat Numéro auto-généré (ex: "CST-2024-0001").
 * @property dateAccident Date de l'accident (dd/MM/yyyy).
 * @property heureAccident Heure de l'accident (HH:mm).
 * @property lieuAccident Adresse ou lieu exact de l'accident.
 * @property gouvernorat Gouvernorat tunisien (ex: "Tunis", "Sfax").
 * @property descriptionAccident Description détaillée des circonstances.
 * @property degatsVehiculeA Description des dégâts sur le véhicule A.
 * @property degatsVehiculeB Description des dégâts sur le véhicule B.
 * @property temoins Liste ou noms des témoins éventuels.
 * @property conducteurA Informations du conducteur A (utilisateur).
 * @property vehiculeA Informations du véhicule A.
 * @property conducteurB Informations du conducteur B (autre partie).
 * @property vehiculeB Informations du véhicule B.
 * @property statut Statut actuel du constat (En cours, Soumis, etc.).
 * @property dateCreation Date et heure de création du constat.
 * @property dateSoumission Date et heure de soumission (si applicable).
 */
data class Constat(
    val id: String = UUID.randomUUID().toString(),
    val numeroConstat: String = "",
    val dateAccident: String = "",
    val heureAccident: String = "",
    val lieuAccident: String = "",
    val gouvernorat: String = "",
    val descriptionAccident: String = "",
    val degatsVehiculeA: String = "",
    val degatsVehiculeB: String = "",
    val temoins: String = "",
    val conducteurA: Conducteur = Conducteur(),
    val vehiculeA: Vehicule = Vehicule(),
    val conducteurB: Conducteur = Conducteur(),
    val vehiculeB: Vehicule = Vehicule(),
    val statut: StatutConstat = StatutConstat.EN_COURS,
    val dateCreation: String = "",
    val dateSoumission: String? = null
)
