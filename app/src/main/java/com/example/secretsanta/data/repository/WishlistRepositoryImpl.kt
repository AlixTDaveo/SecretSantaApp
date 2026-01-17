package com.example.secretsanta.data.repository

import android.util.Log
import com.example.secretsanta.core.util.Resource
import com.example.secretsanta.data.remote.DummyJsonApi
import com.example.secretsanta.data.remote.FakeStoreApi
import com.example.secretsanta.data.remote.toProductSuggestion
import com.example.secretsanta.domain.model.ProductSuggestion
import com.example.secretsanta.domain.model.WishlistItem
import com.example.secretsanta.domain.repository.WishlistRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class WishlistRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val fakeStoreApi: FakeStoreApi,
    private val dummyJsonApi: DummyJsonApi
) : WishlistRepository {

    companion object {
        private const val TAG = "WishlistRepo"
    }

    override fun getMyWishlist(userId: String): Flow<List<WishlistItem>> = callbackFlow {
        Log.d(TAG, "╔══════════════════════════════════════")
        Log.d(TAG, "║ getMyWishlist")
        Log.d(TAG, "║ userId: $userId")
        Log.d(TAG, "╚══════════════════════════════════════")

        val listener = firestore.collection("wishlists")
            .document(userId)
            .collection("items")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "❌ Firestore listener error", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val items = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        WishlistItem(
                            id = doc.id,
                            userId = userId,
                            title = doc.getString("title") ?: "",
                            priceEstimate = doc.getLong("priceEstimate")?.toInt(),
                            link = doc.getString("link"),
                            imageUrl = doc.getString("imageUrl"),
                            createdAt = doc.getLong("createdAt") ?: 0L,
                            order = doc.getLong("order")?.toInt() ?: 0,
                            reservedBy = doc.getString("reservedBy"),
                            reservedInSecretSanta = doc.getString("reservedInSecretSanta")
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Parse error for doc ${doc.id}", e)
                        null
                    }
                } ?: emptyList()

                Log.d(TAG, "✅ Received ${items.size} items from Firestore")
                trySend(items)
            }

        awaitClose {
            Log.d(TAG, "🔌 Removing Firestore listener")
            listener.remove()
        }
    }

    override fun getUserWishlist(userId: String): Flow<List<WishlistItem>> = getMyWishlist(userId)

    override suspend fun addItem(item: WishlistItem): Resource<Unit> {
        return try {
            Log.d(TAG, "╔══════════════════════════════════════")
            Log.d(TAG, "║ addItem")
            Log.d(TAG, "║ title: ${item.title}")
            Log.d(TAG, "║ userId: ${item.userId}")
            Log.d(TAG, "║ itemId: ${item.id}")
            Log.d(TAG, "╚══════════════════════════════════════")

            if (item.userId.isBlank()) {
                Log.e(TAG, "❌ userId is BLANK! Cannot add item.")
                return Resource.Error("userId est vide - utilisateur non connecté?")
            }

            val data = hashMapOf(
                "title" to item.title,
                "priceEstimate" to item.priceEstimate,
                "link" to item.link,
                "imageUrl" to item.imageUrl,
                "createdAt" to System.currentTimeMillis(),
                "order" to item.order,
                "reservedBy" to null,
                "reservedInSecretSanta" to null
            )

            Log.d(TAG, "📝 Writing to: wishlists/${item.userId}/items/${item.id}")

            firestore.collection("wishlists")
                .document(item.userId)
                .collection("items")
                .document(item.id)
                .set(data)
                .await()

            Log.d(TAG, "✅ Item added successfully!")
            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Add error", e)
            Resource.Error(e.message ?: "Erreur lors de l'ajout")
        }
    }

    override suspend fun deleteItem(itemId: String, userId: String): Resource<Unit> {
        return try {
            Log.d(TAG, "🗑️ Deleting: wishlists/$userId/items/$itemId")
            firestore.collection("wishlists")
                .document(userId)
                .collection("items")
                .document(itemId)
                .delete()
                .await()
            Log.d(TAG, "✅ Deleted")
            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Delete error", e)
            Resource.Error(e.message ?: "Erreur")
        }
    }

    override suspend fun toggleReservation(
        itemId: String,
        ownerId: String,
        reserverId: String,
        secretSantaId: String
    ): Resource<Unit> {
        return try {
            Log.d(TAG, "🔄 toggleReservation - item: $itemId, owner: $ownerId, reserver: $reserverId")

            val docRef = firestore.collection("wishlists")
                .document(ownerId)
                .collection("items")
                .document(itemId)

            val doc = docRef.get().await()
            val currentReserver = doc.getString("reservedBy")

            Log.d(TAG, "Current reserver: $currentReserver")

            val updates: Map<String, Any?> = when {
                currentReserver == reserverId -> {
                    Log.d(TAG, "Releasing reservation")
                    mapOf("reservedBy" to null, "reservedInSecretSanta" to null)
                }
                currentReserver == null -> {
                    Log.d(TAG, "Creating reservation")
                    mapOf("reservedBy" to reserverId, "reservedInSecretSanta" to secretSantaId)
                }
                else -> {
                    Log.w(TAG, "Already reserved by: $currentReserver")
                    return Resource.Error("Déjà réservé par quelqu'un d'autre")
                }
            }

            docRef.update(updates).await()
            Log.d(TAG, "✅ Reservation updated")
            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Reservation error", e)
            Resource.Error(e.message ?: "Erreur")
        }
    }

    override suspend fun searchProducts(query: String): Resource<List<ProductSuggestion>> {
        return try {
            Log.d(TAG, "🔍 Searching: '$query'")
            val results = mutableListOf<ProductSuggestion>()

            // FakeStore
            try {
                val fakeStoreProducts = fakeStoreApi.getAllProducts()
                    .filter { it.title.contains(query, ignoreCase = true) || it.category.contains(query, ignoreCase = true) }
                    .take(10)
                    .map { it.toProductSuggestion() }
                results.addAll(fakeStoreProducts)
                Log.d(TAG, "FakeStore: ${fakeStoreProducts.size} products")
            } catch (e: Exception) {
                Log.e(TAG, "FakeStore error", e)
            }

            // DummyJSON
            try {
                val dummyProducts = dummyJsonApi.searchProducts(query, 15).products.map { it.toProductSuggestion() }
                results.addAll(dummyProducts)
                Log.d(TAG, "DummyJSON: ${dummyProducts.size} products")
            } catch (e: Exception) {
                Log.e(TAG, "DummyJSON error", e)
            }

            Log.d(TAG, "✅ Total: ${results.size} products")
            Resource.Success(results.distinctBy { it.title.lowercase() }.take(20))
        } catch (e: Exception) {
            Log.e(TAG, "❌ Search error", e)
            Resource.Error(e.message ?: "Erreur")
        }
    }
}