package com.example.secretsanta.ui.feature.messaging

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.secretsanta.R
import com.example.secretsanta.ui.navigation.Screen
import androidx.navigation.NavController
import androidx.compose.material3.ExperimentalMaterial3Api


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagingScreen(
    navController: NavController,
    viewModel: MessagingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(

            title = { Text(stringResource(R.string.messaging_title)) }
        )

        state.info?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(16.dp)
            )
        }

        if (state.isLoading) {
            Box(Modifier.fillMaxSize()) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
            return
        }

        state.error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp)
            )
        }

        if (state.conversations.isEmpty()) {
            Box(Modifier.fillMaxSize()) {
                Text(
                    text = stringResource(R.string.messaging_empty),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            return
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(state.conversations) { c ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = LocalIndication.current
                        ) {
                            navController.navigate(Screen.Chat.createRoute(c.id))
                        }
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(text = c.title, style = MaterialTheme.typography.titleMedium)
                        if (c.lastMessage != null) {
                            Spacer(Modifier.height(6.dp))
                            Text(text = c.lastMessage, style = MaterialTheme.typography.bodyMedium)
                        } else {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = stringResource(R.string.messaging_no_message_yet),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
