-- =============================================================================
-- DATA EVOLUTION SEEDER: V2__insert_test_data.sql
-- Microservicio de Vehículos (FleetOps - Core Storage Layer)
-- Objetivo: Carga completa de 10 tipos y 100 vehículos (10 por tipo)
-- Corrección: Formato de Placas Colombianas y campos de fechas obligatorios aplicados
-- =============================================================================

-- SECCIÓN 1: TIPOS DE VEHÍCULO (10 Tipos)
INSERT INTO tipos_vehiculo (id_tipo_vehiculo, nombre_tipo, descripcion, capacidad_carga) VALUES
(1, 'Camion Carga Pesada', 'Transporte de carga pesada nacional', 20000.50),
(2, 'Bus Urbano', 'Transporte de pasajeros perímetro ciudad', 4500.00),
(3, 'Furgon Refrigerado', 'Transporte de alimentos refrigerados', 5000.75),
(4, 'Camioneta 4x4', 'Operaciones en campo y talleres', 800.25),
(5, 'Bus Intermunicipal', 'Transporte de pasajeros ciudades', 5000.00),
(6, 'Motocicleta Mensajeria', 'Entrega rápida urbana', 150.00),
(7, 'Camion Volquete', 'Transporte de materiales de construcción', 15000.00),
(8, 'Van de Carga', 'Transporte de carga liviana', 2500.50),
(9, 'Automovil Ejecutivo', 'Transporte administrativo', 0.00),
(10, 'Grua Plataforma', 'Recuperación de vehículos', 3500.00);

-- =============================================================================
-- CORRECCIÓN DE LA SECUENCIA DE LA TABLA (EVITA ERROR DE DUPLICATE KEY)
-- =============================================================================
-- Como forzamos la inserción de los IDs 1 al 10 manualmente, debemos decirle 
-- al contador automático (BIGSERIAL) de PostgreSQL que salte al número 10.
SELECT setval(
    pg_get_serial_sequence('tipos_vehiculo', 'id_tipo_vehiculo'), 
    (SELECT COALESCE(MAX(id_tipo_vehiculo), 1) FROM tipos_vehiculo)
);


-- SECCIÓN 2: VEHÍCULOS (100 Vehículos - 10 por cada tipo)
INSERT INTO vehiculos (numero_placa, marca, modelo, anio_fabricacion, color, numero_chasis, numero_motor, kilometraje, ciudad_operacion, sede_operacion, estado_vehiculo, id_tipo_vehiculo, fecha_soat, fecha_rtm, fecha_ultimo_mant) VALUES
-- TIPO 1: Camión Carga Pesada (Placas TWA)
('TWA101','Volvo','FH16',2022,'Blanco','CH101','MOT101',120000,'Bogotá','Terminal Norte','DISPONIBLE',1, '2026-12-31', '2026-12-31', '2026-05-15'),
('TWA102','Scania','R450',2021,'Blanco','CH102','MOT102',150000,'Bogotá','Terminal Norte','DISPONIBLE',1, '2026-12-31', '2026-12-31', '2026-05-15'),
('TWA103','Kenworth','T800',2023,'Gris','CH103','MOT103',45000,'Bogotá','Terminal Norte','DISPONIBLE',1, '2026-12-31', '2026-12-31', '2026-05-15'),
('TWA104','Freightliner','Cascadia',2022,'Blanco','CH104','MOT104',98000,'Bogotá','Terminal Norte','DISPONIBLE',1, '2026-12-31', '2026-12-31', '2026-05-15'),
('TWA105','Volvo','FH16',2024,'Blanco','CH105','MOT105',10000,'Bogotá','Terminal Norte','DISPONIBLE',1, '2026-12-31', '2026-12-31', '2026-05-15'),
('TWA106','Mack','Anthem',2020,'Azul','CH106','MOT106',210000,'Bogotá','Terminal Norte','DISPONIBLE',1, '2026-12-31', '2026-12-31', '2026-05-15'),
('TWA107','Scania','S500',2022,'Rojo','CH107','MOT107',85000,'Bogotá','Terminal Norte','DISPONIBLE',1, '2026-12-31', '2026-12-31', '2026-05-15'),
('TWA108','Freightliner','Cascadia',2023,'Blanco','CH108','MOT108',35000,'Bogotá','Terminal Norte','DISPONIBLE',1, '2026-12-31', '2026-12-31', '2026-05-15'),
('TWA109','Volvo','FH16',2021,'Blanco','CH109','MOT109',140000,'Bogotá','Terminal Norte','DISPONIBLE',1, '2026-12-31', '2026-12-31', '2026-05-15'),
('TWA110','International','LT',2022,'Verde','CH110','MOT110',92000,'Bogotá','Terminal Norte','DISPONIBLE',1, '2026-12-31', '2026-12-31', '2026-05-15'),

