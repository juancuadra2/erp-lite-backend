# Functional Specification: Módulo de Gestión de Inventarios

**Feature**: 07-inventory  
**Version**: 1.0  
**Created**: 2026-02-01  
**Last Updated**: 2026-02-01  
**Status**: ⏳ PHASE 2 - Awaiting Functional Review

---

## 🎯 Overview

El módulo de **Gestión de Inventarios** permite controlar productos, stock multi-bodega, movimientos de inventario, transferencias entre bodegas, unidades de medida con conversiones, y trazabilidad completa mediante kardex.

### Business Context

- **Problem**: Control manual de inventarios causa errores, roturas de stock y pérdidas económicas
- **Solution**: Sistema automatizado con validaciones, alertas, control de concurrencia y auditoría completa
- **Value**: Reduce pérdidas por stock negativo, previene roturas de stock, mejora trazabilidad y auditoría

### Scope

**In Scope**:
- CRUD de productos con múltiples códigos de barras
- Categorías jerárquicas
- Control de stock multi-bodega con mínimos y máximos
- Alertas automáticas de stock bajo
- Ajustes de inventario con auditoría
- Transferencias entre bodegas (flujo: solicitud → aprobación → recepción)
- Unidades de medida con conversiones automáticas
- Kardex detallado con exportación

**Out of Scope**:
- Gestión de bodegas (definido en módulo Warehouses)
- Módulo de compras y ventas (integración definida en sus módulos)
- Control de lotes y vencimientos (futura fase)
- Códigos QR/RFID (futura fase)

---

## 👥 User Stories

### US-01: Gestión de Productos con Múltiples Códigos de Barras 🔴 P1

**Como** encargado de inventario  
**Quiero** crear y mantener productos con toda su información (SKU, nombre, descripción, categoría, precios) y asignar múltiples códigos de barras  
**Para** facilitar búsquedas y ventas desde diferentes presentaciones del mismo producto

**Why this priority?**: Sin productos no hay inventario. Es la base fundamental del módulo.

#### Acceptance Scenarios

**✅ Scenario 1: Crear producto con información completa**
```gherkin
Given estoy autenticado con permisos de crear productos
When envío POST /api/products con:
  {
    "sku": "CC500",
    "name": "Coca Cola 500ml",
    "description": "Bebida carbonatada sabor cola",
    "categoryId": 5,
    "baseUnitId": 1,
    "purchasePrice": 1500,
    "salePrice": 2000,
    "taxTypeId": 1,
    "isInventoryManaged": true,
    "barcodes": ["7702010123456", "123456"]
  }
Then recibo status 201 con el producto creado
And el primer código "7702010123456" se marca como isPrimary=true
And se registra en AuditLog: action=PRODUCT_CREATED
```

**✅ Scenario 2: Validación de SKU único**
```gherkin
Given existe producto con SKU "CC500"
When intento crear otro producto con SKU "CC500"
Then recibo status 409 con mensaje "SKU ya existe"
```

**✅ Scenario 3: Validación de código de barras único**
```gherkin
Given existe producto con código de barras "7702010123456"
When intento asignar ese código a otro producto
Then recibo status 409 con mensaje "Código de barras ya está asignado a otro producto"
```

**✅ Scenario 4: Búsqueda por código de barras primario**
```gherkin
Given producto "CC500" tiene código primario "7702010123456"
When envío GET /api/products/by-barcode/7702010123456
Then recibo status 200 con el producto "CC500"
```

**✅ Scenario 5: Búsqueda por código de barras secundario**
```gherkin
Given producto "CC500" tiene código secundario "123456"
When envío GET /api/products/by-barcode/123456
Then recibo status 200 con el producto "CC500"
```

**✅ Scenario 6: Actualización de producto con auditoría**
```gherkin
Given producto "CC500" con precio de venta $2000
When envío PUT /api/products/{id} con {"salePrice": 2500}
Then recibo status 200 con producto actualizado
And se registra en AuditLog: oldValue={"salePrice":2000}, newValue={"salePrice":2500}
```

---

### US-02: Categorías Jerárquicas de Productos 🔴 P1

**Como** encargado de inventario  
**Quiero** organizar productos en categorías y subcategorías jerárquicas  
**Para** mejor clasificación, búsqueda y generación de reportes por categoría

