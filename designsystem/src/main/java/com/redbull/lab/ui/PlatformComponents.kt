package com.redbull.lab.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.redbull.lab.ui.theme.Dimensions
import com.redbull.lab.ui.theme.Strings

/** A labeled search input shared by consumer applications. */
@Composable
fun PlatformSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = Strings.Search,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
    )
}

/** Shared loading presentation for platform-backed screens. */
@Composable
fun PlatformLoadingIndicator(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(Dimensions.Dim24dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimensions.Dim8dp),
    ) {
        CircularProgressIndicator()
        Text(Strings.Loading, style = MaterialTheme.typography.bodyMedium)
    }
}

/** Shared empty state with optional supporting text. */
@Composable
fun PlatformEmptyState(
    title: String,
    message: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(Dimensions.Dim16dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        if (message != null) Text(message, style = MaterialTheme.typography.bodyMedium)
    }
}

/** Shared recoverable error state. The consumer supplies its retry action. */
@Composable
fun PlatformErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(Dimensions.Dim16dp)) {
        Text(message, style = MaterialTheme.typography.bodyMedium)
        Button(onClick = onRetry, modifier = Modifier.padding(top = Dimensions.Dim8dp)) {
            Text(Strings.Retry)
        }
    }
}