-- TIPO 2: Bus Urbano (Placas SAB)
('SAB201','Mercedes','O500',2022,'Azul','CH201','MOT201',50000,'Medellín','Terminal Sur','DISPONIBLE',2, '2026-12-31', '2026-12-31', '2026-05-15'),
('SAB202','Scania','K250',2021,'Azul','CH202','MOT202',65000,'Medellín','Terminal Sur','DISPONIBLE',2, '2026-12-31', '2026-12-31', '2026-05-15'),
('SAB203','Volvo','B270F',2023,'Azul','CH203','MOT203',20000,'Medellín','Terminal Sur','DISPONIBLE',2, '2026-12-31', '2026-12-31', '2026-05-15'),
('SAB204','Mercedes','O500',2020,'Blanco','CH204','MOT204',90000,'Medellín','Terminal Sur','DISPONIBLE',2, '2026-12-31', '2026-12-31', '2026-05-15'),
('SAB205','Hino','FB8',2022,'Azul','CH205','MOT205',40000,'Medellín','Terminal Sur','DISPONIBLE',2, '2026-12-31', '2026-12-31', '2026-05-15'),
('SAB206','Scania','K250',2021,'Azul','CH206','MOT206',75000,'Medellín','Terminal Sur','DISPONIBLE',2, '2026-12-31', '2026-12-31', '2026-05-15'),
('SAB207','Mercedes','O500',2024,'Azul','CH207','MOT207',5000,'Medellín','Terminal Sur','DISPONIBLE',2, '2026-12-31', '2026-12-31', '2026-05-15'),
('SAB208','Volvo','B270F',2022,'Blanco','CH208','MOT208',35000,'Medellín','Terminal Sur','DISPONIBLE',2, '2026-12-31', '2026-12-31', '2026-05-15'),
('SAB209','Hino','FB8',2023,'Azul','CH209','MOT209',25000,'Medellín','Terminal Sur','DISPONIBLE',2, '2026-12-31', '2026-12-31', '2026-05-15'),
('SAB210','Mercedes','O500',2021,'Azul','CH210','MOT210',80000,'Medellín','Terminal Sur','DISPONIBLE',2, '2026-12-31', '2026-12-31', '2026-05-15'),

-- TIPO 3: Furgón Refrigerado (Placas FRG)
('FRG301','Renault','Master',2022,'Blanco','CH301','MOT301',30000,'Cali','Depósito','DISPONIBLE',3, '2026-12-31', '2026-12-31', '2026-05-15'),
('FRG302','Iveco','Daily',2021,'Blanco','CH302','MOT302',45000,'Cali','Depósito','DISPONIBLE',3, '2026-12-31', '2026-12-31', '2026-05-15'),
('FRG303','Mercedes','Sprinter',2023,'Blanco','CH303','MOT303',15000,'Cali','Depósito','DISPONIBLE',3, '2026-12-31', '2026-12-31', '2026-05-15'),
('FRG304','Renault','Master',2022,'Blanco','CH304','MOT304',35000,'Cali','Depósito','DISPONIBLE',3, '2026-12-31', '2026-12-31', '2026-05-15'),
('FRG305','Iveco','Daily',2020,'Blanco','CH305','MOT305',60000,'Cali','Depósito','DISPONIBLE',3, '2026-12-31', '2026-12-31', '2026-05-15'),
('FRG306','Mercedes','Sprinter',2024,'Blanco','CH306','MOT306',5000,'Cali','Depósito','DISPONIBLE',3, '2026-12-31', '2026-12-31', '2026-05-15'),
('FRG307','Renault','Master',2021,'Blanco','CH307','MOT307',55000,'Cali','Depósito','DISPONIBLE',3, '2026-12-31', '2026-12-31', '2026-05-15'),
('FRG308','Iveco','Daily',2022,'Blanco','CH308','MOT308',25000,'Cali','Depósito','DISPONIBLE',3, '2026-12-31', '2026-12-31', '2026-05-15'),
('FRG309','Mercedes','Sprinter',2023,'Blanco','CH309','MOT309',20000,'Cali','Depósito','DISPONIBLE',3, '2026-12-31', '2026-12-31', '2026-05-15'),
('FRG310','Renault','Master',2020,'Blanco','CH310','MOT310',70000,'Cali','Depósito','DISPONIBLE',3, '2026-12-31', '2026-12-31', '2026-05-15'),

