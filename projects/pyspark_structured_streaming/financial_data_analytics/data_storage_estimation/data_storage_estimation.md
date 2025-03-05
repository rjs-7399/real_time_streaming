# PySpark Streaming Project: Storage Estimation

## 📖 Data Schema
```
{
    "transaction_id": "string"  // UUID, e.g., "1234", "3456" (36 bytes)
    "amount": "float"           // e.g., 123456.123, 3456.789 (8 bytes)
    "user_id": "string"         // e.g., "user_123", "user_456" (12 bytes)
    "transaction_time": "bigint"// Timestamp in milliseconds (8 bytes)
    "merchant_id": "string"     // e.g., "merchant_1", "merchant_2" (12 bytes)
    "transaction_type": "string"// e.g., "purchase", "refund" (8 bytes)
    "location": "string"        // Full address (12 bytes)
    "payment_method": "string"  // e.g., "cash", "card", "online" (15 bytes)
    "is_international_txn": "boolean" // e.g., true, false (5 bytes)
    "currency": "string"        // e.g., "INR", "USD" (5 bytes)
}
```

### 📊 Storage Calculation
- **Total size per record:** 120 bytes
- **Expected records per hour:** 1.2 billion
- **Hourly storage requirement:**
  - **120 bytes/record × 1.2 billion records = 216 GB/hour**

## ⚙️ Kafka Compression & Replication
Kafka uses JSON compression with configurable rates like Snappy/Gzip.

| Compression  | Replicas | Calculation | Data Size |
|--------------|----------|-------------|-----------|
| 5x           | 1        | (216/5) × 1 | 43 GB     |
| 5x           | 3        | (216/5) × 3 | 129 GB    |

With Snappy/Gzip compression, we reduce the packet size significantly.

- **1 hour:** ~130 GB
- **1 day:** ~3120 GB (3.12 TB)
- **1 year:** ~1138800 TB (~1138.8 PB)

## 📈 Data Growth Estimation
We anticipate a **20% year-over-year** growth.

### 📆 Yearly Growth Formula:
```plaintext
Size = Base × (1 + growth_rate)^years
```

### 🔍 Projections:
- **Base size:** 1138800 TB
- **Growth rate:** 20% (0.20)

**2-Year Projection:**
- \( 1138800 × (1 + 0.20)^2 \)
- **= 1,639,872 TB (~1600 PB)**

**5-Year Projection:**
- \( 1138800 × (1 + 0.20)^5 \)
- **= 2,833,879.62 TB (~2768 PB)**

## 🌐 Architectural Considerations
- Ingesting **1.2 billion records/hour** with a **20% annual growth** requires **robust architecture**.
- Efficient use of **Kafka's compression and replication** to manage large-scale data.
- Strategic use of **batch and streaming pipelines** with optimized **storage and compute resources**.

### 🧠 Key Insights:
- **Annual data growth:** 20%
- **Projected 5-year data volume:** ~2.77 EB
- **Real-time ingestion** with **Kafka, PySpark Streaming**, and **optimized cloud storage** is crucial.

Building an efficient architecture ensures scalability, reliability, and performance for our **fraud analysis pipelines**. 🚀

