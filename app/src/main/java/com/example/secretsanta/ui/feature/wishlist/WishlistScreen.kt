package com.example.secretsanta.ui.feature.wishlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.secretsanta.domain.model.WishlistItem
import com.example.secretsanta.ui.components.SnowfallBackground
import com.example.secretsanta.ui.navigation.Screen
import com.example.secretsanta.ui.theme.ChristmasColors
import coil.compose.AsyncImage
import com.example.secretsanta.R



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistScreen(
    navController: NavController,
    viewModel: WishlistViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        // Fond parchemin avec flocons
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ChristmasColors.AuthBackground
//                    Brush.verticalGradient(
//                        colors = listOf(
//                            Color(0xFFF5E6D3),
//                            Color(0xFFE8D5B7)
//                        )
//                    )
                )
        ) {
            SnowfallBackground(
                snowflakeCount = 60,
                snowColor = Color.White.copy(alpha = 0.6f)
            )
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🎅")
                            Text(
                                text = if (state.isViewingOtherUser)
                                    stringResource(R.string.wishlist_title_other)
                                else
                                    stringResource(R.string.wishlist_title_mine),

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
                        containerColor = Color(0xFFD32F2F),
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            },
            floatingActionButton = {
                // FAB uniquement pour le propriétaire
                if (!state.isViewingOtherUser) {
                    FloatingActionButton(
                        onClick = { navController.navigate(Screen.AddWishlistItem.route) },
                        containerColor = ChristmasColors.AppButtonRed,
                        contentColor = Color.White,
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add), modifier = Modifier.size(28.dp))
                    }
                }
            },
            containerColor = Color.Transparent
        ) { padding ->
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = ChristmasColors.AppButtonRed)
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // ========== EN-TÊTE ==========
                        item {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White.copy(alpha = 0.95f)
                                ),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = stringResource(R.string.wishlist_dear_santa),
                                        fontSize = 24.sp,
                                        fontFamily = FontFamily.Cursive,
                                        fontWeight = FontWeight.Bold,
                                        color = ChristmasColors.AppButtonRed
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = if (state.isViewingOtherUser)
                                            stringResource(R.string.wishlist_intro_other)
                                        else
                                            stringResource(R.string.wishlist_intro_mine)
                                        ,
                                        fontSize = 16.sp,
                                        fontFamily = FontFamily.Cursive,
                                        textAlign = TextAlign.Center,
                                        color = Color(0xFF5D4037)
                                    )
                                }
                            }
                        }

                        // ========== TITRE LISTE ==========
                        item {
                            Text(
                                text = if (state.isViewingOtherUser)
                                    stringResource(R.string.wishlist_list_title_other, state.items.size)
                                else
                                    stringResource(R.string.wishlist_list_title_mine, state.items.size),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Cursive,
                                color = ChristmasColors.White
                            )
                        }

                        // ========== LISTE VIDE ==========
                        if (state.items.isEmpty()) {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White.copy(alpha = 0.9f)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(text = "🎄", fontSize = 48.sp)
                                        Spacer(Modifier.height(12.dp))
                                        Text(
                                            text = if (state.isViewingOtherUser)
                                                stringResource(R.string.wishlist_empty_other)
                                            else
                                                stringResource(R.string.wishlist_empty_mine),
                                            textAlign = TextAlign.Center,
                                            color = Color(0xFF5D4037),
                                            fontStyle = FontStyle.Italic,
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            }
                        }

                        // ========== LISTE DES ITEMS ==========
                        items(state.items) { item ->
                            // IMPORTANT : Le propriétaire ne voit JAMAIS les infos de réservation
                            // Seul le parrain (isViewingOtherUser = true) voit les réservations
                            val isReservedByMe = state.isViewingOtherUser && item.reservedBy == state.currentUserId
                            val isReservedByOther = state.isViewingOtherUser && item.reservedBy != null && item.reservedBy != state.currentUserId

                            WishlistItemCard(
                                item = item,
                                isOwner = !state.isViewingOtherUser,
                                isReservedByMe = isReservedByMe,
                                isReservedByOther = isReservedByOther,
                                onDelete = { viewModel.onEvent(WishlistEvent.DeleteItem(item.id)) },
                                onToggleReservation = { viewModel.onEvent(WishlistEvent.ToggleReservation(item.id)) }
                            )
                        }

                        // ========== FOOTER ==========
                        item {
                            Text(
                                text = stringResource(R.string.wishlist_footer_signed),
                                fontSize = 16.sp,
                                fontFamily = FontFamily.Cursive,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                                color = ChristmasColors.White
                            )
                            Spacer(Modifier.height(80.dp))
                        }
                    }
                }
            }
        }

        // ========== ERREUR ==========
        if (state.error != null) {
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.onEvent(WishlistEvent.DismissError) }) {
                        Text(stringResource(R.string.ok), color = Color.White)

                    }
                },
                containerColor = ChristmasColors.AppButtonRed
            ) {
                Text(state.error ?: "", color = Color.White)
            }
        }
    }
}

// ========== CARTE ITEM ==========
@Composable
private fun WishlistItemCard(
    item: WishlistItem,
    isOwner: Boolean,
    isReservedByMe: Boolean,
    isReservedByOther: Boolean,
    onDelete: () -> Unit,
    onToggleReservation: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = when {
                // Couleurs uniquement pour le parrain
                isReservedByMe -> Color(0xFFE8F5E9)      // Vert clair
                isReservedByOther -> Color(0xFFFFEBEE)   // Rouge clair
                else -> Color.White.copy(alpha = 0.95f)
            }
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (item.imageUrl != null) {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF5F5F5)
                    )
                ) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.size(65.dp).padding(4.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    // Barré uniquement si réservé par quelqu'un d'autre (vue parrain)
                    color = if (isReservedByOther) Color(0xFF9E9E9E) else Color(0xFF5D4037),
                    textDecoration = if (isReservedByOther) TextDecoration.LineThrough else TextDecoration.None
                )

                Spacer(Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.priceEstimate != null) {
                        Text(
                            text = stringResource(R.string.wishlist_price_estimate, item.priceEstimate),
                            fontSize = 14.sp,
                            color = if (isReservedByOther) Color(0xFF9E9E9E) else ChristmasColors.AppButtonRed,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (item.link != null) {
                        Text(
                            text = stringResource(R.string.link_label_short)
                            ,
                            fontSize = 12.sp,
                            color = if (isReservedByOther) Color(0xFF9E9E9E) else Color(0xFF1976D2)
                        )
                    }
                }

                // Badge réservation UNIQUEMENT pour le parrain (pas le propriétaire)
                if (!isOwner && (isReservedByMe || isReservedByOther)) {
                    Spacer(Modifier.height(6.dp))
                    Surface(
                        color = if (isReservedByMe) Color(0xFF4CAF50) else Color(0xFF9E9E9E),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (isReservedByMe)
                                stringResource(R.string.reserved_by_you)
                            else
                                stringResource(R.string.already_reserved),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Actions
            if (isOwner) {
                // Propriétaire : corbeille
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete), tint = ChristmasColors.AppButtonRed)
                }
            } else if (!isReservedByOther) {
                // Parrain : bouton réserver (sauf si déjà réservé par quelqu'un d'autre)
                Button(
                    onClick = onToggleReservation,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isReservedByMe) Color(0xFF4CAF50) else ChristmasColors.AppButtonRed
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (isReservedByMe)
                            stringResource(R.string.cancel)
                        else
                            stringResource(R.string.reserve),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}