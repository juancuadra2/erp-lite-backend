# Technical Specification: Módulo de Gestión de Inventarios

**Feature**: 07-inventory  
**Version**: 1.0  
**Created**: 2026-02-01  
**Last Updated**: 2026-02-01  
**Status**: ⏳ PHASE 2 - Awaiting Technical Review

---

## 🎯 Architecture Overview

Módulo complejo que implementa **gestión integral de inventarios** con arquitectura hexagonal, control de concurrencia optimista, cálculo automático de costo promedio y auditoría exhaustiva.

### Tech Stack

- **Backend**: Java 17+, Spring Boot 3.x
- **Persistence**: MySQL 8.0+ con Flyway, Spring Data JPA
- **Concurrency**: Optimistic Locking con `@Version`
- **Calculations**: BigDecimal para cálculos monetarios precisos
- **Mapping**: MapStruct 1.5+
- **Testing**: JUnit 5, Mockito, Testcontainers (MySQL), Spring Boot Test
- **Events**: Spring Events para desacoplamiento de alertas

---

## 🏗️ Architecture Layers

### Domain Layer (Core Business Logic)

#### Domain Models (`domain/inventory/model/`)

**ProductCategory.java**
```java
@Getter
@Builder
@AllArgsConstructor
public class ProductCategory {
    private Long id;
    private Long parentId;
    private String name;
    private String code;              // unique, "BEB", "BEB-GAS"
    private String description;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static ProductCategory create(String name, String code, Long parentId) {
        return ProductCategory.builder()
            .name(name)
            .code(code)
            .parentId(parentId)
            .active(true)
            .createdAt(LocalDateTime.now())
            .build();
    }
    
    public boolean isRoot() {
        return parentId == null;
    }
    
    public boolean hasParent() {
        return parentId != null;
    }
}
```

**Product.java** (Aggregate Root)
```java
@Getter
@Builder
@AllArgsConstructor
public class Product {
    private Long id;
    private String sku;               // unique, required
    private String name;
    private String description;
    private Long categoryId;
    private Long baseUnitId;
    private BigDecimal purchasePrice;
    private BigDecimal salePrice;
    private Long taxTypeId;
    private String imageUrl;
    private boolean inventoryManaged;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private Long version;
    
    // Barcodes collection
    private List<ProductBarcode> barcodes = new ArrayList<>();
    
    public static Product create(String sku, String name, Long categoryId, 
                                Long baseUnitId, BigDecimal purchasePrice, 
                                BigDecimal salePrice, Long userId) {
        // Validations
        if (sku == null || sku.isBlank()) {
            throw new InvalidProductDataException("SKU es requerido");
        }
        if (purchasePrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidProductDataException("Precio de compra no puede ser negativo");
        }
        if (salePrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidProductDataException("Precio de venta no puede ser negativo");
        }
        
        return Product.builder()
            .sku(sku)
            .name(name)
            .categoryId(categoryId)
            .baseUnitId(baseUnitId)
            .purchasePrice(purchasePrice)
            .salePrice(salePrice)
            .inventoryManaged(true)
            .active(true)
            .createdAt(LocalDateTime.now())
            .barcodes(new ArrayList<>())
            .build();
    }
    
    public void addBarcode(String barcode, boolean isPrimary) {
        ProductBarcode pb = ProductBarcode.create(this.id, barcode, isPrimary);
        this.barcodes.add(pb);
    }
    
    public void removeBarcode(String barcode) {
        this.barcodes.removeIf(b -> b.getBarcode().equals(barcode));
    }
    
    public Optional<ProductBarcode> getPrimaryBarcode() {
        return barcodes.stream()
            .filter(ProductBarcode::isPrimary)
            .findFirst();
    }
    
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.active = false;
    }
    
    public boolean isDeleted() {
        return deletedAt != null;
    }
}
```

