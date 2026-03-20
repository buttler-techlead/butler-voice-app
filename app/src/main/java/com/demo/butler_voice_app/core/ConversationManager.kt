package com.demo.butler_voice_app.core

import android.util.Log
import com.demo.butler_voice_app.api.ApiClient

class ConversationManager(
    private val cartManager: CartManager,
    private val speak: (String) -> Unit,
    private val apiClient: ApiClient
) {

    private var state = State.IDLE
    private var currentProduct = ""

    fun onWakeWord() {
        state = State.ASK_PRODUCT
        speak("Yes, I am your Butler. What would you like to order?")
    }

    fun onUserInput(inputRaw: String) {

        val input = inputRaw.lowercase().trim()

        when (state) {

            // 🛒 STEP 1: ASK PRODUCT
            State.ASK_PRODUCT -> {
                currentProduct = input
                speak("How much quantity of $currentProduct?")
                state = State.ASK_QUANTITY
            }

            // 🧮 STEP 2: ASK QUANTITY
            State.ASK_QUANTITY -> {

                val qty = extractNumber(input)

                // 🔥 CLEAN PRODUCT NAME
                currentProduct = currentProduct
                    .replace(Regex("\\d"), "")
                    .replace(Regex("\\b(kg|kgs|kilo|litre|liter|liters)\\b"), "")
                    .trim()

                speak("Searching for $currentProduct")

                // 🔗 API CALL
                apiClient.getProduct(currentProduct) { product ->

                    if (product == null) {
                        Log.e("AI", "Product not found: $currentProduct")

                        speak("Sorry, I could not find that product. Please try again.")
                        state = State.ASK_PRODUCT
                        return@getProduct
                    }

                    val item = product.copy(quantity = qty)

                    cartManager.addItem(
                        item.name,
                        item.quantity,
                        item.price
                    )

                    logCart() // 🔥 log instead of UI

                    speak("Do you want to add more products?")
                    state = State.ASK_ADD_MORE
                }
            }

            // ➕ STEP 3: ADD MORE
            State.ASK_ADD_MORE -> {
                if (input.contains("yes")) {
                    speak("Tell me the product name")
                    state = State.ASK_PRODUCT
                } else {
                    speakCartSummary()
                    state = State.CONFIRM_ORDER
                }
            }

            // ✅ STEP 4: CONFIRM ORDER
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

    // 🧾 CART LOG (SAFE FOR CI)
    private fun logCart() {
        val items = cartManager.getItems()

        if (items.isEmpty()) {
            Log.d("CART", "Cart is empty")
            return
        }

        val text = buildString {
            append("Cart:\n")
            items.forEach {
                append("${it.quantity} x ${it.name} = ${it.price * it.quantity}\n")
            }
            append("Total: ${cartManager.getTotal()} ₹")
        }

        Log.d("CART", text)
    }

    // 🧠 CART SUMMARY VOICE
    private fun speakCartSummary() {
        val items = cartManager.getItems()

        if (items.isEmpty()) {
            speak("Your cart is empty")
            return
        }

        val summary = buildString {
            append("Your cart has ")
            items.forEach {
                append("${it.quantity} ${it.name}, ")
            }
            append("Total is ${cartManager.getTotal()} rupees. Confirm order?")
        }

        speak(summary)
    }

    // 🔢 EXTRACT NUMBER
    private fun extractNumber(input: String): Int {
        return input.filter { it.isDigit() }.toIntOrNull() ?: 1
    }

    // 🔄 RESET FLOW
    private fun reset() {
        cartManager.clear()
        state = State.IDLE
        currentProduct = ""
    }
}