**Why this priority?**: Organización básica necesaria para gestionar catálogos grandes.

#### Acceptance Scenarios

**✅ Scenario 1: Crear categoría padre**
```gherkin
Given estoy autenticado con permisos
When envío POST /api/categories con:
  {
    "name": "Bebidas",
    "code": "BEB",
    "description": "Categoría principal de bebidas",
    "parentId": null
  }
Then recibo status 201 con categoría creada
```

**✅ Scenario 2: Crear subcategoría**
```gherkin
Given existe categoría "Bebidas" (id=1)
When creo categoría con parentId=1:
  {
    "name": "Gaseosas",
    "code": "BEB-GAS",
    "parentId": 1
  }
Then recibo status 201
And la categoría "Gaseosas" es hija de "Bebidas"
```

**✅ Scenario 3: Listar productos por categoría incluyendo subcategorías**
```gherkin
Given categoría "Bebidas" tiene subcategoría "Gaseosas"
And "Gaseosas" tiene 10 productos
When envío GET /api/products?categoryId=1&includeSubcategories=true
Then recibo los 10 productos de "Gaseosas"
```

**✅ Scenario 4: Validación de eliminación de categoría con productos**
```gherkin
Given categoría "Gaseosas" tiene 5 productos asignados
When intento DELETE /api/categories/{id}
Then recibo status 409 con mensaje "No se puede eliminar categoría con productos asignados"
```

---

### US-03: Control de Stock Multi-bodega con Mínimos y Máximos 🔴 P1

**Como** encargado de inventario  
**Quiero** controlar stock de cada producto en cada bodega con niveles mínimos y máximos  
**Para** recibir alertas de reabastecimiento y prevenir roturas de stock

**Why this priority?**: Control de stock es el núcleo del módulo de inventario.

#### Acceptance Scenarios

**✅ Scenario 1: Configurar stock inicial en bodega**
```gherkin
Given producto "CC500" (id=10) y Bodega Principal (id=1)
When envío POST /api/stock/initialize con:
  {
    "productId": 10,
    "warehouseId": 1,
    "quantity": 50,
    "minQuantity": 20,
    "maxQuantity": 100,
    "averageCost": 1500
  }
Then recibo status 201 con stock creado
And se registra movimiento tipo INITIAL con cantidad 50
```

**✅ Scenario 2: Consultar stock por producto y bodega**
```gherkin
Given producto "CC500" tiene stock en 3 bodegas:
  - Principal (50), Sucursal1 (30), Sucursal2 (20)
When envío GET /api/products/10/stock
Then recibo status 200 con totalQuantity=100 y detalle por bodega
```

**✅ Scenario 3: Validación de stock negativo**
```gherkin
Given producto con stock actual de 10 unidades en bodega
When intento descontar 15 unidades
Then recibo status 400 con mensaje "Stock insuficiente. Disponible: 10, Requerido: 15"
```

**✅ Scenario 4: Control de concurrencia optimista con @Version**
```gherkin
Given stock actual es 100 con version=1
When dos usuarios intentan descontar simultáneamente:
  - Usuario A: -30 unidades
  - Usuario B: -40 unidades
Then uno de los requests recibe OptimisticLockException
And debe reintentar con versión actualizada
And la suma final es correcta (100 - 30 - 40 = 30)
```

---

### US-04: Alertas de Stock Bajo 🔴 P1

**Como** encargado de inventario  
**Quiero** recibir alertas automáticas cuando el stock cae por debajo del mínimo  
**Para** poder reabastecer a tiempo y prevenir pérdida de ventas

**Why this priority?**: Previene roturas de stock y pérdida de ventas.

#### Acceptance Scenarios

**✅ Scenario 1: Alerta generada cuando stock cae bajo mínimo**
```gherkin
Given producto "CC500" en Bodega Principal:
  - stock=25, minQuantity=20
When se realiza venta de 10 unidades
Then stock queda en 15
And sistema genera alerta automática
And GET /api/stock/low-alerts retorna el producto
```

**✅ Scenario 2: Dashboard de productos con stock crítico**
```gherkin
Given 5 productos con stock por debajo del mínimo en diferentes bodegas
When envío GET /api/stock/low-alerts
Then recibo status 200 con lista de productos y suggestedOrder = maxQuantity - currentQuantity
```

