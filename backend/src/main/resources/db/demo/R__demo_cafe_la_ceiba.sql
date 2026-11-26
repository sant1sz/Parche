-- DATOS SIMULADOS. Café La Ceiba es un negocio ficticio de demostración (es_demo = TRUE).
-- Ningún precio, mesa ni producto corresponde a un negocio real.
--
-- Migración repetible: Flyway la carga solo con el perfil "demo" (db/demo) y la vuelve a
-- ejecutar entera cada vez que cambia el contenido de este archivo.
--
-- Cada INSERT usa ON CONFLICT DO NOTHING sobre una clave natural (slug del negocio, número y
-- token de mesa, nombre de categoría o de producto). Ejecutarla de nuevo sin cambios en la base
-- no duplica nada, pero NO protege los cambios hechos desde la aplicación:
--   - si se renombra o borra un producto, una categoría o una mesa, la siguiente ejecución
--     vuelve a insertar la fila original junto a la editada;
--   - si se renombra una categoría, sus productos de este archivo ya no encuentran su categoría
--     y no se insertan.
-- Para volver a la demo original, reinicia la base: docker compose down -v && docker compose up -d
--
-- Los tokens de mesa son fijos para que los QR de la demo no cambien entre entornos.

INSERT INTO negocio (nombre, slug, barrio, ciudad, es_demo)
VALUES ('Café La Ceiba', 'cafe-la-ceiba', 'Laureles', 'Medellín', TRUE)
ON CONFLICT (slug) DO NOTHING;

INSERT INTO mesa (id_negocio, numero, token)
SELECT n.id, m.numero, m.token
FROM negocio n
CROSS JOIN (VALUES
    (1,  'hGo0Aj_ObuejR_d4UPJumA'),
    (2,  'UNpYxN9KkvxDlc7z9edxJA'),
    (3,  'gyRfWDrysC8mEc4TVw7h5g'),
    (4,  '3boKdMAZexhThyo-pJr3yw'),
    (5,  'ip7zuxXOpFILgMaF01eVOQ'),
    (6,  '2YeZJtNLFpXZ8dcmbBPsSA'),
    (7,  'mqsRCU9YQ2_1f93bsUTp8A'),
    (8,  '6DA9qCWP-iuaBtlE7QLSmg'),
    (9,  'DINK2plKrWwf32YiBrS8uA'),
    (10, 'mlyasaC_cLok7R0lAoc02Q'),
    (11, '1ynOI0cYXSG87uXu8Mw4pw'),
    (12, 'V4hNwvn3u1x6iIgcBNv-FA')
) AS m (numero, token)
WHERE n.slug = 'cafe-la-ceiba'
ON CONFLICT DO NOTHING;

INSERT INTO categoria (id_negocio, nombre, orden)
SELECT n.id, c.nombre, c.orden
FROM negocio n
CROSS JOIN (VALUES
    ('Desayunos',          1),
    ('Almuerzos',          2),
    ('Bebidas calientes',  3),
    ('Bebidas frías',      4),
    ('Postres',            5)
) AS c (nombre, orden)
WHERE n.slug = 'cafe-la-ceiba'
ON CONFLICT DO NOTHING;

