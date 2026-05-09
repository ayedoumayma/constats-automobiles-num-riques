package com.example.constatauto.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.constatauto.model.Constat
import com.example.constatauto.model.StatutConstat
import com.example.constatauto.ui.constat.components.ConstatCard
import com.example.constatauto.viewmodel.AuthViewModel
import com.example.constatauto.viewmodel.ConstatViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ConstatViewModel,
    authViewModel: AuthViewModel,
    onNavigateToForm: (String?) -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onLogout: () -> Unit
) {
    val constats by viewModel.filteredConstats.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedStatut by viewModel.selectedStatut.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }
    var constatToDelete by remember { mutableStateOf<Constat?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Déconnexion") },
            text = { Text("Êtes-vous sûr de vouloir vous déconnecter ?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogout()
                }) { Text("Déconnexion") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Annuler") }
            }
        )
    }

    if (constatToDelete != null) {
        AlertDialog(
            onDismissRequest = { constatToDelete = null },
            title = { Text("Supprimer le constat ?") },
            text = { Text("Cette action est irréversible. Le constat ${constatToDelete?.numeroConstat} sera définitivement supprimé.") },
            confirmButton = {
                TextButton(onClick = {
                    val id = constatToDelete?.id
                    if (id != null) {
                        viewModel.deleteConstat(id)
                        scope.launch { snackbarHostState.showSnackbar("Constat supprimé") }
                    }
                    constatToDelete = null
                }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { constatToDelete = null }) { Text("Annuler") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Mes Constats", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                        if (currentUser != null) {
                            Text(currentUser ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Default.Logout, contentDescription = "Déconnexion", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToForm(null) },
                containerColor = MaterialTheme.colorScheme.secondary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nouveau constat", tint = Color.White)
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Dashboard Header Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem("Total", stats["total"] ?: 0)
                        StatItem("En cours", stats["en_cours"] ?: 0)
                        StatItem("Soumis", stats["soumis"] ?: 0)
                        StatItem("Acceptés", stats["acceptes"] ?: 0)
                    }
                }
            }

            // Search and Filter
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    placeholder = { Text("Rechercher (N°, Lieu, Nom...)") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp)),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedStatut == null,
                            onClick = { viewModel.selectedStatut.value = null },
                            label = { Text("Tous") }
                        )
                    }
                    items(StatutConstat.values()) { statut ->
                        FilterChip(
                            selected = selectedStatut == statut,
                            onClick = { viewModel.selectedStatut.value = statut },
                            label = {
                                Text(
                                    when (statut) {
                                        StatutConstat.EN_COURS -> "En cours"
                                        StatutConstat.SOUMIS -> "Soumis"
                                        StatutConstat.ACCEPTE -> "Accepté"
                                        StatutConstat.REFUSE -> "Refusé"
                                    }
                                )
                            }
                        )
                    }
                }
            }

            // List Header
            Text(
                text = "Historique des constats (${constats.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // List
            if (constats.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Aucun constat trouvé",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray
                        )
                        if (stats["total"] == 0) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { onNavigateToForm(null) }) {
                                Text("Créer votre premier constat")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp) // padding for FAB
                ) {
                    items(constats) { constat ->
                        ConstatCard(
                            constat = constat,
                            onViewClick = { onNavigateToDetail(constat.id) },
                            onEditClick = {
                                if (constat.statut == StatutConstat.EN_COURS) {
                                    onNavigateToForm(constat.id)
                                } else {
                                    scope.launch { snackbarHostState.showSnackbar("Constat verrouillé — non modifiable") }
                                }
                            },
                            onDeleteClick = { constatToDelete = constat }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, count: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}
