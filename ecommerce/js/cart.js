import {
    getCart,
    increaseQuantity,
    decreaseQuantity,
    removeFromCart,
    clearCart,
    getWishlist
} from "./api.js";

const user = JSON.parse(localStorage.getItem("user"));

if (!user || user.role !== "CUSTOMER") {

    window.location.href ="login.html";
}

const userId = user.id;
let cart = { cartItems: [] };
let wishlist = [];
let selectedProducts = [];
let removeProductId = null;
const loading = document.getElementById("loading");
const errorMessage = document.getElementById("error-message");
const emptyCart = document.getElementById("empty-cart");
const cartContent = document.getElementById("cart-content");
const cartItemsContainer = document.getElementById("cart-items-container");
const selectAll = document.getElementById("select-all");
const cartCount = document.getElementById("cart-count");
const wishlistCount = document.getElementById("wishlist-count");
const selectedItemCount = document.getElementById("selected-item-count");
const cartSubtotal = document.getElementById("cart-subtotal");
const cartDiscount = document.getElementById("cart-discount");
const cartTotal = document.getElementById("cart-total");
const checkoutButton = document.getElementById("checkout-btn");
const clearCartButton = document.getElementById("clear-cart-btn");

async function loadCart() {
    try {
        loading.classList.remove("d-none");
        cartContent.classList.add("d-none");
        emptyCart.classList.add("d-none");
        errorMessage.classList.add("d-none");
        cart = await getCart(userId);

        if (Array.isArray(cart)) {
            cart = {cartItems: cart};
        }
        selectedProducts = [];
        displayCart();
    }
    catch (error) {
        console.error("Cart error:", error);
        if ( error.message && error.message.toLowerCase().includes("cart not found")) {
            cart = { cartItems: []};
            selectedProducts = [];
            displayCart();
        }
        else {
            showError(error.message);
        }
    }
    finally {
        loading.classList.add("d-none");
    }

}

async function loadWishlist() {
    try {
        wishlist = await getWishlist(userId);
        if (!Array.isArray(wishlist)) {
            wishlist = [];
        }
        wishlistCount.textContent =  wishlist.length;
    }
    catch (error) {
        console.error( "Wishlist error:", error);
        wishlistCount.textContent = "0";
    }
}


function displayCart() {
    const items = cart.cartItems || [];
    cartItemsContainer.innerHTML = "";
    updateCartCount(items);

    if (items.length === 0) {
        cartContent.classList.add( "d-none" );
        emptyCart.classList.remove("d-none");
        selectAll.checked = false;
        updateSummary();
        return;
    }
    emptyCart.classList.add( "d-none");
    cartContent.classList.remove( "d-none");
    for (let i = 0;i < items.length;i++)
    {
        createCartItem(items[i]);
    }
    updateSelectAll();
    updateSummary();
}

function createCartItem(item) {
    const productId = item.productId;
    const selected = selectedProducts.includes(productId);
    const productName = item.productName || "Product no longer exists";
    const category =item.category ||"-";
    const price = Number(item.price || 0);
    const quantity = Number(item.quantity || 0);
    const itemTotal =  Number(item.itemTotal || 0);
    const card =  document.createElement("div");
    card.className = "card border-0 shadow-sm mb-3";
    card.innerHTML = `
        <div class="card-body">
            <div class="row align-items-center g-3">
                <div class="col-1">
                    <input
                        type="checkbox"
                        class="form-check-input product-checkbox"
                        data-id="${productId}"
                        ${selected ? "checked" : ""}
                    >
                </div>
                <div class="col-12 col-md-4">
                    <h5 class="fw-bold mb-1">
                        ${productName}
                    </h5>
                    <p class="text-secondary mb-1">
                        ${category}
                    </p>
                    <p class="mb-0">
                        ₹${price.toFixed(2)}
                    </p>
                </div>
                <div class="col-12 col-md-3">
                    <button
                        type="button"
                        class="btn btn-outline-secondary decrease-btn"
                        data-id="${productId}"
                    >
                        −
                    </button>
                    <span class="mx-3 fw-bold">
                        ${quantity}
                    </span>
                    <button
                        type="button"
                        class="btn btn-outline-secondary increase-btn"
                        data-id="${productId}"
                    >
                        +
                    </button>
                </div>
                <div class="col-12 col-md-2">
                    <strong>
                        ₹${itemTotal.toFixed(2)}
                    </strong>
                </div>
                <div class="col-12 col-md-2">
                    <button
                        type="button"
                        class="btn btn-outline-danger btn-sm remove-btn"
                        data-id="${productId}"
                    >
                        Remove
                    </button>
                </div>
            </div>
        </div>
    `;
    cartItemsContainer.appendChild(card);

}

cartItemsContainer.addEventListener("change",function (event) {
        if (!event.target.classList.contains( "product-checkbox")) {
            return;
        }
        const productId = event.target.getAttribute("data-id");
        if (event.target.checked) {
            if (!selectedProducts.includes(productId)) {
                selectedProducts.push(productId);
            }
        }else {
            selectedProducts = selectedProducts.filter( function (id) {
                        return id !== productId;
                    });
        }

        updateSelectAll();
        updateSummary();
    }
);



