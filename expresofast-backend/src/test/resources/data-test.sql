INSERT INTO Conductor (nombre, apellidos, licencia, telefono, activo) VALUES
    ('Carlos', 'Mora Rojas', 'LIC-001', '8888-0001', 1),
    ('Laura', 'Brenes Solano', 'LIC-002', '8888-0002', 0);

INSERT INTO Vehiculo (placa, capacidad_kg, estado) VALUES
    ('CL-100', 99999.00, 'DISPONIBLE'),
    ('CL-200', 10.00, 'DISPONIBLE');

INSERT INTO Envio (codigo_rastreo, destinatario, direccion_destino, peso_kg, costo, estado_envio,
                   vehiculo_id, conductor_id, fecha_creacion, fecha_modificacion)
VALUES ('EXP-1001', 'Jorge Castro Mena', 'San Jose, Barrio Escalante', 12.50, 8500.00, 'PENDIENTE',
        1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
