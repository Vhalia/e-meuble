--groupe 16, de Foy Jehan, Dorrekens Thomas, le Grelle Max et Lempereur Thomas

DROP SCHEMA IF EXISTS projet CASCADE;
CREATE SCHEMA projet;

CREATE TABLE projet.adresses (
	id_adresse SERIAL PRIMARY KEY,
	rue VARCHAR(50) NOT NULL,
	numero VARCHAR(5) NOT NULL,
	boite VARCHAR(4) NULL,
	code_postal VARCHAR(10) NOT NULL,
	commune VARCHAR(50) NOT NULL,
	pays VARCHAR(50) NOT NULL
);

CREATE TABLE projet.utilisateurs (
	id_utilisateur SERIAL PRIMARY KEY,
	pseudo VARCHAR(50) NOT NULL,
	mot_de_passe VARCHAR(256) NULL,
	nom VARCHAR(50) NOT NULL,
	prenom VARCHAR(50) NOT NULL,
	email VARCHAR(80)NOT NULL,
	u_role CHARACTER(3) NOT NULL,
	inscription_validee BOOLEAN NOT NULL,
	adresse INTEGER REFERENCES projet.adresses(id_adresse) NULL,
	date_inscription DATE NOT NULL,
	nbr_meubles_achetes INTEGER NOT NULL DEFAULT 0,
	nbr_meubles_vendus INTEGER NOT NULL DEFAULT 0,
	client_factice BOOLEAN NOT NULL
);

CREATE TABLE projet.visites(
	id_visite SERIAL PRIMARY KEY,
	date_demande DATE NOT NULL,
	plage_horaire_client VARCHAR(255) NOT NULL,
	adresse_entreposage INTEGER REFERENCES projet.adresses(id_adresse) NOT NULL,
	date_et_heure_visite TIMESTAMP NULL,
	etat_visite VARCHAR(4) NOT NULL,
	client INTEGER REFERENCES projet.utilisateurs(id_utilisateur) NOT NULL,
	mot_annulation VARCHAR(255) NULL
);


CREATE TABLE projet.types(
	id_type SERIAL PRIMARY KEY,
	libelle VARCHAR(80) NOT NULL
);

CREATE TABLE projet.meubles (
	id_meuble SERIAL PRIMARY KEY,
	type INTEGER REFERENCES projet.types(id_type) NOT NULL,
	description VARCHAR(255) NOT NULL,
	m_etat CHARACTER(5) NOT NULL DEFAULT 'PROPO',
	client INTEGER REFERENCES projet.utilisateurs(id_utilisateur) NOT NULL,
	prix_achat FLOAT NULL,
	date_emporte_mag DATE NULL,
	date_depot_en_mag DATE NULL,
	date_retrait DATE NULL,
	prix_vente FLOAT NULL,
	prix_special FLOAT NULL,
	visite INTEGER REFERENCES projet.visites(id_visite) NOT NULL
);

CREATE TABLE projet.photos(
	id_photo SERIAL NOT NULL PRIMARY KEY,
	photo VARCHAR(255) NOT NULL,
	meuble INTEGER REFERENCES projet.meubles(id_meuble) NOT NULL,
	defilable BOOLEAN NOT NULL
);

ALTER TABLE projet.meubles ADD COLUMN photo_preferee INTEGER REFERENCES projet.photos(id_photo) NULL;

CREATE TABLE projet.options(
	id_option SERIAL PRIMARY KEY,
    client INTEGER REFERENCES projet.utilisateurs(id_utilisateur),
    meuble INTEGER REFERENCES projet.meubles(id_meuble),
    duree INTEGER NOT NULL,
    date_limite DATE NOT NULL,
    est_annulee BOOLEAN NOT NULL,
    jours_restants INTEGER NOT NULL
);

CREATE TABLE projet.ventes(
	id_meuble INTEGER REFERENCES projet.meubles PRIMARY KEY,
	date_vente DATE NOT NULL,
	id_client INTEGER REFERENCES projet.utilisateurs NULL
);


/*Démo finale*/