INSERT INTO producto (id_negocio, id_categoria, nombre, descripcion, precio, disponible, orden)
SELECT n.id, c.id, p.nombre, p.descripcion, p.precio, p.disponible, p.orden
FROM negocio n
JOIN categoria c ON c.id_negocio = n.id
JOIN (VALUES
    -- Desayunos
    ('Desayunos', 1, 'Calentado paisa', 'Frijoles y arroz calentados, huevo frito, chorizo, arepa y quesito', 18000, TRUE),
    ('Desayunos', 2, 'Huevos pericos con arepa', 'Huevos revueltos con tomate y cebolla, arepa con mantequilla y quesito', 12000, TRUE),
    ('Desayunos', 3, 'Arepa de chócolo con quesito', 'Arepa dulce de maíz tierno asada, con quesito antioqueño', 11000, TRUE),
    ('Desayunos', 4, 'Desayuno La Ceiba', 'Huevos al gusto, pan de la casa, fruta picada, jugo natural y café', 19000, TRUE),
    ('Desayunos', 5, 'Changua', 'Caldo de leche con huevo, cebolla larga, cilantro y calado', 13000, TRUE),
    ('Desayunos', 6, 'Tamal antioqueño', 'Tamal de cerdo y pollo envuelto en hoja de plátano, con arepa. Solo fines de semana', 16000, FALSE),
    -- Almuerzos
    ('Almuerzos', 1, 'Bandeja paisa', 'Frijoles, arroz, carne molida, chicharrón, chorizo, huevo, maduro, aguacate y arepa', 32000, TRUE),
    ('Almuerzos', 2, 'Menú del día', 'Sopa, principio, proteína, arroz, ensalada y jugo. Pregunta por el de hoy', 22000, TRUE),
    ('Almuerzos', 3, 'Sancocho de gallina', 'Con mazorca, papa, yuca y plátano. Acompañado de arroz y aguacate', 26000, TRUE),
    ('Almuerzos', 4, 'Cazuela de frijoles', 'Frijoles con chicharrón, maduro, chorizo y aguacate, gratinada', 24000, TRUE),
    ('Almuerzos', 5, 'Pechuga a la plancha', 'Con arroz, ensalada fresca y papas a la francesa', 27000, TRUE),
    ('Almuerzos', 6, 'Ajiaco', 'Con pollo desmechado, mazorca, crema de leche, alcaparras y arroz', 25000, TRUE),
    -- Bebidas calientes
    ('Bebidas calientes', 1, 'Tinto', 'Café negro de origen antioqueño', 3500, TRUE),
    ('Bebidas calientes', 2, 'Café americano', 'Espresso doble alargado con agua caliente', 5000, TRUE),
    ('Bebidas calientes', 3, 'Capuchino', 'Espresso con leche vaporizada y espuma', 8000, TRUE),
    ('Bebidas calientes', 4, 'Latte', 'Espresso con abundante leche vaporizada', 8500, TRUE),
    ('Bebidas calientes', 5, 'Chocolate con queso', 'Chocolate de mesa en leche, con quesito para derretir', 7500, TRUE),
    ('Bebidas calientes', 6, 'Aromática de frutas', 'Infusión de frutas frescas de temporada', 5500, TRUE),
    -- Bebidas frías
    ('Bebidas frías', 1, 'Jugo natural en agua', 'Mango, mora, lulo, maracuyá o guanábana', 7000, TRUE),
    ('Bebidas frías', 2, 'Jugo natural en leche', 'Mango, mora, lulo, maracuyá o guanábana', 8500, TRUE),
    ('Bebidas frías', 3, 'Limonada de coco', 'Limonada natural licuada con crema de coco', 11000, TRUE),
    ('Bebidas frías', 4, 'Café frappé', 'Café de la casa licuado con hielo, leche y arequipe', 12000, TRUE),
    ('Bebidas frías', 5, 'Avena fría', 'Avena en leche con canela, al estilo de la casa', 6500, TRUE),
    ('Bebidas frías', 6, 'Gaseosa', 'Botella personal de 400 ml', 5000, TRUE),
    -- Postres
    ('Postres', 1, 'Torta tres leches', 'Bizcocho húmedo bañado en tres leches, con canela', 10000, TRUE),
    ('Postres', 2, 'Brownie con helado', 'Brownie tibio de chocolate con una bola de helado de vainilla', 13000, TRUE),
    ('Postres', 3, 'Torta de zanahoria', 'Con nueces y cubierta de queso crema', 9500, TRUE),
    ('Postres', 4, 'Postre de natas', 'Receta tradicional con natas de leche y uvas pasas', 10500, TRUE),
    ('Postres', 5, 'Arroz con leche', 'Cremoso, con canela y uvas pasas', 8000, TRUE),
    ('Postres', 6, 'Obleas con arequipe', 'Dos obleas con arequipe, queso rallado y mora', 7000, TRUE)
) AS p (categoria, orden, nombre, descripcion, precio, disponible)
    ON p.categoria = c.nombre
WHERE n.slug = 'cafe-la-ceiba'
ON CONFLICT DO NOTHING;
