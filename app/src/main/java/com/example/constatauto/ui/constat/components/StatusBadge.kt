package com.example.constatauto.ui.constat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.constatauto.model.StatutConstat
import com.example.constatauto.ui.theme.StatusAccepte
import com.example.constatauto.ui.theme.StatusEnCours
import com.example.constatauto.ui.theme.StatusRefuse
import com.example.constatauto.ui.theme.StatusSoumis

@Composable
fun StatusBadge(statut: StatutConstat, modifier: Modifier = Modifier) {
    val backgroundColor = when (statut) {
        StatutConstat.EN_COURS -> StatusEnCours
        StatutConstat.SOUMIS -> StatusSoumis
        StatutConstat.ACCEPTE -> StatusAccepte
        StatutConstat.REFUSE -> StatusRefuse
    }
    
    val text = when (statut) {
        StatutConstat.EN_COURS -> "EN COURS \uD83DF\uDFE4" // Orange circle
        StatutConstat.SOUMIS -> "SOUMIS \uD83DF\uDFE3" // Blue circle
        StatutConstat.ACCEPTE -> "ACCEPTÉ ✅"
        StatutConstat.REFUSE -> "REFUSÉ ❌"
    }

    Text(
        text = text,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}