-- TIPO 4: Camioneta 4x4 (Placas CXT)
('CXT401','Toyota','Hilux',2023,'Gris','CH401','MOT401',10000,'Cali','Campo','DISPONIBLE',4, '2026-12-31', '2026-12-31', '2026-05-15'),
('CXT402','Ford','Ranger',2022,'Negro','CH402','MOT402',20000,'Cali','Campo','DISPONIBLE',4, '2026-12-31', '2026-12-31', '2026-05-15'),
('CXT403','Mitsubishi','L200',2024,'Blanco','CH403','MOT403',2000,'Cali','Campo','DISPONIBLE',4, '2026-12-31', '2026-12-31', '2026-05-15'),
('CXT404','Toyota','Hilux',2021,'Gris','CH404','MOT404',30000,'Cali','Campo','DISPONIBLE',4, '2026-12-31', '2026-12-31', '2026-05-15'),
('CXT405','Nissan','Frontier',2022,'Gris','CH405','MOT405',25000,'Cali','Campo','DISPONIBLE',4, '2026-12-31', '2026-12-31', '2026-05-15'),
('CXT406','Toyota','Hilux',2023,'Negro','CH406','MOT406',15000,'Cali','Campo','DISPONIBLE',4, '2026-12-31', '2026-12-31', '2026-05-15'),
('CXT407','Ford','Ranger',2020,'Gris','CH407','MOT407',40000,'Cali','Campo','DISPONIBLE',4, '2026-12-31', '2026-12-31', '2026-05-15'),
('CXT408','Mitsubishi','L200',2022,'Blanco','CH408','MOT408',22000,'Cali','Campo','DISPONIBLE',4, '2026-12-31', '2026-12-31', '2026-05-15'),
('CXT409','Toyota','Hilux',2024,'Gris','CH409','MOT409',1000,'Cali','Campo','DISPONIBLE',4, '2026-12-31', '2026-12-31', '2026-05-15'),
('CXT410','Nissan','Frontier',2021,'Negro','CH410','MOT410',35000,'Cali','Campo','DISPONIBLE',4, '2026-12-31', '2026-12-31', '2026-05-15'),