/*types de meubles*/
INSERT INTO projet.types VALUES (DEFAULT, 'Armoire');
INSERT INTO projet.types VALUES (DEFAULT, 'Bahut');
INSERT INTO projet.types VALUES (DEFAULT, 'Bibliothèque');
INSERT INTO projet.types VALUES (DEFAULT, 'Bonnetière');
INSERT INTO projet.types VALUES (DEFAULT, 'Buffet');
INSERT INTO projet.types VALUES (DEFAULT, 'Bureau');
INSERT INTO projet.types VALUES (DEFAULT, 'Chaise');
INSERT INTO projet.types VALUES (DEFAULT, 'Chiffonnier');
INSERT INTO projet.types VALUES (DEFAULT, 'Coffre');
INSERT INTO projet.types VALUES (DEFAULT, 'Coiffeuse');
INSERT INTO projet.types VALUES (DEFAULT, 'Commode');
INSERT INTO projet.types VALUES (DEFAULT, 'Confident/Indiscret');
INSERT INTO projet.types VALUES (DEFAULT, 'Console');
INSERT INTO projet.types VALUES (DEFAULT, 'Dresse');
INSERT INTO projet.types VALUES (DEFAULT, 'Fauteuil');
INSERT INTO projet.types VALUES (DEFAULT, 'Guéridon');
INSERT INTO projet.types VALUES (DEFAULT, 'Lingère');
INSERT INTO projet.types VALUES (DEFAULT, 'Lit');
INSERT INTO projet.types VALUES (DEFAULT, 'Penderie');
INSERT INTO projet.types VALUES (DEFAULT, 'Secrétaire');
INSERT INTO projet.types VALUES (DEFAULT, 'Table');
INSERT INTO projet.types VALUES (DEFAULT, 'Tabouret');
INSERT INTO projet.types VALUES (DEFAULT, 'Vaisselier');
INSERT INTO projet.types VALUES (DEFAULT, 'Valet muet');


/*adresses*/

--adresse de Albert Satcho
INSERT INTO projet.adresses VALUES (DEFAULT,'sente des artistes','1bis',NULL,'4800','Verviers','Belgique');
--adresse de Laurent Satcho
INSERT INTO projet.adresses VALUES (DEFAULT,'sente des artistes','18',NULL,'4800','Verviers','Belgique');
--adresse de Caroline Line
INSERT INTO projet.adresses VALUES (DEFAULT,'Rue de l''Eglise','11','B1','4987','Stoumont','Belgique');
--adresse de Achille Ile et Théophile Ile
INSERT INTO projet.adresses VALUES (DEFAULT,'Rue de Renkin','7',NULL,'4800','Verviers','Belgique');
--adresse de Basile Ile
INSERT INTO projet.adresses VALUES (DEFAULT,'Lammerskreuzstrasse','6',NULL,'52159','Roetgen','Allemagne');
--adresse de Charles Line
INSERT INTO projet.adresses VALUES (DEFAULT,'Rue des Minières','45','Ter','4800','Verviers','Belgique');
--addresse visite 4
INSERT INTO projet.adresses VALUES (DEFAULT,'Rue Victor Bouillenne','9','4C','4800','Verviers','Belgique');



/*utilisateurs*/

--Albert Satcho
INSERT INTO projet.utilisateurs VALUES (DEFAULT,'bert','$2a$10$Is.uR3NcuNSS2sUCyVO6IeCHT1y165WUAfppp.r2e4N4ADlEiOy8m','Satcho','Albert','bert.satcho@gmail.com', 'ADM',true,1,DATE('2021-03-22'),DEFAULT,DEFAULT,false);
--Laurent Satcho
INSERT INTO projet.utilisateurs VALUES (DEFAULT,'lau','$2a$10$3bbZ33hjq5SA4u5qXCn2EODyUV3Q.UoEGnM1ZbQOxh3TX5wWzcm5m','Satcho','Laurent','laurent.satcho@gmail.com', 'ADM',true,2,DATE('2021-03-22'),DEFAULT,DEFAULT,false);
--Caroline Line
INSERT INTO projet.utilisateurs VALUES (DEFAULT,'Caro','$2a$10$WbbKSujmkW/SlRItbDkrBO5hPyojq0eYE8WHmODmzBLjf1kQigcXO','Line','Caroline','caro.line@hotmail.com', 'ANT',true,3,DATE('2021-03-23'),DEFAULT,DEFAULT,false);
--Achille Ile
INSERT INTO projet.utilisateurs VALUES (DEFAULT,'achil','$2a$10$WbbKSujmkW/SlRItbDkrBO5hPyojq0eYE8WHmODmzBLjf1kQigcXO','Ile','Achille','ach.ile@gmail.com', 'CLI',true,4,DATE('2021-03-23'),DEFAULT,DEFAULT,false);
--Basile Ile
INSERT INTO projet.utilisateurs VALUES (DEFAULT,'bazz','$2a$10$WbbKSujmkW/SlRItbDkrBO5hPyojq0eYE8WHmODmzBLjf1kQigcXO','Ile','Basile','bas.ile@gmail.com', 'CLI',true,5,DATE('2021-03-23'),DEFAULT,DEFAULT,false);
--Théophile Ile
INSERT INTO projet.utilisateurs VALUES (DEFAULT,'Theo','$2a$10$pmuJO3fnyRFEnf/eKBRFveMGgFffKdy8O7rssDMzQ.Ip7FRF8XyeC','Ile','Théophile','theo.phile@proximus.be', 'ANT',true,4,DATE('2021-03-30'),DEFAULT,DEFAULT,false);
--Charles Line
INSERT INTO projet.utilisateurs VALUES (DEFAULT,'charline','$2a$10$bYpgxbLQdh3sV/.h8JckWeckkWztXf59pncTzW7hANLdFyepeNj8K','Line','Charles','charline@proximus.be', 'CLI',true,6,DATE('2021-04-22'),DEFAULT,DEFAULT,false);


