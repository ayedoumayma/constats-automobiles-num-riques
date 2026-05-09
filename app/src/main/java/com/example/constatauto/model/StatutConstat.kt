package com.example.constatauto.model

/**
 * Représente le statut actuel d'un constat.
 */
enum class StatutConstat {
    /**
     * Brouillon, non encore soumis à l'assurance. Modifiable.
     */
    EN_COURS,

    /**
     * Soumis à l'assurance. Non modifiable.
     */
    SOUMIS,

    /**
     * Accepté par l'assurance.
     */
    ACCEPTE,

    /**
     * Refusé par l'assurance.
     */
    REFUSE
}