cartItemsContainer.addEventListener( "click", function (event) {
        const productId =  event.target.getAttribute( "data-id");
        if (!productId) {
            return;
        }
        if ( event.target.classList.contains("increase-btn")) {
            increaseProduct(productId);
        }
        if ( event.target.classList.contains("decrease-btn")) {
            decreaseProduct(productId);
        }
        if (event.target.classList.contains("remove-btn")) {
            openRemoveModal(productId);
        }
    }
);


selectAll.addEventListener("change", function () {
        const items = cart.cartItems || [];
        selectedProducts = [];
        if (selectAll.checked) {
            for (let i = 0;i < items.length;i++) {
                selectedProducts.push(items[i].productId);
            }
        }
        displayCart();
    }
);

function updateSelectAll() {
    const items = cart.cartItems || [];
    if (items.length === 0) {
        selectAll.checked = false;
        return;
    }
    selectAll.checked = selectedProducts.length === items.length;
}

function updateSummary() {

    const items = cart.cartItems || [];
    let subtotal = 0;
    let count = 0;
    for (let i = 0;i < items.length;i++) {
        const item = items[i];
        if (selectedProducts.includes(item.productId)) 
            {
                subtotal += Number(item.price || 0) * Number(item.quantity || 0);
                count += Number(item.quantity || 0);
            }
    }

    let discount = 0;
    if (subtotal >= 5000) {

        discount = subtotal * 0.10;
    }
    else if (subtotal >= 3000) {
        discount = subtotal * 0.05;
    }

    const total = subtotal - discount;
    selectedItemCount.textContent = count;
    cartSubtotal.textContent =`₹${subtotal.toFixed(2)}`;
    cartDiscount.textContent =`₹${discount.toFixed(2)}`;
    cartTotal.textContent =`₹${total.toFixed(2)}`;
    checkoutButton.disabled =count === 0;
}

function openRemoveModal(productId) {
    removeProductId =  productId;
    const modal = new bootstrap.Modal( document.getElementById( "removeModal"));
    modal.show();
}

document.getElementById("confirm-remove-btn").addEventListener("click",async function () {
            if (!removeProductId) {
                return;
            }
            try {
                await removeFromCart(userId,removeProductId);
                selectedProducts =selectedProducts.filter(function (id) {
                            return id !== removeProductId;
                        }
                    );
                const modal =bootstrap.Modal.getInstance( document.getElementById( "removeModal"));
                modal.hide();
                removeProductId = null;
                await loadCart();
               showMessage(text, type);
            }
            catch (error) {
                console.error("Remove error:",error);
                showError( error.message);
            }
        }
    );

clearCartButton.addEventListener("click",function () {
        const modal = new bootstrap.Modal( document.getElementById( "clearCartModal"));
        modal.show();
    }
);

document.getElementById("confirm-clear-btn").addEventListener("click",async function () {
            try {
                await clearCart(userId);
                selectedProducts = [];
                const modal = bootstrap.Modal.getInstance( document.getElementById("clearCartModal"));
                modal.hide();
                await loadCart();
                showError("Cart cleared successfully.");
            }
            catch (error) {
                console.error("Clear cart error:",error);
                showError(error.message);
            }
        }
    );

checkoutButton.addEventListener("click",function () {
        const items = cart.cartItems || [];
        const selectedItems = [];
        for (let i = 0;i < items.length;i++) {
            if (selectedProducts.includes(items[i].productId)) {
                selectedItems.push(items[i]);
            }
        }
        if (selectedItems.length === 0) {
            showError("Please select at least one product.");
            return;
        }

        localStorage.setItem("checkoutItems",JSON.stringify(selectedItems));
        window.location.href ="checkout.html";

    }
);

function updateCartCount(items) {
    let count = 0;
    for (let i = 0;i < items.length;i++) {
        count += Number(items[i].quantity || 0);
    }
    cartCount.textContent = count;
}

document.getElementById("profileModal").addEventListener("show.bs.modal",function () {
            document.getElementById("profile-name").textContent =user.name || "-";
            document.getElementById("profile-email").textContent =user.email || "-";
            document.getElementById( "profile-phone").textContent =user.phone || "-";
            document.getElementById("profile-address").textContent =user.address || "-";
            document.getElementById("profile-role").textContent = user.role || "CUSTOMER";
        }
    );

document.getElementById("logout-btn").addEventListener( "click", function () {
            localStorage.removeItem("user");
            localStorage.removeItem("checkoutItems");
            window.location.href ="./login.html";

        }
    );

function showError(message) {
    errorMessage.textContent = message || "Something went wrong.";
    errorMessage.className ="alert alert-danger";
    errorMessage.classList.remove( "d-none");
    setTimeout(function () {
            errorMessage.classList.add("d-none");
        },
        5000
    );
}

document.addEventListener("DOMContentLoaded",function () {
        loadCart();
        loadWishlist();
    }
);