/*visites*/

--demande de visite 1
INSERT INTO projet.visites VALUES(DEFAULT, DATE('2021-03-24'), 'lundi de 18h à 22h', 4, TIMESTAMP '2021-03-29 20:00:00', 'CONF', 4, NULL);
--demande de visite 2
INSERT INTO projet.visites VALUES(DEFAULT, DATE('2021-03-25'), 'lundi de 18h à 22h', 4, NULL, 'ANN', 4, 'Meuble trop récent');
--demande de visite 3
INSERT INTO projet.visites VALUES(DEFAULT, DATE('2021-03-25'), 'tous les jours de 15h à 18h', 5, TIMESTAMP '2021-03-29 15:00:00', 'CONF', 5, NULL);
--demande de visite 4
INSERT INTO projet.visites VALUES(DEFAULT, DATE('2021-04-21'), 'tous les matins de 9h à 13h', 7, NULL, 'DEM', 6, NULL);
--demande de visite 5
INSERT INTO projet.visites VALUES(DEFAULT, DATE('2021-04-22'), 'tous les jours de 16h à 19h', 3, TIMESTAMP '2021-03-29 18:00:00', 'CONF', 3, NULL);


/*meubles*/

--meuble 1: Bahut
INSERT INTO projet.meubles VALUES (DEFAULT, 2, 'Bahut profond d''une largeur de 112 cm et d''une hauteur de 147 cm.', 'ENMAG', 4, 200, DATE('2021-03-30'), DATE('2021-03-30'), NULL, -1, -1, 1);
--meuble 2: Bureau
INSERT INTO projet.meubles VALUES (DEFAULT, 6, 'Large bureau 1m87 cm, deux colonnes de tiroirs', 'ENVEN', 4, 159, DATE('2021-03-30'), DATE('2021-03-30'), NULL, 299, -1, 1);
--meuble 3: Table
INSERT INTO projet.meubles VALUES (DEFAULT, 21, 'Table jardin en bois brut', 'PASCO', 4, NULL, NULL, NULL, NULL, -1, -1, 2);
--meuble 4: Table
INSERT INTO projet.meubles VALUES (DEFAULT, 21, 'Table en chêne, pieds en fer forgé', 'RETIR', 5, 140, DATE('2021-03-29'), NULL, NULL, 459, -1, 3);
--meuble 5: Secretaire
INSERT INTO projet.meubles VALUES (DEFAULT, 20, 'Secrétaire en acajou, marqueterie', 'ENMAG', 5, 90, DATE('2021-03-29'), DATE('2021-03-29'), NULL, -1, -1, 3);
--meuble 6: Lit
INSERT INTO projet.meubles VALUES (DEFAULT, 18, 'Lit à baldaquin en acajou', 'PROPO', 6, NULL, NULL, NULL, NULL, -1, -1, 4);
--meuble 7: Bureau
INSERT INTO projet.meubles VALUES (DEFAULT, 6, 'Bureau en bois ciré', 'ENRES', 3, 220, DATE('2021-07-27'), NULL, NULL, -1, -1, 5);
--meuble 8: Bureau
INSERT INTO projet.meubles VALUES (DEFAULT, 6, 'Bureau en chêne massif, sous-main intégré', 'ENVEN', 3, 325, DATE('2021-07-27'), DATE('2021-07-27'), NULL, 378, -1, 3);
--meuble 9: Bureau
INSERT INTO projet.meubles VALUES (DEFAULT, 6, 'Magnifique bureau en acajou', 'ENVEN', 3, 180, DATE('2021-07-27'), DATE('2021-07-27'), NULL, 239, -1, 3);
--meuble 10: Coiffeuse
INSERT INTO projet.meubles VALUES (DEFAULT, 10, 'Splendide coiffeuse aux reliefs travaillés', 'ENVEN', 3, 150, DATE('2021-07-27'), DATE('2021-07-27'), NULL, 199, -1, 3);
--meuble 11: Coiffeuse
INSERT INTO projet.meubles VALUES (DEFAULT, 10, 'Coiffeuse marqueterie', 'ENVEN', 3, 145, DATE('2021-07-27'), DATE('2021-07-27'), NULL, 199, -1, 3);


