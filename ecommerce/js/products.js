import {
    getProducts,
    getCart,
    addToCart,
    increaseQuantity,
    decreaseQuantity,
    getWishlist,
    addToWishlist,
    removeFromWishlist
} from "./api.js";

const user = JSON.parse(localStorage.getItem("user"));

if (!user || user.role !== "CUSTOMER") {
    window.location.href = "./login.html";
}

let products = [];
let cart = null;
let wishlist = [];
let currentPage = 1;
const productsPerPage = 5;
let searchText = "";
let searchTimer;

async function loadProducts() {
    try {
        showLoading();
        products = await getProducts();
        await loadCart();
        await loadWishlist();
        createCategoryOptions();
        displayProducts();
    }
    catch (error) {
        showMessage(error.message, "danger");
    }
    finally {
        hideLoading();
    }
}

async function loadCart() {
    try {
        cart = await getCart(user.id);
        updateCartCount();
    }
    catch (error) {
        console.error(error);
        cart = null;
    }
}

async function loadWishlist() {
    try {
        wishlist = await getWishlist(user.id);
        updateWishlistCount();
    }
    catch (error) {
        console.error(error);
        wishlist = [];
    }
}

function createCategoryOptions() {
    const categoryFilter =  document.getElementById("category-filter");
    const categories = [];
    products.forEach(function (product) {
        if (product.category &&!categories.includes(product.category)) {
            categories.push(product.category);
        }
    });
    categories.sort();
    categoryFilter.innerHTML = `<option value="all">All Categories</option>`;
    categories.forEach(function (category) {
        categoryFilter.innerHTML += `<option value="${category}">${category}</option>`;
    });
}
function getFilteredProducts() {
    const category = document.getElementById("category-filter").value;
    const price = document.getElementById("price-filter").value;
    return products.filter(function (product) {
        const searchMatch = product.name.toLowerCase().includes(searchText.toLowerCase());
        const categoryMatch = category === "all" || product.category === category;
        const priceMatch = price === "all" || Number(product.price) <= Number(price);
        return (searchMatch && categoryMatch && priceMatch);
    });
}
function displayProducts() {
    const container = document.getElementById("product-container");
    const noProducts = document.getElementById("no-products");
    const pagination = document.getElementById("pagination");
    const filteredProducts = getFilteredProducts();
    container.innerHTML = "";
    pagination.innerHTML = "";
    if (filteredProducts.length === 0) {
        noProducts.classList.remove("d-none");
        return;
    }
    noProducts.classList.add("d-none");
    const totalPages = Math.ceil(filteredProducts.length / productsPerPage);
    if(currentPage > totalPages) {
        currentPage = 1;
    }
    const start = (currentPage - 1) * productsPerPage;
    const end = start + productsPerPage;
    const pageProducts = filteredProducts.slice(start, end);
    pageProducts.forEach(function (product) {
        container.innerHTML += createProductCard(product);
    });
    createPagination(totalPages);
}

function createProductCard(product) {
    const cartItem = getCartItem(product.id);
    const wishlistItem = isInWishlist(product.id);
    let cartButton = "";
    if (cartItem) {
        cartButton = `
            <div class="d-flex justify-content-center gap-2">
                <button class="btn btn-outline-primary decrease-cart-btn" data-product-id="${product.id}">
                    -
                </button>
                <span class="fw-bold p-2">
                    ${cartItem.quantity}
                </span>
                <button class="btn btn-outline-primary increase-cart-btn"
                    data-product-id="${product.id}">
                    +
                </button>
            </div>
        `;
    }
    else {
        cartButton = `
            <button class="btn btn-primary w-100 add-cart-btn"
                data-product-id="${product.id}">
                Add to Cart
            </button>
        `;
    }
    let wishlistButton = "";
    if (wishlistItem) {
        wishlistButton = `
            <button class="btn btn-danger w-100 remove-wishlist-btn"
                data-product-id="${product.id}">
                ♥ Remove from Wishlist
            </button>
        `;
    }
    else {
        wishlistButton = `
            <button class="btn btn-outline-danger w-100 add-wishlist-btn"
                data-product-id="${product.id}">
                ♡ Add to Wishlist
            </button>
        `;
    }
    return `
        <div class="col-12 col-md-6 col-lg-4">
            <div class="card h-100 border-0 shadow-sm">
                <div class="card-body d-flex flex-column">
                    <h5 class="card-title fw-bold">
                        ${product.name}
                    </h5>
                    <p class="text-secondary">
                        ${product.category}
                    </p>
                    <p>
                        ${product.description}
                    </p>
                    <h5 class="text-primary fw-bold">
                        ₹${Number(product.price).toFixed(2)}
                    </h5>
                    <p class="text-secondary">
                        Stock: ${product.stock}
                    </p>
                    <div class="mt-auto">
                        ${cartButton}
                        <div class="mt-2">
                            ${wishlistButton}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `;
}

function getCartItem(productId) {
    if (!cart || !cart.cartItems) {
        return null;
    }
    return cart.cartItems.find(function (item) {
        return item.productId === productId;

    });
}