-- TIPO 5: Bus Intermunicipal (Placas BIM)
('BIM501','Scania','K440',2022,'Blanco','CH501','MOT501',110000,'Barranquilla','Terminal','DISPONIBLE',5, '2026-12-31', '2026-12-31', '2026-05-15'),
('BIM502','Volvo','B430R',2021,'Rojo','CH502','MOT502',130000,'Barranquilla','Terminal','DISPONIBLE',5, '2026-12-31', '2026-12-31', '2026-05-15'),
('BIM503','Scania','K440',2023,'Blanco','CH503','MOT503',60000,'Barranquilla','Terminal','DISPONIBLE',5, '2026-12-31', '2026-12-31', '2026-05-15'),
('BIM504','Volvo','B430R',2020,'Blanco','CH504','MOT504',180000,'Barranquilla','Terminal','DISPONIBLE',5, '2026-12-31', '2026-12-31', '2026-05-15'),
('BIM505','Scania','K440',2022,'Blanco','CH505','MOT505',90000,'Barranquilla','Terminal','DISPONIBLE',5, '2026-12-31', '2026-12-31', '2026-05-15'),
('BIM506','Volvo','B430R',2024,'Blanco','CH506','MOT506',20000,'Barranquilla','Terminal','DISPONIBLE',5, '2026-12-31', '2026-12-31', '2026-05-15'),
('BIM507','Scania','K440',2021,'Rojo','CH507','MOT507',120000,'Barranquilla','Terminal','DISPONIBLE',5, '2026-12-31', '2026-12-31', '2026-05-15'),
('BIM508','Volvo','B430R',2023,'Blanco','CH508','MOT508',50000,'Barranquilla','Terminal','DISPONIBLE',5, '2026-12-31', '2026-12-31', '2026-05-15'),
('BIM509','Scania','K440',2020,'Blanco','CH509','MOT509',200000,'Barranquilla','Terminal','DISPONIBLE',5, '2026-12-31', '2026-12-31', '2026-05-15'),
('BIM510','Volvo','B430R',2022,'Blanco','CH510','MOT510',80000,'Barranquilla','Terminal','DISPONIBLE',5, '2026-12-31', '2026-12-31', '2026-05-15'),

-- TIPO 6: Motocicleta Mensajería (Placas Especiales Motos)
('MOT01A','Honda','CB125',2024,'Negro','CH601','MOT601',1000,'Bogotá','Centro','DISPONIBLE',6, '2026-12-31', '2026-12-31', '2026-05-15'),
('MOT02B','Yamaha','FZ15',2023,'Negro','CH602','MOT602',5000,'Bogotá','Centro','DISPONIBLE',6, '2026-12-31', '2026-12-31', '2026-05-15'),
('MOT03C','Honda','CB125',2024,'Negro','CH603','MOT603',2000,'Bogotá','Centro','DISPONIBLE',6, '2026-12-31', '2026-12-31', '2026-05-15'),
('MOT04D','Bajaj','Pulsar',2023,'Rojo','CH604','MOT604',8000,'Bogotá','Centro','DISPONIBLE',6, '2026-12-31', '2026-12-31', '2026-05-15'),
('MOT05E','Honda','CB125',2022,'Negro','CH605','MOT605',15000,'Bogotá','Centro','DISPONIBLE',6, '2026-12-31', '2026-12-31', '2026-05-15'),
('MOT06F','Yamaha','FZ15',2024,'Azul','CH606','MOT606',1000,'Bogotá','Centro','DISPONIBLE',6, '2026-12-31', '2026-12-31', '2026-05-15'),
('MOT07G','Honda','CB125',2023,'Negro','CH607','MOT607',6000,'Bogotá','Centro','DISPONIBLE',6, '2026-12-31', '2026-12-31', '2026-05-15'),
('MOT08H','Bajaj','Pulsar',2024,'Negro','CH608','MOT608',3000,'Bogotá','Centro','DISPONIBLE',6, '2026-12-31', '2026-12-31', '2026-05-15'),
('MOT09I','Honda','CB125',2023,'Negro','CH609','MOT609',9000,'Bogotá','Centro','DISPONIBLE',6, '2026-12-31', '2026-12-31', '2026-05-15'),
('MOT10J','Yamaha','FZ15',2022,'Negro','CH610','MOT610',12000,'Bogotá','Centro','DISPONIBLE',6, '2026-12-31', '2026-12-31', '2026-05-15'),