/*photos*/

--1: photo meuble 1
INSERT INTO projet.photos VALUES (DEFAULT, 'images/Bahut_2.jpg', 1, true);
UPDATE projet.meubles SET photo_preferee = 1 WHERE id_meuble = 1;
--2: photo meuble 2
INSERT INTO projet.photos VALUES (DEFAULT, 'images/Bureau_1.jpg', 2, false);
--3: photo meuble 2
INSERT INTO projet.photos VALUES (DEFAULT, 'images/Bureau_1-Visible-Préférée.jpg', 2, true);
UPDATE projet.meubles SET photo_preferee = 3 WHERE id_meuble = 2;
--4: photo meuble 3
INSERT INTO projet.photos VALUES (DEFAULT, 'images/table-jardin-recente.jpg', 3, true);
UPDATE projet.meubles SET photo_preferee = 4 WHERE id_meuble = 3;
--5: photo meuble 4
INSERT INTO projet.photos VALUES (DEFAULT, 'images/Table.jpg', 4, true);
UPDATE projet.meubles SET photo_preferee = 5 WHERE id_meuble = 4;
--6: photo meuble 5
INSERT INTO projet.photos VALUES (DEFAULT, 'images/Secretaire.jpg', 5, true);
UPDATE projet.meubles SET photo_preferee = 6 WHERE id_meuble = 5;
--7: photo meuble 6
INSERT INTO projet.photos VALUES (DEFAULT, 'images/Lit_LitBaldaquin.jpg', 6, true);
UPDATE projet.meubles SET photo_preferee = 7 WHERE id_meuble = 6;
--8: photo meuble 7
INSERT INTO projet.photos VALUES (DEFAULT, 'images/Bureau_2.jpg', 7, true);
UPDATE projet.meubles SET photo_preferee = 8 WHERE id_meuble = 7;
--9: photo meuble 8
INSERT INTO projet.photos VALUES (DEFAULT, 'images/Bureau-3_ImageClient.jpg', 8, false);
--10: photo meuble 8
INSERT INTO projet.photos VALUES (DEFAULT, 'images/Bureau-3-Visible.jpg', 8, true);
--11: photo meuble 8
INSERT INTO projet.photos VALUES (DEFAULT, 'images/Bureau-3-Visible-Préférée.jpg', 8, true);
UPDATE projet.meubles SET photo_preferee = 11 WHERE id_meuble = 8;
--12: photo meuble 9
INSERT INTO projet.photos VALUES (DEFAULT, 'images/Bureau-8.jpg', 9, false);
--13: photo meuble 9
INSERT INTO projet.photos VALUES (DEFAULT, 'images/Bureau-8-Visible-Préférée.jpg', 9, true);
UPDATE projet.meubles SET photo_preferee = 13 WHERE id_meuble = 9;
--14: photo meuble 10
INSERT INTO projet.photos VALUES (DEFAULT, 'images/Coiffeuse_1_Details.jpg', 10, false);
--15: photo meuble 10
INSERT INTO projet.photos VALUES (DEFAULT, 'images/Coiffeuse_1-Visible_Préférée.jpg', 10, true);
UPDATE projet.meubles SET photo_preferee = 15 WHERE id_meuble = 10;
--16: photo meuble 11
INSERT INTO projet.photos VALUES (DEFAULT, 'images/Coiffeuse_2.jpg', 11, false);
--17: photo meuble 11
INSERT INTO projet.photos VALUES (DEFAULT, 'images/Coiffeuse_2-Visible_Préférée.jpg', 11, true);
UPDATE projet.meubles SET photo_preferee = 17 WHERE id_meuble = 11;

/***************************************************************/

/*********************SELECT pour la demo**********************/

/*Nombre de photos visibles*/
SELECT count(p.id_photo)
FROM projet.photos p
WHERE p.defilable = true;

/*Nombre de meubles en vente, en option ou vendu*/
SELECT count(m.id_meuble)
FROM projet.meubles m
WHERE m.m_etat = 'ENVEN'
OR m.m_etat = 'ENOPT'
OR m.m_etat = 'VENDU';

/*Nombre de photos préférées*/
SELECT count(m.id_meuble)
FROM projet.meubles m
WHERE m.photo_preferee IS NOT NULL;