**ProductBarcode.java**
```java
@Getter
@Builder
@AllArgsConstructor
public class ProductBarcode {
    private Long id;
    private Long productId;
    private String barcode;           // unique
    private boolean isPrimary;
    
    public static ProductBarcode create(Long productId, String barcode, boolean isPrimary) {
        if (barcode == null || barcode.isBlank()) {
            throw new InvalidBarcodeException("Código de barras es requerido");
        }
        return ProductBarcode.builder()
            .productId(productId)
            .barcode(barcode)
            .isPrimary(isPrimary)
            .build();
    }
}
```

**Stock.java** (Aggregate Root with Optimistic Locking)
```java
@Getter
@Builder
@AllArgsConstructor
public class Stock {
    private Long id;
    private Long productId;
    private Long warehouseId;
    private BigDecimal quantity;      // >= 0, stored in base unit
    private BigDecimal minQuantity;
    private BigDecimal maxQuantity;
    private BigDecimal averageCost;
    private Long version;             // @Version for optimistic locking
    
    public static Stock initialize(Long productId, Long warehouseId, 
                                  BigDecimal quantity, BigDecimal minQuantity,
                                  BigDecimal maxQuantity, BigDecimal averageCost) {
        validateNonNegative(quantity, "Cantidad");
        validateNonNegative(averageCost, "Costo promedio");
        
        return Stock.builder()
            .productId(productId)
            .warehouseId(warehouseId)
            .quantity(quantity)
            .minQuantity(minQuantity)
            .maxQuantity(maxQuantity)
            .averageCost(averageCost)
            .version(0L)
            .build();
    }
    
    public void increase(BigDecimal amount, BigDecimal newCost) {
        validateNonNegative(amount, "Cantidad");
        
        // Recalculate average cost
        BigDecimal totalValue = this.quantity.multiply(this.averageCost);
        BigDecimal addedValue = amount.multiply(newCost);
        BigDecimal newQuantity = this.quantity.add(amount);
        
        this.averageCost = totalValue.add(addedValue).divide(newQuantity, 2, RoundingMode.HALF_UP);
        this.quantity = newQuantity;
    }
    
    public void decrease(BigDecimal amount) {
        validateNonNegative(amount, "Cantidad");
        
        if (this.quantity.compareTo(amount) < 0) {
            throw new InsufficientStockException(
                String.format("Stock insuficiente. Disponible: %s, Requerido: %s", 
                             this.quantity, amount)
            );
        }
        
        this.quantity = this.quantity.subtract(amount);
    }
    
    public void adjust(BigDecimal adjustmentAmount) {
        BigDecimal newQuantity = this.quantity.add(adjustmentAmount);
        
        if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidStockAdjustmentException(
                String.format("Ajuste resultaría en stock negativo (%s + %s = %s)", 
                             this.quantity, adjustmentAmount, newQuantity)
            );
        }
        
        this.quantity = newQuantity;
    }
    
    public boolean isBelowMinimum() {
        return minQuantity != null && quantity.compareTo(minQuantity) < 0;
    }
    
    private static void validateNonNegative(BigDecimal value, String fieldName) {
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidStockException(fieldName + " no puede ser negativo");
        }
    }
}
```

**InventoryMovement.java**
```java
@Getter
@Builder
@AllArgsConstructor
public class InventoryMovement {
    private Long id;
    private InventoryMovementType type;  // INITIAL, ADJUSTMENT, SALE, PURCHASE, TRANSFER_OUT, TRANSFER_IN
    private String referenceType;        // "SaleDocument", "PurchaseReceipt", "Transfer"
    private Long referenceId;
    private Long productId;
    private Long warehouseId;
    private BigDecimal quantity;         // can be negative for OUT movements
    private Long unitId;
    private BigDecimal cost;
    private Long userId;
    private String reason;               // mandatory for ADJUSTMENT
    private String notes;
    private LocalDateTime createdAt;
    
    public static InventoryMovement create(InventoryMovementType type, Long productId,
                                          Long warehouseId, BigDecimal quantity,
                                          BigDecimal cost, Long userId, String reason) {
        if (type == InventoryMovementType.ADJUSTMENT && (reason == null || reason.isBlank())) {
            throw new InvalidMovementException("Ajustes requieren motivo obligatorio");
        }
        
        return InventoryMovement.builder()
            .type(type)
            .productId(productId)
            .warehouseId(warehouseId)
            .quantity(quantity)
            .cost(cost)
            .userId(userId)
            .reason(reason)
            .createdAt(LocalDateTime.now())
            .build();
    }
    
    public boolean isInbound() {
        return quantity.compareTo(BigDecimal.ZERO) > 0;
    }
    
    public boolean isOutbound() {
        return quantity.compareTo(BigDecimal.ZERO) < 0;
    }
}
```

