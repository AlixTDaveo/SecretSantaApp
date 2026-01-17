package com.example.secretsanta.data.remote

import com.example.secretsanta.domain.model.ProductSuggestion
import retrofit2.http.GET
import retrofit2.http.Query

// ========== FAKESTORE API ==========
interface FakeStoreApi {
    @GET("products")
    suspend fun getAllProducts(): List<FakeStoreProduct>
}

data class FakeStoreProduct(
    val id: Int,
    val title: String,
    val price: Double,
    val image: String,
    val category: String
)

fun FakeStoreProduct.toProductSuggestion() = ProductSuggestion(
    id = id,
    title = title,
    price = price,
    imageUrl = image,
    category = category
)

// ========== DUMMYJSON API ==========
interface DummyJsonApi {
    @GET("products/search")
    suspend fun searchProducts(
        @Query("q") query: String,
        @Query("limit") limit: Int = 20
    ): DummyJsonResponse
}

data class DummyJsonResponse(
    val products: List<DummyJsonProduct>,
    val total: Int
)

data class DummyJsonProduct(
    val id: Int,
    val title: String,
    val price: Double,
    val category: String,
    val thumbnail: String
)

fun DummyJsonProduct.toProductSuggestion() = ProductSuggestion(
    id = id + 1000,
    title = title,
    price = price,
    imageUrl = thumbnail,
    category = category
)