**✅ Scenario 3: Alerta se elimina cuando stock vuelve sobre mínimo**
```gherkin
Given producto con alerta activa (stock=15, min=20)
When se recibe compra de 20 unidades (stock=35)
Then la alerta desaparece de GET /api/stock/low-alerts
```

---

### US-05: Ajustes de Inventario con Auditoría 🔴 P1

**Como** encargado de bodega  
**Quiero** realizar ajustes de inventario (positivos o negativos) con motivo y auditoría completa  
**Para** corregir diferencias entre sistema y conteo físico

**Why this priority?**: Necesario para mantener integridad entre sistema y realidad física.

#### Acceptance Scenarios

**✅ Scenario 1: Ajuste positivo de inventario**
```gherkin
Given producto "CC500" tiene stock de 95 en Bodega Principal
When envío POST /api/stock/adjust con:
  {
    "productId": 10,
    "warehouseId": 1,
    "quantity": 5,
    "reason": "Ajuste por conteo físico",
    "notes": "Se encontraron 5 unidades no registradas"
  }
Then recibo status 200
And stock queda en 100
And se registra InventoryMovement tipo=ADJUSTMENT, quantity=+5
And se audita en AuditLog con usuario que hizo el ajuste
```

**✅ Scenario 2: Ajuste negativo por merma**
```gherkin
Given producto con stock de 100
When ajusto con quantity=-5 y reason="Merma por rotura"
Then stock queda en 95
And se registra movimiento con quantity=-5
```

**✅ Scenario 3: Validación de permisos para ajustes**
```gherkin
Given usuario "vendedor" sin permiso "ADJUST_INVENTORY"
When intenta realizar ajuste
Then recibo status 403 con mensaje "No tiene permisos para ajustar inventario"
```

**✅ Scenario 4: Ajuste no puede dejar stock negativo**
```gherkin
Given producto con stock=10
When intento ajustar con quantity=-15
Then recibo status 400 con mensaje "Ajuste resultaría en stock negativo (10 - 15 = -5)"
```

---

### US-06: Transferencias entre Bodegas con Flujo de Aprobación 🔴 P1

**Como** encargado de bodega  
**Quiero** transferir productos entre bodegas con un flujo completo: solicitud → aprobación → recepción  
**Para** tener trazabilidad y actualización de stock en origen y destino

**Why this priority?**: Esencial para empresas multi-bodega, permite distribución controlada de inventario.

#### Acceptance Scenarios

**✅ Scenario 1: Crear solicitud de transferencia**
```gherkin
Given Bodega Principal tiene 100 unidades de "CC500"
When envío POST /api/transfers con:
  {
    "fromWarehouseId": 1,
    "toWarehouseId": 2,
    "items": [{"productId": 10, "quantity": 30, "unitId": 1}],
    "notes": "Reabastecimiento sucursal"
  }
Then recibo status 201 con transferencia creada
And estado = PENDING
And se asigna transfer_number: "TRF-2026-0001"
```

**✅ Scenario 2: Aprobar transferencia**
```gherkin
Given transferencia TRF-2026-0001 en estado PENDING
When usuario con permiso "APPROVE_TRANSFER" envía POST /api/transfers/{id}/approve
Then estado cambia a APPROVED
And approved_by = userId actual
And stock aún NO se modifica (espera recepción)
```

**✅ Scenario 3: Recibir transferencia completa**
```gherkin
Given transferencia TRF-2026-0001 APPROVED con 30 unidades
When envío POST /api/transfers/{id}/receive con:
  {"items": [{"productId": 10, "quantityReceived": 30}]}
Then estado cambia a RECEIVED
And stock Bodega Principal: 100 - 30 = 70
And stock Bodega Sucursal: original + 30
And se registran 2 movimientos: TRANSFER_OUT y TRANSFER_IN
```

**✅ Scenario 4: Recepción parcial con diferencias**
```gherkin
Given transferencia APPROVED de 50 unidades
When recibo solo 45 unidades (5 dañadas en tránsito)
Then estado cambia a RECEIVED
And stock origen: -50 (lo que se envió)
And stock destino: +45 (lo que se recibió)
And las 5 unidades faltantes quedan documentadas en notes
```