**WarehouseTransfer.java** (Aggregate Root)
```java
@Getter
@Builder
@AllArgsConstructor
public class WarehouseTransfer {
    private Long id;
    private String transferNumber;    // "TRF-2026-0001"
    private Long fromWarehouseId;
    private Long toWarehouseId;
    private TransferStatus status;    // PENDING, APPROVED, RECEIVED, CANCELLED
    private Long requestedBy;
    private Long approvedBy;
    private Long receivedBy;
    private String notes;
    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime receivedAt;
    private Long version;
    
    private List<WarehouseTransferDetail> details = new ArrayList<>();
    
    public static WarehouseTransfer create(Long fromWarehouseId, Long toWarehouseId,
                                          String transferNumber, Long userId, String notes) {
        if (fromWarehouseId.equals(toWarehouseId)) {
            throw new InvalidTransferException("Bodega origen y destino no pueden ser iguales");
        }
        
        return WarehouseTransfer.builder()
            .transferNumber(transferNumber)
            .fromWarehouseId(fromWarehouseId)
            .toWarehouseId(toWarehouseId)
            .status(TransferStatus.PENDING)
            .requestedBy(userId)
            .requestedAt(LocalDateTime.now())
            .notes(notes)
            .details(new ArrayList<>())
            .build();
    }
    
    public void approve(Long userId) {
        if (status != TransferStatus.PENDING) {
            throw new InvalidTransferStateException("Solo se pueden aprobar transferencias PENDING");
        }
        this.status = TransferStatus.APPROVED;
        this.approvedBy = userId;
        this.approvedAt = LocalDateTime.now();
    }
    
    public void receive(Long userId) {
        if (status != TransferStatus.APPROVED) {
            throw new InvalidTransferStateException("Solo se pueden recibir transferencias APPROVED");
        }
        this.status = TransferStatus.RECEIVED;
        this.receivedBy = userId;
        this.receivedAt = LocalDateTime.now();
    }
    
    public void cancel() {
        if (status == TransferStatus.RECEIVED) {
            throw new InvalidTransferStateException("No se puede cancelar transferencia ya recibida");
        }
        this.status = TransferStatus.CANCELLED;
    }
    
    public void addDetail(Long productId, BigDecimal quantity, Long unitId) {
        WarehouseTransferDetail detail = WarehouseTransferDetail.create(
            this.id, productId, quantity, unitId
        );
        this.details.add(detail);
    }
}
```

**WarehouseTransferDetail.java**
```java
@Getter
@Builder
@AllArgsConstructor
public class WarehouseTransferDetail {
    private Long id;
    private Long transferId;
    private Long productId;
    private BigDecimal quantity;
    private BigDecimal quantityReceived;
    private Long unitId;
    private String notes;
    
    public static WarehouseTransferDetail create(Long transferId, Long productId,
                                                BigDecimal quantity, Long unitId) {
        validatePositive(quantity, "Cantidad");
        return WarehouseTransferDetail.builder()
            .transferId(transferId)
            .productId(productId)
            .quantity(quantity)
            .unitId(unitId)
            .build();
    }
    
    public void setReceivedQuantity(BigDecimal received) {
        if (received.compareTo(quantity) > 0) {
            throw new InvalidTransferException("No se puede recibir más de lo transferido");
        }
        this.quantityReceived = received;
    }
    
    public boolean hasDiscrepancy() {
        return quantityReceived != null && 
               quantityReceived.compareTo(quantity) != 0;
    }
}
```

