DROP DATABASE IF EXISTS delivery_system;
CREATE DATABASE delivery_system;
USE delivery_system;

CREATE TABLE locations (id INT AUTO_INCREMENT PRIMARY KEY,code VARCHAR(20) NOT NULL UNIQUE,
name VARCHAR(100) NOT NULL,type ENUM('LOC', 'DP', 'WH') NOT NULL,
distance_km DOUBLE NOT NULL DEFAULT 0.0, priority ENUM('LOW', 'MEDIUM', 'HIGH') NULL,
current_load INT NULL,max_capacity INT NULL,created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE ai_query_log (
id INT AUTO_INCREMENT PRIMARY KEY,location_code VARCHAR(20) NOT NULL,query_type ENUM('INSIGHTS', 'RESTOCKING', 'COMPARISON', 'ROUTE') NOT NULL,
prompt_summary VARCHAR(500),ai_response TEXT NOT NULL,queried_at TIMESTAMP DEFAULT NOW(),
FOREIGN KEY (location_code) REFERENCES locations(code)
);


INSERT INTO locations (code, name, type, distance_km, priority, current_load, max_capacity) VALUES
('LOC_1', 'City Center',       'LOC', 2.0,  NULL,     NULL, NULL),
('DP_1',  'Point A - Ramallah','DP',  15.5, 'HIGH',   NULL, NULL),
('WH_1',  'Main Warehouse',    'WH',  5.0,  NULL,     350,  500),
('DP_2',  'Point B - Birzeit', 'DP',  22.3, 'MEDIUM', NULL, NULL),
('WH_2',  'Backup Warehouse',  'WH',  8.5,  NULL,     80,   300),
('DP_3',  'Point C - Nablus',  'DP',  30.1, 'LOW',    NULL, NULL);