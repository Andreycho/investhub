# InvestHub

> A modern web-based cryptocurrency investment simulation platform built with Spring Boot

## Overview

InvestHub is a comprehensive cryptocurrency investment simulation platform that allows users to trade popular cryptocurrencies with virtual currency in a risk-free environment. The platform provides real-time market data, portfolio management, transaction history, and performance analytics.

###  Key Features

- **Secure Authentication** - JWT-based user authentication and authorization
- **Real-Time Trading** - Buy and sell cryptocurrencies with live market prices from Binance
- **Portfolio Management** - Track your investments and performance metrics
- **Performance Analytics** - Detailed profit/loss analysis with winning indicators
- **Watchlist** - Monitor your favorite cryptocurrencies
- **RESTful API** - Clean and well-documented API endpoints
- **Transaction History** - Complete audit trail of all trades
- **Virtual Currency** - Start with $30,000 virtual USD for risk-free trading

## Technology Stack

### Backend
- **Java 21** - Latest LTS version with modern language features
- **Spring Boot 3.4.3** - Production-ready application framework
- **H2 Database** - In-memory database for development
- **WebSocket** - Real-time price updates from Binance API

### Database Schema
- **Users** - User authentication and account management
- **Assets** - Cryptocurrency information (BTC, ETH, ADA, etc.)
- **Holdings** - User's current cryptocurrency positions
- **Transactions** - Complete trading history
- **Watchlist** - User's favorite cryptocurrencies

## Complete API Documentation

### Authentication Endpoints

#### Register New User
**POST** `/api/auth/register`

**Request Body:**
```json
{
  "username": "john_doe_trader",
  "password": "password"
}
```

**Response (Success - 200 OK):**
```json
"User registered successfully"
```

**Response (Error - 409 Conflict):**
```json
"Username already exists"
```

---

#### Login User
**POST** `/api/auth/login`

**Request Body:**
```json
{
  "username": "john_doe_trader",
  "password": "password"
}
```

**Response (Success - 200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 3600000,
  "userId": 1,
  "username": "john_doe_trader",
  "balance": 30000.00
}
```

**Response (Error - 401 Unauthorized):**
```json
"Invalid credentials"
```

---

#### Get All Users
**GET** `/api/auth/users`

**Headers:** `Authorization: Bearer {jwt_token}`

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "username": "john_doe_trader",
    "balance": 29500.50
  },
  {
    "id": 2,
    "username": "crypto_expert",
    "balance": 32000.25
  }
]
```

---

### Market Data Endpoints

#### Get All Cryptocurrency Prices
**GET** `/api/market/prices`

**Headers:** `Authorization: Bearer {jwt_token}`

**Response (200 OK):**
```json
[
  {
    "symbol": "BTC",
    "price": 45250.75
  },
  {
    "symbol": "ETH",
    "price": 3125.40
  },
  {
    "symbol": "ADA",
    "price": 0.85
  }
]
```

---

#### Get Specific Cryptocurrency Price
**GET** `/api/market/prices/{symbol}`

**Path Parameters:**
- `symbol` (string) - Cryptocurrency symbol (e.g., "BTC", "ETH")

**Headers:** `Authorization: Bearer {jwt_token}`

**Response (200 OK):**
```json
{
  "symbol": "BTC",
  "price": 45250.75
}
```

---

#### Get All Available Assets
**GET** `/api/market/assets`

**Headers:** `Authorization: Bearer {jwt_token}`

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "symbol": "BTC",
    "name": "Bitcoin"
  },
  {
    "id": 2,
    "symbol": "ETH",
    "name": "Ethereum"
  }
]
```

---

#### Get Asset Details
**GET** `/api/market/assets/{symbol}`

**Path Parameters:**
- `symbol` (string) - Cryptocurrency symbol

**Headers:** `Authorization: Bearer {jwt_token}`

**Response (200 OK):**
```json
{
  "id": 1,
  "symbol": "BTC",
  "name": "Bitcoin"
}
```

---

#### Search Assets
**GET** `/api/market/assets/search`

**Query Parameters:**
- `query` (string) - Search term (e.g., "bitcoin", "eth")

**Headers:** `Authorization: Bearer {jwt_token}`

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "symbol": "BTC",
    "name": "Bitcoin"
  }
]
```