#### Domain Enums

```java
public enum InventoryMovementType {
    INITIAL, ADJUSTMENT, SALE, PURCHASE, TRANSFER_OUT, TRANSFER_IN
}

public enum TransferStatus {
    PENDING, APPROVED, RECEIVED, CANCELLED
}
```

#### Domain Services

**AverageCostCalculator.java**
```java
@Service
public class AverageCostCalculator {
    
    public BigDecimal calculate(BigDecimal currentQuantity, BigDecimal currentCost,
                               BigDecimal incomingQuantity, BigDecimal incomingCost) {
        BigDecimal currentValue = currentQuantity.multiply(currentCost);
        BigDecimal incomingValue = incomingQuantity.multiply(incomingCost);
        BigDecimal totalQuantity = currentQuantity.add(incomingQuantity);
        
        if (totalQuantity.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        return currentValue.add(incomingValue)
            .divide(totalQuantity, 2, RoundingMode.HALF_UP);
    }
}
```

**StockValidator.java**
```java
@Service
@RequiredArgsConstructor
public class StockValidator {
    private final StockPort stockPort;
    
    public void validateSufficientStock(Long productId, Long warehouseId, 
                                       BigDecimal required) {
        Stock stock = stockPort.findByProductAndWarehouse(productId, warehouseId)
            .orElseThrow(() -> new StockNotFoundException(
                "No existe stock para producto en bodega"
            ));
        
        if (stock.getQuantity().compareTo(required) < 0) {
            throw new InsufficientStockException(
                String.format("Stock insuficiente. Disponible: %s, Requerido: %s",
                             stock.getQuantity(), required)
            );
        }
    }
}
```

---

### Application Layer (Use Cases)

#### Input Ports (Use Cases) - `application/port/input/inventory/`

```java
// Products
public interface CreateProductUseCase {
    Product execute(CreateProductCommand command);
}

public interface UpdateProductUseCase {
    Product execute(Long id, UpdateProductCommand command);
}

public interface DeleteProductUseCase {
    void execute(Long id);
}

public interface SearchProductByBarcodeUseCase {
    Product execute(String barcode);
}

// Stock
public interface InitializeStockUseCase {
    Stock execute(InitializeStockCommand command);
}

public interface AdjustStockUseCase {
    void execute(AdjustStockCommand command);
}

public interface GetStockByProductUseCase {
    StockByProductResponse execute(Long productId);
}

public interface GetLowStockAlertsUseCase {
    List<LowStockAlert> execute();
}

// Transfers
public interface CreateTransferUseCase {
    WarehouseTransfer execute(CreateTransferCommand command);
}

public interface ApproveTransferUseCase {
    void execute(Long transferId);
}

public interface ReceiveTransferUseCase {
    void execute(Long transferId, ReceiveTransferCommand command);
}

public interface CancelTransferUseCase {
    void execute(Long transferId);
}

// Kardex
public interface GetKardexUseCase {
    Page<KardexEntry> execute(Long productId, KardexFilter filter, Pageable pageable);
}
```

#### Output Ports (Repository Interfaces)

```java
public interface ProductPort {
    Product save(Product product);
    Optional<Product> findById(Long id);
    Optional<Product> findBySku(String sku);
    Optional<Product> findByBarcode(String barcode);
    Page<Product> findAll(Pageable pageable);
    Page<Product> findByCategoryId(Long categoryId, boolean includeSubcategories, Pageable pageable);
    boolean existsBySku(String sku);
    boolean existsByBarcode(String barcode);
}

public interface StockPort {
    Stock save(Stock stock);
    Optional<Stock> findByProductAndWarehouse(Long productId, Long warehouseId);
    List<Stock> findByProductId(Long productId);
    List<Stock> findLowStock(); // quantity < minQuantity
}

public interface InventoryMovementPort {
    InventoryMovement save(InventoryMovement movement);
    Page<InventoryMovement> findByProductAndWarehouse(Long productId, Long warehouseId, Pageable pageable);
}

public interface TransferPort {
    WarehouseTransfer save(WarehouseTransfer transfer);
    Optional<WarehouseTransfer> findById(Long id);
    Page<WarehouseTransfer> findAll(Pageable pageable);
    String generateTransferNumber();
}
```

