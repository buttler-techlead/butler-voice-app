package com.demo.butler_voice_app.api

import android.util.Log
import com.demo.butler_voice_app.core.CartItem
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class ApiClient {

    private val client = OkHttpClient()

    private val BASE_URL = "https://dcabhsrchagikwzjmbvj.supabase.co/rest/v1"

    private val API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImRjYWJoc3JjaGFnaWt3emptYnZqIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzI0MjUwMTQsImV4cCI6MjA4ODAwMTAxNH0.h6v1GefgjWt_hlwzVgqcuH-eYJ1cK-I9RuP48MSdfCs" // 🔥 replace properly

    // 🔍 FETCH PRODUCT (price + name)
    fun getProduct(productName: String, callback: (CartItem?) -> Unit) {

        val url =
            "$BASE_URL/products?base_name=ilike.*${productName.lowercase()}*&select=base_name,price&limit=1"

        val request = Request.Builder()
            .url(url)
            .addHeader("apikey", API_KEY)
            .addHeader("Authorization", "Bearer $API_KEY")
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                Log.e("API", "Product fetch failed")
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {

                val body = response.body?.string() ?: "[]"
                val jsonArray = JSONArray(body)

                if (jsonArray.length() > 0) {

                    val obj = jsonArray.getJSONObject(0)

                    val name = obj.getString("base_name")
                    val price = obj.getDouble("price").toInt()

                    Log.d("API", "Matched: $name Price: $price")

                    callback(CartItem(name, 1, price))

                } else {
                    Log.e("API", "No product found")
                    callback(null)
                }
            }
        })
    }

    // 🛒 PLACE ORDER (MULTI ITEMS)
    fun placeOrder(cart: List<CartItem>) {

        val total = cart.sumOf { it.price * it.quantity }

        val orderJson = JSONObject()
        orderJson.put("total", total)

        val orderRequest = Request.Builder()
            .url("$BASE_URL/orders")
            .post(
                RequestBody.create(
                    "application/json".toMediaTypeOrNull(),
                    orderJson.toString()
                )
            )
            .addHeader("apikey", API_KEY)
            .addHeader("Authorization", "Bearer $API_KEY")
            .addHeader("Prefer", "return=representation")
            .build()

        client.newCall(orderRequest).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                Log.e("API", "Order failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {

                val body = response.body?.string() ?: return
                val orderId = JSONArray(body).getJSONObject(0).getString("id")

                Log.d("API", "Order created: $orderId")

                insertOrderItems(orderId, cart)
            }
        })
    }

    // 📦 INSERT ORDER ITEMS
    private fun insertOrderItems(orderId: String, cart: List<CartItem>) {

        val itemsArray = JSONArray()

        cart.forEach {
            val obj = JSONObject()
            obj.put("order_id", orderId)
            obj.put("product_name", it.name)
            obj.put("quantity", it.quantity)
            obj.put("price", it.price)
            itemsArray.put(obj)
        }

        val request = Request.Builder()
            .url("$BASE_URL/order_items")
            .post(
                RequestBody.create(
                    "application/json".toMediaTypeOrNull(),
                    itemsArray.toString()
                )
            )
            .addHeader("apikey", API_KEY)
            .addHeader("Authorization", "Bearer $API_KEY")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("API", "Items insert failed")
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("API", "Items inserted successfully")
            }
        })
    }
}