**✅ Scenario 5: Cancelar transferencia pendiente**
```gherkin
Given transferencia en estado PENDING o APPROVED
When envío POST /api/transfers/{id}/cancel
Then estado cambia a CANCELLED
And no se afecta stock
And no se puede recibir después de cancelada
```

**✅ Scenario 6: Validación de stock suficiente en origen**
```gherkin
Given Bodega Principal tiene 20 unidades de producto
When intento crear transferencia de 30 unidades
Then recibo status 400 con mensaje "Stock insuficiente en bodega origen"
```

---

### US-07: Unidades de Medida con Conversiones 🟡 P2

**Como** encargado de inventario  
**Quiero** gestionar productos con diferentes unidades de medida (unidad, caja, pallet) y conversiones automáticas  
**Para** ventas y compras flexibles (vender por unidad o por caja)

**Why this priority?**: Importante para B2B, pero puede implementarse después del stock básico.

#### Acceptance Scenarios

**✅ Scenario 1: Configurar conversión de unidades**
```gherkin
Given producto "CC500" con base "unidad"
When envío POST /api/products/{id}/unit-conversions con:
  {"fromUnitId": 2, "toUnitId": 1, "conversionFactor": 12}
Then se registra que 1 caja (id=2) = 12 unidades (id=1)
```

**✅ Scenario 2: Venta en unidad alternativa con conversión automática**
```gherkin
Given producto con stock de 100 unidades y conversión 1 caja = 12 unidades
When vendo 2 cajas
Then stock se descuenta 24 unidades (2 × 12)
And stock final = 76 unidades
```

---

### US-08: Kardex Detallado por Producto y Bodega 🟡 P2

**Como** encargado de inventario  
**Quiero** consultar el kardex (historial de movimientos) de cualquier producto en cualquier bodega  
**Para** auditar y analizar movimientos de inventario

**Why this priority?**: Importante para auditoría, pero puede implementarse después de tener movimientos básicos funcionando.

#### Acceptance Scenarios

**✅ Scenario 1: Consultar kardex de producto en bodega específica**
```gherkin
Given producto "CC500" con múltiples movimientos en Bodega Principal
When envío GET /api/products/10/kardex?warehouseId=1&startDate=2026-01-01&endDate=2026-01-31
Then recibo status 200 con lista de movimientos ordenados por fecha
And cada movimiento muestra: date, type, quantity, balance, user
```

**✅ Scenario 2: Filtrado de kardex por tipo de movimiento**
```gherkin
Given producto con movimientos SALE, PURCHASE, ADJUSTMENT, TRANSFER
When consulto GET /api/products/10/kardex?type=SALE
Then solo recibo movimientos de tipo SALE
```

**✅ Scenario 3: Exportar kardex a Excel**
```gherkin
Given kardex de producto con 100+ movimientos
When envío GET /api/products/10/kardex/export?format=xlsx
Then recibo archivo Excel con todos los movimientos formateados
```

---

## 📋 Business Rules

### BR-01: Unicidad de Identificadores

- **R01.1**: SKU debe ser único en toda la base de datos
- **R01.2**: Código de barras debe ser único en toda la base de datos
- **R01.3**: Primer código de barras agregado se marca automáticamente como primary

### BR-02: Validaciones de Stock

- **R02.1**: Stock nunca puede ser negativo (quantity >= 0)
- **R02.2**: Control de concurrencia optimista obligatorio con @Version
- **R02.3**: Stock se almacena siempre en unidad base del producto
- **R02.4**: Costo promedio se recalcula con cada entrada de inventario

### BR-03: Alertas de Stock

- **R03.1**: Alerta se genera automáticamente cuando stock < minQuantity
- **R03.2**: Alerta se elimina automáticamente cuando stock >= minQuantity
- **R03.3**: suggestedOrder = maxQuantity - currentQuantity

### BR-04: Flujo de Transferencias

- **R04.1**: Estados válidos: PENDING → APPROVED → RECEIVED o CANCELLED
- **R04.2**: Stock se descuenta de origen solo al RECIBIR (no al aprobar)
- **R04.3**: Stock se suma a destino solo al RECIBIR
- **R04.4**: No se puede recibir más de lo transferido
- **R04.5**: Se permite recepción parcial con diferencias documentadas