#### Key Service Implementation

**AdjustStockService.java**
```java
@Service
@RequiredArgsConstructor
@Transactional
public class AdjustStockService implements AdjustStockUseCase {
    private final StockPort stockPort;
    private final InventoryMovementPort movementPort;
    private final AuditLogPort auditLogPort;
    
    @Override
    public void execute(AdjustStockCommand command) {
        // 1. Find stock (with version for optimistic locking)
        Stock stock = stockPort.findByProductAndWarehouse(
            command.productId(), command.warehouseId()
        ).orElseThrow(() -> new StockNotFoundException("Stock no encontrado"));
        
        // 2. Validate adjustment
        stock.adjust(command.quantity());
        
        // 3. Save stock (version will be checked)
        try {
            stockPort.save(stock);
        } catch (OptimisticLockException e) {
            throw new ConcurrentStockModificationException(
                "Stock fue modificado por otro usuario. Por favor reintente."
            );
        }
        
        // 4. Register movement
        InventoryMovement movement = InventoryMovement.create(
            InventoryMovementType.ADJUSTMENT,
            command.productId(),
            command.warehouseId(),
            command.quantity(),
            stock.getAverageCost(),
            command.userId(),
            command.reason()
        );
        movement.setNotes(command.notes());
        movementPort.save(movement);
        
        // 5. Audit
        auditLogPort.save(AuditLog.create(
            command.userId(),
            "Stock",
            stock.getId(),
            AuditAction.STOCK_ADJUSTED,
            null,
            command
        ));
        
        // 6. Publish event if below minimum
        if (stock.isBelowMinimum()) {
            eventPublisher.publishEvent(new LowStockAlertEvent(stock));
        }
    }
}
```

---

### Infrastructure Layer

#### Database Schema (`resources/db/migration/V1.4__inventory_tables.sql`)

