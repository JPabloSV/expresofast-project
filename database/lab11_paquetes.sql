/* =====================================================================
   ExpresoFast - Laboratorio 11
   Script T-SQL para la relación 1:N Envio -> PAQUETES.

   Se ejecuta sobre la base de datos existente de los laboratorios 5 a 9
   (ExpresoFastC5K023_II2026) DESPUÉS de los scripts 01 a 06.

   Adaptación del script oficial del enunciado:
   - El enunciado referencia ENVIOS(id) con BIGINT. En esta base la tabla
     se llama dbo.Envio y su llave primaria es envio_id de tipo INT.
     Una llave foránea debe tener exactamente el mismo tipo que la llave
     que referencia, por eso PAQUETES.envio_id es INT.
   - Se agregan a dbo.Envio las columnas fecha_despacho y
     fecha_entrega_estimada que el formulario del laboratorio necesita.
   El script es idempotente: se puede ejecutar más de una vez.
   ===================================================================== */

USE ExpresoFastC5K023_II2026;
GO

/* 1. Tabla PAQUETES (relación 1:N con Envio) */
IF OBJECT_ID(N'dbo.PAQUETES', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.PAQUETES (
        id          BIGINT IDENTITY(1,1) PRIMARY KEY,
        envio_id    INT            NOT NULL,
        descripcion VARCHAR(255)   NOT NULL,
        peso_kg     DECIMAL(5,2)   NOT NULL,
        CONSTRAINT FK_Paquetes_Envios FOREIGN KEY (envio_id)
            REFERENCES dbo.Envio (envio_id) ON DELETE CASCADE
    );
END
GO

/* 2. Fechas operativas del envío */
IF COL_LENGTH(N'dbo.Envio', N'fecha_despacho') IS NULL
    ALTER TABLE dbo.Envio ADD fecha_despacho DATE NULL;
GO

IF COL_LENGTH(N'dbo.Envio', N'fecha_entrega_estimada') IS NULL
    ALTER TABLE dbo.Envio ADD fecha_entrega_estimada DATE NULL;
GO

/* 3. Regla de negocio también en la base: la entrega estimada debe ser
      estrictamente posterior al despacho (los envíos anteriores al
      laboratorio quedan con ambas fechas en NULL y no se ven afectados). */
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = N'CK_Envio_Fechas')
    ALTER TABLE dbo.Envio ADD CONSTRAINT CK_Envio_Fechas
        CHECK (fecha_entrega_estimada IS NULL
               OR fecha_despacho IS NULL
               OR fecha_entrega_estimada > fecha_despacho);
GO

/* Verificación rápida */
SELECT name AS tabla FROM sys.tables WHERE name IN (N'Envio', N'PAQUETES');
SELECT COLUMN_NAME, DATA_TYPE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = N'Envio' AND COLUMN_NAME LIKE N'fecha%';
GO