---

### Portfolio Management Endpoints

#### Get Complete Portfolio
**GET** `/api/portfolio`

**Headers:** `Authorization: Bearer {jwt_token}`

**Response (200 OK):**
```json
{
  "usdBalance": 28500.00,
  "holdings": [
    {
      "id": 1,
      "assetSymbol": "BTC",
      "quantity": 0.5,
      "avgBuyPrice": 44000.00
    },
    {
      "id": 2,
      "assetSymbol": "ETH",
      "quantity": 2.0,
      "avgBuyPrice": 3000.00
    }
  ]
}
```

---

#### Get USD Balance
**GET** `/api/portfolio/balance`

**Headers:** `Authorization: Bearer {jwt_token}`

**Response (200 OK):**
```json
{
  "balance": 28500.00
}
```

---

#### Get All Holdings
**GET** `/api/portfolio/holdings`

**Headers:** `Authorization: Bearer {jwt_token}`

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "assetSymbol": "BTC",
    "quantity": 0.5,
    "avgBuyPrice": 44000.00
  },
  {
    "id": 2,
    "assetSymbol": "ETH",
    "quantity": 2.0,
    "avgBuyPrice": 3000.00
  }
]
```

---

#### Get Specific Holding
**GET** `/api/portfolio/holdings/{symbol}`

**Path Parameters:**
- `symbol` (string) - Cryptocurrency symbol

**Headers:** `Authorization: Bearer {jwt_token}`

**Response (200 OK):**
```json
{
  "id": 1,
  "assetSymbol": "BTC",
  "quantity": 0.5,
  "avgBuyPrice": 44000.00
}
```

---

#### Get Portfolio Statistics
**GET** `/api/portfolio/stats`

**Headers:** `Authorization: Bearer {jwt_token}`

**Response (200 OK):**
```json
{
  "stats": {
    "totalPortfolioValue": 30020.98,
    "usdBalance": 28063.33,
    "totalInvested": 951.84,
    "currentValue": 969.66,
    "netGain": 17.82,
    "returnPercentage": 1.87,
    "performanceStatus": "WINNING",
    "holdingsCount": 2
  }
}
```

**Performance Status Values:**
- `"WINNING"` - Making profit
- `"LOSING"` - Currently at loss
- `"BREAK_EVEN"` - No gain or loss

---

### Trading Endpoints

#### Buy/Sell Cryptocurrency
**POST** `/api/transactions`

**Headers:** `Authorization: Bearer {jwt_token}`

**Request Body (Buy Example):**
```json
{
  "type": "BUY",
  "assetSymbol": "BTC",
  "quantity": 0.1
}
```

**Request Body (Sell Example):**
```json
{
  "type": "SELL",
  "assetSymbol": "BTC",
  "quantity": 0.05
}
```

**Field Descriptions:**
- `type` (string) - Transaction type: `"BUY"` or `"SELL"`
- `assetSymbol` (string) - Cryptocurrency symbol (BTC, ETH, ADA, etc.)
- `quantity` (number) - Amount to buy/sell (must be > 0)

**Response (Success - 200 OK):**
```json
{
  "id": 15,
  "type": "BUY",
  "assetSymbol": "BTC",
  "quantity": 0.1,
  "pricePerUnit": 45250.75,
  "timestamp": "2026-01-15T18:30:45.123Z"
}
```

**Error Responses:**

*Insufficient Balance (400 Bad Request):*
```json
{
  "status": 400,
  "error": "Insufficient balance",
  "message": "Required: $4525.08, Available: $1200.00",
  "details": {
    "required": 4525.08,
    "available": 1200.00
  }
}
```

*Insufficient Holdings (400 Bad Request):*
```json
{
  "status": 400,
  "error": "Insufficient holdings",
  "message": "Insufficient BTCUSDT holdings: owned=0.05, requested=0.1",
  "details": {
    "assetSymbol": "BTCUSDT",
    "owned": 0.05,
    "requested": 0.1
  }
}
```

---

#### Get All Transactions
**GET** `/api/transactions`

**Headers:** `Authorization: Bearer {jwt_token}`

**Response (200 OK):**
```json
[
  {
    "id": 15,
    "type": "BUY",
    "assetSymbol": "BTC",
    "quantity": 0.1,
    "pricePerUnit": 45250.75,
    "timestamp": "2026-01-15T18:30:45.123Z"
  },
  {
    "id": 14,
    "type": "SELL",
    "assetSymbol": "ETH",
    "quantity": 0.5,
    "pricePerUnit": 3125.40,
    "timestamp": "2026-01-15T17:15:30.456Z"
  }
]
```

---

#### Get Specific Transaction
**GET** `/api/transactions/{transactionId}`

**Path Parameters:**
- `transactionId` (number) - Transaction ID

**Headers:** `Authorization: Bearer {jwt_token}`

**Response (200 OK):**
```json
{
  "id": 15,
  "type": "BUY",
  "assetSymbol": "BTC",
  "quantity": 0.1,
  "pricePerUnit": 45250.75,
  "timestamp": "2026-01-15T18:30:45.123Z"
}
```

---

#### Get Transactions by Asset
**GET** `/api/transactions/asset/{symbol}`

**Path Parameters:**
- `symbol` (string) - Cryptocurrency symbol

**Headers:** `Authorization: Bearer {jwt_token}`

**Response (200 OK):**
```json
[
  {
    "id": 15,
    "type": "BUY",
    "assetSymbol": "BTC",
    "quantity": 0.1,
    "pricePerUnit": 45250.75,
    "timestamp": "2026-01-15T18:30:45.123Z"
  },
  {
    "id": 12,
    "type": "BUY",
    "assetSymbol": "BTC",
    "quantity": 0.05,
    "pricePerUnit": 44800.50,
    "timestamp": "2026-01-15T16:45:20.789Z"
  }
]
```

---

### Watchlist Endpoints

#### Get User Watchlist
**GET** `/api/watchlist`

**Headers:** `Authorization: Bearer {jwt_token}`

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "symbol": "BTC",
    "name": "Bitcoin"
  },
  {
    "id": 2,
    "symbol": "ETH",
    "name": "Ethereum"
  }
]
```

