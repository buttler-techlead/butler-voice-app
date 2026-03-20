package com.demo.butler_voice_app.core

import com.demo.butler_voice_app.api.ApiClient
import android.util.Log

class ConversationManager(
    private val cartManager: CartManager,
    private val speak: (String) -> Unit,
    private val apiClient: ApiClient
) {


    private fun refreshCartUI() {
        val items = cartManager.getItems()

        val text = if (items.isEmpty()) {
            "Cart is empty"
        } else {
            buildString {
                append("Cart:\n")
                items.forEach {
                    append("${it.quantity} x ${it.name} = ${it.price * it.quantity}\n")
                }
                append("Total: ${cartManager.getTotal()} ₹")
            }
        }

        Log.d("CART", text) // 🔥 for now log instead of UI
    }
    private var state = State.IDLE
    private var currentProduct = ""

    fun onWakeWord() {
        state = State.ASK_PRODUCT
        speak("Yes, I am your Butler. What would you like to order?")
    }

    fun onUserInput(inputRaw: String) {
        val input = inputRaw.lowercase().trim()

        when (state) {

            State.ASK_PRODUCT -> {
                currentProduct = input
                speak("How much quantity of $currentProduct?")
                state = State.ASK_QUANTITY
            }

            State.ASK_QUANTITY -> {

                val qty = extractNumber(input)

                // 🔥 CLEAN PRODUCT NAME (IMPORTANT)
                currentProduct = currentProduct
                    .replace(Regex("\\d"), "")   // remove numbers
                    .replace("kg", "")
                    .replace("kgs", "")
                    .replace("kilo", "")
                    .replace("litre", "")
                    .replace("liters", "")
                    .trim()

                speak("Searching for $currentProduct")

                apiClient.getProduct(currentProduct) { product ->

                    if (product == null) {
                        Log.e("AI", "Product not found: $currentProduct")

                        speak("Sorry, I could not find that product. Please try again.")
                        state = State.ASK_PRODUCT
                        return@getProduct
                    }

                    val item = product.copy(quantity = qty)

                    cartManager.addItem(item.name, item.quantity, item.price)

                    refreshCartUI() // if you added UI step

                    speak("Do you want to add more products?")
                    state = State.ASK_ADD_MORE
                }
            }

            State.ASK_ADD_MORE -> {
                if (input.contains("yes")) {
                    speak("Tell me the product name")
                    state = State.ASK_PRODUCT
                } else {
                    speakCartSummary()
                    state = State.CONFIRM_ORDER
                }
            }

            State.CONFIRM_ORDER -> {
                if (input.contains("yes")) {
                    apiClient.placeOrder(cartManager.getItems())
                    speak("Your order has been placed successfully")
                    reset()
                } else {
                    speak("Order cancelled")
                    reset()
                }
            }

            else -> {}
        }
    }

    private fun speakCartSummary() {
        val items = cartManager.getItems()

        val summary = buildString {
            append("Your cart has ")
            items.forEach {
                append("${it.quantity} ${it.name}, ")
            }
            append("Total is ${cartManager.getTotal()} rupees. Confirm order?")
        }

        speak(summary)
    }

    private fun extractNumber(input: String): Int {
        return input.filter { it.isDigit() }.toIntOrNull() ?: 1
    }

    private fun reset() {
        cartManager.clear()
        state = State.IDLE
    }
}