```sql
CREATE TABLE product_categories (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id   BIGINT,
    name        VARCHAR(100) NOT NULL,
    code        VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  DATETIME NOT NULL,
    updated_at  DATETIME,
    FOREIGN KEY (parent_id) REFERENCES product_categories(id),
    INDEX idx_category_parent (parent_id),
    INDEX idx_category_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE products (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku                VARCHAR(50) NOT NULL UNIQUE,
    name               VARCHAR(200) NOT NULL,
    description        TEXT,
    category_id        BIGINT,
    base_unit_id       BIGINT NOT NULL,
    purchase_price     DECIMAL(15,2) NOT NULL,
    sale_price         DECIMAL(15,2) NOT NULL,
    tax_type_id        BIGINT,
    image_url          VARCHAR(500),
    inventory_managed  BOOLEAN NOT NULL DEFAULT TRUE,
    active             BOOLEAN NOT NULL DEFAULT TRUE,
    created_at         DATETIME NOT NULL,
    updated_at         DATETIME,
    deleted_at         DATETIME,
    version            BIGINT NOT NULL DEFAULT 0,
    FOREIGN KEY (category_id) REFERENCES product_categories(id),
    FOREIGN KEY (base_unit_id) REFERENCES units_of_measure(id),
    INDEX idx_product_sku (sku),
    INDEX idx_product_category (category_id),
    INDEX idx_product_active (active),
    INDEX idx_product_deleted (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE product_barcodes (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    barcode    VARCHAR(100) NOT NULL UNIQUE,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    INDEX idx_barcode (barcode)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE stock (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id   BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    quantity     DECIMAL(15,3) NOT NULL DEFAULT 0 CHECK (quantity >= 0),
    min_quantity DECIMAL(15,3),
    max_quantity DECIMAL(15,3),
    average_cost DECIMAL(15,2) NOT NULL DEFAULT 0,
    version      BIGINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_stock_product_warehouse (product_id, warehouse_id),
    FOREIGN KEY (product_id) REFERENCES products(id),
    INDEX idx_stock_warehouse (warehouse_id),
    INDEX idx_stock_low (product_id, warehouse_id, quantity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE inventory_movements (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    type           VARCHAR(50) NOT NULL,
    reference_type VARCHAR(50),
    reference_id   BIGINT,
    product_id     BIGINT NOT NULL,
    warehouse_id   BIGINT NOT NULL,
    quantity       DECIMAL(15,3) NOT NULL,
    unit_id        BIGINT,
    cost           DECIMAL(15,2) NOT NULL,
    user_id        BIGINT NOT NULL,
    reason         VARCHAR(255),
    notes          TEXT,
    created_at     DATETIME NOT NULL,
    FOREIGN KEY (product_id) REFERENCES products(id),
    INDEX idx_movement_product (product_id),
    INDEX idx_movement_warehouse (warehouse_id),
    INDEX idx_movement_type (type),
    INDEX idx_movement_date (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE warehouse_transfers (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    transfer_number   VARCHAR(50) NOT NULL UNIQUE,
    from_warehouse_id BIGINT NOT NULL,
    to_warehouse_id   BIGINT NOT NULL,
    status            VARCHAR(20) NOT NULL,
    requested_by      BIGINT NOT NULL,
    approved_by       BIGINT,
    received_by       BIGINT,
    notes             TEXT,
    requested_at      DATETIME NOT NULL,
    approved_at       DATETIME,
    received_at       DATETIME,
    version           BIGINT NOT NULL DEFAULT 0,
    INDEX idx_transfer_number (transfer_number),
    INDEX idx_transfer_status (status),
    INDEX idx_transfer_from (from_warehouse_id),
    INDEX idx_transfer_to (to_warehouse_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE warehouse_transfer_details (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    transfer_id       BIGINT NOT NULL,
    product_id        BIGINT NOT NULL,
    quantity          DECIMAL(15,3) NOT NULL,
    quantity_received DECIMAL(15,3),
    unit_id           BIGINT NOT NULL,
    notes             TEXT,
    FOREIGN KEY (transfer_id) REFERENCES warehouse_transfers(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

## 📊 Performance & Optimization

### Database Optimization

1. **Indexes**: SKU, barcode, category_id, warehouse_id, created_at
2. **Composite unique index**: (product_id, warehouse_id) on stock
3. **Query optimization**: Use JOIN FETCH for eager loading of barcodes
4. **Partitioning**: Consider partitioning inventory_movements by date for large datasets

### Concurrency Strategy

1. **Optimistic Locking**: @Version on Stock and WarehouseTransfer
2. **Retry mechanism**: Automatic retry on OptimisticLockException (max 3 attempts)
3. **Transaction isolation**: READ_COMMITTED for stock updates

### Performance Goals

| Operation | Target | Strategy |
|-----------|--------|----------|
| Product search by barcode | < 100ms p95 | Index on barcode |
| Stock query | < 200ms p95 | Composite index, caching |
| Stock update | < 300ms p95 | Optimistic locking |
| Kardex query (1 year) | < 2s p95 | Pagination, indexes |
| Transfer creation | < 500ms p95 | Batch insert details |

---

## 🧪 Testing Strategy

### Unit Tests
- Domain model business methods
- Average cost calculator
- Stock validations
- Transfer state transitions

### Integration Tests
- Stock concurrency (Testcontainers)
- Transfer flow (create → approve → receive)
- Kardex generation
- Low stock alert triggers

### Performance Tests
- 50 concurrent stock updates
- Kardex query with 100k movements
- Bulk product import

### Test Coverage Target
- **Overall**: >= 80%
- **Domain layer**: >= 90%
- **Application layer**: >= 85%
- **Infrastructure layer**: >= 70%

---

**Status**: ⚠️ PHASE 2 - Technical Draft  
**Next Step**: Tech Lead Review → Approve → Move to PHASE 3 Planning
