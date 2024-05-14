-- Crea la base de datos (opcional, si no existe)
CREATE DATABASE nombre_de_tu_base_datos;

-- Selecciona la base de datos
USE nombre_de_tu_base_datos;

-- Crea la tabla producto
CREATE TABLE producto (
    id_producto SERIAL PRIMARY KEY,
    cantidad INT NOT NULL,
    precio DECIMAL (10, 2) NOT NULL,
    descripcion TEXT,
    imagen BYTEA
);

-- Crea la tabla usuario
CREATE TABLE usuario (
    id_usuario INET PRIMARY KEY  
);

-- Crea la tabla carrito
CREATE TABLE carrito (
    id_carrito SERIAL PRIMARY KEY,
    id_usuario INET REFERENCES usuario(id_usuario),
    comprado BOOLEAN DEFAULT FALSE
);

-- Crea la tabla productos_en_carritos
CREATE TABLE productos_en_carritos (
    id_carrito INT REFERENCES carrito(id_carrito),
    id_producto INT REFERENCES producto(id_producto),
    cantidad INT NOT NULL,
    PRIMARY KEY (id_carrito, id_producto)
);