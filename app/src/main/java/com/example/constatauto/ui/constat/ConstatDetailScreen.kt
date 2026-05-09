package com.example.constatauto.ui.constat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.constatauto.model.Conducteur
import com.example.constatauto.model.Constat
import com.example.constatauto.model.StatutConstat
import com.example.constatauto.model.Vehicule
import com.example.constatauto.ui.constat.components.StatusBadge
import com.example.constatauto.viewmodel.ConstatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConstatDetailScreen(
    id: String,
    viewModel: ConstatViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit
) {
    val constat = viewModel.getConstatById(id)

    if (constat == null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            Text("Constat introuvable")
            TextButton(onClick = onNavigateBack) { Text("Retour") }
        }
        return
    }

    var menuExpanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showStatusDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Supprimer le constat ?") },
            text = { Text("Cette action est irréversible. Le constat ${constat.numeroConstat} sera définitivement supprimé.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteConstat(constat.id)
                    showDeleteDialog = false
                    onNavigateBack()
                }, colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Annuler") }
            }
        )
    }

    if (showStatusDialog) {
        var tempStatut by remember { mutableStateOf(constat.statut) }
        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            title = { Text("Modifier le statut") },
            text = {
                Column {
                    StatutOption(StatutConstat.EN_COURS, "En cours (brouillon)", tempStatut) { tempStatut = it }
                    StatutOption(StatutConstat.SOUMIS, "Soumis à l'assurance", tempStatut) { tempStatut = it }
                    StatutOption(StatutConstat.ACCEPTE, "Accepté", tempStatut) { tempStatut = it }
                    StatutOption(StatutConstat.REFUSE, "Refusé", tempStatut) { tempStatut = it }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateStatut(constat.id, tempStatut)
                    showStatusDialog = false
                }) { Text("Confirmer") }
            },
            dismissButton = {
                TextButton(onClick = { showStatusDialog = false }) { Text("Annuler") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(constat.numeroConstat) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("✏ Modifier") },
                            onClick = {
                                menuExpanded = false
                                onNavigateToEdit(constat.id)
                            },
                            enabled = constat.statut == StatutConstat.EN_COURS
                        )
                        DropdownMenuItem(
                            text = { Text("🗑 Supprimer") },
                            onClick = {
                                menuExpanded = false
                                showDeleteDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("📤 Changer le statut") },
                            onClick = {
                                menuExpanded = false
                                showStatusDialog = true
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                StatusBadge(statut = constat.statut)
            }

            ExpandableSection(title = "Informations de l'accident") {
                InfoRow("Date & heure", "${constat.dateAccident} à ${constat.heureAccident}")
                InfoRow("Lieu", "${constat.lieuAccident}, ${constat.gouvernorat}")
                InfoRow("Description", constat.descriptionAccident)
                InfoRow("Dégâts A", constat.degatsVehiculeA)
                InfoRow("Dégâts B", constat.degatsVehiculeB)
                if (constat.temoins.isNotEmpty()) {
                    InfoRow("Témoins", constat.temoins)
                }
            }

            ExpandableSection(title = "Véhicule A — ${constat.vehiculeA.immatriculation}") {
                VehiculeInfo(constat.vehiculeA)
            }

            ExpandableSection(title = "Conducteur A — ${constat.conducteurA.prenom} ${constat.conducteurA.nom}") {
                ConducteurInfo(constat.conducteurA)
            }

            ExpandableSection(title = "Véhicule B — ${constat.vehiculeB.immatriculation}") {
                VehiculeInfo(constat.vehiculeB)
            }

            ExpandableSection(title = "Conducteur B — ${constat.conducteurB.prenom} ${constat.conducteurB.nom}") {
                ConducteurInfo(constat.conducteurB)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Créé le : ${constat.dateCreation}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                if (constat.dateSoumission != null) {
                    Text("Soumis le : ${constat.dateSoumission}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun StatutOption(statut: StatutConstat, label: String, selectedStatut: StatutConstat, onSelect: (StatutConstat) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(statut) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selectedStatut == statut, onClick = { onSelect(statut) })
        Text(label)
    }
}

@Composable
fun ExpandableSection(title: String, content: @Composable () -> Unit) {
    var expanded by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Réduire" else "Étendre"
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))
                    content()
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun VehiculeInfo(vehicule: Vehicule) {
    InfoRow("Marque / Modèle", "${vehicule.marque} ${vehicule.modele}")
    InfoRow("Année", vehicule.annee.toString())
    InfoRow("Couleur", vehicule.couleur)
    InfoRow("Type", vehicule.typeVehicule)
}

@Composable
fun ConducteurInfo(conducteur: Conducteur) {
    InfoRow("CIN", conducteur.cin)
    InfoRow("Téléphone", conducteur.telephone)
    InfoRow("Email", conducteur.email)
    InfoRow("Permis", "${conducteur.permisNumero} (Catégorie ${conducteur.permisCategorie}) - Délivré à ${conducteur.permisDelivreA}")
    InfoRow("Assurance", "${conducteur.compagnieAssurance} (Police: ${conducteur.numeroPolice})")
    InfoRow("Expiration assurance", conducteur.dateExpirationAssurance)
}
