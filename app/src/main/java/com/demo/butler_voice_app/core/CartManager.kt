package com.demo.butler_voice_app.core

class CartManager {

    private val cart = mutableListOf<CartItem>()

    fun addItem(name: String, qty: Int, price: Int) {
        cart.add(CartItem(name, qty, price))
    }

    fun getItems(): List<CartItem> = cart

    fun getTotal(): Int {
        return cart.sumOf { it.price * it.quantity }
    }

    fun clear() {
        cart.clear()
    }
}