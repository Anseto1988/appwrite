package com.example.snacktrack.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.snacktrack.ui.components.CommonTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodSubmissionAdminScreen(
    navController: NavController
) {
    val context = LocalContext.current
    
    // Beispiel-Daten für eingereichte Futtermittel
    val submissions = remember {
        listOf(
            FoodSubmission(
                id = "1",
                name = "Royal Canin Adult",
                brand = "Royal Canin",
                barcode = "3182550702119",
                submittedBy = "user@example.com",
                status = SubmissionStatus.PENDING
            ),
            FoodSubmission(
                id = "2",
                name = "Pedigree DentaStix",
                brand = "Pedigree",
                barcode = "5998749109557",
                submittedBy = "test@example.com",
                status = SubmissionStatus.APPROVED
            )
        )
    }
    
    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "Futtermittel-Moderation",
                onBackClick = { navController.navigateUp() }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Statistiken
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            count = submissions.count { it.status == SubmissionStatus.PENDING },
                            label = "Ausstehend",
                            icon = Icons.Default.Schedule
                        )
                        StatItem(
                            count = submissions.count { it.status == SubmissionStatus.APPROVED },
                            label = "Genehmigt",
                            icon = Icons.Default.CheckCircle,
                            color = MaterialTheme.colorScheme.primary
                        )
                        StatItem(
                            count = submissions.count { it.status == SubmissionStatus.REJECTED },
                            label = "Abgelehnt",
                            icon = Icons.Default.Cancel,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            // Filter
            item {
                var selectedFilter by remember { mutableStateOf(SubmissionStatus.PENDING) }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SubmissionStatus.entries.forEach { status ->
                        FilterChip(
                            selected = selectedFilter == status,
                            onClick = { selectedFilter = status },
                            label = { Text(status.displayName) }
                        )
                    }
                }
            }
            
            // Einreichungen
            items(submissions.filter { it.status == SubmissionStatus.PENDING }) { submission ->
                SubmissionCard(
                    submission = submission,
                    onApprove = { /* TODO: Implementieren */ },
                    onReject = { /* TODO: Implementieren */ }
                )
            }
        }
    }
}

@Composable
fun StatItem(
    count: Int,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = color
        )
        Text(
            count.toString(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = color
        )
    }
}

@Composable
fun SubmissionCard(
    submission: FoodSubmission,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        submission.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        submission.brand,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Badge(
                    containerColor = when (submission.status) {
                        SubmissionStatus.PENDING -> MaterialTheme.colorScheme.secondary
                        SubmissionStatus.APPROVED -> MaterialTheme.colorScheme.primary
                        SubmissionStatus.REJECTED -> MaterialTheme.colorScheme.error
                    }
                ) {
                    Text(submission.status.displayName)
                }
            }
            
            // Details
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row {
                    Icon(
                        Icons.Default.QrCode,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        submission.barcode,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Eingereicht von: ${submission.submittedBy}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Aktionen
            if (submission.status == SubmissionStatus.PENDING) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onReject,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ablehnen")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = onApprove
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Genehmigen")
                    }
                }
            }
        }
    }
}

data class FoodSubmission(
    val id: String,
    val name: String,
    val brand: String,
    val barcode: String,
    val submittedBy: String,
    val status: SubmissionStatus
)

enum class SubmissionStatus(val displayName: String) {
    PENDING("Ausstehend"),
    APPROVED("Genehmigt"),
    REJECTED("Abgelehnt")
}