-- TIPO 7: Camión Volquete (Placas VOL)
('VOL701','Caterpillar','770G',2022,'Amarillo','CH701','MOT701',50000,'Cali','Obra','DISPONIBLE',7, '2026-12-31', '2026-12-31', '2026-05-15'),
('VOL702','Komatsu','HD465',2021,'Amarillo','CH702','MOT702',60000,'Cali','Obra','DISPONIBLE',7, '2026-12-31', '2026-12-31', '2026-05-15'),
('VOL703','Caterpillar','770G',2023,'Amarillo','CH703','MOT703',20000,'Cali','Obra','DISPONIBLE',7, '2026-12-31', '2026-12-31', '2026-05-15'),
('VOL704','Komatsu','HD465',2022,'Amarillo','CH704','MOT704',35000,'Cali','Obra','DISPONIBLE',7, '2026-12-31', '2026-12-31', '2026-05-15'),
('VOL705','Caterpillar','770G',2020,'Amarillo','CH705','MOT705',80000,'Cali','Obra','DISPONIBLE',7, '2026-12-31', '2026-12-31', '2026-05-15'),
('VOL706','Komatsu','HD465',2024,'Amarillo','CH706','MOT706',5000,'Cali','Obra','DISPONIBLE',7, '2026-12-31', '2026-12-31', '2026-05-15'),
('VOL707','Caterpillar','770G',2022,'Amarillo','CH707','MOT707',40000,'Cali','Obra','DISPONIBLE',7, '2026-12-31', '2026-12-31', '2026-05-15'),
('VOL708','Komatsu','HD465',2023,'Amarillo','CH708','MOT708',15000,'Cali','Obra','DISPONIBLE',7, '2026-12-31', '2026-12-31', '2026-05-15'),
('VOL709','Caterpillar','770G',2021,'Amarillo','CH709','MOT709',55000,'Cali','Obra','DISPONIBLE',7, '2026-12-31', '2026-12-31', '2026-05-15'),
('VOL710','Komatsu','HD465',2022,'Amarillo','CH710','MOT710',45000,'Cali','Obra','DISPONIBLE',7, '2026-12-31', '2026-12-31', '2026-05-15'),

-- TIPO 8: Van de Carga (Placas VAN)
('VAN801','Ford','Transit',2022,'Blanco','CH801','MOT801',40000,'Medellín','Logística','DISPONIBLE',8, '2026-12-31', '2026-12-31', '2026-05-15'),
('VAN802','Chevrolet','N300',2021,'Blanco','CH802','MOT802',55000,'Medellín','Logística','DISPONIBLE',8, '2026-12-31', '2026-12-31', '2026-05-15'),
('VAN803','Ford','Transit',2023,'Blanco','CH803','MOT803',20000,'Medellín','Logística','DISPONIBLE',8, '2026-12-31', '2026-12-31', '2026-05-15'),
('VAN804','Chevrolet','N300',2022,'Blanco','CH804','MOT804',35000,'Medellín','Logística','DISPONIBLE',8, '2026-12-31', '2026-12-31', '2026-05-15'),
('VAN805','Ford','Transit',2020,'Blanco','CH805','MOT805',70000,'Medellín','Logística','DISPONIBLE',8, '2026-12-31', '2026-12-31', '2026-05-15'),
('VAN806','Chevrolet','N300',2024,'Blanco','CH806','MOT806',8000,'Medellín','Logística','DISPONIBLE',8, '2026-12-31', '2026-12-31', '2026-05-15'),
('VAN807','Ford','Transit',2021,'Blanco','CH807','MOT807',50000,'Medellín','Logística','DISPONIBLE',8, '2026-12-31', '2026-12-31', '2026-05-15'),
('VAN808','Chevrolet','N300',2023,'Blanco','CH808','MOT808',25000,'Medellín','Logística','DISPONIBLE',8, '2026-12-31', '2026-12-31', '2026-05-15'),
('VAN809','Ford','Transit',2022,'Blanco','CH809','MOT809',30000,'Medellín','Logística','DISPONIBLE',8, '2026-12-31', '2026-12-31', '2026-05-15'),
('VAN810','Chevrolet','N300',2020,'Blanco','CH810','MOT810',85000,'Medellín','Logística','DISPONIBLE',8, '2026-12-31', '2026-12-31', '2026-05-15'),