### BR-05: Auditoría de Movimientos

- **R05.1**: Todos los movimientos de inventario se registran en InventoryMovement
- **R05.2**: Ajustes requieren motivo obligatorio (reason)
- **R05.3**: Cada movimiento registra: tipo, cantidad, costo, usuario, timestamp
- **R05.4**: Movimientos son inmutables (no se pueden editar ni eliminar)

### BR-06: Soft Delete

- **R06.1**: Productos se eliminan con soft delete (deletedAt)
- **R06.2**: No se puede eliminar producto con stock > 0
- **R06.3**: No se puede eliminar producto con movimientos históricos

---

## 🔌 API Contracts

### Products API

| Endpoint | Method | Description | Auth Required |
|----------|--------|-------------|---------------|
| `/api/products` | POST | Crear producto | PRODUCT_CREATE |
| `/api/products/{id}` | GET | Obtener producto | PRODUCT_READ |
| `/api/products/{id}` | PUT | Actualizar producto | PRODUCT_UPDATE |
| `/api/products/{id}` | DELETE | Eliminar producto (soft) | PRODUCT_DELETE |
| `/api/products` | GET | Listar productos (paginado) | PRODUCT_READ |
| `/api/products/by-barcode/{barcode}` | GET | Buscar por código de barras | PRODUCT_READ |

### Categories API

| Endpoint | Method | Description | Auth Required |
|----------|--------|-------------|---------------|
| `/api/categories` | POST | Crear categoría | CATEGORY_CREATE |
| `/api/categories/{id}` | GET | Obtener categoría | CATEGORY_READ |
| `/api/categories/{id}` | PUT | Actualizar categoría | CATEGORY_UPDATE |
| `/api/categories/{id}` | DELETE | Eliminar categoría | CATEGORY_DELETE |
| `/api/categories` | GET | Listar categorías | CATEGORY_READ |

### Stock API

| Endpoint | Method | Description | Auth Required |
|----------|--------|-------------|---------------|
| `/api/stock/initialize` | POST | Inicializar stock | STOCK_INITIALIZE |
| `/api/stock/adjust` | POST | Ajustar stock | STOCK_ADJUST |
| `/api/products/{id}/stock` | GET | Consultar stock por producto | STOCK_READ |
| `/api/stock/low-alerts` | GET | Dashboard de alertas | STOCK_READ |

### Transfers API

| Endpoint | Method | Description | Auth Required |
|----------|--------|-------------|---------------|
| `/api/transfers` | POST | Crear transferencia | TRANSFER_CREATE |
| `/api/transfers/{id}` | GET | Obtener transferencia | TRANSFER_READ |
| `/api/transfers` | GET | Listar transferencias | TRANSFER_READ |
| `/api/transfers/{id}/approve` | POST | Aprobar transferencia | TRANSFER_APPROVE |
| `/api/transfers/{id}/receive` | POST | Recibir transferencia | TRANSFER_RECEIVE |
| `/api/transfers/{id}/cancel` | POST | Cancelar transferencia | TRANSFER_CANCEL |

### Kardex API

| Endpoint | Method | Description | Auth Required |
|----------|--------|-------------|---------------|
| `/api/products/{id}/kardex` | GET | Consultar kardex | KARDEX_READ |
| `/api/products/{id}/kardex/export` | GET | Exportar kardex | KARDEX_EXPORT |

---

## ✅ Success Metrics

| Metric | Target | Measurement Method |
|--------|--------|-------------------|
| Producto creado | < 1 min | User timing test |
| Búsqueda por barcode | < 100ms p95 | APM logs |
| Stock negativo prevenido | 100% | Integration tests |
| Concurrencia manejada | 100% | Testcontainers tests |
| Alertas generadas | < 1s | Event timing logs |
| Transferencia completa | 100% success | E2E tests |
| Movimientos en kardex | 100% | Database audit |
| Costo promedio calculado | 100% accurate | Unit tests |
| Test coverage | >= 80% | JaCoCo |

---

**Status**: ⏳ PHASE 2 - Functional Draft  
**Next Step**: Product Owner Review → Approve → Move to Technical Design
