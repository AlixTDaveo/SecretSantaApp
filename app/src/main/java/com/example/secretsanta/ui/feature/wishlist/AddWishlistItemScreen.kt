package com.example.secretsanta.ui.feature.wishlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.secretsanta.domain.model.ProductSuggestion
import com.example.secretsanta.ui.components.SnowfallBackground
import com.example.secretsanta.ui.theme.ChristmasColors
import androidx.compose.ui.res.stringResource
import com.example.secretsanta.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWishlistItemScreen(
    navController: NavController,
    viewModel: AddWishlistItemViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    // Navigation après succès
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            navController.popBackStack()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fond parchemin
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFF5E6D3), Color(0xFFE8D5B7))
                    )
                )
        ) {
            SnowfallBackground(snowflakeCount = 50, snowColor = Color.White.copy(alpha = 0.5f))
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("✨")
                            Text(
                                stringResource(R.string.add_wishlist_item_title),
                                fontFamily = FontFamily.Cursive,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = ChristmasColors.AppBackground,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ========== AJOUT MANUEL ==========
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("🦌", fontSize = 24.sp)
                                Text(stringResource(R.string.write_to_santa_title),

                                fontSize = 18.sp,
                                    fontFamily = FontFamily.Cursive,
                                    fontWeight = FontWeight.Bold,
                                    color = ChristmasColors.AppBackground
                                )
                            }

                            Spacer(Modifier.height(16.dp))

                            OutlinedTextField(
                                value = state.title,
                                onValueChange = { viewModel.onEvent(AddWishlistItemEvent.TitleChanged(it)) },
                                label = { Text(stringResource(R.string.wishlist_item_label)) },
                                placeholder = { Text(stringResource(R.string.wishlist_item_placeholder)) },

                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                leadingIcon = { Text("🎁", fontSize = 18.sp) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ChristmasColors.AppBackground,
                                    focusedLabelColor = ChristmasColors.AppBackground
                                )
                            )

                            Spacer(Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = state.price,
                                    onValueChange = { viewModel.onEvent(AddWishlistItemEvent.PriceChanged(it)) },
                                    label = { Text(stringResource(R.string.price_label)) } ,
                                    placeholder = { Text(stringResource(R.string.price_placeholder)) },

                                            modifier = Modifier.weight(0.4f),
                                    singleLine = true,
                                    leadingIcon = { Text("💰", fontSize = 16.sp) },
                                    suffix = { Text("€") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ChristmasColors.AppBackground,
                                        focusedLabelColor = ChristmasColors.AppBackground
                                    )
                                )

                                OutlinedTextField(
                                    value = state.link,
                                    onValueChange = { viewModel.onEvent(AddWishlistItemEvent.LinkChanged(it)) },
                                    label = { Text(stringResource(R.string.link_label)) },
                                    placeholder = { Text("https://...") },
                                    modifier = Modifier.weight(0.6f),
                                    singleLine = true,
                                    leadingIcon = { Text("🔗", fontSize = 16.sp) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ChristmasColors.AppBackground,
                                        focusedLabelColor = ChristmasColors.AppBackground
                                    )
                                )
                            }

                            Spacer(Modifier.height(16.dp))

                            Button(
                                onClick = { viewModel.onEvent(AddWishlistItemEvent.AddManualItem) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = state.title.isNotBlank() && !state.isLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = ChristmasColors.AppButtonRed),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(vertical = 14.dp)
                            ) {
                                if (state.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("🎄", fontSize = 20.sp)
                                    Spacer(Modifier.width(8.dp))
                                    Text(stringResource(R.string.add_to_my_list_button), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                        }
                    }
                }

                // ========== RECHERCHE ==========
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFECB3).copy(alpha = 0.95f)),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("🔍", fontSize = 24.sp)
                                Text(
                                    stringResource(R.string.search_product_title),
                                    fontSize = 18.sp,
                                    fontFamily = FontFamily.Cursive,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF6F00)
                                )
                            }

                            Spacer(Modifier.height(8.dp))
                            Text(
                                stringResource(R.string.search_examples_hint),                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF666666)
                            )

                            Spacer(Modifier.height(12.dp))

                            OutlinedTextField(
                                value = state.searchQuery,
                                onValueChange = { viewModel.onEvent(AddWishlistItemEvent.SearchQueryChanged(it)) },
                                label = { Text(stringResource(R.string.search_label)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFFFF6F00)) },
                                trailingIcon = {
                                    if (state.searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { viewModel.onEvent(AddWishlistItemEvent.ClearSearch) }) {
                                            Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.clear))
                                        }
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFFF6F00),
                                    focusedLabelColor = Color(0xFFFF6F00)
                                )
                            )

                            Spacer(Modifier.height(8.dp))

                            when {
                                state.searchQuery.length < 2 -> {
                                    Text(
                                        text = stringResource(R.string.search_min_chars),
                                        color = Color.Gray
                                    )
                                }

                                state.isSearching -> {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = Color(0xFFFF6F00)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = stringResource(R.string.search_in_progress),
                                            color = Color.Gray
                                        )
                                    }
                                }

                                state.noResults -> {
                                    Text(
                                        text = stringResource(R.string.search_no_results),
                                        color = ChristmasColors.AppButtonRed
                                    )
                                }

                                state.suggestions.isNotEmpty() -> {
                                    Text(
                                        text = stringResource(
                                            R.string.search_results_count,
                                            state.suggestions.size
                                        ),
                                        color = Color(0xFF4CAF50),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                        }
                    }
                }

                // ========== RÉSULTATS ==========
                if (state.suggestions.isNotEmpty()) {
                    item {
                        Text(stringResource(R.string.click_to_add_hint), fontSize = 16.sp, fontFamily = FontFamily.Cursive, fontWeight = FontWeight.Bold, color = Color(0xFF5D4037))
                    }

                    items(state.suggestions) { suggestion ->
                        ProductCard(
                            suggestion = suggestion,
                            isAdded = suggestion.id in state.addedSuggestionIds,
                            onAdd = { viewModel.onEvent(AddWishlistItemEvent.AddFromSuggestion(suggestion)) }
                        )
                    }
                }

                // ========== ERREUR ==========
                if (state.error != null) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = ChristmasColors.AppButtonRed.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("⚠️", fontSize = 24.sp)
                                Spacer(Modifier.width(12.dp))
                                Text(state.error!!, color = ChristmasColors.AppButtonRed, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
private fun ProductCard(suggestion: ProductSuggestion, isAdded: Boolean, onAdd: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = if (isAdded) Color(0xFFE8F5E9) else Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))) {
                AsyncImage(model = suggestion.imageUrl, contentDescription = null, modifier = Modifier.size(65.dp).padding(4.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(suggestion.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, maxLines = 2)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.product_price_eur, suggestion.price.toInt()), fontWeight = FontWeight.Bold, color = ChristmasColors.AppButtonRed)
                    Surface(color = Color(0xFFE0E0E0), shape = RoundedCornerShape(4.dp)) {
                        Text(suggestion.category, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }
            }

            if (isAdded) {

                Icon(Icons.Default.CheckCircle, contentDescription = stringResource(R.string.added), tint = Color(0xFF4CAF50), modifier = Modifier.size(36.dp))
            } else {
                IconButton(onClick = onAdd) {
                    Icon(Icons.Default.AddCircle, contentDescription = stringResource(R.string.add), tint = ChristmasColors.AppBackground, modifier = Modifier.size(36.dp))
                }
            }
        }
    }
}