-- TIPO 9: Automóvil Ejecutivo (Placas AUT)
('AUT901','Toyota','Corolla',2024,'Negro','CH901','MOT901',5000,'Bogotá','Gerencia','DISPONIBLE',9, '2026-12-31', '2026-12-31', '2026-05-15'),
('AUT902','Mazda','Mazda6',2023,'Gris','CH902','MOT902',15000,'Bogotá','Gerencia','DISPONIBLE',9, '2026-12-31', '2026-12-31', '2026-05-15'),
('AUT903','Toyota','Corolla',2022,'Blanco','CH903','MOT903',25000,'Bogotá','Gerencia','DISPONIBLE',9, '2026-12-31', '2026-12-31', '2026-05-15'),
('AUT904','Mazda','Mazda6',2024,'Negro','CH904','MOT904',2000,'Bogotá','Gerencia','DISPONIBLE',9, '2026-12-31', '2026-12-31', '2026-05-15'),
('AUT905','Toyota','Corolla',2023,'Negro','CH905','MOT905',10000,'Bogotá','Gerencia','DISPONIBLE',9, '2026-12-31', '2026-12-31', '2026-05-15'),
('AUT906','Mazda','Mazda6',2022,'Gris','CH906','MOT906',30000,'Bogotá','Gerencia','DISPONIBLE',9, '2026-12-31', '2026-12-31', '2026-05-15'),
('AUT907','Toyota','Corolla',2021,'Negro','CH907','MOT907',40000,'Bogotá','Gerencia','DISPONIBLE',9, '2026-12-31', '2026-12-31', '2026-05-15'),
('AUT908','Mazda','Mazda6',2023,'Negro','CH908','MOT908',12000,'Bogotá','Gerencia','DISPONIBLE',9, '2026-12-31', '2026-12-31', '2026-05-15'),
('AUT909','Toyota','Corolla',2024,'Blanco','CH909','MOT909',1000,'Bogotá','Gerencia','DISPONIBLE',9, '2026-12-31', '2026-12-31', '2026-05-15'),
('AUT910','Mazda','Mazda6',2022,'Gris','CH910','MOT910',22000,'Bogotá','Gerencia','DISPONIBLE',9, '2026-12-31', '2026-12-31', '2026-05-15'),

-- TIPO 10: Grúa Plataforma (Placas GRU)
('GRU001','Isuzu','NKR',2022,'Amarillo','CH1001','MOT1001',90000,'Cali','Servicio','DISPONIBLE',10, '2026-12-31', '2026-12-31', '2026-05-15'),
('GRU002','Chevrolet','FVR',2021,'Amarillo','CH1002','MOT1002',110000,'Cali','Servicio','DISPONIBLE',10, '2026-12-31', '2026-12-31', '2026-05-15'),
('GRU003','Isuzu','NKR',2023,'Amarillo','CH1003','MOT1003',40000,'Cali','Servicio','DISPONIBLE',10, '2026-12-31', '2026-12-31', '2026-05-15'),
('GRU004','Chevrolet','FVR',2022,'Amarillo','CH1004','MOT1004',70000,'Cali','Servicio','DISPONIBLE',10, '2026-12-31', '2026-12-31', '2026-05-15'),
('GRU005','Isuzu','NKR',2020,'Amarillo','CH1005','MOT1005',130000,'Cali','Servicio','DISPONIBLE',10, '2026-12-31', '2026-12-31', '2026-05-15'),
('GRU006','Chevrolet','FVR',2024,'Amarillo','CH1006','MOT1006',15000,'Cali','Servicio','DISPONIBLE',10, '2026-12-31', '2026-12-31', '2026-05-15'),
('GRU007','Isuzu','NKR',2021,'Amarillo','CH1007','MOT1007',95000,'Cali','Servicio','DISPONIBLE',10, '2026-12-31', '2026-12-31', '2026-05-15'),
('GRU008','Chevrolet','FVR',2023,'Amarillo','CH1008','MOT1008',45000,'Cali','Servicio','DISPONIBLE',10, '2026-12-31', '2026-12-31', '2026-05-15'),
('GRU009','Isuzu','NKR',2022,'Amarillo','CH1009','MOT1009',60000,'Cali','Servicio','DISPONIBLE',10, '2026-12-31', '2026-12-31', '2026-05-15'),
('GRU010','Chevrolet','FVR',2020,'Amarillo','CH1010','MOT1010',140000,'Cali','Servicio','DISPONIBLE',10, '2026-12-31', '2026-12-31', '2026-05-15');