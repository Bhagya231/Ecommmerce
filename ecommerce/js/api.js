const BASE_URL = "http://localhost:8080";

export async function register(request) {
    const response = await fetch(`${BASE_URL}/auth/register`, 
        {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(request)
    });

    const data = await response.json();
    if (!response.ok) {
        throw new Error(data.message || "Registration failed");
    }
    return data;
}

export async function login(request) {
    const response = await fetch(`${BASE_URL}/auth/login`, 
        {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(request)
    });
    const data = await response.json();

    if (!response.ok) {
        throw new Error(data.message || "Login failed");
    }
    return data;
}

export async function getProducts() {
    const response = await fetch(`${BASE_URL}/products`);
    const data = await response.json();
    if (!response.ok) {
        throw new Error(data.message || "Unable to load products");
    }

    return data;
}


export async function getProductById(id) {
    const response = await fetch(`${BASE_URL}/products/${id}`);
    const data = await response.json();
    if (!response.ok) {
        throw new Error(data.message || "Unable to load product");
    }
    return data;
}

export async function getProductsByName(name) {

    const response = await fetch(`${BASE_URL}/products/name?name=${encodeURIComponent(name)}`);

    const data = await response.json();

    if (!response.ok) {
        throw new Error(data.message || "Unable to search products");
    }

    return data;
}

export async function getProductsByCategory(category) {

    const response = await fetch( `${BASE_URL}/products/category?category=${encodeURIComponent(category)}`);

    const data = await response.json();

    if (!response.ok) {
        throw new Error(data.message || "Unable to load products by category");
    }

    return data;
}

export async function getProductsByPrice(price) {

    const response = await fetch(`${BASE_URL}/products/price?price=${encodeURIComponent(price)}`);

    const data = await response.json();

    if (!response.ok) {
        throw new Error(data.message || "Unable to load products by price");
    }

    return data;
}

export async function addProduct(email, request) {

    const response = await fetch(
        `${BASE_URL}/products?email=${encodeURIComponent(email)}`,
        {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(request)
        }
    );

    const data = await response.json();
    if (!response.ok) {
        throw new Error(data.message || "Unable to add product");
    }

    return data;
}


export async function updateProduct(email, id, request) {

    const response = await fetch(
        `${BASE_URL}/products/${id}?email=${encodeURIComponent(email)}`,
        {
            method: "PUT",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(request)
        }
    );

    const data = await response.json();

    if (!response.ok) {
        throw new Error(data.message || "Unable to update product");
    }

    return data;
}


export async function deleteProduct(email, id) {

    const response = await fetch(
        `${BASE_URL}/products/${id}?email=${encodeURIComponent(email)}`,
        {
            method: "DELETE"
        }
    );

    if (!response.ok) {
        const data = await response.json();
        throw new Error(data.message || "Unable to delete product");
    }
}

export async function getCart(userId) {

    const response = await fetch( `${BASE_URL}/cart/${userId}`);

    const data = await response.json();

    if (!response.ok) {
        throw new Error(data.message || "Unable to load cart");
    }

    return data;
}


export async function addToCart(userId, productId, quantity) {

    const response = await fetch( `${BASE_URL}/cart/${userId}`,
        {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                productId: productId,
                quantity: quantity
            })
        }
    );

    const data = await response.json();
    if (!response.ok) {
        throw new Error(data.message || "Unable to add product to cart");
    }

    return data;
}


export async function increaseQuantity(userId, productId) {

    const response = await fetch(`${BASE_URL}/cart/${userId}/increase/${productId}`,
        {
            method: "PUT"
        }
    );

    const data = await response.json();

    if (!response.ok) {
        throw new Error(data.message || "Unable to increase quantity");
    }

    return data;
}

export async function decreaseQuantity(userId, productId) {

    const response = await fetch(`${BASE_URL}/cart/${userId}/decrease/${productId}`,
        {
            method: "PUT"
        }
    );

    const data = await response.json();

    if (!response.ok) {
        throw new Error(data.message || "Unable to decrease quantity");
    }

    return data;
}


export async function removeFromCart(userId, productId) {

    const response = await fetch(`${BASE_URL}/cart/${userId}/remove/${productId}`,
        {
            method: "DELETE"
        }
    );

    if (!response.ok) {

        const data = await response.json();

        throw new Error(data.message || "Unable to remove product");
    }
}

export async function clearCart(userId) {

    const response = await fetch(`${BASE_URL}/cart/${userId}/clear`,
        {
            method: "DELETE"
        }
    );

    if (!response.ok) {

        const data = await response.json();

        throw new Error(data.message || "Unable to clear cart");
    }
}


export async function addToWishlist(userId, productId) {

    const response = await fetch(
        `${BASE_URL}/wishlist/add?userId=${userId}`,
        {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                productId: productId
            })
        }
    );

    const data = await response.json();

    if (!response.ok) {
        throw new Error(
            data.message || "Unable to add product to wishlist"
        );
    }

    return data;
}


export async function getWishlist(userId) {

    const response = await fetch(`${BASE_URL}/wishlist/view?userId=${userId}`);

    const data = await response.json();

    if (!response.ok) {
        throw new Error(
            data.message || "Unable to load wishlist"
        );
    }

    return data;
}


export async function removeFromWishlist(userId, productId) {

    const response = await fetch(
        `${BASE_URL}/wishlist/${productId}?userId=${userId}`,
        {
            method: "DELETE"
        }
    );

    if (!response.ok) {

        const data = await response.json();

        throw new Error(
            data.message || "Unable to remove product from wishlist"
        );
    }
}

export async function moveWishlistToCart(userId, productId) {

    const response = await fetch(
        `${BASE_URL}/wishlist/${productId}/movetocart?userId=${userId}`,
        {
            method: "POST"
        }
    );

    const data = await response.json();

    if (!response.ok) {
        throw new Error(
            data.message || "Unable to move product to cart"
        );
    }

    return data;
}

export async function placeOrder(email, request) {

    const response = await fetch(
        `${BASE_URL}/orders?email=${encodeURIComponent(email)}`,
        {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(request)
        }
    );

    const data = await response.json();

    if (!response.ok) {
        throw new Error(data.message || "Unable to place order");
    }

    return data;
}


export async function getCustomerOrders(email) {

    const response = await fetch(
        `${BASE_URL}/orders/customer?email=${encodeURIComponent(email)}`
    );

    const data = await response.json();

    if (!response.ok) {
        throw new Error(data.message || "Unable to load orders");
    }

    return data;
}


export async function getCustomerOrder(email, orderId) {

    const response = await fetch(
        `${BASE_URL}/orders/customer/${orderId}?email=${encodeURIComponent(email)}`
    );

    const data = await response.json();

    if (!response.ok) {
        throw new Error(data.message || "Unable to load order");
    }

    return data;
}


export async function getAllOrders(email) {

    const response = await fetch(
        `${BASE_URL}/orders/admin?email=${encodeURIComponent(email)}`
    );

    const data = await response.json();

    if (!response.ok) {
        throw new Error(data.message || "Unable to load all orders");
    }

    return data;
}


export async function getOrderByOrderNumber(email, orderNumber) {

    const response = await fetch(`${BASE_URL}/orders/admin/${encodeURIComponent(orderNumber)}?email=${encodeURIComponent(email)}`);

    const data = await response.json();
    if (!response.ok) {
        throw new Error(data.message || "Unable to load order");
    }

    return data;
}