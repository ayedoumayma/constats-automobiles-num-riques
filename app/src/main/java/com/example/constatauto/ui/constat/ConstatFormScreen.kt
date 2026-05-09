package com.example.constatauto.ui.constat

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.constatauto.model.Conducteur
import com.example.constatauto.model.Constat
import com.example.constatauto.model.StatutConstat
import com.example.constatauto.model.Vehicule
import com.example.constatauto.ui.constat.components.SectionHeader
import com.example.constatauto.ui.constat.components.StepIndicator
import com.example.constatauto.utils.DateUtils
import com.example.constatauto.utils.ValidationUtils
import com.example.constatauto.viewmodel.ConstatViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConstatFormScreen(
    id: String?,
    viewModel: ConstatViewModel,
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var currentStep by remember { mutableStateOf(1) }
    var constat by remember { mutableStateOf(Constat()) }
    var isEditing by remember { mutableStateOf(false) }

    LaunchedEffect(id) {
        if (id != null) {
            val existing = viewModel.getConstatById(id)
            if (existing != null) {
                constat = existing
                isEditing = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Modifier Constat" else "Nouveau Constat") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentStep > 1) {
                    OutlinedButton(onClick = { currentStep-- }) {
                        Text("← Précédent")
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                if (currentStep < 4) {
                    Button(onClick = {
                        val error = validateCurrentStep(currentStep, constat)
                        if (error == null) {
                            currentStep++
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar(error)
                            }
                        }
                    }) {
                        Text("Suivant →")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            StepIndicator(
                currentStep = currentStep,
                totalSteps = 4,
                labels = listOf("Accident", "Véh.A", "Véh.B", "Résumé")
            )

            when (currentStep) {
                1 -> Etape1Accident(constat, onUpdate = { constat = it })
                2 -> Etape2VehiculeA(constat, onUpdate = { constat = it })
                3 -> Etape3VehiculeB(constat, onUpdate = { constat = it })
                4 -> Etape4Resume(
                    constat = constat,
                    onUpdate = { constat = it },
                    onSave = { saveAsStatut ->
                        val finalConstat = constat.copy(
                            statut = saveAsStatut,
                            dateCreation = if (!isEditing) DateUtils.getCurrentTimestamp() else constat.dateCreation,
                            dateSoumission = if (saveAsStatut == StatutConstat.SOUMIS) DateUtils.getCurrentTimestamp() else constat.dateSoumission
                        )

                        val result = if (isEditing) {
                            viewModel.updateConstat(finalConstat)
                        } else {
                            viewModel.addConstat(finalConstat)
                        }

                        result.onSuccess {
                            scope.launch {
                                snackbarHostState.showSnackbar("✅ Constat enregistré avec succès")
                                onSaveSuccess()
                            }
                        }.onFailure {
                            scope.launch {
                                snackbarHostState.showSnackbar("Erreur lors de l'enregistrement")
                            }
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.height(100.dp)) // padding for bottom bar
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Etape1Accident(constat: Constat, onUpdate: (Constat) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    SectionHeader(title = "\uD83D\uDCCD Détails de l'accident")

    // Date Picker
    OutlinedTextField(
        value = constat.dateAccident,
        onValueChange = {},
        label = { Text("Date de l'accident*") },
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = {
                DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        val formattedDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                        onUpdate(constat.copy(dateAccident = formattedDate))
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }) {
                Icon(Icons.Default.CalendarMonth, contentDescription = "Choisir date")
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))

    // Time Picker
    OutlinedTextField(
        value = constat.heureAccident,
        onValueChange = {},
        label = { Text("Heure de l'accident*") },
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = {
                TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        val formattedTime = String.format("%02d:%02d", hourOfDay, minute)
                        onUpdate(constat.copy(heureAccident = formattedTime))
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            }) {
                Icon(Icons.Default.Schedule, contentDescription = "Choisir heure")
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = constat.lieuAccident,
        onValueChange = { onUpdate(constat.copy(lieuAccident = it)) },
        label = { Text("Lieu de l'accident*") },
        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
        modifier = Modifier.fillMaxWidth(),
        minLines = 2,
        maxLines = 3
    )
    Spacer(modifier = Modifier.height(8.dp))

    val gouvernorats = listOf(
        "Tunis", "Ariana", "Ben Arous", "Manouba", "Nabeul", "Zaghouan",
        "Bizerte", "Béja", "Jendouba", "Kef", "Siliana", "Sousse",
        "Monastir", "Mahdia", "Sfax", "Kairouan", "Kasserine",
        "Sidi Bouzid", "Gabès", "Medenine", "Tataouine", "Gafsa",
        "Tozeur", "Kébili"
    )
    var expandedGov by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expandedGov,
        onExpandedChange = { expandedGov = !expandedGov },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = constat.gouvernorat,
            onValueChange = {},
            readOnly = true,
            label = { Text("Gouvernorat*") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGov) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expandedGov,
            onDismissRequest = { expandedGov = false }
        ) {
            gouvernorats.forEach { gov ->
                DropdownMenuItem(
                    text = { Text(gov) },
                    onClick = {
                        onUpdate(constat.copy(gouvernorat = gov))
                        expandedGov = false
                    }
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = constat.descriptionAccident,
        onValueChange = { if (it.length <= 500) onUpdate(constat.copy(descriptionAccident = it)) },
        label = { Text("Description de l'accident*") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 3,
        maxLines = 5,
        supportingText = {
            Text("${constat.descriptionAccident.length}/500", modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.End)
        }
    )
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = constat.degatsVehiculeA,
        onValueChange = { onUpdate(constat.copy(degatsVehiculeA = it)) },
        label = { Text("Dégâts Véhicule A*") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 2
    )
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = constat.degatsVehiculeB,
        onValueChange = { onUpdate(constat.copy(degatsVehiculeB = it)) },
        label = { Text("Dégâts Véhicule B*") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 2
    )
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = constat.temoins,
        onValueChange = { onUpdate(constat.copy(temoins = it)) },
        label = { Text("Témoins (optionnel)") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 2
    )
}

@Composable
fun Etape2VehiculeA(constat: Constat, onUpdate: (Constat) -> Unit) {
    VehiculeConducteurForm(
        titleVehicule = "\uD83D\uDE97 Véhicule A",
        titleConducteur = "\uD83D\uDC64 Conducteur A (Vous)",
        vehicule = constat.vehiculeA,
        conducteur = constat.conducteurA,
        onVehiculeUpdate = { onUpdate(constat.copy(vehiculeA = it)) },
        onConducteurUpdate = { onUpdate(constat.copy(conducteurA = it)) }
    )
}

@Composable
fun Etape3VehiculeB(constat: Constat, onUpdate: (Constat) -> Unit) {
    VehiculeConducteurForm(
        titleVehicule = "\uD83D\uDE97 Véhicule B",
        titleConducteur = "\uD83D\uDC64 Conducteur B (Autre partie)",
        vehicule = constat.vehiculeB,
        conducteur = constat.conducteurB,
        onVehiculeUpdate = { onUpdate(constat.copy(vehiculeB = it)) },
        onConducteurUpdate = { onUpdate(constat.copy(conducteurB = it)) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehiculeConducteurForm(
    titleVehicule: String,
    titleConducteur: String,
    vehicule: Vehicule,
    conducteur: Conducteur,
    onVehiculeUpdate: (Vehicule) -> Unit,
    onConducteurUpdate: (Conducteur) -> Unit
) {
    val context = LocalContext.current
    
    SectionHeader(title = titleVehicule)

    OutlinedTextField(
        value = vehicule.immatriculation,
        onValueChange = { onVehiculeUpdate(vehicule.copy(immatriculation = it.uppercase())) },
        label = { Text("Immatriculation*") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = vehicule.marque,
            onValueChange = { onVehiculeUpdate(vehicule.copy(marque = it)) },
            label = { Text("Marque*") },
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = vehicule.modele,
            onValueChange = { onVehiculeUpdate(vehicule.copy(modele = it)) },
            label = { Text("Modèle*") },
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(modifier = Modifier.height(8.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = if (vehicule.annee > 0) vehicule.annee.toString() else "",
            onValueChange = { onVehiculeUpdate(vehicule.copy(annee = it.toIntOrNull() ?: 0)) },
            label = { Text("Année*") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = vehicule.couleur,
            onValueChange = { onVehiculeUpdate(vehicule.copy(couleur = it)) },
            label = { Text("Couleur*") },
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(modifier = Modifier.height(8.dp))

    val typesVehicule = listOf("Berline", "Break", "SUV / 4x4", "Coupé", "Cabriolet", "Monospace", "Utilitaire", "Camion", "Moto", "Autre")
    var expandedType by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expandedType,
        onExpandedChange = { expandedType = !expandedType },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = vehicule.typeVehicule,
            onValueChange = {},
            readOnly = true,
            label = { Text("Type de véhicule*") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expandedType,
            onDismissRequest = { expandedType = false }
        ) {
            typesVehicule.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type) },
                    onClick = {
                        onVehiculeUpdate(vehicule.copy(typeVehicule = type))
                        expandedType = false
                    }
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
    SectionHeader(title = titleConducteur)

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = conducteur.prenom,
            onValueChange = { onConducteurUpdate(conducteur.copy(prenom = it)) },
            label = { Text("Prénom*") },
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = conducteur.nom,
            onValueChange = { onConducteurUpdate(conducteur.copy(nom = it)) },
            label = { Text("Nom*") },
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(modifier = Modifier.height(8.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = conducteur.telephone,
            onValueChange = { onConducteurUpdate(conducteur.copy(telephone = it)) },
            label = { Text("Téléphone*") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = conducteur.cin,
            onValueChange = { onConducteurUpdate(conducteur.copy(cin = it)) },
            label = { Text("Numéro CIN*") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = conducteur.email,
        onValueChange = { onConducteurUpdate(conducteur.copy(email = it)) },
        label = { Text("Email*") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = conducteur.permisNumero,
            onValueChange = { onConducteurUpdate(conducteur.copy(permisNumero = it)) },
            label = { Text("N° Permis*") },
            modifier = Modifier.weight(1.5f)
        )

        val categoriesPermis = listOf("A", "A1", "B", "C", "C1", "D", "D1", "EB", "EC")
        var expandedCat by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = expandedCat,
            onExpandedChange = { expandedCat = !expandedCat },
            modifier = Modifier.weight(1f)
        ) {
            OutlinedTextField(
                value = conducteur.permisCategorie,
                onValueChange = {},
                readOnly = true,
                label = { Text("Catégorie*") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCat) },
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expandedCat,
                onDismissRequest = { expandedCat = false }
            ) {
                categoriesPermis.forEach { cat ->
                    DropdownMenuItem(
                        text = { Text(cat) },
                        onClick = {
                            onConducteurUpdate(conducteur.copy(permisCategorie = cat))
                            expandedCat = false
                        }
                    )
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = conducteur.permisDelivreA,
        onValueChange = { onConducteurUpdate(conducteur.copy(permisDelivreA = it)) },
        label = { Text("Permis délivré à (ville)*") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = conducteur.compagnieAssurance,
        onValueChange = { onConducteurUpdate(conducteur.copy(compagnieAssurance = it)) },
        label = { Text("Compagnie d'assurance*") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))
    
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = conducteur.numeroPolice,
            onValueChange = { onConducteurUpdate(conducteur.copy(numeroPolice = it)) },
            label = { Text("N° de police*") },
            modifier = Modifier.weight(1f)
        )
        
        OutlinedTextField(
            value = conducteur.dateExpirationAssurance,
            onValueChange = {},
            label = { Text("Expiration*") },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = {
                    val cal = Calendar.getInstance()
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            val formattedDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                            onConducteurUpdate(conducteur.copy(dateExpirationAssurance = formattedDate))
                        },
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = "Choisir date")
                }
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun Etape4Resume(constat: Constat, onUpdate: (Constat) -> Unit, onSave: (StatutConstat) -> Unit) {
    var selectedStatut by remember { mutableStateOf(StatutConstat.EN_COURS) }

    SectionHeader(title = "📝 Résumé & Confirmation")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("📍 Accident", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("${constat.dateAccident} à ${constat.heureAccident}")
            Text("${constat.lieuAccident}, ${constat.gouvernorat}")
            Spacer(modifier = Modifier.height(8.dp))
            Text("Description:", fontWeight = FontWeight.SemiBold)
            Text(constat.descriptionAccident, maxLines = 3, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("🚗 Véhicule A — ${constat.vehiculeA.immatriculation}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("${constat.vehiculeA.marque} ${constat.vehiculeA.modele} (${constat.vehiculeA.annee})")
            Text("Conducteur: ${constat.conducteurA.prenom} ${constat.conducteurA.nom}")
            Text("Assurance: ${constat.conducteurA.compagnieAssurance}")
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("🚗 Véhicule B — ${constat.vehiculeB.immatriculation}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("${constat.vehiculeB.marque} ${constat.vehiculeB.modele} (${constat.vehiculeB.annee})")
            Text("Conducteur: ${constat.conducteurB.prenom} ${constat.conducteurB.nom}")
            Text("Assurance: ${constat.conducteurB.compagnieAssurance}")
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
    Text("Statut initial :", fontWeight = FontWeight.Bold)
    Row(verticalAlignment = Alignment.CenterVertically) {
        RadioButton(
            selected = selectedStatut == StatutConstat.EN_COURS,
            onClick = { selectedStatut = StatutConstat.EN_COURS }
        )
        Text("⚪ Enregistrer comme brouillon (EN_COURS)")
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        RadioButton(
            selected = selectedStatut == StatutConstat.SOUMIS,
            onClick = { selectedStatut = StatutConstat.SOUMIS }
        )
        Text("🔵 Soumettre maintenant (SOUMIS)")
    }

    if (selectedStatut == StatutConstat.SOUMIS) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(
                "ℹ️ Une fois soumis, le constat ne pourra plus être modifié.",
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
    Button(
        onClick = { onSave(selectedStatut) },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Text("💾 Enregistrer le constat", fontSize = 16.sp)
    }
}

private fun validateCurrentStep(step: Int, constat: Constat): String? {
    return when (step) {
        1 -> {
            if (constat.dateAccident.isBlank()) "Date de l'accident requise"
            else if (constat.heureAccident.isBlank()) "Heure de l'accident requise"
            else if (constat.lieuAccident.isBlank()) "Lieu de l'accident requis"
            else if (constat.gouvernorat.isBlank()) "Gouvernorat requis"
            else if (constat.descriptionAccident.isBlank()) "Description requise"
            else if (constat.degatsVehiculeA.isBlank()) "Dégâts Véhicule A requis"
            else if (constat.degatsVehiculeB.isBlank()) "Dégâts Véhicule B requis"
            else null
        }
        2 -> {
            val v = constat.vehiculeA
            val c = constat.conducteurA
            if (v.immatriculation.isBlank()) "Immatriculation Véhicule A requise"
            else if (v.marque.isBlank()) "Marque Véhicule A requise"
            else if (v.modele.isBlank()) "Modèle Véhicule A requis"
            else if (v.annee < 1900 || v.annee > 2025) "Année Véhicule A invalide"
            else if (c.nom.isBlank() || c.prenom.isBlank()) "Nom et Prénom Conducteur A requis"
            else if (!ValidationUtils.isValidPhone(c.telephone)) "Téléphone Conducteur A invalide"
            else if (!ValidationUtils.isValidCIN(c.cin)) "CIN Conducteur A invalide (8 chiffres)"
            else if (c.compagnieAssurance.isBlank()) "Compagnie d'assurance A requise"
            else null
        }
        3 -> {
            val v = constat.vehiculeB
            val c = constat.conducteurB
            if (v.immatriculation.isBlank()) "Immatriculation Véhicule B requise"
            else if (v.marque.isBlank()) "Marque Véhicule B requise"
            else if (v.modele.isBlank()) "Modèle Véhicule B requis"
            else if (v.annee < 1900 || v.annee > 2025) "Année Véhicule B invalide"
            else if (c.nom.isBlank() || c.prenom.isBlank()) "Nom et Prénom Conducteur B requis"
            else if (!ValidationUtils.isValidPhone(c.telephone)) "Téléphone Conducteur B invalide"
            else if (!ValidationUtils.isValidCIN(c.cin)) "CIN Conducteur B invalide (8 chiffres)"
            else if (c.compagnieAssurance.isBlank()) "Compagnie d'assurance B requise"
            else null
        }
        else -> null
    }
}