function isInWishlist(productId) {
    return wishlist.some(function (item) {
        return item.productId === productId;

    });
}
async function handleAddToCart(productId) {
    try {
        const product = products.find(function (item) {
                return item.id === productId;
            });
        if (!product) {
            return;
        }
        if (product.stock <= 0) {
            showMessage("Product is out of stock.","danger");
            return;
        }
        cart = await addToCart(user.id,productId,1);
        updateCartCount();
        displayProducts();
        showMessage( "Product added to cart.", "success");
    }
    catch (error) {
        showMessage(error.message,"danger");

    }
}
async function handleIncrease(productId) {
    try {
        cart = await increaseQuantity( user.id, productId);
        updateCartCount();
        displayProducts();
    }
    catch (error) {
        showMessage(error.message,"danger");
    }
}

async function handleDecrease(productId) {
    try {
        cart = await decreaseQuantity(user.id,productId);
        updateCartCount();
        displayProducts();
    }
    catch (error) {
        showMessage(error.message,"danger");
    }
}

async function handleAddToWishlist(productId) {
    try {
        const item = await addToWishlist(user.id,productId);
        wishlist.push(item);
        updateWishlistCount();
        displayProducts();
        showMessage("Product added to wishlist.","success");
    }
    catch (error) {
        showMessage( error.message,"danger");
    }
}

async function handleRemoveFromWishlist(productId) {
    try {
        await removeFromWishlist(user.id,productId);
        wishlist = wishlist.filter(function (item) {
                return item.productId !== productId;
            });

        updateWishlistCount();
        displayProducts();
        showMessage("Product removed from wishlist.","success");
    }
    catch (error) {
        showMessage( error.message, "danger");
    }
}

function updateCartCount() {
    const cartCount = document.getElementById("cart-count");
    if (!cart || !cart.cartItems) {
        cartCount.textContent = "0";
        return;
    }
    let count = 0;
    cart.cartItems.forEach(function (item) {
        count += item.quantity;
    });

    cartCount.textContent = count;
}

function updateWishlistCount() {
    document.getElementById("wishlist-count").textContent = wishlist.length;
}

function createPagination(totalPages) {
    const pagination = document.getElementById("pagination");
    pagination.innerHTML = "";
    const previous =  document.createElement("li");
    previous.className = `page-item ${currentPage === 1? "disabled": ""}`;
    previous.innerHTML = `
        <button class="page-link">
            Previous
        </button>
    `;
    previous.addEventListener("click",function () {
            if (currentPage > 1) {
                currentPage--;
                displayProducts();
            }
        }
    );
    pagination.appendChild(previous);
    for (let page = 1;page <= totalPages;page++) {
        const pageItem = document.createElement("li");
        pageItem.className = `page-item ${page === currentPage? "active" : ""}`;
        pageItem.innerHTML = `
            <button class="page-link">
                ${page}
            </button>
        `;
        pageItem.addEventListener("click",function () {
                currentPage = page;
                displayProducts();
            }
        );
        pagination.appendChild(pageItem);
    }
    const next = document.createElement("li");
    next.className =
        `page-item ${currentPage === totalPages? "disabled": ""}`;
    next.innerHTML = `
        <button class="page-link">
            Next
        </button>
    `;

    next.addEventListener( "click", function () {
            if (currentPage < totalPages) {
                currentPage++;
                displayProducts();
            }
        }
    );
    pagination.appendChild(next);
}

document.getElementById("product-container").addEventListener("click",function (event) {
            const productId = event.target.dataset.productId;
            if (!productId) {
                return;
            }
            if (event.target.classList.contains("add-cart-btn")) {
                handleAddToCart(productId);
            }
            if (event.target.classList.contains("increase-cart-btn")) {
                handleIncrease(productId);
            }
            if (event.target.classList.contains("decrease-cart-btn")) {
                handleDecrease(productId);
            }
            if (event.target.classList.contains("add-wishlist-btn")) {
                handleAddToWishlist(productId);
            }
            if (event.target.classList.contains("remove-wishlist-btn")) {
                handleRemoveFromWishlist(productId);
            }
        }
    );
document.getElementById("search-input").addEventListener("input",function () {
            clearTimeout(searchTimer);
            searchTimer = setTimeout(function () {
                        searchText =document.getElementById("search-input").value.trim();
                        currentPage = 1;
                        displayProducts();
                    },500
                );
        }
    );
document.getElementById("category-filter").addEventListener("change",function () {
            currentPage = 1;
            displayProducts();
        }
    );

document.getElementById("price-filter").addEventListener("change",function () {
            currentPage = 1;
            displayProducts();
        }
    );
document.querySelector('[data-bs-target="#profileModal"]').addEventListener("click",function () {
            document.getElementById("profile-name").textContent = user.name || "-";
            document.getElementById("profile-email").textContent = user.email || "-";
            document.getElementById("profile-phone").textContent = user.phone || "-";
            document.getElementById("profile-address").textContent = user.address || "-";
            document.getElementById("profile-role").textContent = user.role || "CUSTOMER";
        }
    );

document.getElementById("logout-btn").addEventListener("click",function () {
            localStorage.removeItem("user");
            window.location.href ="./login.html";
        }
    );

function showLoading() {
    document.getElementById("loading").classList.remove("d-none");
}
function hideLoading() {
    document.getElementById("loading").classList.add("d-none");
}

function showMessage(text, type) {
    const message = document.getElementById("error-message");
    message.textContent = text;
    message.className = `alert alert-${type}`;
    setTimeout(function () {
        message.classList.add("d-none");
    }, 3000);
}

loadProducts();