---

#### Add to Watchlist
**POST** `/api/watchlist/{assetSymbol}`

**Path Parameters:**
- `assetSymbol` (string) - Cryptocurrency symbol to add

**Headers:** `Authorization: Bearer {jwt_token}`

**Response (Success - 201 Created):**
```json
{
  "id": 1,
  "symbol": "BTC",
  "name": "Bitcoin"
}
```

---

#### Remove from Watchlist
**DELETE** `/api/watchlist/{assetSymbol}`

**Path Parameters:**
- `assetSymbol` (string) - Cryptocurrency symbol to remove

**Headers:** `Authorization: Bearer {jwt_token}`

**Response (Success - 204 No Content)**
```
(Empty response body)
```

---

#### Check if Asset in Watchlist
**GET** `/api/watchlist/contains/{assetSymbol}`

**Path Parameters:**
- `assetSymbol` (string) - Cryptocurrency symbol to check

**Headers:** `Authorization: Bearer {jwt_token}`

**Response (200 OK):**
```json
true
```
or
```json
false
```

---

### Account Management Endpoints

#### Reset Account
**POST** `/api/account/reset`

**Headers:** `Authorization: Bearer {jwt_token}`

**Response (200 OK):**
```json
{
  "message": "Account reset successfully",
  "newBalance": 30000.00,
  "clearedTransactions": 15,
  "clearedHoldings": 3
}
```

---

## Error Response Format

All API errors follow this consistent format:

```json
{
  "status": 400,
  "error": "Error Type",
  "message": "Detailed error message",
  "details": {
    "field": "additional context"
  }
}
```

## Supported Cryptocurrencies

- **BTC** (Bitcoin)
- **ETH** (Ethereum)
- **BNB** (Binance coin)
- **ADA** (Cardano)
- **DOG** (Dogecoin)
- **XRPU** (Ripple)
- **SOL** (Solana)

